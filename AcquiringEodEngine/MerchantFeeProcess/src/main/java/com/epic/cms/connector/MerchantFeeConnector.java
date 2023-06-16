/**
 * Author : rasintha_j
 * Date : 1/24/2023
 * Time : 12:45 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;


import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.MerchantFeeBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantFeeRepo;
import com.epic.cms.service.MerchantFeeService;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;



@Service
public class MerchantFeeConnector extends ProcessBuilder {
    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    MerchantFeeService merchantFeeService;

    @Autowired
    MerchantFeeRepo merchantFeeRepo;

    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        List<MerchantFeeBean> merchantFeeCountList = new ArrayList<MerchantFeeBean>();
        ProcessBean processBean = new ProcessBean();

        try {
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_MERCHANT_FEE);
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MERCHANT_FEE;
            CommonMethods.eodDashboardProgressParametersReset();

            if (processBean != null) {
                LinkedHashMap summery = new LinkedHashMap();

                merchantFeeCountList = merchantFeeRepo.getMerchantFeeCountList();
                logInfo.info("  " + merchantFeeCountList.size() + " Fees Selected for Merchant Fee Process");

                if (merchantFeeCountList != null && merchantFeeCountList.size() > 0) {
                    summery.put("No of fee to be processed: ", merchantFeeCountList.size() + "");
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantFeeCountList.size();

                    for (MerchantFeeBean merchantFeeBean : merchantFeeCountList) {
                        merchantFeeService.MerchantFee(merchantFeeBean);
                    }

                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }

                    failedCount = Configurations.PROCESS_FAILD_COUNT;
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantFeeCountList.size();
                    Configurations.PROCESS_SUCCESS_COUNT = (merchantFeeCountList.size() - failedCount);
                    Configurations.PROCESS_FAILD_COUNT = failedCount;
                } else {
                    summery.put("Accounts eligible for Merchant Fee process ", 0 + "");
                }
            }
        } catch (Exception e) {
            logInfo.info(logManager.logStartEnd("Merchant Fee Process Terminated Because of Error"));
            logError.error("Merchant Fee Process Terminated Because of Error", e);
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Number of transaction to sync", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Number of success transaction", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Number of failure transaction", Configurations.PROCESS_FAILD_COUNT);
    }
}
