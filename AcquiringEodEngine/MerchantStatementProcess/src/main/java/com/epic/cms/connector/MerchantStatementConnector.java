package com.epic.cms.connector;


import com.epic.cms.dao.MerchantStatementDao;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.MerchantStatementService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.epic.cms.common.ProcessBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;


@Service
public class MerchantStatementConnector extends ProcessBuilder {


    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    MerchantStatementDao merchantStatementDao;

    @Autowired
    MerchantStatementService merchantStatementService;

    @Autowired
    LogManager logManager;


    HashMap<String, MerchantLocationBean> merchantList = new HashMap<String, MerchantLocationBean>();

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public void concreteProcess() throws Exception {

        try {

            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_MERCHANT_STATEMENT);
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MERCHANT_STATEMENT;
            CommonMethods.eodDashboardProgressParametersReset();

            if (processBean != null) {
                merchantList = merchantStatementDao.getMerchantlocationstobill();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantList.size();
                try {
                    for (Map.Entry<String, MerchantLocationBean> entry : merchantList.entrySet()) {
                        merchantStatementService.merchantStatementService(entry);
                    }
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {

                }
                if (merchantList.size() > 0) {
                    try {
                        merchantStatementDao.insertMerchantEodStatus("ML", "Y");
                        merchantStatementDao.insertAuditMerchantEodStatus("ML", "Y");
                        merchantStatementDao.callMerchantStatementProcedure();
                        merchantStatementDao.callAuditMerchantStatementProcedure();
                        logInfo.info(logManager.logStartEnd("Merchant E-Statement AP procedures Finished"));
                        // logLevel3.info(logLevels.ProcessStartEndStyle(""));
                    } catch (Exception ex) {
                        logError.error("Error Occurs, when running merchant E-Statement AP procedures. ", ex);
                        //  errorLog.error("Error Occurs, when running merchant E-Statement AP procedures. ", ex);
                    }
                }

            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw e;
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("Total No of Effected Merchants", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Total Success Merchants ", Integer.toString(Configurations.PROCESS_SUCCESS_COUNT));
        summery.put("Total Fail Merchants ", Integer.toString(Configurations.PROCESS_FAILD_COUNT));
    }


}
