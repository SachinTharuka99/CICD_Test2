/**
 * Author :
 * Date : 12/14/2022
 * Time : 9:36 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.common;

import com.epic.cms.model.bean.ProcessBean;


import static com.epic.cms.util.LogManager.infoLoggerEFPE;
import static com.epic.cms.util.LogManager.errorLoggerEFPE;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.LinkedHashMap;

public abstract class FileProcessingProcessBuilder {
    @Autowired
    CommonRepo commonRepo;

    public LinkedHashMap details = new LinkedHashMap();
    public LinkedHashMap summery = new LinkedHashMap();
    public ProcessBean processBean = null;

    public String processHeader = "Define Process";
    String startHeader = null, endHeader = null, failedHeader = null, completedHeader = null;

    private void setupProcessDescriptions() {
        this.startHeader = processHeader + " started";
        this.endHeader = processHeader + " finished";
        this.failedHeader = processHeader + " failed";
        this.completedHeader = processHeader + " completed";
    }

    @Async("ThreadPool_FileHandler")
    public void startProcess(String fileId) {
        try {
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ATM_FILE_READ);
            this.processHeader = processBean.getProcessDes();
            setupProcessDescriptions();

            LogManager.logProcessHeader(processHeader, infoLoggerEFPE);
            LogManager.logProcessStartEnd(startHeader, infoLoggerEFPE);

            //3 - insert to process summery table
            //4 - Abstract method call
            concreteProcess(fileId);
            //5 - Update process summery table
        } catch (Exception e) {
            try {
                LogManager.logProcessStartEnd(failedHeader, infoLoggerEFPE);
                LogManager.logProcessError(failedHeader, e, errorLoggerEFPE);
                //update process summery table
            } catch (Exception e2) {
                LogManager.logProcessError(failedHeader, e2, errorLoggerEFPE);
            }
        } finally {
            try {
                addSummaries();
                LogManager.logProcessSummery(summery, infoLoggerEFPE);
                LogManager.logProcessStartEnd(completedHeader, infoLoggerEFPE);
            } catch (Exception e2) {
                LogManager.logProcessError(e2.getMessage(), e2, errorLoggerEFPE);
            }
        }

    }


    /**
     * Implement Concrete method here
     *
     * @param fileId
     * @throws Exception
     */
    public abstract void concreteProcess(String fileId) throws Exception;

    /**
     * Add the process summaries here
     */
    public abstract void addSummaries();
}
