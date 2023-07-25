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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
public class MerchantEasyPaymentRequestConnector extends ProcessBuilder {
    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    MerchantEasyPaymentRequestService merchantEasyPaymentRequestService;

    @Autowired
    MerchantEasyPaymentRequestRepo merchantEasyPaymentRequestRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


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

                failedCount = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = easyPaymentTranList.size();
                Configurations.PROCESS_SUCCESS_COUNT = (easyPaymentTranList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = failedCount;
            } else {
                summery.put("Merchant Easy Payment Process Failed", + 0 + "");
            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logError.error("Merchant Easy Payment process Error", e);
        } finally {
            logInfo.info(logManager.logSummery(summery));
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
                logError.error("Exception", e3);
            }
        }
    }

    @Override
    public void addSummaries() {

            summery.put("Number of transaction to sync", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("Number of success transaction", Configurations.PROCESS_SUCCESS_COUNT);
            summery.put("Number of failure transaction", Configurations.PROCESS_FAILD_COUNT);

    }
}
