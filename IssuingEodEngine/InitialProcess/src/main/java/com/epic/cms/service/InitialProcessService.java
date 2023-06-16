package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InitialProcessRepo;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class InitialProcessService  {

    @Autowired
    InitialProcessRepo initialProcessRepo;

    @Autowired
    CommonVarList commonVarList;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    public String getMessage() {
        return "Message from Initial Process:" + commonVarList.getTitle();
    }
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void startInitialProcess() throws Exception {
        if (!Configurations.isInterrupted) {
            ProcessBean processBean = null;
            try {
                processBean = new ProcessBean();
                processBean = commonRepo.getProcessDetails(23);

                if (processBean != null) {
                    //Update EODProcess Summary Table
                    //commonRepo.insertToEodProcessSumery(Configurations.PROCESS_ID_INITIAL_PROCESS, processBean.getEodmodule());

                    Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_INITIAL_PROCESS;
                    CommonMethods.eodDashboardProgressParametersReset();

                    try {
                        //EOD Card Balance Swapping
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.swapEodCardBalance();
                        }
                        logInfo.info(logManager.logStartEnd("Swapping EODCARDBALANCE Completed"));

                        //Online Card TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimit("ECMS_ONLINE_CARD");
                        }
                        logInfo.info(logManager.logStartEnd("CapsLimit Card Resetting Completed"));

                        //Online Account TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimitAccount("ECMS_ONLINE_ACCOUNT");
                        }
                        logManager.logStartEnd("CapsLimit Account Resetting Completed");

                        //Online Customer TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimitAccount("ECMS_ONLINE_CUSTOMER");
                        }
                        logInfo.info(logManager.logStartEnd("CapsLimit Customer Resetting Completed"));

                        //Swapping openning OTBCARDACCOUNT
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.insertIntoOpeningAccBal();
                        }
                        logInfo.info(logManager.logStartEnd("Account Starting OTB set To OPENNINGOTB Completed"));

                        // update eodprocesssummery table..
                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getSUCCES_STATUS(), Configurations.PROCESS_ID_INITIAL_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT));


                    } catch (Exception e) {
                        logError.error("Error in Initial Process ");
                        throw e;
                    }
                }

            } catch (Exception e) {
                try {
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                    logError.error("Initial Process failed");
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_INITIAL_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT));
                } catch (Exception e2) {
                    logError.error("Initial Process ended with");
                }
            }
        }
    }
}
