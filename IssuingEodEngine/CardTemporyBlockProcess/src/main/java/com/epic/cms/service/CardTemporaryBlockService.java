package com.epic.cms.service;

import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CardTemporaryBlockService {
    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Autowired
    CardBlockRepo cardTemporaryBlockRepo;

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void processCardTemporaryBlock(BlockCardBean blockCardBean, ProcessBean processBean) throws Exception {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            String status = null;
            int count = 0;

            String maskedCardNumber = CommonMethods.cardNumberMask(blockCardBean.getCardNo());
            try {
                details.put("Card Number", maskedCardNumber);
                //update card status of card table and get the old card status
                status = cardTemporaryBlockRepo.updateCardTableForBlock(blockCardBean.getCardNo(), statusList.getCARD_TEMPORARY_BLOCK_Status());
                //de-activate existing data for the particular card number

                details.put("Current Status", status);
                if (status.equals(statusList.getCARD_INIT()) || status.equals(statusList.getCARD_BLOCK_STATUS())
                        || status.equals(statusList.getCARD_TEMPORARY_BLOCK_Status()) || status.equals(statusList.getCARD_EXPIRED_STATUS())) {
                    //insert the card details with old card status in card block table
                    if (status.equals(statusList.getCARD_TEMPORARY_BLOCK_Status())) {
                        details.put("Process Status", "Passed");
                    }
                    cardTemporaryBlockRepo.deactivateCardBlock(blockCardBean.getCardNo());
                    count = cardTemporaryBlockRepo.insertIntoCardBlock(blockCardBean.getCardNo(), statusList.getCARD_TEMPORARY_BLOCK_Status(), status, Configurations.TEMP_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_TEMPORARY_BLOCK + "_months");
                    details.put("Old Status in card block", statusList.getCARD_TEMPORARY_BLOCK_Status());
                    details.put("New Status in card block", status);
                } else {
                    cardTemporaryBlockRepo.deactivateCardBlock(blockCardBean.getCardNo());
                    //update online card status
                    count = cardTemporaryBlockRepo.updateOnlineCardStatus(blockCardBean.getCardNo(), statusList.getONLINE_CARD_TEMPORARILY_BLOCKED_STATUS()); //33
                    //de-activate existing data for the particular card number
                    cardTemporaryBlockRepo.deactivateCardBlockOnline(blockCardBean.getCardNo());
                    //insert the card details with old card status in card block table
                    count = cardTemporaryBlockRepo.insertIntoCardBlock(blockCardBean.getCardNo(), status, statusList.getCARD_TEMPORARY_BLOCK_Status(), Configurations.PERM_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK + "_months");
                    //insert into onlie card block
                    count = cardTemporaryBlockRepo.insertToOnlineCardBlock(blockCardBean.getCardNo(), statusList.getONLINE_CARD_PERMANENTLY_BLOCKED_STATUS());
                    details.put("Old Status in card block", status);
                    details.put("New Status in card block", statusList.getONLINE_CARD_TEMPORARY_BLOCK());
                }
                //Deactivate the record from minpayment table
                cardTemporaryBlockRepo.updateMinimumPaymentTable(blockCardBean.getCardNo(), statusList.getCARD_TEMPORARY_BLOCK_Status());
                details.put("Process Status", "Passed");
                Configurations.PROCESS_SUCCESS_COUNT++;

            } catch (Exception ex) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(blockCardBean.getCardNo()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                logManager.logInfo("Card Temporary block process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), infoLogger);
                logManager.logError("Card Temporary block process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), ex, errorLogger);
                details.put("Process Status", "Failed");
                Configurations.PROCESS_FAILD_COUNT++;
            } finally {
                logManager.logDetails(details, infoLogger);
            }
        }
    }
}
