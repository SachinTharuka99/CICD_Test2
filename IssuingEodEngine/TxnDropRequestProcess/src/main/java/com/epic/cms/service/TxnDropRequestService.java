package com.epic.cms.service;

import com.epic.cms.model.bean.DropRequestBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.TxnDropRequestRepo;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
public class TxnDropRequestService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    TxnDropRequestRepo txnDropRequestRepo;
    @Autowired
    StatusVarList statusList;
    @Autowired
    LogManager logManager;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processTxnDropRequest(DropRequestBean bean, ProcessBean processBean) {

        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            Set<StringBuffer> failedCardList = new HashSet<>();
            String maskedCardNumber = CommonMethods.cardNumberMask(bean.getCardNumber());

            try {
                details.put("TxnID ", bean.getTxnId());
                details.put("Card Number", maskedCardNumber);

                /**check online transaction table for transaction reverse status*/
                boolean isOnlineTranReversed = txnDropRequestRepo.getTransactionReverseStatus(bean.getTxnId());

                if (!isOnlineTranReversed) { /** if online transaction not in reversed status*/
                    /**add drop transaction request*/
                    txnDropRequestRepo.addTxnDropRequest(bean.getTxnId(), bean.getCardNumber());
                }
                Configurations.SuccessCount_TxnDropRequest++;
                Configurations.PROCESS_SUCCESS_COUNT++;
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                Configurations.FailedCards_TxnDropRequest++;
                Configurations.FailedCount_TxnDropRequest++;
                Configurations.PROCESS_FAILD_COUNT++;
                logError.error("Transaction Drop Request process failed for card number " + CommonMethods.cardInfo(maskedCardNumber, processBean) + " txnid: " + bean.getTxnId(), e);
                details.put("Process Status", "Failed");
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(bean.getCardNumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
