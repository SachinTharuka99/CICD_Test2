/**
 * Author :
 * Date : 2/2/2023
 * Time : 2:09 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.bean.RecATMFileIptRowDataBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public interface ATMFileClearingDao {
    //read
    FileBean getATMFileInfo(String fileId) throws Exception;

    void updateATMFileStatus(String status, String fileId) throws Exception;

    //validate
    ProcessBean getProcessDetails(int processId) throws Exception;

    Hashtable<String, String[]> getATMFieldsValidation() throws Exception;

    ArrayList<RecATMFileIptRowDataBean> getAtmFileContents(String fileid) throws Exception;

    String getErrorDesc(String validaionId) throws Exception;

    String getATMFieldDesc(String fieldId) throws Exception;

    boolean checkForValidCard(StringBuffer cardNumber) throws Exception;

    int insertToATMTRANSACTION(String fileId, String txnId, String[] paymentFields) throws Exception;

    int insertExceptionalTransactionData(String fileID, String txnID, String TC, StringBuffer cardNumber, String authCode, String MID,
                                         String sourceAmount, String sourceCurrencyCode, String txnDate, String txnTime, String processingDate, String lstUpdateUser,
                                         Date lstUpdateDate, String destinationAmount, String destinationCurrencyCode, String financialStatus, String merchantName,
                                         String merchantCity, String merchantCountryCode, String MCC, String merchantZipCode, String merchantState,
                                         String rrn, String tid, String posEntryMode, String fileType, String description) throws Exception;

    int updateRawAtm(String fileId, BigDecimal linenumber) throws Exception;

    void markAtmReversal(String fileId) throws Exception;

    public int insertToRECATMFILEINVALID(String fileId, BigDecimal linenumber, String errorMsg) throws Exception;
}
