package com.epic.cms.dao;

import com.epic.cms.model.bean.ProcessBean;
import org.springframework.stereotype.Repository;

@Repository
public interface InitialProcessDao {
    int swapEodCardBalance() throws Exception;
    boolean insertIntoOpeningAccBal() throws Exception ;

    void setResetCapsLimit(String tableName);

    void setResetCapsLimitAccount(String tableName);

    ProcessBean getProcessDetails(int processId);
}
