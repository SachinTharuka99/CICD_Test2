package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.TransactionUpdateService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class TransactionUpdateConnector extends ProcessBuilder {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    LogManager logManager;
    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    TransactionUpdateService transactionUpdateService;
    private final String cardAssociationVisa = "VISA";
    private final String cardAssociationMaster = "MASTER";

    @Override
    public void concreteProcess() throws Exception {
        LinkedHashMap summery = new LinkedHashMap();
        Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_EODTRANSACTIONUPDATE;
        try {
            CommonMethods.eodDashboardProgressParametersReset();
            //update visa transactions...
            transactionUpdateService.transactionUpdate(cardAssociationVisa);
            //update Master transactions...
            transactionUpdateService.transactionUpdate(cardAssociationMaster);

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                updateEodEngineDashboardProcessProgress();
                Thread.sleep(1000);
            }

            // update EOD Dashboard progress details
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (Configurations.VISA_TXN_UPDATE_COUNT + Configurations.MASTER_TXN_UPDATE_COUNT);
            Configurations.PROCESS_SUCCESS_COUNT = (((Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS) - (Configurations.FAILED_VISA_TXN_COUNT + Configurations.FAILED_MASTER_TXN_COUNT)));
            Configurations.PROCESS_FAILD_COUNT = (Configurations.FAILED_VISA_TXN_COUNT + Configurations.FAILED_MASTER_TXN_COUNT);

        } catch (Exception ex) {
            logError.error("Transaction Update Process Error", ex);
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Posted Visa Transactions Count", Configurations.VISA_TXN_UPDATE_COUNT);
        summery.put("Failed Visa Txn Count", Configurations.FAILED_VISA_TXN_COUNT);
        summery.put("Posted MasterCard Transactions Count", Configurations.MASTER_TXN_UPDATE_COUNT);
        summery.put("Failed MasterCard Txn Count", Configurations.FAILED_MASTER_TXN_COUNT);
        summery.put("Total Transaction Count ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Total Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Total Failed Count ", Configurations.PROCESS_FAILD_COUNT);
    }
}
