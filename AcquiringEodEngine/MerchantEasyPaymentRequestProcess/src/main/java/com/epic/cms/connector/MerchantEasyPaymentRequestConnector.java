/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 1:42 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.Repository.MerchantEasyPaymentRequestRepo;
import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.MerchantEasyPaymentRequestBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.MerchantEasyPaymentRequestService;
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
public class MerchantEasyPaymentRequestConnector extends ProcessBuilder {
    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    MerchantEasyPaymentRequestService merchantEasyPaymentRequestService;

    @Autowired
    MerchantEasyPaymentRequestRepo merchantEasyPaymentRequestRepo;

    ProcessBean processBean = new ProcessBean();
    private ArrayList<MerchantEasyPaymentRequestBean> easyPaymentTranList;
    int totalTxn = 0;
    int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_EOD_MERCHANT_EASY_PAYMENT_REQUEST;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_EOD_MERCHANT_EASY_PAYMENT_REQUEST);

            if (processBean != null) {
                easyPaymentTranList = merchantEasyPaymentRequestRepo.getAllEasypaymentTransactions();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = easyPaymentTranList.size();

                for (MerchantEasyPaymentRequestBean tranBean : easyPaymentTranList) {
                    totalTxn++;
                    merchantEasyPaymentRequestService.merchantEasyPayment(tranBean);
                }

                //wait till all the threads are completed
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

                failedCount = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = easyPaymentTranList.size();
                Configurations.PROCESS_SUCCESS_COUNT = (easyPaymentTranList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = failedCount;
            } else {
                summery.put("Merchant Easy Payment Process Failed", + 0 + "");
            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Merchant Easy Payment process Error", e);
        } finally {
            addSummaries();
            infoLogger.info(logManager.processSummeryStyles(summery));
            try {
                if (easyPaymentTranList != null && easyPaymentTranList.size() != 0) {
                    /* PADSS Change -
                    variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                    for (MerchantEasyPaymentRequestBean tranBean : easyPaymentTranList) {
                        CommonMethods.clearStringBuffer(tranBean.getCardNumber());
                    }
                    easyPaymentTranList = null;
                }
            } catch (Exception e3) {
                errorLogger.error("Exception", e3);
            }
        }
    }

    public void addSummaries() {
        if (merchantErrorList != null) {
            summery.put("Number of transaction to sync", easyPaymentTranList.size());
            summery.put("Number of success transaction", easyPaymentTranList.size() - failedCount);
            summery.put("Number of failure transaction", failedCount);
        } else {
            summery.put("Number of transaction to sync", 0);
            summery.put("Number of success transaction", 0);
            summery.put("Number of failure transaction", 0);
        }
    }
}
