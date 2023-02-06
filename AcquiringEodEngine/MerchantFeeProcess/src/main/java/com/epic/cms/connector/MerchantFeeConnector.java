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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

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
            Configurations.RUNNING_PROCESS_ID=Configurations.PROCESS_ID_MERCHANT_FEE;
            CommonMethods.eodDashboardProgressParametersReset();

            if (processBean != null) {
                LinkedHashMap summery = new LinkedHashMap();

                merchantFeeCountList = merchantFeeRepo.getMerchantFeeCountList();
                infoLogger.info("  " + merchantFeeCountList.size() + " Fees Selected for Merchant Fee Process");

                if (merchantFeeCountList != null && merchantFeeCountList.size() > 0) {
                    summery.put("No of fee to be processed: ", merchantFeeCountList.size() + "");
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS =merchantFeeCountList.size();

                    for (MerchantFeeBean merchantFeeBean : merchantFeeCountList) {
                        merchantFeeService.MerchantFee(merchantFeeBean);
                    }

                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }

                    infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

                    failedCount = Configurations.PROCESS_FAILD_COUNT;
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantFeeCountList.size();
                    Configurations.PROCESS_SUCCESS_COUNT = (merchantFeeCountList.size() - failedCount);
                    Configurations.PROCESS_FAILD_COUNT = failedCount;
                }else {
                    summery.put("Accounts eligible for Merchant Fee process ", 0 + "");
                }
            }
        } catch (Exception e) {
            infoLogger.info(logManager.ProcessStartEndStyle("Merchant Fee Process Terminated Because of Error"));
            errorLogger.error("Merchant Fee Process Terminated Because of Error", e);
        } finally {
            addSummaries();
            infoLogger.info(logManager.processSummeryStyles(summery));
        }
    }

    public void addSummaries() {
        if (merchantErrorList != null) {
            summery.put("Number of transaction to sync", merchantErrorList.size());
            summery.put("Number of success transaction", merchantErrorList.size() - failedCount);
            summery.put("Number of failure transaction", failedCount);
        } else {
            summery.put("Number of transaction to sync", 0);
            summery.put("Number of success transaction", 0);
            summery.put("Number of failure transaction", 0);
        }
    }
}
