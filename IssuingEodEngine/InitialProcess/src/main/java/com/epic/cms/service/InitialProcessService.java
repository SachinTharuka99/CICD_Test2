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
                    commonRepo.insertToEodProcessSumery(Configurations.PROCESS_ID_INITIAL_PROCESS, processBean.getEodmodule());

                    Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_INITIAL_PROCESS;
                    CommonMethods.eodDashboardProgressParametersReset();

                    try {
                        //EOD Card Balance Swapping
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.swapEodCardBalance();
                        }
                        logManager.logStartEnd("Swapping EODCARDBALANCE Completed", infoLogger);

                        //Online Card TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimit("ECMS_ONLINE_CARD");
                        }
                        logManager.logStartEnd("CapsLimit Card Resetting Completed", infoLogger);

                        //Online Account TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimitAccount("ECMS_ONLINE_ACCOUNT");
                        }
                        logManager.logStartEnd("CapsLimit Account Resetting Completed", infoLogger);

                        //Online Customer TxnCount,TXN Amount resetting
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.setResetCapsLimitAccount("ECMS_ONLINE_CUSTOMER");
                        }
                        logManager.logStartEnd("CapsLimit Customer Resetting Completed", infoLogger);

                        //Swapping openning OTBCARDACCOUNT
                        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                            initialProcessRepo.insertIntoOpeningAccBal();
                        }
                        logManager.logStartEnd("Account Starting OTB set To OPENNINGOTB Completed", infoLogger);

                        // update eodprocesssummery table..
                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getSUCCES_STATUS(), Configurations.PROCESS_ID_INITIAL_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT));


                    } catch (Exception e) {
                        logManager.logError("Error in Initial Process ", e, errorLogger);
                        throw e;
                    }
                }

            } catch (Exception e) {
                try {
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                    logManager.logError("Initial Process failed", e, errorLogger);
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_INITIAL_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT));
                } catch (Exception e2) {
                    logManager.logError("Initial Process ended with", e2, errorLogger);
                }
            }
        }
    }
}
