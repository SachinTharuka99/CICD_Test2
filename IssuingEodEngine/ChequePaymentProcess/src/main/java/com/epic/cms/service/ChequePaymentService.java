package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ReturnChequePaymentDetailBean;
import com.epic.cms.repository.ChequePaymentRepo;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.BlockingQueue;


@Service
public class ChequePaymentService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    ChequePaymentRepo chequePaymentRepo;
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList statusList;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processChequePayment(ReturnChequePaymentDetailBean bean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            try {
                chequePaymentRepo.insertChequePayments(bean);
                int updateChqPayCount = chequePaymentRepo.updateChequePayment(bean);
                if (updateChqPayCount == 1) {
                    /*update CQIN in check payments to EDON*/
                    Statusts.SUMMARY_FOR_CHEQUE_PAYMENTS++;
                }
                //Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
            } catch (Exception e) {
                logError.error("Failed Cheque Payment Process for Card" + CommonMethods.cardNumberMask(bean.getCardnumber()), e);
                //Configurations.PROCESS_FAILD_COUNT++;
                failCount.add(1);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(bean.getCardnumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            }
        }
    }
}
