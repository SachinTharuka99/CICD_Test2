package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.LimitIncrementBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.IncrementLimitExpireRepo;
import com.epic.cms.service.IncrementLimitExpireService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class IncrementLimitExpireConnector extends ProcessBuilder {

    @Autowired
    IncrementLimitExpireService incrementLimitExpireService;

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList status;

    @Autowired
    IncrementLimitExpireRepo incrementLimitExpireRepo;

    @Autowired
    CommonRepo commonRepo;

    public List<ErrorCardBean> cardErrorList = new ArrayList<ErrorCardBean>();
    public int configProcess = Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE;
    public String processHeader = "LIMIT INCEREMENT EXPIRE PROCESS";

    @Override
    public void concreteProcess() throws Exception {
        ArrayList<LimitIncrementBean> cardList = new ArrayList<LimitIncrementBean>();
        int noOfCards = 0;
        int failedCards = 0;
        int count = 0;
        int[] txnCounts;
        ProcessBean processBean = null;
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE);

            if (processBean != null) {
                /** Expire the Increment*/
                cardList = incrementLimitExpireRepo.getLimitExpiredCardList();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
                noOfCards =Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS;

                /** Limit Expiring card one by one*/
                for (LimitIncrementBean limitIncrementBean : cardList) {
                    incrementLimitExpireService.processCreditLimitExpire(limitIncrementBean,processBean, configProcess, processHeader);
                }
                /**wait till all the threads are completed*/
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
                infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

                failedCards = Configurations.Failed_Count_IncrementLimit;

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = noOfCards;
                Configurations.PROCESS_SUCCESS_COUNT = (noOfCards - failedCards);
                Configurations.PROCESS_FAILD_COUNT = failedCards;
                summery.put("Started Date", Configurations.EOD_DATE.toString());
                summery.put("No of Card effected", Integer.toString(noOfCards));
                summery.put("No of Success Card ", Integer.toString(noOfCards - failedCards));
                summery.put("No of fail Card ", Integer.toString(failedCards));
                infoLogger.info(logManager.processSummeryStyles(summery));

            }

        }catch (Exception e){
            try {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                errorLogger.error("Increment Limit Expire Process Completely failed", e);

                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }
            } catch (Exception e2) {
                errorLogger.error("Increment Limit Expire process ended with", e2);
            }
        } finally {
            try {
                if (cardList != null && cardList.size() != 0) {
                    /* PADSS Change -
                    variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                    for (LimitIncrementBean limitIncrementBean : cardList) {
                        CommonMethods.clearStringBuffer(limitIncrementBean.getCardNumber());
                    }
                    cardList = null;
                }
            } catch (Exception e2) {
                errorLogger.error("Exception", e2);
            }
        }
    }
}