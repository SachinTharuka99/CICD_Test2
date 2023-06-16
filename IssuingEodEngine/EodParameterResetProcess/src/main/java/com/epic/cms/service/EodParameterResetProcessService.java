package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EodParameterResetProcessRepo;
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


@Service
public class EodParameterResetProcessService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
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
                    //commonRepo.insertToEodProcessSumery(Configurations.PROCESS_ID_EOD_PARAMETER_RESET, processBean.getEodmodule());
                    Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_EOD_PARAMETER_RESET;
                    CommonMethods.eodDashboardProgressParametersReset();
                    try {
                        //EOD Reset Merchant Parameters
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            eodParameterResetProcessRepo.resetMerchantParameters();
                        }
                        logInfo.info(logManager.logStartEnd("Reset Merchant Parameters Completed"));
                        //Online TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            eodParameterResetProcessRepo.resetTerminalParameters();
                        }
                        logInfo.info(logManager.logStartEnd("Reset Terminal Parameters Completed"));
                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getSUCCES_STATUS(), Configurations.PROCESS_ID_EOD_PARAMETER_RESET, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));

                    } catch (Exception e) {
                        logError.error("Exception in EOD Parameter Reset Process ", e);
                        throw e;
                    }
                }

            } catch (Exception e) {
                try {
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                    logError.error("EOD Parameter Reset Process failed", e);
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_EOD_PARAMETER_RESET, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                } catch (Exception ex) {
                    logError.error("EOD Parameter Reset Process failed", e);
                }
            }
        }
    }
}
