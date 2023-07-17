package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.repository.CollectionAndRecoveryAlertRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CollectionAndRecoveryAlertService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CollectionAndRecoveryAlertConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    CollectionAndRecoveryAlertRepo collectionAndRecoveryAlertRepo;

    @Autowired
    CollectionAndRecoveryAlertService collectionAndRecoveryAlertService;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    HashMap<StringBuffer, String> confirmCardList = new HashMap<>();

    @Override
    public void concreteProcess() throws Exception {

        try {

            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_ALERT_PROCESS;
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_ALERT_PROCESS);

            CommonMethods.eodDashboardProgressParametersReset();
            confirmCardList = collectionAndRecoveryAlertRepo.getConfirmedCardToAlert();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = confirmCardList.size();

            for (Map.Entry<StringBuffer, String> entry : confirmCardList.entrySet()) {
                collectionAndRecoveryAlertService.processCollectionAndRecoveryAlertService(entry.getKey(), entry.getValue(), processBean,faileCardCount);
            }

            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (Configurations.successCardNoCount_CollectionAndRecoveryAlert + Configurations.failedCardNoCount_CollectionAndRecoveryAlert);
            //Configurations.PROCESS_SUCCESS_COUNT = (Configurations.successCardNoCount_CollectionAndRecoveryAlert);
            //Configurations.PROCESS_FAILD_COUNT = (Configurations.failedCardNoCount_CollectionAndRecoveryAlert);

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logError.error("Collection and Recovery Alert process failed", e);
            throw e;
        } finally {
            //logInfo.info(logManager.logSummery(summery));
            confirmCardList.clear();
        }
    }

    @Override
    public void addSummaries() {

        summery.put("Number of transaction to sync ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Number of success transaction", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - faileCardCount.get());
        summery.put("Number of failure transaction", faileCardCount.get());

    }
}
