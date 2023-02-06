/**
 * Author :
 * Date : 2/3/2023
 * Time : 10:32 AM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.dao;

public interface CommonFileReadDao {
    void updateFileReadStartTime(String tableName, String fileId) throws Exception;

    void updateFileReadSummery(String tableName, int recordCount, String fileId) throws Exception;
}
