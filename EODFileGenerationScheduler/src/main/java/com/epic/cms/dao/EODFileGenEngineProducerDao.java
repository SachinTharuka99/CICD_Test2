package com.epic.cms.dao;

import com.epic.cms.model.bean.ProcessBean;

import java.util.List;

public interface EODFileGenEngineProducerDao {
    List<ProcessBean> getProcessListByFileGenCategoryId(int categoryId, int issuingOrAcquiring) throws Exception;

    int getCurrentEodId(String initial_status, String error_status) throws Exception;

    String getEodStatusByEodID(int eodId) throws Exception;

    String getProcessIdByUniqueId(String uniqueId) throws Exception;

    void insertToEODProcessCount(String uniqueId, int size, String includedProcess) throws Exception;

    int updateEodProcessProgress(int successCount, int failedCount, String progress, int processId) throws Exception;

    List<String> getErrorProcessIdList() throws Exception;

    void updateProcessProgressForErrorProcess(String processId) throws Exception;

    int updateEodProcessStateCount() throws Exception;

    int getCompletedProcessCount(String uniqueId) throws Exception;

    void updateEodFileGenStatus(int eodId, String status) throws Exception;

    boolean hasErrorforLastEOD() throws Exception;

    void clearEodProcessCountTable() throws Exception;
}
