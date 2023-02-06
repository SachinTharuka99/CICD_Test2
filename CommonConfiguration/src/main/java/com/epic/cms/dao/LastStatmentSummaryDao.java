package com.epic.cms.dao;

import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.model.bean.StatementBean;

import java.util.List;

public interface LastStatmentSummaryDao {
    List<LastStatementSummeryBean> getStatementCardList() throws Exception;

    LastStatementSummeryBean getLastStatementSummaryInfo(StringBuffer cardNo)  throws Exception;

    StatementBean getLastStatementDetails(StringBuffer cardNumber)  throws Exception;
}
