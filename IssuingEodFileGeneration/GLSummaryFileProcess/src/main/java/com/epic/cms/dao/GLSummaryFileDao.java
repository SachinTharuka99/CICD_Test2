package com.epic.cms.dao;

import com.epic.cms.model.bean.GlAccountBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface GLSummaryFileDao {
    ArrayList<GlAccountBean> getCashbackDataToEODGL() throws Exception;

    int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception;

    int updateCashback(int key, int i) throws Exception;

    ArrayList<GlAccountBean> getCashbackExpAndRedeemDataToEODGL() throws Exception;

    int updateCashbackExpAndRedeem(int key, int i) throws Exception;

    int updateAdjusment(String key, int i) throws Exception;

    int updateFeeTable(String key, int i) throws Exception;

    ArrayList<GlAccountBean> getAdjustmentDataToEODGL() throws Exception;

    ArrayList<GlAccountBean> getFeeDataToEODGL() throws Exception;

    ArrayList<GlAccountBean> getEODTxnDataToGL() throws Exception;

    void updateEODTxn(String key, int i) throws Exception;

    int updateEodGLAccount(int key) throws Exception;

    HashMap<String, ArrayList<GlAccountBean>> getDataFromEODGl() throws Exception;

    HashMap<String, String[]> getGLTypesData() throws Exception;
}
