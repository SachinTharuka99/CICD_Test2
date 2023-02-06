/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:34 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.MasterFieldsDataBean;
import com.epic.cms.model.bean.MasterPDSBean;
import com.epic.cms.model.bean.MasterRejectBean;

import java.sql.Date;
import java.util.ArrayList;

public interface MasterFileClearingDao {
    boolean isFilesAvailable(String status) throws Exception;

    ArrayList<FileBean> getFileDetails(String status) throws Exception;

    void loadFilePaths() throws Exception;

    FileBean getMasterFileInfo(String fileId) throws Exception;

    void updateFileStartTime(String fileId) throws Exception;

    void updateFileStatus(String fileId, String status) throws Exception;

    void insertFileDetailsIntoEODMasterInputRowData(String fileID, String lineNumber, String Content) throws Exception;

    void insertFileDetailsIntoEODMasterFieldIdentity(MasterFieldsDataBean masterBean) throws Exception;

    void insertFileDetailsIntoEODMasterTransaction(MasterFieldsDataBean masterBean) throws Exception;

    int insertExceptionalTransactionData(String fileID, String txnID, String TC, StringBuffer cardNumber, String authCode, String MID, String sourceAmount, String sourceCurrencyCode, String txnDate, String txnTime, String processingDate, String lstUpdateUser, Date lstUpdateDate, String destinationAmount, String destinationCurrencyCode, String financialStatus, String merchantName, String merchantCity, String merchantCountryCode, String MCC, String merchantZipCode, String merchantState, String rrn, String tid, String posEntryMode, String fileType) throws Exception;

    void insertRejectedMasterDetails(MasterRejectBean rejectBean, String user) throws Exception;

    void updateFileRecordCount(String fileId, String count) throws Exception;

    void updateFileTxnCount(String fileId, String tCount) throws Exception;

    int loadMasterTransactionCount(String fileId) throws Exception;

    void insertMasterPDSDetailsIntoEODMASTERPDSDATA(MasterPDSBean masterPDSBean) throws Exception;
}
