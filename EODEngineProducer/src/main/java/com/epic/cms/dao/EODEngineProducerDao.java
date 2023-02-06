/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:35 PM
 * Project Name : eod-engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.ProcessBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EODEngineProducerDao {
    List<String> getEODStatusFromEODID(String eodID);

    boolean checkUploadedFileStatus();

    List<ProcessBean> getProcessListByCategoryId(int categoryId);

    void insertToEODProcessCount(String uniqueId, int size, String includedProcess);

    int getCompletedProcessCount(String uniqueId) throws Exception;

    void clearEodProcessCountTable() throws Exception;
}