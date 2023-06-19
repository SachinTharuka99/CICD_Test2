package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.PaymentReversalRepo;
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
import java.util.List;

@Service
public class PaymentReversalService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    PaymentReversalRepo paymentReversalRepo;
    LinkedHashMap details = new LinkedHashMap();
    private final List<PaymentBean> paymentReversals = null;

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void setPaymentReversals(PaymentBean bean) throws Exception {

        if (!Configurations.isInterrupted) {
            try {
                /**
                 * UPDATE cash reversals in Payment table. *
                 */
                String traceNo = bean.getTraceid();
                paymentReversalRepo.updatePaymentsForCashReversals(bean.getCardnumber(), traceNo);
                Configurations.ReversedTxnCount++;

                details.put("EOD ID ", bean.getEodid());
                details.put("Card Number ", bean.getCardnumber());
                details.put("Transaction Amount ", bean.getAmount());
                details.put("Transaction Type ", bean.getTransactiontype());
                details.put("Reference ", bean.getReference());
                details.put("Sequence Number ", bean.getSequencenumber());
                details.put("Cheque Number ", bean.getChequenumber());
                details.put("Card Main ID ", bean.getCrdrmaintind());
                details.put("Trace ID ", bean.getTraceid());

                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                logError.error("Exception occurred for card: " + CommonMethods.cardNumberMask(bean.getCardnumber()), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(bean.getCardnumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.PROCESS_FAILD_COUNT++;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
