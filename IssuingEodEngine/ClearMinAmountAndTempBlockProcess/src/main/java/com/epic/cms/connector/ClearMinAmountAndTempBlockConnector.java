package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.repository.LastStatementSummaryRepo;
import com.epic.cms.service.ClearMinAmountAndTempBlockService;
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
public class ClearMinAmountAndTempBlockConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    LastStatementSummaryRepo lastStatementSummaryRepo;

    @Autowired
    ClearMinAmountAndTempBlockService clearMinAmountAndTempBlockService;

    List<LastStatementSummeryBean> cardList = new ArrayList<>();
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        StringBuffer cardNo = null;
        try{
            Configurations.RUNNING_PROCESS_ID=Configurations.PROCESS_CLEAR_MINPAYMENTS_AND_TEMPBLOCK;
            CommonMethods.eodDashboardProgressParametersReset();
            cardList = lastStatementSummaryRepo.getStatementCardList();

            summery.put("Checking cards for min payment", cardList.size() + "");

            for (LastStatementSummeryBean lastStatement : cardList) {
                clearMinAmountAndTempBlockService.processClearMinAmountAndTempBlock(lastStatement);
            }
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

            failedCount = Configurations.PROCESS_FAILD_COUNT;
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
            Configurations.PROCESS_SUCCESS_COUNT = (cardList.size() - failedCount);
            Configurations.PROCESS_FAILD_COUNT = failedCount;

            infoLogger.info(logManager.processSummeryStyles(summery));
        }catch(Exception e){
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Failed Clear Min Amount & Temp Block Process ", e);
        }finally{
            addSummaries();
            infoLogger.info(logManager.processSummeryStyles(summery));
            try {
                if (cardList != null && cardList.size() != 0) {
                    for (LastStatementSummeryBean lastStatement : cardList) {
                        CommonMethods.clearStringBuffer(lastStatement.getCardno());
                    }
                    cardList = null;
                }
            } catch (Exception e) {
                errorLogger.error("Exception Occurred for Clear Min Amount & Temp Block Process ",e);
            }
        }
    }

    public void addSummaries() {
        if (cardList != null) {
            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("Number of transaction to sync", cardList.size());
            summery.put("Number of success transaction", cardList.size() - failedCount);
            summery.put("Number of failure transaction", failedCount);
        } else {
            summery.put("Number of transaction to sync", 0);
            summery.put("Number of success transaction", 0);
            summery.put("Number of failure transaction", 0);
        }
    }
}
