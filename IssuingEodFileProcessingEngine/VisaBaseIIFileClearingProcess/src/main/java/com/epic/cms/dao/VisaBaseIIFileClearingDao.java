/**
 * Author :
 * Date : 2/3/2023
 * Time : 3:48 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.VisaTC56ComposingDataBean;
import com.epic.cms.model.bean.VisaTC56CurrencyEntryBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface VisaBaseIIFileClearingDao {
    FileBean getVisaFileInfo(String fileId) throws Exception;

    int updateEODVISAFILE(String fileid) throws Exception;

    int updateEODVISAILE(String fileid, String status) throws Exception;

    void updateRecVisaFileStatus(String fileId, String status) throws Exception;

    void updateVisaProcessingStartTime(String fileId) throws Exception;

    void updateVisaFileLineNumbers(int noOfRecords, String fileID) throws Exception;

    int visaFileValidate(String fileId, String fileStatus, String sessionId) throws Exception;

    int composeVisaFileTransactions(String fileId, String sessionId) throws Exception;

    ArrayList<String> getVisaTxnIDListForTC56(String fileID) throws Exception;

    VisaTC56ComposingDataBean getVisaComposingDataForTC56(String txnID, String tcr, String fileID) throws Exception;

    void insertVisaTC56ComposedData(List<VisaTC56CurrencyEntryBean> currencyList, String fileBaseCurrencyCode, BigDecimal eodBaseCurrencyBuyingRate, BigDecimal eodBaseCurrencySellingRate) throws Exception;

    int updateTC56RecordsAsComposed(String fileId) throws Exception;

    void updateVisaProcessingStopTime(String fileID) throws Exception;

    void updateVisaFileStatus(String status, String fileId) throws Exception;
}
