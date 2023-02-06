package com.epic.cms.service;

import com.epic.cms.repository.AtmCashAdvanceUpdateRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class AtmCashAdvanceUpdateService{

    @Autowired
    AtmCashAdvanceUpdateRepo atmCashAdvanceUpdateRepo;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void startEodAtmCashAdvanceUpdate() throws Exception {
        try {
            CommonMethods.eodDashboardProgressParametersReset();
            LinkedHashMap summery = new LinkedHashMap();
            int[] txnCounts = new int[3];

            //call procedure TRANSACTIONSYNCPROC
            txnCounts = atmCashAdvanceUpdateRepo.callStoredProcedureForCashAdvUpdate();

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS=txnCounts[2];
            Configurations.PROCESS_SUCCESS_COUNT=txnCounts[0];
            Configurations.PROCESS_FAILD_COUNT=txnCounts[1];

            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("No of Card effected", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("No of Success Card ", Integer.toString(Configurations.PROCESS_SUCCESS_COUNT));
            summery.put("No of fail Card ", Configurations.PROCESS_FAILD_COUNT);

            infoLogger.info(logManager.processSummeryStyles(summery));
        } catch (SQLException e) {
            errorLogger.error("ATM Cash Advance Update Process process failed ", e);
        }
    }
}
