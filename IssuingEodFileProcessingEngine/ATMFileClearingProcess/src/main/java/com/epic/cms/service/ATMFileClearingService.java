/**
 * Author :
 * Date : 2/2/2023
 * Time : 2:17 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.RecATMFileIptRowDataBean;
import com.epic.cms.repository.ATMFileClearingRepo;
import com.epic.cms.util.*;
import com.epic.cms.validation.PaymentValidations;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.epic.cms.util.LogManager.*;

@Service
public class ATMFileClearingService {
    @Autowired
    public ATMFileClearingRepo atmFileClearingRepo;
    @Autowired
    public LogManager logManager;
    @Autowired
    public StatusVarList status;
    @Autowired
    private QueryParametersList queryParametersList;
    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    @Qualifier("file_read_job")
    private Job atmFileReadJob;

    /**
     * read file
     *
     * @param fileBean
     * @return
     * @throws Exception
     */
    public boolean readFile(FileBean fileBean) throws Exception {
        boolean fileReadStatus = false;
        System.out.println("Class Name:ATMFileReadService,File ID:" + fileBean.getFileId() + ",Current Thread:" + Thread.currentThread().getName());
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .addString("fileId", fileBean.getFileId())
                    .addString("fileName", fileBean.getFileName())
                    .addString("filePath", fileBean.getFilePath())
                    .addString("insertQuery", queryParametersList.getATMFileClearingInsertRecInputRowData())
                    .addString("tableName", "EODATMFILE").toJobParameters();
            JobExecution execution = jobLauncher.run(atmFileReadJob, jobParameters);
            final ExitStatus status = execution.getExitStatus();
            if (ExitStatus.COMPLETED.getExitCode().equals(status.getExitCode())) {
                System.out.println("ATM file reading job completed for file " + fileBean.getFileId());
                fileReadStatus = true;
            } else {
                final List<Throwable> exceptions = execution
                        .getAllFailureExceptions();
                for (final Throwable throwable : exceptions) {
                    errorLogger.error(throwable.getMessage(), throwable);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        return fileReadStatus;
    }

    /**
     * validate file
     *
     * @param fileId
     * @param paymentFileBean
     */
    @Async("ThreadPool_ATMFileValidator")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void validateFile(String fileId, RecATMFileIptRowDataBean paymentFileBean) {
        System.out.println("Class Name:ATMFileValidateService,File ID:" + paymentFileBean.getFileid() + ",Line Number:" + paymentFileBean.getLinenumber() + ",Current Thread:" + Thread.currentThread().getName());
        LinkedHashMap details = new LinkedHashMap();
        String errorMsg = "";

        BigDecimal lineNumber = new BigDecimal(BigInteger.ZERO);

        String[] atmFields = null;
        String[] fieldsValidation = null;

        boolean isValid = true;
        boolean isLineRejected = false;

        try {

            lineNumber = paymentFileBean.getLinenumber();//line number
            atmFields = paymentFileBean.getLinecontent().split("\\t");//split line

            int index = 0;
            for (String atmField : atmFields) {
                index++;
                fieldsValidation = (String[]) Configurations.ATM_VALIDATION_HASH_TABLE.get(String.valueOf(index));

                int validationCount = 0;
                while (validationCount < fieldsValidation.length) {

                    isValid = validate(fieldsValidation[validationCount], atmField);

                    if (!isValid) {
                        String validationDesc = atmFileClearingRepo.getErrorDesc(fieldsValidation[validationCount]);
                        String fieldDesc = atmFileClearingRepo.getATMFieldDesc(String.valueOf(index));
                        errorMsg = fieldDesc + " validation failed in " + validationDesc + "|" + atmField;
                        atmFileClearingRepo.insertToRECATMFILEINVALID(fileId, lineNumber, errorMsg);
                        errorMsg = "validation failed in line number " + lineNumber + "|" + atmField;
                        errorLogger.error(errorMsg);
                        //invalidCount.addAndGet(1);//increase invalid count by 1
                        Configurations.PROCESS_ATM_FILE_CLEARING_INVALID_COUNT++;
                        if (!isLineRejected) {
                            isLineRejected = true;
                        }
                    }
                    validationCount++;
                }
            }
            details.put("File ID", fileId);
            details.put("Line Number", lineNumber);
            details.put("Authorizer", atmFields[3]);
            details.put("Card Number", CommonMethods.cardNumberMask(new StringBuffer(atmFields[8])));
            details.put("Retrieval Reference Number", atmFields[6]);
            details.put("Transaction Date And Time", atmFields[4]);
            details.put("Transaction Amount", atmFields[10]);
            if (atmFields.length != 11) {
                details.put("Transaction Currency", atmFields[11]);
            }
            details.put("Transaction Validity", isLineRejected == true ? "invalid" : "valid");

            infoLogger.info(logManager.processDetailsStyles(details));

            int count = 0;
            String txnId = getTxnId(lineNumber.intValue());//generate a txn ID
            if (atmFileClearingRepo.checkForValidCard(new StringBuffer(atmFields[8]))) {
                if (!isLineRejected) {
                    //if on us card, then insert to RECATMTRANSACTION table
                    count = atmFileClearingRepo.insertToATMTRANSACTION(fileId, txnId, atmFields);
                }
            } else {
                // if off us card, then insert to EODEXCEPTIONALTRANSACTION table
                count = atmFileClearingRepo.insertExceptionalTransactionData(fileId, txnId, "", new StringBuffer(atmFields[8]), "", "", "", "", atmFields[4], "", "", Configurations.USER, new java.sql.Date(System.currentTimeMillis()), atmFields[10], atmFields[11], "YES", "", "", "", "", "", "", atmFields[6], "", "", "ATM", "Card No Invalid");
                errorLogger.error("Invalid card number found while ATM file validation:" + CommonMethods.cardNumberMask(new StringBuffer(atmFields[8])));
            }
            if (count > 0) {
                //update RECATMINPUTROWDATA.STATUS to EDON
                atmFileClearingRepo.updateRawAtm(paymentFileBean.getFileid(), paymentFileBean.getLinenumber());
                //successCount.addAndGet(1);//increase success count by 1
                Configurations.PROCESS_ATM_FILE_CLEARING_SUCCESS_COUNT++;
            }

        } catch (Exception ex) {
            infoLogger.error("ATM file validation failed for" +
                    "\nFile ID : " + fileId +
                    "\nLine Number : " + lineNumber);
            errorLogger.error(ex.getMessage(), ex);
            //failCount.addAndGet(1);//increase fail count by 1
            Configurations.PROCESS_ATM_FILE_CLEARING_FAILD_COUNT++;
        }
        isLineRejected = false;
    }

    private boolean validate(String fieldValidationId, String atmField) throws Exception {
        boolean valid = true;
        try {
            if ("PV001".equals(fieldValidationId)) {
                valid = PaymentValidations.isNumeric(atmField);
            } else if ("PV002".equals(fieldValidationId)) {
                valid = PaymentValidations.isValidDate(atmField);
            } else if ("PV003".equals(fieldValidationId)) {
                valid = PaymentValidations.isString(atmField);
            } else if ("PV004".equals(fieldValidationId)) {
                valid = PaymentValidations.isValidDateTime(atmField);
            } else if ("PV005".equals(fieldValidationId)) {
                valid = PaymentValidations.isValidDateTimeHHMM(atmField);
            } else if ("PV006".equals(fieldValidationId)) {
                valid = PaymentValidations.isDouble(atmField);
            } else if ("PV007".equals(fieldValidationId)) {
                valid = PaymentValidations.isAlphanumeric(atmField);
            }
        } catch (Exception ex) {
            throw ex;
        }
        return valid;
    }

    private String getTxnId(int lineNumber) throws IOException {
        String padLine = "";
        String referenceNo = "";
        String date = "", time = "";

        try {
            date = ISODate.getANSIDate(new Date());

            time = ISODate.getTime(new Date());

            padLine = ISOUtil.zeropad(lineNumber + "", 5);

            referenceNo = "V" + date + time + padLine;

        } catch (ISOException e) {
            errorLogger.error("ATM FILE VALIDATION", e);
        }
        return referenceNo;
    }
}
