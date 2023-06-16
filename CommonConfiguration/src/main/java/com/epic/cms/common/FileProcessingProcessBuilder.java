/**
 * Author :
 * Date : 12/14/2022
 * Time : 9:36 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.common;

import com.epic.cms.model.bean.ProcessBean;


import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.LinkedHashMap;

public abstract class FileProcessingProcessBuilder {

    public LinkedHashMap details = new LinkedHashMap();
    public LinkedHashMap summery = new LinkedHashMap();
    public ProcessBean processBean = null;

    public String processHeader = "Define Process";
    String startHeader = null, endHeader = null, failedHeader = null, completedHeader = null;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_FileHandler")
    public void startProcess(String fileId, int processId) {
        try {
            processBean = commonRepo.getProcessDetails(processId);
            this.processHeader = processBean.getProcessDes();
            setupProcessDescriptions();

            logInfo.info(logManager.logHeader(processHeader));
            logInfo.info(logManager.logStartEnd(startHeader));

            //3 - insert to process summery table
            commonRepo.insertToEodProcessSumery(processId);
            //4 - Abstract method call
            concreteProcess(fileId);
            //5 - Update process summery table
            commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getSUCCES_STATUS(), processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
        } catch (Exception e) {
            try {
                logInfo.info(logManager.logStartEnd(failedHeader));
                logError.error(failedHeader, e);
                //update process summery table
                commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getERROR_STATUS(), processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
            } catch (Exception e2) {
                logError.error(String.valueOf(e2));
            }
        } finally {
            try {
                addSummaries();
                logInfo.info(logManager.logSummery(summery));
                logInfo.info(logManager.logStartEnd(completedHeader));
            } catch (Exception e2) {
                logError.error(String.valueOf(e2));
            }
        }

    }

    private void setupProcessDescriptions() {
        this.startHeader = processHeader + " started";
        this.endHeader = processHeader + " finished";
        this.failedHeader = processHeader + " failed";
        this.completedHeader = processHeader + " completed";
    }

    public abstract void concreteProcess(String fileId) throws Exception;

    public abstract void addSummaries();

    public static String eodDashboardProcessProgress(int successCount, int totalCount) {
        String progressStr = "";
        int progress = 0;
        if (successCount != 0 && totalCount != 0) {
            progress = (successCount * 100 / totalCount);
            progressStr = String.valueOf(progress) + "%";
            return progressStr;
        } else if (successCount == 0 && totalCount != 0) {
            return "0%";
        } else {
            return "100%";
        }
    }
}
