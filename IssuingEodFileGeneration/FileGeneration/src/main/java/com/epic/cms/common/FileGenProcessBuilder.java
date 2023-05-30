/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 1:20 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.common;

import com.epic.cms.Exception.FailedCardException;
import com.epic.cms.dao.ProcessBuilderDao;
import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.FileGenerationService;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

public abstract class FileGenProcessBuilder extends ProcessBuilder{

    @Autowired
    public CommonRepo commonRepo;

    @Autowired
    public FileGenerationService fileGenerationService;

    public String fileName = "";
    public String fileExtension = "";
    public String fileDirectory = "";

    public String filePath =null;
    public String backUpFilePath =null;

    public String backUpName = "BACKUP" + File.separator;
    public FileGenerationModel fileGenerationModel = null;
    public boolean toDeleteStatus = true;

    public String fieldDelimeter = "~";

    public String eodEngineStatus = null;
    @Autowired
    ProcessBuilderDao processBuilderRepo;

    @Override
    public void startProcess(int processId, String uniqueId) throws Exception {
        try {
            ProcessBean processBean = processBuilderRepo.getProcessDetails(processId);

            eodEngineStatus = Configurations.COMPLETE_STATUS;
            boolean isErrorProcess = processBuilderRepo.isErrorProcess(processId);
            this.processHeader = processBean.getProcessDes();
            setupProcessDescriptions();
            StartEodStatus = Configurations.STARTING_EOD_STATUS;

            if(eodEngineStatus.equals("COMP")){
                if (StartEodStatus.equals("EROR")) { //starteodstatus - File Generation Eod status
                    if (isErrorProcess) {
                        //Current process threw errors at previous EOD
                        hasErrorEODandProcess = 1;
                    } else {
                        //Current process did not throw errors. So ignore this process.
                        hasErrorEODandProcess = 2;
                    }
                } else {
                    //This is a normal process in INIT EOD state
                    hasErrorEODandProcess = 0;
                }

                if (hasErrorEODandProcess == 1 && processBean != null || hasErrorEODandProcess == 0 && processBean != null) {
                    logManager.logHeader(processHeader, infoLogger);
                    logManager.logStartEnd(startHeader, infoLogger);
                    commonRepo.insertToEodProcessSumery(processId);
                    /**
                     * Abstract method call.
                     */
                    concreteProcess();

                    if (Configurations.IS_PROCESS_COMPLETELY_FAILED) {
                        throw new FailedCardException(processHeader);
                    }
                    /**
                     * Add any failed cards at EOD
                     */
                    insertFailedEODCards();
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getSUCCES_STATUS(), processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                } else if (hasErrorEODandProcess == 2 && processBean != null) {
                    System.out.println("Skipping this process since Process not under error: " + processBean.getProcessDes());
                    commonRepo.updateEODProcessCount(uniqueId);
                    return;
                }
            }


        }catch (Exception e){
            logManager.logStartEnd(failedHeader, infoLogger);
            logManager.logError(failedHeader, e, errorLogger);
        }
    }

    private void setupProcessDescriptions() {
        this.startHeader = processHeader + " started";
        this.endHeader = processHeader + " finished";
        this.failedHeader = processHeader + " failed";
        this.completedHeader = processHeader + " completed";
    }
}
