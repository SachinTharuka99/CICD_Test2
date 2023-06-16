package com.epic.cms.service;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EodPaymentUpdateRepo;
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

import java.util.LinkedHashMap;


@Service
public class EodPaymentUpdateService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    EodPaymentUpdateRepo eodPaymentUpdateRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startEODPaymentUpdate() throws Exception {
        try {
            CommonMethods.eodDashboardProgressParametersReset();
            LinkedHashMap summery = new LinkedHashMap();
            int[] txnCounts = new int[3];
            /**
             *          *
             * 1st method take all online transactions to backend with status EPEN
             * through Online to backend txn update process
             *
             * 2nd method match all those transactions with the settlement file with
             * the required parameters - VISA txn posting date update
             *
             * 3rd method insert to eodTransaction table with those matching
             * transactions. - eod transaction update process
             *
             */
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_EODPAYMENTUPDATE;

            //call procedure for payment transaction update
            txnCounts = eodPaymentUpdateRepo.callStoredProcedureForEodPaymentUpdate();

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = txnCounts[2];
            Configurations.PROCESS_SUCCESS_COUNT = txnCounts[0];
            Configurations.PROCESS_FAILD_COUNT = txnCounts[1];

            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("No of Card effected", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("No of Success Card ", Integer.toString(Configurations.PROCESS_SUCCESS_COUNT));
            summery.put("No of fail Card ", Configurations.PROCESS_FAILD_COUNT);

            logInfo.info(logManager.logSummery(summery));
        } catch (Exception e) {
            logError.error("EOD Payment Update Process process failed ", e);
        }
    }
}
