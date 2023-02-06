package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.ReturnChequePaymentDetailsBean;
import com.epic.cms.repository.ChequeReturnRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.ChequeReturnService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class ChequeReturnConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    ChequeReturnService chequeReturnService;

    @Autowired
    ChequeReturnRepo chequeReturnRepo;

    Map<StringBuffer, List<ReturnChequePaymentDetailsBean>> totalChequeReturnsList = new HashMap<StringBuffer, List<ReturnChequePaymentDetailsBean>>();
    private int failedCount = 0;

    @Override
    public void concreteProcess() {
        try {
            StringBuffer cardNo = null;
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CHEQUERETURN;
            CommonMethods.eodDashboardProgressParametersReset();
            //update cheque returns
            chequeReturnService.updateChequeReturns();

            //Get all the entries in the chequepayment table for status as returned. Need cheque returned status cycle.
            totalChequeReturnsList = chequeReturnRepo.returnChequePaymentDetails();

            //Iterate over the entry set over a card.
            Iterator it = totalChequeReturnsList.entrySet().iterator();
            if (!totalChequeReturnsList.isEmpty()) {
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    chequeReturnService.proceedChequeReturn(pair);
                }
                //wait till all the threads are completed
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

                failedCount = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = totalChequeReturnsList.size();
                Configurations.PROCESS_SUCCESS_COUNT = (totalChequeReturnsList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = failedCount;

            } else {
                summery.put("cheque Returns not found", 0);
            }
        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Cheque Return process Error", ex);
        } finally {
            addSummaries();
            infoLogger.info(logManager.processSummeryStyles(summery));
             /* PADSS Change -
               variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
            totalChequeReturnsList.clear();
        }
    }

    public void addSummaries() {
        if (totalChequeReturnsList != null) {
            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("Number of transaction to sync", totalChequeReturnsList.size());
            summery.put("Number of success transaction", totalChequeReturnsList.size() - failedCount);
            summery.put("Number of failure transaction", failedCount);
        } else {
            summery.put("Number of transaction to sync", 0);
            summery.put("Number of success transaction", 0);
            summery.put("Number of failure transaction", 0);
        }
    }
}