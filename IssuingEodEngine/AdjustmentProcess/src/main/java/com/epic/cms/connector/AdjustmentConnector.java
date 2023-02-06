/**
 * Author :
 * Date : 10/29/2022
 * Time : 7:34 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.AdjustmentDao;
import com.epic.cms.model.bean.AdjustmentBean;
import com.epic.cms.service.AdjustmentService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class AdjustmentConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    AdjustmentService adjustmentService;

    @Autowired
    AdjustmentDao adjustmentDao;

    @Override
    public void concreteProcess() throws Exception {
        LinkedHashMap summery = new LinkedHashMap();
        Configurations.ADJUSTMENT_SEQUENCE_NO = 1;

        List<AdjustmentBean> adjustmentList = new ArrayList<>();
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.ADJUSTMENT_PROCESS;
            CommonMethods.eodDashboardProgressParametersReset();

            //get adjustment list
            adjustmentList = adjustmentDao.getAdjustmentList();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = adjustmentList.size();

            for (AdjustmentBean adjustmentBean : adjustmentList) {
                adjustmentService.proceedAdjustment(adjustmentBean);
            }

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("No of Card effected", Integer.toString(Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
            summery.put("No of Success Card ", Integer.toString(Configurations.PROCESS_SUCCESS_COUNT));
            summery.put("No of fail Card ", Integer.toString(Configurations.PROCESS_FAILD_COUNT));
        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Failed Adjustment Process", ex);
        } finally {
             /* PADSS Change -
               variables handling card data should be nullified
               by replacing the value of variable with zero and call NULL function */
            for (AdjustmentBean adjustBean : adjustmentList) {
                CommonMethods.clearStringBuffer(adjustBean.getCardNumber());
            }
            adjustmentList = null;
        }

    }
}
