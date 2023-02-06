package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ReturnChequePaymentDetailBean;
import com.epic.cms.repository.ChequePaymentRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class ChequePaymentService {

    @Autowired
    ChequePaymentRepo chequePaymentRepo;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusList;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processChequePayment(ReturnChequePaymentDetailBean bean) {
        if (!Configurations.isInterrupted) {
            try {
                chequePaymentRepo.insertChequePayments(bean);
                int updateChqPayCount = chequePaymentRepo.updateChequePayment(bean);
                if (updateChqPayCount == 1) {
                    /*update CQIN in check payments to EDON*/
                    Statusts.SUMMARY_FOR_CHEQUE_PAYMENTS++;
                }
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                errorLogger.error("Failed Cheque Payment Process for Card" + CommonMethods.cardNumberMask(bean.getCardnumber()), e);
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(bean.getCardnumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            }
        }
    }
}
