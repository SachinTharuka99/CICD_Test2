package com.epic.cms.dao;

import com.epic.cms.model.bean.OtbBean;

import java.util.ArrayList;

public interface TxnMismatchPostDao {
    ArrayList<OtbBean> getInitEodTxnMismatchPostCustAcc() throws Exception;

    ArrayList<OtbBean> getErrorEodTxnMismatchPostCustAcc() throws Exception;

    ArrayList<OtbBean> getInitTxnMismatch(String accountNumber) throws Exception;
}
