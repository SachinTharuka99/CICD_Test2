package com.epic.cms.dao;

import com.epic.cms.model.bean.GlBean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface CommonFileGenProcessDao {
    List<String> getCardProductCardType(StringBuffer cardNo) throws Exception;

    void InsertIntoDownloadTable(StringBuffer cardNo, String filename, List<String> cardDetails) throws Exception;

    Date getNextWorkingDay(Date dueDate) throws Exception;

    HashMap<String, GlBean> getGLAccData() throws Exception;

    HashMap<String, String[]> getGLTxnTypes() throws Exception;

    String getCRDRFromGlTxn(String key) throws Exception;

    public List<String> getCardProductCardTypeByApplicationId(String applicationId) throws Exception;
}
