package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.TransactionUpdateService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Component
public class TransactionUpdateConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    TransactionUpdateService transactionUpdateService;

    private String cardAssociationVisa = "VISA", cardAssociationMaster = "MASTER";

    @Override
    public void concreteProcess() throws Exception {
        LinkedHashMap summery = new LinkedHashMap();
        Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_EODTRANSACTIONUPDATE;
        try {
            //update visa transactions...
            transactionUpdateService.transactionUpdate(cardAssociationVisa);
            //update Master transactions...
            transactionUpdateService.transactionUpdate(cardAssociationMaster);

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            // update EOD Dashboard progress details
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (Configurations.VISA_TXN_UPDATE_COUNT + Configurations.MASTER_TXN_UPDATE_COUNT);
            Configurations.PROCESS_SUCCESS_COUNT = (((Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS) - (Configurations.FAILED_VISA_TXN_COUNT + Configurations.FAILED_MASTER_TXN_COUNT)));
            Configurations.PROCESS_FAILD_COUNT = (Configurations.FAILED_VISA_TXN_COUNT + Configurations.FAILED_MASTER_TXN_COUNT);

            summery.put("Posted Visa Transactions Count", Configurations.VISA_TXN_UPDATE_COUNT);
            summery.put("Failed Visa Txn Count", Configurations.FAILED_VISA_TXN_COUNT);
            summery.put("Posted MasterCard Transactions Count", Configurations.MASTER_TXN_UPDATE_COUNT);
            summery.put("Failed MasterCard Txn Count", Configurations.FAILED_MASTER_TXN_COUNT);
            summery.put("Total Transaction Count ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("Total Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
            summery.put("Total Failed Count ", Configurations.PROCESS_FAILD_COUNT);

        } catch (Exception ex) {
            errorLogger.error("Transaction Update Process Error", ex);
        } finally {
            infoLogger.info(logManager.processSummeryStyles(summery));
        }
    }
}
