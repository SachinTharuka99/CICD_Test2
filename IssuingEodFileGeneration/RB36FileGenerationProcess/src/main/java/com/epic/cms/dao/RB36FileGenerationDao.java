package com.epic.cms.dao;

import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.GlBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface RB36FileGenerationDao {
    ArrayList<StringBuffer> getNPCard() throws Exception;

    HashMap<String, ArrayList<GlAccountBean>> getPaymentDataFromEODGl() throws Exception;

    HashMap<String, GlBean> getGLAccData() throws Exception;

    int updateEodGLAccount(int key) throws Exception;

    Date getNextWorkingDay(Date DueDate) throws Exception;
}
