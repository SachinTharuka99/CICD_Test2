/**
 * Author :
 * Date : 2/3/2023
 * Time : 3:47 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileProcessingProcessBuilder;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.VisaBaseIIFileClearingRepo;
import com.epic.cms.service.VisaBaseIIFileClearingService;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class VisaBaseIIFileClearingConnector extends FileProcessingProcessBuilder {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    VisaBaseIIFileClearingRepo visaBaseIIFileClearingRepo;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    VisaBaseIIFileClearingService visaBaseIIFileClearingService;
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public void concreteProcess(String fileId) {
        System.out.println("Class Name:VisaBaseIIFileClearingConnector,File ID:" + fileId + ",Current Thread:" + Thread.currentThread().getName());
        //initialize variables
        FileBean fileBean;
        boolean fileReadStatus = false;
        String filepath = "";
        String print;
        //validation
        String sessionId;
        int validationOutput = 0;
        int txnComposingOutput = 0;
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_VISA_BASEII_CLEARING;
            CommonMethods.eodDashboardProgressParametersReset();

            if ("LINUX".equals(Configurations.SERVER_RUN_PLATFORM)) {
                filepath = commonRepo.getLinuxFilePath(Configurations.FILE_CODE_VISA);
            } else if ("WINDOWS".equals(Configurations.SERVER_RUN_PLATFORM)) {
                filepath = commonRepo.getWindowsFilePath(Configurations.FILE_CODE_VISA);
            }

            //get VISA file info
            fileBean = this.getVisaFileInfo(fileId);
            if (fileBean != null) {
                //if (fileBean.getFileStatus().equals(DatabaseStatus.STATUS_FILE_INIT)) {// Fresh file
                File file = new File(filepath + File.separator + fileBean.getFileName());
                //Make sure the file exist and can read
                if (file.isFile() && file.exists() && file.canRead()) {
                    fileBean.setFilePath(file.getAbsolutePath());
                    //file reading
                    fileReadStatus = visaBaseIIFileClearingService.readFile(fileBean);
                } else {
                    print = "Visa Base II file not found..."
                            + "\nFile Name : " + file.getName()
                            + "\nFile ID   : " + fileBean.getFileId();
                    logInfo.info(print);
                    //update file status to ERROR
                    visaBaseIIFileClearingRepo.updateRecVisaFileStatus(fileBean.getFileId(), DatabaseStatus.STATUS_FILE_ERROR);
                }
                /*} else {// Repeat file, File reading already completed
                    fileReadStatus = true;
                }*/
                if (fileReadStatus) {//File read completed, Proceed validation and composing parts
                    try {
                        //create a session id
                        sessionId = System.currentTimeMillis() + "";

                        //file validation
                        validationOutput = visaBaseIIFileClearingRepo.visaFileValidate(fileBean.getFileId(), fileBean.getFileStatus(), sessionId);

                        //transactions composing
                        txnComposingOutput = visaBaseIIFileClearingRepo.composeVisaFileTransactions(fileBean.getFileId(), sessionId);

                        //T56 currency update records composing
                        visaBaseIIFileClearingService.composeCurrencyUpdateRecords(fileId);

                        //update file status
                        if (validationOutput == 1) {
                            visaBaseIIFileClearingRepo.updateRecVisaFileStatus(fileBean.getFileId(), DatabaseStatus.STATUS_FILE_COMP);
                        } else {
                            visaBaseIIFileClearingRepo.updateRecVisaFileStatus(fileBean.getFileId(), DatabaseStatus.STATUS_FILE_REJECT);
                        }

                    } catch (Exception ex) {
                        throw ex;
                    }
                } else {
                    logInfo.info("VISA Base II file reading failed for file " + fileId);
                    //update file read status to FAIL
                    visaBaseIIFileClearingRepo.updateRecVisaFileStatus(fileId, Configurations.FAIL_STATUS);
                }
            } else {
                //file cannot proceed due to invalid status
                logError.error("Cannot read, VISA Base II file " + fileId + " is not in the initial or repeat status");
            }

        } catch (Exception ex) {
            logError.error("VISA Base II File clearing process failed for file " + fileId, ex);
            //update file status to FAIL
            try {
                visaBaseIIFileClearingRepo.updateRecVisaFileStatus(fileId, Configurations.FAIL_STATUS);
            } catch (Exception e) {
                logError.error("", e);
            }
        }
    }

    @Override
    public void addSummaries() {

    }

    private synchronized FileBean getVisaFileInfo(String fileId) throws Exception {
        FileBean fileBean;
        try {
            fileBean = visaBaseIIFileClearingRepo.getVisaFileInfo(fileId);
            if (fileBean.getFileId() != null) {
                //file status is INIT or FREPT,so it can proceed.
                //update file status to intermediate status FPROS
                //visaBaseIIFileClearingRepo.updateVisaFileStatus(DatabaseStatus.STATUS_FILE_PROS, fileId);
            } else {
                fileBean = null;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return fileBean;
    }
}
