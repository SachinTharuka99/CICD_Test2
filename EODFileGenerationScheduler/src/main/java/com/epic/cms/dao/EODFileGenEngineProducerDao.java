package com.epic.cms.dao;

import com.epic.cms.model.bean.ProcessBean;

import java.util.List;

public interface EODFileGenEngineProducerDao {
    List<ProcessBean> getProcessListByFileGenCategoryId(int categoryId, int issuingOrAcquiring) throws Exception;

    int getCurrentEodId(String initial_status, String error_status) throws Exception;

    String getEodStatusByEodID(int eodId) throws Exception;
}
