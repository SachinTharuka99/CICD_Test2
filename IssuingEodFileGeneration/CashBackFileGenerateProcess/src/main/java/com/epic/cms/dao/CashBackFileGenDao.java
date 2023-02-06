package com.epic.cms.dao;

import com.epic.cms.model.bean.GlAccountBean;

import java.util.ArrayList;

public interface CashBackFileGenDao {
    ArrayList<GlAccountBean> getCahsBackRedeemList() throws Exception;

    String getCashBackDebitAccount() throws Exception;

    int updateCashBackRedeemExp(int key) throws Exception;
}
