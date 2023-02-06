package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.TxnMismatchPostRepo;
import com.epic.cms.service.TxnMismatchPostService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class TxnMismatchPostConnector extends ProcessBuilder {

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList status;

    @Autowired
    TxnMismatchPostRepo txnMismatchPostRepo;

    @Autowired
    TxnMismatchPostService txnMismatchPostService;

    private ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();
    private ArrayList<OtbBean> txnList;
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_TXNMISMATCH_POST;
            CommonMethods.eodDashboardProgressParametersReset();

            if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                custAccList = txnMismatchPostRepo.getInitEodTxnMismatchPostCustAcc();
            } else if (Configurations.STARTING_EOD_STATUS.equals(status.getERROR_STATUS())) {
                custAccList = txnMismatchPostRepo.getErrorEodTxnMismatchPostCustAcc();
            }

            for (OtbBean bean : custAccList) {

                txnList = txnMismatchPostRepo.getInitTxnMismatch(bean.getAccountnumber());
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += txnList.size();
                int iterator = 1;

                txnMismatchPostService.processTxnMismatch(txnList, bean, iterator);

                iterator++;
            }
            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }
            infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

            failedCount = Configurations.failedCount_TxnMisMatchProcess;

            if (custAccList != null) {
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (custAccList.size());
                Configurations.PROCESS_SUCCESS_COUNT = (custAccList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = (failedCount);
            }

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error(String.valueOf(e));
        } finally {
            try {
                summery.put("Number of accounts to fee post ", custAccList.size());
                summery.put("Number of success fee post ", custAccList.size() - failedCount);
                summery.put("Number of failure fee post ", failedCount);
                infoLogger.info(logManager.processSummeryStyles(summery));
                /* PADSS Change -
                variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                if (custAccList != null && custAccList.size() != 0) {
                    for (OtbBean bean : custAccList) {
                        CommonMethods.clearStringBuffer(bean.getCardnumber());
                        CommonMethods.clearStringBuffer(bean.getMaincardno());
                    }
                    custAccList = null;
                }

                if (txnList != null && txnList.size() != 0) {
                    for (OtbBean card : txnList) {
                        CommonMethods.clearStringBuffer(card.getCardnumber());
                        CommonMethods.clearStringBuffer(card.getMaincardno());
                    }
                    txnList = null;
                }
            } catch (Exception e) {
                errorLogger.error(String.valueOf(e));
            }

        }
    }
}