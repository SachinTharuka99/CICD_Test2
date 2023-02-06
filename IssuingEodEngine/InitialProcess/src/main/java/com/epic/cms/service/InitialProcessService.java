package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InitialProcessRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

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

    public String getMessage() {
        return "Message from Initial Process:" + commonVarList.getTitle();
    }
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void startInitialProcess() throws Exception {
        if (!Configurations.isInterrupted) {
            ProcessBean processBean = null;
            try {
                processBean = new ProcessBean();
                processBean = commonRepo.getProcessDetails(100);

                if (processBean != null) {
                    //Update EODProcess Summary Table
                    commonRepo.insertToEodProcessSumery(Configurations.PROCESS_ID_INITIAL_PROCESS);

                    Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_INITIAL_PROCESS;
                    CommonMethods.eodDashboardProgressParametersReset();

                    try {
                        //EOD Card Balance Swapping
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.swapEodCardBalance();
                        }
                        infoLogger.info(logManager.processStartEndStyle("Swapping EODCARDBALANCE Completed"));

                        //Online Card TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimit("ECMS_ONLINE_CARD");
                        }
                        infoLogger.info(logManager.processStartEndStyle("CapsLimit Card Resetting Completed"));

                        //Online Account TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimitAccount("ECMS_ONLINE_ACCOUNT");
                        }
                        infoLogger.info(logManager.processStartEndStyle("CapsLimit Account Resetting Completed"));

                        //Online Customer TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimitAccount("ECMS_ONLINE_CUSTOMER");
                        }
                        infoLogger.info(logManager.processStartEndStyle("CapsLimit Customer Resetting Completed"));

                        //Swapping openning OTBCARDACCOUNT
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.insertIntoOpeningAccBal();
                        }
                        infoLogger.info(logManager.processStartEndStyle("Account Starting OTB set To OPENNINGOTB Completed"));

                        // update eodprocesssummery table..
                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getSUCCES_STATUS(), Configurations.PROCESS_ID_INITIAL_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT));


                    } catch (Exception e) {
                        errorLogger.error("Error in Initial Process ", e);
                        throw e;
                    }
                }

            } catch (Exception e) {
                try {
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                    errorLogger.error("Initial Process failed", e);
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_INITIAL_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT));
                } catch (Exception e2) {
                    errorLogger.error("Initial Process ended with", e2);
                }
            }
        }
    }
}
