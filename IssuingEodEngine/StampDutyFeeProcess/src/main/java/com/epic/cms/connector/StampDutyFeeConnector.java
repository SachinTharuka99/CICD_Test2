package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.StampDutyBean;
import com.epic.cms.repository.StampDutyFeeRepo;
import com.epic.cms.service.StampDutyFeeService;
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
public class StampDutyFeeConnector extends ProcessBuilder {

    @Autowired
    StampDutyFeeRepo stampDutyFeeRepo;

    @Autowired
    StampDutyFeeService stampDutyFeeService;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void concreteProcess() throws Exception {
        ArrayList<StampDutyBean> statementAccountList = new ArrayList<>();
        int totalFailedCount = 0;
        int noOfCards = 0;

        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_STAMP_DUTY_FEE;
            CommonMethods.eodDashboardProgressParametersReset();
            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                statementAccountList = stampDutyFeeRepo.getInitStatementAccountList();
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                statementAccountList = stampDutyFeeRepo.getErrorStatementAccountList();
            }
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = statementAccountList.size();
            noOfCards = statementAccountList.size();

            for (StampDutyBean stampDutyAcoountBean : statementAccountList) {
                stampDutyFeeService.StampDutyFee(stampDutyAcoountBean);
            }

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

            totalFailedCount = Configurations.PROCESS_FAILD_COUNT;
            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("No of Card effected", Integer.toString(noOfCards));
            summery.put("No of Success Card ", Integer.toString(noOfCards - totalFailedCount));
            summery.put("No of fail Card ", Integer.toString(totalFailedCount));

            infoLogger.info(logManager.processSummeryStyles(summery));

            totalFailedCount = Configurations.PROCESS_FAILD_COUNT;
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (statementAccountList.size());
            Configurations.PROCESS_SUCCESS_COUNT = (statementAccountList.size() - totalFailedCount);
            Configurations.PROCESS_FAILD_COUNT = (totalFailedCount);

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Stan Duty Fee Process Fail" , e);
        } finally {
            try {
                /* PADSS Change -variables handling card data should be nullified by replacing
                 the value of variable with zero and call NULL function */
                if (statementAccountList != null && statementAccountList.size() != 0) {
                    for (StampDutyBean stampDutyAcoountBean : statementAccountList) {
                        CommonMethods.clearStringBuffer(stampDutyAcoountBean.getCardNumber());
                    }
                    statementAccountList = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorLogger.error(String.valueOf(e));
            }
        }
    }
}
