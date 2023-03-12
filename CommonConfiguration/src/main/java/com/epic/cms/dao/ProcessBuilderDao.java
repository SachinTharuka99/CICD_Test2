package com.epic.cms.dao;

import com.epic.cms.model.bean.ProcessBean;

public interface ProcessBuilderDao {
    ProcessBean getProcessDetails(int processId) throws Exception;

    boolean isErrorProcess(int processId);
}
