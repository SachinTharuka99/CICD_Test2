package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.BalanceComponentBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.CardLimitEnhancementRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CardLimitEnhancementService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class CardLimitEnhancementConnector extends ProcessBuilder {

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CardLimitEnhancementService cardLimitEnhancementService;

    @Autowired
    CardLimitEnhancementRepo cardLimitEnhancementRepo;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    LogManager logManager;

    private ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();
    ArrayList<BalanceComponentBean> enhancementList;
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {

        try {
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                custAccList = cardLimitEnhancementRepo.getInitLimitEnhanceCustAcc();
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                custAccList = cardLimitEnhancementRepo.getErrorLimitEnhanceCustAcc();
            }

            if (custAccList != null && custAccList.size() > 0) {
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_LIMIT_ENHANCEMENT;
                CommonMethods.eodDashboardProgressParametersReset();

                summery.put("Accounts eligible for limit enhance process: ", custAccList.size() + "");
                enhancementList = new ArrayList<>();

                for (OtbBean bean : custAccList) {
                    enhancementList = cardLimitEnhancementRepo.getLimitEnhanceReqConCardList(bean.getCustomerid(), bean.getAccountnumber());
                    cardLimitEnhancementService.processCardLimitEnhancement(enhancementList, bean);

                }
                //wait till all the threads are completed
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                failedCount = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = enhancementList.size();
                Configurations.PROCESS_SUCCESS_COUNT = (enhancementList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = failedCount;
            } else {
                summery.put("Accounts eligible for fee posting process ", 0 + "");
            }
        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw ex;
        } finally {
            logManager.logSummery(summery, infoLogger);
            try {
                if (custAccList != null && custAccList.size() != 0) {
                    for (OtbBean bean : custAccList) {
                        CommonMethods.clearStringBuffer(bean.getCardnumber());
                        CommonMethods.clearStringBuffer(bean.getMaincardno());
                    }
                    custAccList = null;
                }

                if (enhancementList != null && enhancementList.size() != 0) {
                    for (BalanceComponentBean componentBean : enhancementList) {
                        CommonMethods.clearStringBuffer(componentBean.getCardNumber());
                    }
                    enhancementList = null;
                }
            } catch (Exception e) {
                logManager.logError("Exception Occurred for Card Limit Enhancement ", e, errorLogger);
            }

        }
    }

    @Override
    public void addSummaries() {
        if (enhancementList != null) {
            summery.put("Number of transaction to sync", enhancementList.size());
            summery.put("Number of success transaction", enhancementList.size() - failedCount);
            summery.put("Number of failure transaction", failedCount);
        } else {
            summery.put("Number of transaction to sync", 0);
            summery.put("Number of success transaction", 0);
            summery.put("Number of failure transaction", 0);
        }
    }
}
