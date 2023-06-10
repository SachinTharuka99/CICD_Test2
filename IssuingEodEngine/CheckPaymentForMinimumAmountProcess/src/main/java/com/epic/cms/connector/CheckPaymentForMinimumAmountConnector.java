package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.repository.CheckPaymentForMinimumAmountRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CheckPaymentForMinimumAmountService;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CheckPaymentForMinimumAmountConnector extends ProcessBuilder {

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CheckPaymentForMinimumAmountRepo checkPaymentForMinimumAmountRepo;

    @Autowired
    CheckPaymentForMinimumAmountService checkPaymentForMinimumAmountService;

    @Autowired
    CommonRepo commonRepo;

    List<LastStatementSummeryBean> cardList = new ArrayList<LastStatementSummeryBean>();
    int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CHECK_PAYMENTS_FOR_MIN_AMOUNT;
            CommonMethods.eodDashboardProgressParametersReset();
            cardList = checkPaymentForMinimumAmountRepo.getStatementCardList();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
            summery.put("Checking cards for min payment", cardList.size() + "");

            for (LastStatementSummeryBean lastStatement : cardList) {
                checkPaymentForMinimumAmountService.CheckPaymentForMinimumAmount(lastStatement);
            }

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            failedCount = Configurations.PROCESS_FAILD_COUNT;
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
            Configurations.PROCESS_SUCCESS_COUNT = (cardList.size() - failedCount);
            Configurations.PROCESS_FAILD_COUNT = failedCount;

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logManager.logError("Check Payment For Minimum Amount process ended with", e, errorLogger);
        } finally {
            logManager.logSummery(summery, infoLogger);
            try {
                if (cardList != null && cardList.size() != 0) {
                    for (LastStatementSummeryBean lastStatementSummeryBean : cardList) {
                        CommonMethods.clearStringBuffer(new StringBuffer(lastStatementSummeryBean.getCardno()));
                    }
                    cardList = null;
                }
            } catch (Exception e) {
                logManager.logError("Check Payment For Minimum Amount process Error ", e, errorLogger);
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Cards falling on Due date", Statusts.SUMMARY_FOR_CARDS_ON_DUEDATE + "");
        summery.put("Cards which have not payed the Min Amount and Risk profile added", Statusts.SUMMARY_FOR_MINPAYMENT_RISK_ADDED + "");
        summery.put("Cards which have payed the Min Amount", Statusts.SUMMARY_FOR_CARDS_MINAMOUNT_PAID + "");
    }
}
