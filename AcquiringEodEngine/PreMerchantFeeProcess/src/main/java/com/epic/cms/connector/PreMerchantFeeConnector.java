/**
 * Author : sharuka_j
 * Date : 1/26/2023
 * Time : 12:52 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.dao.PreMerchantFeeDao;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantBeanForFee;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.PreMerchantFeeService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.epic.cms.common.ProcessBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class PreMerchantFeeConnector extends ProcessBuilder {


    public List<ErrorMerchantBean> merchantErrorList;
    public int configProcess = Configurations.PROCESS_PRE_MERCHANT_FEE_PROCESS;
    public String processHeader = "PRE_MERCHANT_FEE_PROCESS";

    @Autowired
    LogManager logManager;
    @Autowired
    PreMerchantFeeService preMerchantFeeService;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    PreMerchantFeeDao preMerchantFeeDao;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    private int success_merchant_recurring_fee_count = 0;
    private int success_terminal_recurring_count = 0;
    private int fail_merchant_count = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {

            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_PRE_MERCHANT_FEE_PROCESS);

            if (processBean != null) {
                System.out.println("---------------------->> Pre Merchant Fee Process ....");


                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_PRE_MERCHANT_FEE_PROCESS;
                CommonMethods.eodDashboardProgressParametersReset();
                //get fee codes with fee profile in to a hashmap
                HashMap<String, List<String>> feeCodeMap = preMerchantFeeDao.getFeeCodeListForFeeProfile();

                ArrayList<MerchantBeanForFee> merchantListForFeeProcess = preMerchantFeeDao.getMerchantListForFeeProcess();
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

                for (MerchantBeanForFee merchantBean : merchantListForFeeProcess) {
                    preMerchantFeeService.preMerchantFee(merchantBean, feeCodeMap);

                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                }
                //update all merchants next recurring fee dates if recurring fee dates<=eoddate
                preMerchantFeeDao.updateAllMerchantRecurringDates();

                //update all terminals next recurring fee dates if recurring fee dates<=eoddate
                preMerchantFeeDao.updateAllTerminalRecurringDates();

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (success_merchant_recurring_fee_count + success_terminal_recurring_count + fail_merchant_count);
                Configurations.PROCESS_SUCCESS_COUNT = (success_merchant_recurring_fee_count + success_terminal_recurring_count);
                Configurations.PROCESS_FAILD_COUNT = fail_merchant_count;

            }
        } catch (Exception e) {
            errorLogger.error("Error occurred", e);
            try {
                System.out.println("---------------------->> Pre Merchant Fee process failed....");
                summery.put("Pre Merchant Fee process failed", "");
//                dbConCommitTrue.updateEodProcessSummery(Configurations.EOD_ID, Statusts.ERROR_STATUS, Configurations.PROCESS_PRE_MERCHANT_FEE_PROCESS,Configurations.PROCESS_SUCCESS_COUNT,Configurations.PROCESS_FAILD_COUNT,CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));

                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }

            } catch (Exception e2) {
                errorLogger.error("Errors occurred", e2);
            }
        } finally {
            summery.put("Success merchant  fee count", success_merchant_recurring_fee_count);
            summery.put("Success terminal  fee count", success_terminal_recurring_count);
            summery.put("Failed merchant count", fail_merchant_count);

            infoLogger.info(logManager.processSummeryStyles(summery));
        }
    }
}