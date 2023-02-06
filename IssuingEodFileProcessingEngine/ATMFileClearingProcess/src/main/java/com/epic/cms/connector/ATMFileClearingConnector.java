/**
 * Author :
 * Date : 2/2/2023
 * Time : 2:15 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileProcessingProcessBuilder;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.RecATMFileIptRowDataBean;
import com.epic.cms.repository.ATMFileClearingRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.ATMFileClearingService;
import com.epic.cms.util.*;
import com.epic.cms.validation.PaymentValidations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class ATMFileClearingConnector extends FileProcessingProcessBuilder {
    @Autowired
    ATMFileClearingRepo atmFileClearingRepo;
    @Autowired
    ATMFileClearingService atmFileClearingService;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    QueryParametersList queryParametersList;
    @Autowired
    @Qualifier("ThreadPool_ATMFileValidator")
    ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void concreteProcess(String fileId) {
        System.out.println("Class Name:ATMFileReadConnector,File ID:" + fileId + ",Current Thread:" + Thread.currentThread().getName());
        FileBean fileBean;
        //for reading part
        ArrayList<String> nameFieldList = new ArrayList<String>();
        String filepath = "";
        String isFileNameValid = "";
        boolean fileReadStatus = false;
        //for validation part
        int totalCount = 0;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger invalidCount = new AtomicInteger(0);
        ArrayList<RecATMFileIptRowDataBean> fileContent = new ArrayList<RecATMFileIptRowDataBean>();
        try {
            infoLogger.info(logManager.processHeaderStyle("ATM File Clearing Process, File ID: " + fileId));
            infoLogger.info(logManager.processStartEndStyle("ATM File Clearing Process Started"));
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ATM_FILE_READ;

            if ("LINUX".equals(Configurations.SERVER_RUN_PLATFORM)) {
                filepath = commonRepo.getLinuxFilePath(Configurations.FILE_CODE_ATM);
            } else if ("WINDOWS".equals(Configurations.SERVER_RUN_PLATFORM)) {
                filepath = commonRepo.getWindowsFilePath(Configurations.FILE_CODE_ATM);
            }

            //get ATM file info
            fileBean = this.getATMFileInfo(fileId);
            if (fileBean != null) {
                //get name field list
                nameFieldList = commonRepo.getNameFields(Configurations.FILE_CODE_ATM);
                //validate file name
                isFileNameValid = PaymentValidations.fileNameValidation(nameFieldList, fileBean.getFileName());
                if (isFileNameValid.isEmpty()) {
                    File file = new File(filepath + File.separator + fileBean.getFileName());
                    // Make sure file exist
                    if (file.isFile() && file.exists() && file.canRead()) {
                        fileBean.setFilePath(file.getAbsolutePath());
                        //file reading
                        fileReadStatus = atmFileClearingService.readFile(fileBean);
                        if (fileReadStatus) {
                            // if file reading completed then start validation part
                            infoLogger.info(logManager.ProcessStartEndStyle("ATM File Validation Started"));

                            Configurations.ATM_VALIDATION_HASH_TABLE = atmFileClearingRepo.getATMFieldsValidation();
                            fileContent = atmFileClearingRepo.getAtmFileContents(fileId);
                            totalCount = fileContent.size();//no of lines of the file
                            for (RecATMFileIptRowDataBean paymentFileBean : fileContent) {
                                atmFileClearingService.validateFile(fileId, paymentFileBean, successCount, failCount, invalidCount);
                            }

                            //wait till all the threads are completed
                            while (!(taskExecutor.getActiveCount() == 0)) {
                                Thread.sleep(1000);
                            }
                            //mark reversal and original transaction to EDON if it come in same file
                            atmFileClearingRepo.markAtmReversal(fileId);
                            //update file status to COMP
                            atmFileClearingRepo.updateATMFileStatus(status.getCOMMON_COMPLETED(), fileId);

                            summery.put("File ID ", fileId);
                            summery.put("Number of ATM transactions to process ", totalCount);
                            summery.put("Number of success transactions ", successCount);
                            summery.put("Number of invalid transactions ", invalidCount);
                            summery.put("Number of failure transactions ", failCount);

                            infoLogger.info(logManager.processSummeryStyles(summery));
                            infoLogger.info(logManager.ProcessStartEndStyle("ATM File Validation Completed"));
                        } else {
                            errorLogger.error("ATM file reading failed for file " + fileId);
                            //update file read status to FAIL
                            atmFileClearingRepo.updateATMFileStatus(Configurations.FAIL_STATUS, fileId);
                        }
                    } else {
                        infoLogger.info("ATM file not found..."
                                + "\nFile Name : " + fileBean.getFileName()
                                + "\nFile ID   : " + fileBean.getFileId());
                        //update file read status to FAIL
                        atmFileClearingRepo.updateATMFileStatus(Configurations.FAIL_STATUS, fileId);
                    }
                } else {
                    errorLogger.error("ATM file clearing process failed for file " + fileId + " , " + isFileNameValid);
                    //update file read status to FAIL
                    atmFileClearingRepo.updateATMFileStatus(Configurations.FAIL_STATUS, fileId);
                }
            } else {
                //file cannot proceed due to invalid status
                errorLogger.error("Cannot read, ATM file " + fileId + " is not in the initial status");
            }
        } catch (Exception ex) {
            errorLogger.error("ATM File clearing process failed for file " + fileId, ex);
            //update file status to FAIL
            try {
                atmFileClearingRepo.updateATMFileStatus(Configurations.FAIL_STATUS, fileId);
            } catch (Exception e) {
                errorLogger.error("", e);
            }
        } finally {
            infoLogger.info(logManager.ProcessStartEndStyle("ATM File Clearing Process Completed"));
        }
    }

    private synchronized FileBean getATMFileInfo(String fileId) throws Exception {
        FileBean fileBean;
        try {
            fileBean = atmFileClearingRepo.getATMFileInfo(fileId);
            if (fileBean.getFileId() != null) {
                //file status is INIT,so it can proceed.
                //update file status to intermediate status FPROS
                atmFileClearingRepo.updateATMFileStatus(DatabaseStatus.STATUS_FILE_PROS, fileId);
            } else {
                fileBean = null;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return fileBean;
    }
}