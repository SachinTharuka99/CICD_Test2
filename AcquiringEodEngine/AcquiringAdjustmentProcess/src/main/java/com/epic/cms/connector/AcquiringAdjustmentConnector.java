/**
 * Author : sharuka_j
 * Date : 1/25/2023
 * Time : 7:03 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.AcquiringAdjustmentDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.AcquiringAdjustmentService;
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
public class AcquiringAdjustmentConnector extends ProcessBuilder {

    public List<ErrorMerchantBean> merchantErrorList = new ArrayList<ErrorMerchantBean>();
    public String processHeader = "ACQUIRING ADJUSTMENT PROCESS";
    public int configProcess = Configurations.PROCESS_ACQUIRING_ADJUSTMENT_PROCESS;
    int totalAdjustment = 0;
    int failAdjustment = 0;
    MerchantPayBean paymentBean = null;
    MerchantDetailsBean merchantDetailsBean = null;
    ArrayList<AcqAdjustmentBean> adjentmentBeanList = null;
    CommissionTxnBean commissionTxnBean = null;
    EodTransactionBean eodTransactionBean = null;
    MerchantFeeBean merchantFeeBean = null;
    ArrayList<String> errorMerchantList = new ArrayList<String>();
    String txnID;
    boolean isOnUs;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    AcquiringAdjustmentDao acquiringAdjustmentDao;

    @Autowired
    AcquiringAdjustmentService acquiringAdjustmentService;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Override
    public void concreteProcess() throws Exception {
        try {
            processBean = commonRepo.getProcessDetails(configProcess);
            if (processBean != null) {

                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ACQUIRING_ADJUSTMENT_PROCESS;
                CommonMethods.eodDashboardProgressParametersReset();

                //Select all accepted Adjustments
                adjentmentBeanList = acquiringAdjustmentDao.getConfirmedAjustments();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = adjentmentBeanList.size();
                if (adjentmentBeanList != null && adjentmentBeanList.size() > 0) {
                    for (AcqAdjustmentBean acqAdjustmentBean : adjentmentBeanList) {
                        acquiringAdjustmentService.acquringAdjustment(acqAdjustmentBean);
                    }
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                }

                // update card product of all adjustments
                setCardProduct();
            }

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logError.error(processHeader + " Terminated Due To Error", e);
            try {
                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }

            } catch (Exception e2) {
                logError.error("Exeption ", e2);
            }
        } finally {
            logInfo.info(logManager.logSummery(details));
            try {
                if (adjentmentBeanList != null && adjentmentBeanList.size() != 0) {
                    for (AcqAdjustmentBean acqAdjustmentBean : adjentmentBeanList) {
                        CommonMethods.clearStringBuffer(acqAdjustmentBean.getCardNumber());
                    }
                    adjentmentBeanList = null;
                }
            } catch (Exception e2) {
                logError.error("exeption ", e2);
            }

        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("No of Adjustments", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("No of Success Adjustments ", Integer.toString(Configurations.PROCESS_SUCCESS_COUNT - Configurations.PROCESS_FAILD_COUNT));
    }

    private void setCardProduct() {
        try {
            acquiringAdjustmentDao.setCardProductToEodMerTxn();
        } catch (Exception e) {
            logError.error("exception in set all cards ", e);
        }
    }
}
