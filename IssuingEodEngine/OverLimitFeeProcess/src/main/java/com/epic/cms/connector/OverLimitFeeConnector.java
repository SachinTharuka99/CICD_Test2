package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.OverLimitFeeRepo;
import com.epic.cms.service.OverLimitFeeService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class OverLimitFeeConnector extends ProcessBuilder {

    /********************************************************************************
     *  This is the process of OverLimit interest calculation and update fee count.  *
     *  update fee count is optional                                                *
     *                                                                              *
     * If there is existing card list which has over limited for particular day       *
     *                                                                              *
     *  1.update card fee count                                                     *
     *                                                                              *
     *  2.commit both backend and online databases.                                 *
     *                                                                              *
     *  3.after finished the over limit fee process update the                  *
     *  next scheduled date in eod process table.                                      *
     ********************************************************************************
     *
     */
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
    OverLimitFeeRepo overLimitFeeRepo;

    @Autowired
    OverLimitFeeService overLimitFeeService;

    public String processHeader = "OVERLIMIT FEE PROCESS";

    @Override
    public void concreteProcess() throws Exception {
        HashMap<String, StringBuffer> accMap = null;
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_OVER_LIMIT_FEE;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_OVER_LIMIT_FEE);

            if (processBean != null) {
                accMap = overLimitFeeRepo.getOverLimitAcc();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = accMap.size();

                if (accMap.size() > 0) {
                    for (Map.Entry<String, StringBuffer> entry : accMap.entrySet()) {
                        overLimitFeeService.addOverLimitFee(entry.getKey(), entry.getValue(), processBean, processHeader);
                    }

                    //wait till all the threads are completed
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                }
                summery.put("Started Date", Configurations.EOD_DATE.toString());
                summery.put("No of Card effected", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
                summery.put("No of Success Card ", Configurations.PROCESS_SUCCESS_COUNT);
                summery.put("No of fail Card ", Configurations.PROCESS_FAILD_COUNT);
                infoLogger.info(logManager.processSummeryStyles(summery));
            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("OverLimit fee process failed", e);
        } finally {
            try {
                if (accMap != null) {
                    /* PADSS Change -
                    variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                    accMap.clear();
                }
            } catch (Exception e3) {
                errorLogger.error("Exception in overlimit fee", e3);
            }
        }
    }
}
