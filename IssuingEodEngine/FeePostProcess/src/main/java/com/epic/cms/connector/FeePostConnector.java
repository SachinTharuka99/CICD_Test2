/**
 * Author :
 * Date : 10/30/2022
 * Time : 8:28 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.FeePostRepo;
import com.epic.cms.service.FeePostService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class FeePostConnector extends ProcessBuilder {
    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList status;

    @Autowired
    FeePostRepo feePostRepo;

    @Autowired
    FeePostService feePostService;

    @Override
    public void concreteProcess() throws Exception {
        System.out.println("this is from Fee Post Process concreteProcess()");

        LinkedHashMap details = new LinkedHashMap();
        LinkedHashMap summery = new LinkedHashMap();

        List<OtbBean> custAccList = new ArrayList<>();
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_FEE_POST;
            Configurations.PROCESS_STEP_ID = 50;
            CommonMethods.eodDashboardProgressParametersReset();

            //get card account list
            if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                custAccList = feePostRepo.getInitEodFeePostCustAcc();
            } else if (Configurations.STARTING_EOD_STATUS.equals(status.getERROR_STATUS())) {
                custAccList = feePostRepo.getErrorEodFeePostCustAcc();
            }
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = custAccList.size();
            if (custAccList != null && custAccList.size() > 0) {
                System.out.println("Accounts eligible for fee posting process: " + custAccList.size());
                summery.put("Accounts eligible for fee posting process: ", custAccList.size() + "");
                for (OtbBean bean : custAccList) {
                    feePostService.proceedFeePost(bean);
                }
            } else {
                summery.put("Accounts eligible for fee posting process ", 0 + "");
            }

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (custAccList.size());

            //expire the fee promotion profile based on the end date mentioned in the profile.
            try {
                details.put("Fee promotion profile expire ", "Started");
                int success = feePostRepo.expireFeePromotionProfile();
                details.put("Fee promotion profile expire success for : " + success, "Finished");
            } catch (Exception ex) {
                errorLogger.error("Fee post process failed when expiring the fee promotion profile ", ex);
                details.put("Fee promotion profile expire ", "Failed");
            }
            infoLogger.info(logManager.processDetailsStyles(details));
        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("--Error occurred--", ex);
        } finally {
            summery.put("Number of success fee post ", custAccList.size() - Configurations.PROCESS_FAILD_COUNT);
            summery.put("Number of failure fee post ", Configurations.PROCESS_FAILD_COUNT);
            infoLogger.info(logManager.processSummeryStyles(summery));
             /* PADSS Change -
            variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
            for (OtbBean bean : custAccList) {
                CommonMethods.clearStringBuffer(bean.getCardnumber());
                CommonMethods.clearStringBuffer(bean.getMaincardno());
            }
        }

    }
}
