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

    @Override
    public void concreteProcess() throws Exception {
        StringBuffer cardNo = null;
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CLEAR_MINPAYMENTS_AND_TEMPBLOCK;
            CommonMethods.eodDashboardProgressParametersReset();
            cardList = lastStatementSummaryRepo.getStatementCardList();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS  = cardList.size();

            summery.put("Checking cards for min payment", cardList.size() + "");

            for (LastStatementSummeryBean lastStatement : cardList) {
                clearMinAmountAndTempBlockService.processClearMinAmountAndTempBlock(lastStatement);
            }
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logManager.logError("Failed Clear Min Amount & Temp Block Process ", e, errorLogger);
        } finally {
            logManager.logSummery(summery, infoLogger);
            try {
                if (cardList != null && cardList.size() != 0) {
                    for (LastStatementSummeryBean lastStatement : cardList) {
                        CommonMethods.clearStringBuffer(new StringBuffer(lastStatement.getCardno()));
                    }
                    cardList = null;
                }
            } catch (Exception e) {
                logManager.logError("Exception Occurred for Clear Min Amount & Temp Block Process ", e, errorLogger);
            }
        }
    }

    @Override
    public void addSummaries() {
            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("Number of transaction Cards Count", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("Number of success Cards Count", Configurations.PROCESS_SUCCESS_COUNT);
            summery.put("Number of failure Cards Count", Configurations.PROCESS_FAILD_COUNT);
    }
}
