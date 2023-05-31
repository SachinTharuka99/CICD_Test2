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

    int updatePreviousEODErrorCardDetails(String currentEodID) throws Exception;

    int updatePreviousEODErrorMerchantDetails(String currentEodID) throws Exception;

    int updateEodProcessProgress() throws Exception;

    List<String> getErrorProcessIdList() throws Exception;

    void updateProcessProgressForErrorProcess(String processId) throws Exception;

    int updateEodProcessStateCount() throws Exception;

    void updateEodStatus(int errorEodId, String status) throws Exception;

    boolean hasErrorforLastEOD() throws Exception;

    void updateEodEndStatus(int errorEodId, String status) throws Exception;

    int getNextRunningEodId();
}