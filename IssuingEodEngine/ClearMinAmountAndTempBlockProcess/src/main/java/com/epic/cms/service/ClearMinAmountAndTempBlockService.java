package com.epic.cms.service;

import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.ClearMinAmountAndTempBlockRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;


@Service
public class ClearMinAmountAndTempBlockService {

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CardBlockRepo cardBlockRepo;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    ClearMinAmountAndTempBlockRepo clearMinAmountAndTempBlockRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processClearMinAmountAndTempBlock(LastStatementSummeryBean lastStatement, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            ArrayList<StringBuffer[]> allCardList = new ArrayList<>();
            LinkedHashMap details = new LinkedHashMap();
            double minAmount = 0;
            double payments = 0;
            String activeStatus = null;
            int activeOnlineStatus = 0;
            StringBuffer cardNo = null;


            try {
                cardNo = lastStatement.getCardno();
                // Check daily payments. try to clear from min payment table.
                //details.put("Checking card for payment", CommonMethods.cardNumberMask(cardNo));

                /**
                 *Check the payment from the payment table and if the total payment done after
                 *statement date is equal or greater than minumum amount, remove from the minimum amount table.                 *
                 */
                int count, monthNo = 0;
                monthNo = getLastMinimumPaymentMonth(cardNo);

                if (monthNo > 0) {
                    ArrayList<Object> lastStmtDetails = clearMinAmountAndTempBlockRepo.getMinimumPaymentExistStatementDate(cardNo, monthNo);

                    payments = commonRepo.getTotalPaymentSinceLastDue(lastStatement.getAccNo(), Configurations.EOD_DATE, (Date) lastStmtDetails.get(0));

                    minAmount = (Double) lastStmtDetails.get(1);

                    if ((minAmount != 0) && (payments >= minAmount)) {
                        //Set old status of card before being blocked.
                        allCardList = clearMinAmountAndTempBlockRepo.getAllCards(cardNo);

                        for (StringBuffer[] cardNumbers : allCardList) {
                            StringBuffer cardNO = cardNumbers[0];
                            StringBuffer cardCategory = cardNumbers[1];

                            if (cardCategory.toString().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                                activeStatus = statusList.getCARD_VIRTUAL_ACTIVE_STATUS();
                                activeOnlineStatus = statusList.getONLINE_CARD_VIRTUAL_ACTIVE_STATUS();
                            } else {
                                activeStatus = statusList.getCARD_ACTIVE_STATUS();
                                activeOnlineStatus = statusList.getONLINE_CARD_ACTIVE_STATUS();
                            }

                            BlockCardBean blockBean = cardBlockRepo.getCardBlockOldCardStatus(cardNO);
                            if (blockBean != null) {
                                if (blockBean.getNewStatus().equals(statusList.getCARD_TEMPORARY_BLOCK_Status())) { // CATB
                                    //If the card is in expired state and the status is temporary blocked, change it to active status.
                                    blockBean.setNewStatus(activeStatus);
                                    count = cardBlockRepo.updateCardStatus(cardNO, blockBean.getNewStatus());
                                    count = cardBlockRepo.updateOnlineCardStatus(cardNO, activeOnlineStatus);
                                    if (count == 1)//Clear temporary block status.
                                    {
                                        cardBlockRepo.deactivateCardBlock(cardNO);
                                        cardBlockRepo.deactivateCardBlockOnline(cardNo);
                                    }
                                } else if (blockBean.getOldStatus().equals(statusList.getCARD_TEMPORARY_BLOCK_Status())
                                        || (blockBean.getOldStatus().equals(statusList.getCARD_EXPIRED_STATUS())
                                        && blockBean.getNewStatus().equals(statusList.getCARD_PERMANENT_BLOCKED_STATUS()))) {
                                    blockBean.setOldStatus(activeStatus);
                                    count = clearMinAmountAndTempBlockRepo.updateCardBlock(cardNO, blockBean.getOldStatus(), blockBean.getNewStatus());
                                } else if (blockBean.getOldStatus().equals(statusList.getCARD_PERMANENT_BLOCKED_STATUS())
                                        && blockBean.getNewStatus().equals(statusList.getCARD_INIT())) {
                                    cardBlockRepo.deactivateCardBlock(cardNo);
                                    //insert the card details with old card status in card block table
                                    count = cardBlockRepo.insertIntoCardBlock(cardNo, activeStatus, statusList.getCARD_INIT(), "Resolved and clear the " + Configurations.PERM_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK + "_months");
                                } else if (blockBean.getOldStatus().equals(statusList.getCARD_PERMANENT_BLOCKED_STATUS())
                                        && blockBean.getNewStatus().equals(statusList.getCARD_BLOCK_STATUS())) {
                                    cardBlockRepo.deactivateCardBlock(cardNo);
                                    //insert the card details with old card status in card block table
                                    count = cardBlockRepo.insertIntoCardBlock(cardNo, activeStatus, statusList.getCARD_BLOCK_STATUS(), "Resolved and clear the " + Configurations.PERM_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK + "_months");
                                } else if (blockBean.getOldStatus().equals(statusList.getCARD_PERMANENT_BLOCKED_STATUS())
                                        && blockBean.getNewStatus().equals(statusList.getCARD_EXPIRED_STATUS())) {
                                    cardBlockRepo.deactivateCardBlock(cardNo);
                                    //insert the card details with old card status in card block table
                                    count = cardBlockRepo.insertIntoCardBlock(cardNo, activeStatus, statusList.getCARD_EXPIRED_STATUS(), "Resolved and clear the " + Configurations.PERM_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK + "_months");
                                }
                                details.put("deactivated temporary block status for card", CommonMethods.cardNumberMask(cardNo));
                            }
                        }
                        Statusts.SUMMARY_FOR_CARDS_MINAMOUNT_PAID++;
                        clearMinAmountAndTempBlockRepo.removeFromMinPayTable(cardNo, payments);
                        details.put("card removed from min pay table", CommonMethods.cardNumberMask(cardNo));
                        details.put("min Amount for card", minAmount);
                        details.put("payments made", payments);

                        Statusts.SUMMARY_FOR_MINPAYMENT_RISK_REMOVED++;

                    }
                }
                successCount.add(1);
            } catch (Exception e) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(lastStatement.getCardno()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                logError.error("Failed Clear Min Amount And Temp Block Process " + CommonMethods.cardNumberMask(cardNo), e);
                failCount.add(1);
               // Configurations.PROCESS_FAILED_COUNT.set(Configurations.PROCESS_FAILED_COUNT.getAndIncrement());
            } finally {
                if (details.size() > 0 ) {
                    logInfo.info(logManager.logDetails(details));
                }

            }
        }
    }

    private int getLastMinimumPaymentMonth(StringBuffer cardNumber) throws Exception {
        double dueAmount = 0;
        int monthNO = 0;
        HashMap<String, Double> dueAmountList = null;
        dueAmountList = commonRepo.getDueAmountList(cardNumber);
        if (dueAmountList != null && dueAmountList.size() > 0) {
            for (int i = 12; i > 0; i--) {
                dueAmount = dueAmountList.get("M" + i);
                if (dueAmount > 0) {
                    monthNO = i;
                    break;
                }
            }
        }
        return monthNO;
    }
}
