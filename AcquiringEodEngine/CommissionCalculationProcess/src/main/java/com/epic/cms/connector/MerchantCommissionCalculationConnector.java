/**
 * Author : lahiru_p
 * Date : 1/30/2023
 * Time : 9:47 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantCommissionCalculationRepo;
import com.epic.cms.service.MerchantCommissionCalculationService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class MerchantCommissionCalculationConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    MerchantCommissionCalculationService commissionCalculationService;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    MerchantCommissionCalculationRepo commissionCalculationRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    List<MerchantLocationBean> merchantList = new ArrayList<>();
    int merchantCount = 0;
    @Override
    public void concreteProcess() throws Exception {
        try {
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_COMMISSION_CALCULATION);
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_COMMISSION_CALCULATION;
            CommonMethods.eodDashboardProgressParametersReset();

            merchantList = commissionCalculationRepo.getAllMerchants();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantList.size();
            if (merchantList.size() > 0) {
                for (MerchantLocationBean merchantLocationBean : merchantList) {
                    merchantCount++;
                    commissionCalculationService.calculateMerchantCommission(merchantLocationBean);
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
            }

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantCount;
            Configurations.PROCESS_SUCCESS_COUNT = (merchantCount - Configurations.PROCESS_FAILD_COUNT);

        } catch (Exception e) {
            logInfo.info(logManager.logStartEnd("Commission Calculation Process Terminated Because of Error"));
            logError.error("Commission Calculation Process Terminated Because of Error", e);
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("No of merchants effected", Integer.toString(merchantCount));
        summery.put("No of Success merchants ", Integer.toString(merchantCount - Configurations.PROCESS_FAILD_COUNT));

    }
}
