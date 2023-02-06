package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EodParameterResetProcessRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class EodParameterResetProcessService {

    @Autowired
    EodParameterResetProcessRepo eodParameterResetProcessRepo;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startEodParameterResetProcess() throws Exception {
        if (!Configurations.isInterrupted) {
            ProcessBean processBean = null;
            try {
                processBean = new ProcessBean();
                processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_EOD_PARAMETER_RESET);
                if (processBean != null) {
                    //Update EODProcess Summary Table
                    commonRepo.insertToEodProcessSumery(Configurations.PROCESS_ID_EOD_PARAMETER_RESET);
                    Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_EOD_PARAMETER_RESET;
                    CommonMethods.eodDashboardProgressParametersReset();
                    try {
                        //EOD Reset Merchant Parameters
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            eodParameterResetProcessRepo.resetMerchantParameters();
                        }
                        infoLogger.info(logManager.processStartEndStyle("Reset Merchant Parameters Completed"));
                        //Online TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            eodParameterResetProcessRepo.resetTerminalParameters();
                        }
                        infoLogger.info(logManager.processStartEndStyle("Reset Terminal Parameters Completed"));

                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getSUCCES_STATUS(), Configurations.PROCESS_ID_EOD_PARAMETER_RESET, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));

                    } catch (Exception e) {
                        errorLogger.error("Exception in EOD Parameter Reset Process ", e);
                        throw e;
                    }
                }

            } catch (Exception e) {
                try {
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                    errorLogger.error("EOD Parameter Reset Process failed", e);
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_EOD_PARAMETER_RESET, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                } catch (Exception ex) {
                    errorLogger.error("EOD Parameter Reset Process failed", e);
                }
            }
        }
    }
}
