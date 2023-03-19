/**
 * Author :
 * Date : 2/2/2023
 * Time : 4:20 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.service;

import com.epic.cms.Exception.FailedCardNumConvertException;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.RecPaymentFileIptRowDataBean;
import com.epic.cms.repository.PaymentFileClearingRepo;
import com.epic.cms.util.*;
import com.epic.cms.validation.PaymentValidations;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.epic.cms.util.LogManager.*;

@Service
public class PaymentFileClearingService {
    @Autowired
    private PaymentFileClearingRepo paymentFileClearingRepo;
    @Autowired
    private QueryParametersList queryParametersList;
    @Autowired
    private StatusVarList status;
    @Autowired
    LogManager logManager;
    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    @Qualifier("file_read_job")
    private Job paymentFileReadJob;

    /**
     * read file
     *
     * @param fileBean
     * @return
     * @throws Exception
     */
    public boolean readFile(FileBean fileBean) throws Exception {
        boolean fileReadStatus = false;
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .addString("fileId", fileBean.getFileId())
                    .addString("fileName", fileBean.getFileName())
                    .addString("filePath", fileBean.getFilePath())
                    .addString("insertQuery", queryParametersList.getPaymentFileClearingInsertRecInputRowData())
                    .addString("tableName", "EODPAYMENTFILE").toJobParameters();
            JobExecution execution = jobLauncher.run(paymentFileReadJob, jobParameters);
            final ExitStatus status = execution.getExitStatus();
            if (ExitStatus.COMPLETED.getExitCode().equals(status.getExitCode())) {
                logManager.logInfo("Payment file reading job completed,File ID:" + fileBean.getFileId(),infoLoggerEFPE);
                fileReadStatus = true;
            } else {
                final List<Throwable> exceptions = execution
                        .getAllFailureExceptions();
                for (final Throwable throwable : exceptions) {
                    logManager.logError(throwable.getMessage(), throwable, errorLoggerEFPE);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        return fileReadStatus;
    }

    /**
     * file validation
     *
     * @param fileId
     * @param paymentFileBean
     */
    @Async("ThreadPool_PaymentFileValidator")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void validateFile(String fileId, RecPaymentFileIptRowDataBean paymentFileBean) {
        LinkedHashMap details = new LinkedHashMap();
        String errorMsg = "";

        BigDecimal lineNumber = new BigDecimal(BigInteger.ZERO);

        String[] paymentFields = null;
        StringBuffer cardNumber = null;
        String[] fieldsValidation = null;

        boolean isValid = true;
        try {
            lineNumber = paymentFileBean.getLinenumber();
            paymentFields = paymentFileBean.getLinecontent().split("\\|");
            //replace card number by considering NIC/BRN/Passport + last 4 digit card number
            try {
                cardNumber = new StringBuffer();
                cardNumber = findCardNumberByCardNumberType(paymentFields[34], paymentFields[25].trim());
                paymentFields[25] = cardNumber.toString();
            } catch (Exception ex) {
                logManager.logError("Card number replace failed for payment record", ex, errorLoggerEFPE);
            }

            int index = 0;
            for (String paymentField : paymentFields) {

                index++;
                fieldsValidation = (String[]) Configurations.PAYMENT_VALIDATION_HASH_TABLE.get(String.valueOf(index));

                int validationCount = 0;
                while (validationCount < fieldsValidation.length) {

                    isValid = validate(fieldsValidation[validationCount], paymentField);

                    if (!isValid) {
                        String validationDesc = paymentFileClearingRepo.getErrorDesc(fieldsValidation[validationCount]);
                        String fieldDesc = paymentFileClearingRepo.getFieldDesc(String.valueOf(index));
                        errorMsg = fieldDesc + " validation failed in " + validationDesc;
                        paymentFileClearingRepo.insertToRECPAYMENTFILEINVALID(fileId, lineNumber, errorMsg);
                        errorMsg = "Payment file validation failed in line number " + lineNumber;
                        logManager.logError(errorMsg, errorLoggerEFPE);
                        //invalidCount.addAndGet(1);//increase invalid count by 1
                        Configurations.PROCESS_PAYMENT_FILE_CLEARING_INVALID_COUNT++;
                    }
                    validationCount++;
                }
            }

            details.put("File ID", fileId);
            details.put("Line Number", lineNumber);
            details.put("Sequence Number", paymentFields[0]);
            details.put("Card Number", CommonMethods.cardNumberMask(cardNumber));
            details.put("Transaction Type", paymentFields[7]);
            details.put("Transaction Date And Time", paymentFields[22]);
            details.put("Transaction Amount", paymentFields[12]);
            details.put("Trace ID", paymentFields[18] + "");

            logManager.logDetails(details,infoLoggerEFPE);

            /**
             * insert appropriate cheque txn type
             */
                        /* String crdr, internelKey;
                        crdr = paymentFields[19].trim();
                        internelKey = paymentFields[1].trim();*/

            String oldPaymentTypeField = paymentFields[7].trim();
            //String newpaymentTypeField = convertOnlineChequesToBackendTxnType(crdr, internelKey);
            String newPaymentTypeField = convertCBTxnTypeToBackendTxnType(oldPaymentTypeField); //decide cash deposit,cheque init, cheque return from corebank transaction type
            paymentFields[7] = newPaymentTypeField;

            //check for valid card in card table
            int count = 0;
            if (paymentFileClearingRepo.checkForValidCard(new StringBuffer(paymentFields[25].trim()))) {
                // valid card, insert into payment table
                count = paymentFileClearingRepo.insertToPAYMENT(paymentFields, oldPaymentTypeField);
            } else {
                // Invalid card, Insert payment details into EodExceptionalTransaction table
                count = paymentFileClearingRepo.insertExceptionalTransactionData(fileId, paymentFields[0], "", "", paymentFields[25].trim(), "", "", "", "", paymentFields[2].trim(), "", "", Configurations.USER, new Date(System.currentTimeMillis()), paymentFields[12].trim(), Configurations.BASE_CURRENCY, "YES", "", "", "", "", "", "", "", "", "", "PAYMENT", "Card No Invalid");
               logManager.logError("Invalid card number found while payment file validation:" + paymentFields[25].trim(), errorLoggerEFPE);
            }
            if (count > 0) {
                paymentFileClearingRepo.updateRecPaymentRaw(fileId, lineNumber);
                //successCount.addAndGet(1);//increase success count by 1
                Configurations.PROCESS_PAYMENT_FILE_CLEARING_SUCCESS_COUNT++;
            }

        } catch (Exception ex) {
            logManager.logError("Payment file validation failed for" +
                    "\nFile ID : " + fileId +
                    "\nLine Number : " + lineNumber, errorLoggerEFPE);
            logManager.logError(ex.getMessage(), ex, errorLoggerEFPE);
            //failCount.addAndGet(1);//increase fail count by 1
            Configurations.PROCESS_PAYMENT_FILE_CLEARING_FAILD_COUNT++;
        } finally {
            try {
                if (cardNumber != null) {
                    CommonMethods.clearStringBuffer(cardNumber);
                }
            } catch (Exception e) {
                logManager.logError(String.valueOf(e),errorLoggerEFPE);
            }
        }
    }

    private boolean validate(String fieldValidationId, String paymentField) throws Exception {
        boolean valied = true;
        try {
            if ("PV001".equals(fieldValidationId)) {
                valied = PaymentValidations.isNumeric(paymentField);
            } else if ("PV002".equals(fieldValidationId)) {
                valied = PaymentValidations.isValidDate(paymentField);
            } else if ("PV003".equals(fieldValidationId)) {
                valied = PaymentValidations.isString(paymentField);
            } else if ("PV004".equals(fieldValidationId)) {
                valied = PaymentValidations.isValidDateTime(paymentField);
            } else if ("PV006".equals(fieldValidationId)) {
                valied = PaymentValidations.isDouble(paymentField);
            } else if ("PV007".equals(fieldValidationId)) {
                valied = PaymentValidations.isAlphanumeric(paymentField);
            } else if ("PV008".equals(fieldValidationId)) {
                valied = PaymentValidations.isStringWithSpace(paymentField);
            } else if ("PV000".equals(fieldValidationId)) {
                valied = true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return valied;
    }

    private String convertCBTxnTypeToBackendTxnType(String cbTransactionType) throws Exception {
        String onlinePaymentType = null;
        try {
            onlinePaymentType = status.getCASH_DEPOSIT_TYPE();
            if (Configurations.PAYMENT_FILE_CHEQUE_INITIATE_TXN_TYPES.contains(cbTransactionType)) {
                onlinePaymentType = status.getCHEQUE_INITIATE_STATUS();
            } else if (Configurations.PAYMENT_FILE_CHEQUE_RETURN_TXN_TYPES.contains(cbTransactionType)) {
                onlinePaymentType = status.getCHEQUE_RETURN_STATUS();
            } else {
                onlinePaymentType = status.getCASH_DEPOSIT_TYPE();
            }
        } catch (Exception ex) {
            throw ex;
        }
        return onlinePaymentType;
    }

    private StringBuffer findCardNumberByCardNumberType(String cardNumberType, String idNumberValue) throws Exception {
        try {
            if (cardNumberType == null || cardNumberType.isEmpty()) {
                throw new FailedCardNumConvertException("Card number empty");
            } else {
                if (cardNumberType.equals("00") || cardNumberType.equals("0")) { // plain card number
                    return new StringBuffer(idNumberValue);
                } else if (cardNumberType.equals("02") || cardNumberType.equals("2")) { // NIC/BRN/Passport number of the Primary Card + Last 4 digits of the Primary or Supplementary Card linked to the NIC
                    return paymentFileClearingRepo.getCardNumberFromMainCardNIC(idNumberValue);
                } else if (cardNumberType.equals("03") || cardNumberType.equals("3")) { // NIC/BRN/Passport number of the  Card + Last 4 digits of the Card linked to the NIC
                    return paymentFileClearingRepo.getCardNumberFromNIC(idNumberValue);
                } else {
                    return new StringBuffer(idNumberValue);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
}
