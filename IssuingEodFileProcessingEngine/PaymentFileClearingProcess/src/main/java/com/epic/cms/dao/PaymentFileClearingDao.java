/**
 * Author :
 * Date : 2/2/2023
 * Time : 4:14 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.RecPaymentFileIptRowDataBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public interface PaymentFileClearingDao {
    FileBean getPaymentFileInfo(String fileId) throws Exception;

    void updatePaymentFileStatus(String status, String fileId) throws Exception;

    //validation
    Hashtable<String, String[]> getPaymentFieldsValidation() throws Exception;

    ArrayList<RecPaymentFileIptRowDataBean> getPaymentFileContents(String fileId) throws Exception;

    String getErrorDesc(String validaionId) throws Exception;

    String getFieldDesc(String fieldId) throws Exception;

    int insertToRECPAYMENTFILEINVALID(String fileId, BigDecimal linenumber, String errorMsg) throws Exception;

    boolean checkForValidCard(StringBuffer cardNumber) throws Exception;

    int insertToPAYMENT(String[] paymentFields, String paymentType) throws Exception;

    int insertExceptionalTransactionData(String fileID, String txnID, String TC, String TCTCQ, String cardNumber, String authCode, String MID,
                                         String sourceAmount, String sourceCurrencyCode, String txnDate, String txnTime, String processingDate, String lstUpdateUser,
                                         Date lstUpdateDate, String destinationAmount, String destinationCurrencyCode, String financialStatus, String merchantName,
                                         String merchantCity, String merchantCountryCode, String MCC, String merchantZipCode, String merchantState,
                                         String rrn, String tid, String posEntryMode, String fileType, String description) throws Exception;


    int updateRecPaymentRaw(String fileId, BigDecimal linenumber) throws Exception;

    StringBuffer getCardNumberFromMainCardNIC(String nicWithLast4DigitCard) throws Exception;

    StringBuffer getCardNumberFromNIC(String nicWithLast4DigitCard) throws Exception;
}
