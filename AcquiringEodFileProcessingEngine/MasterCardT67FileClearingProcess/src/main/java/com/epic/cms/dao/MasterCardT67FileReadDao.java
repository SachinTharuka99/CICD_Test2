/**
 * Author : rasintha_j
 * Date : 7/10/2023
 * Time : 1:23 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.*;

import java.util.ArrayList;

public interface MasterCardT67FileReadDao {
    public FilePathBean loadFilePaths() throws Exception;
    public boolean isFilesAvailable(String status) throws Exception;
    public ArrayList<FileDetailsBean> getFileDetails(String status) throws Exception;
    public void updateFileStartTime(String fileId) throws Exception;
    public void updateFileStatus(String fileId, String status) throws Exception;
    public void updateFileStatistics(String fileId, String status,String transactionCount) throws Exception;
    public int truncateEodMasterIP0075T1Data() throws Exception;
    public int truncateEodMasterIP0040T1Data() throws Exception;
    public boolean isInputFileExists(String fileName) throws Exception;
    public int insertRecordToEODMASTERT67FILE(EODInputFileDetailBean bean) throws Exception;
    public void updateOrInsertMasterIP0040T1Data(IP0040T1Bean ip0040t1Bean, String fileId) throws Exception;
    public void updateOrInsertMasterIP0075T1Data(IP0075T1Bean ip0075t1Bean, String fileId) throws Exception;
}
