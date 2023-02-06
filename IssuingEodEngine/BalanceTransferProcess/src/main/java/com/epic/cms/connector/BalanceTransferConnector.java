/**
 * Created By Lahiru Sandaruwan
 * Date : 10/18/2022
 * Time : 2:30 PM
 * Project Name : ecms_eod_engine
 * Topic : balanceTransfer
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.InstallmentBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InstallmentPaymentRepo;
import com.epic.cms.service.BalanceTransferService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class BalanceTransferConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    BalanceTransferService balanceTransferService;

    @Autowired
    InstallmentPaymentRepo installmentPaymentRepo;

    @Autowired
    StatusVarList statusList;

    @Override
    public void concreteProcess() throws Exception {
        int noOfEasyPayments = 0;
        List<InstallmentBean> txnList = new ArrayList<>();
        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_BALANCE_TRANSFER);

            if (processBean != null) {
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_BALANCE_TRANSFER;
                CommonMethods.eodDashboardProgressParametersReset();
                /**
                 * Acceleration of Manual NP account for Balance Transfer
                 */
                balanceTransferService.accelerateBalanceTransferRequestForNpAccount();

                /**
                 * Balance Transfer Process
                 */
                txnList = installmentPaymentRepo.getBTOrLOCDetails("BALANCETRASFERREQUEST", "BTPAYMENTPLAN");
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += txnList.size();
                for (InstallmentBean installmentBean : txnList) {
                    balanceTransferService.startBalanceTransferProcess(installmentBean, processBean);
                }
                //wait till all the threads are completed
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
                summery.put("Started Date", Configurations.EOD_DATE.toString());
                summery.put("No of Card effected", Integer.toString(Configurations.NO_OF_BALANCE_TRANSFERS));
                summery.put("No of Success Card ", Integer.toString(Configurations.NO_OF_BALANCE_TRANSFERS - Configurations.FAILED_BALANCE_TRANSFERS));
                summery.put("No of fail Card ", Integer.toString(Configurations.FAILED_BALANCE_TRANSFERS));

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = Configurations.NO_OF_BALANCE_TRANSFERS;
                Configurations.PROCESS_SUCCESS_COUNT = (Configurations.NO_OF_BALANCE_TRANSFERS - Configurations.FAILED_BALANCE_TRANSFERS);
                Configurations.PROCESS_FAILD_COUNT = Configurations.FAILED_BALANCE_TRANSFERS;
                infoLogger.info(logManager.processSummeryStyles(summery));

            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Balance Transfer process failed", e);
        } finally {
            /** PADSS Change -
             variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
            if (txnList != null && txnList.size() != 0) {
                for (InstallmentBean installmentBean : txnList) {
                    CommonMethods.clearStringBuffer(installmentBean.getCardNumber());
                }
                txnList = null;
            }
        }
    }
}