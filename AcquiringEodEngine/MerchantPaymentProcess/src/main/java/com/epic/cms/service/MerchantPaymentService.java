/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 10:56 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantPaymentRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.LinkedHashMap;


@Service
public class MerchantPaymentService {
    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    MerchantPaymentRepo merchantPaymentRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    ProcessBean processBean = new ProcessBean();

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startMerchantPayment() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MERCHANT_PAYMENT_PROCESS;
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_MERCHANT_PAYMENT_PROCESS);

            if (processBean != null) {
                Configurations.RUNNING_PROCESS_DESCRIPTION = processBean.getProcessDes();
                CommonMethods.eodDashboardProgressParametersReset();
                LinkedHashMap summery = new LinkedHashMap();
                int[] txnCounts = new int[3];

                //call procedure TRANSACTIONSYNCPROC
                txnCounts = merchantPaymentRepo.callStoredProcedureForEodMerchantPayment();

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = txnCounts[2];
                Configurations.PROCESS_SUCCESS_COUNT = txnCounts[0];
                Configurations.PROCESS_FAILD_COUNT = txnCounts[1];

                summery.put("Started Date", Configurations.EOD_DATE.toString());
                summery.put("No of Card effected", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
                summery.put("No of Success Card ", Integer.toString(Configurations.PROCESS_SUCCESS_COUNT));
                summery.put("No of fail Card ", Configurations.PROCESS_FAILD_COUNT);

                logInfo.info(logManager.logSummery(summery));
            }
        } catch (SQLException e) {
            logError.error("Merchant Payment Process process failed ", e);
        }
    }
}
