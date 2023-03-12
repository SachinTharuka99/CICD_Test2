/**
 * Author :
 * Date : 12/14/2022
 * Time : 9:36 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.common;

import ch.qos.logback.classic.Logger;
import com.epic.cms.model.bean.ProcessBean;

import static com.epic.cms.util.LogManager.*;

import static com.epic.cms.util.LogManager.*;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.LinkedHashMap;

public abstract class FileProcessingProcessBuilder {
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    LogManager logManager;

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
            infoLoggerEFPE.info(logManager.processHeaderStyle(processHeader));
            infoLoggerEFPE.info(logManager.processStartEndStyle(startHeader));
            //3 - insert to process summery table
            //4 - Abstract method call
            concreteProcess(fileId);
            //5 - Update process summery table
        } catch (Exception e) {
            try {
                infoLoggerEFPE.info(logManager.processStartEndStyle(failedHeader));
                errorLoggerEFPE.error(failedHeader, e);
                //update process summery table
            } catch (Exception e2) {
                errorLoggerEFPE.error(e2.getMessage());
            }
        } finally {
            try {
                addSummaries();
                infoLoggerEFPE.info(logManager.processSummeryStyles(summery));
                infoLoggerEFPE.info(logManager.processStartEndStyle(completedHeader));
            } catch (Exception e2) {
                errorLoggerEFPE.error(e2.getMessage());
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
