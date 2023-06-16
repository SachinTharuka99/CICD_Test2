package com.epic.cms.service;

import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;


@Service
public class CardPermanentBlockService {

    @Autowired
    StatusVarList statusList;

    @Autowired
    CardBlockRepo cardPermanentBlockRepo;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processCardPermanentBlock(BlockCardBean blockCardBean, ProcessBean processBean) throws Exception {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            String status;
            int count = 0;

            String maskedCardNumber = CommonMethods.cardNumberMask(blockCardBean.getCardNo());
            try {
                details.put("Card Number", maskedCardNumber);
                //update card status of card table and get the old card status
                status = cardPermanentBlockRepo.updateCardTableForBlock(blockCardBean.getCardNo(), statusList.getCARD_PERMANENT_BLOCKED_STATUS()); //CAPB
                //de-activate existing data for the particular card number

                details.put("Current Status", status);
                if (status.equals(statusList.getCARD_INIT()) || status.equals(statusList.getCARD_BLOCK_STATUS())
                        || status.equals(statusList.getCARD_PERMANENT_BLOCKED_STATUS()) || status.equals(statusList.getCARD_EXPIRED_STATUS())) {
                    //insert the card details with old card status in card block table
                    if (status.equals(statusList.getCARD_PERMANENT_BLOCKED_STATUS())) {
                        details.put("Process Status", "Passed");
                    }
                    cardPermanentBlockRepo.deactivateCardBlock(blockCardBean.getCardNo());
                    count = cardPermanentBlockRepo.insertIntoCardBlock(blockCardBean.getCardNo(), statusList.getCARD_PERMANENT_BLOCKED_STATUS(), status, Configurations.PERM_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK + "_months");
                    details.put("Old Status in card block", statusList.getCARD_PERMANENT_BLOCKED_STATUS());
                    details.put("New Status in card block", status);
                } else {
                    cardPermanentBlockRepo.deactivateCardBlock(blockCardBean.getCardNo());
                    //update online card status
                    count = cardPermanentBlockRepo.updateOnlineCardStatus(blockCardBean.getCardNo(), statusList.getONLINE_CARD_PERMANENTLY_BLOCKED_STATUS());
                    //de-activate existing data for the particular card number
                    cardPermanentBlockRepo.deactivateCardBlockOnline(blockCardBean.getCardNo());
                    //insert the card details with old card status in card block table
                    count = cardPermanentBlockRepo.insertIntoCardBlock(blockCardBean.getCardNo(), status, statusList.getCARD_PERMANENT_BLOCKED_STATUS(), Configurations.PERM_BLOCK_REASON + Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK + "_months");
                    //insert into onlie card block
                    count = cardPermanentBlockRepo.insertToOnlineCardBlock(blockCardBean.getCardNo(), statusList.getONLINE_CARD_PERMANENTLY_BLOCKED_STATUS());
                    details.put("Old Status in card block", status);
                    details.put("New Status in card block", statusList.getCARD_PERMANENT_BLOCKED_STATUS());
                }
                //Deactivate the record from minpayment table
                cardPermanentBlockRepo.updateMinimumPaymentTable(blockCardBean.getCardNo(), statusList.getCARD_PERMANENT_BLOCKED_STATUS()); //CAPB
                details.put("Process Status", "Passed");
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception ex) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(blockCardBean.getCardNo()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                logError.error("Card Permanent block process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), ex);
                details.put("Process Status", "Failed");
                Configurations.PROCESS_FAILD_COUNT++;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
