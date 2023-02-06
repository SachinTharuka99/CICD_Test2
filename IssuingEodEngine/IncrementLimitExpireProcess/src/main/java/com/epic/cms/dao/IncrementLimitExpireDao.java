package com.epic.cms.dao;

import com.epic.cms.model.bean.LimitIncrementBean;

import java.util.ArrayList;

public interface IncrementLimitExpireDao {
    ArrayList<LimitIncrementBean> getLimitExpiredCardList() throws Exception;

    int expireCreditLimit(LimitIncrementBean limitIncrementBean) throws Exception;

    int expireOnlineCreditLimit(LimitIncrementBean limitIncrementBean) throws Exception;

    void limitExpireOnAccount(LimitIncrementBean limitIncrementBean) throws Exception;

    void limitOnlineExpireOnAccount(LimitIncrementBean limitIncrementBean) throws Exception;

    void limitExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception;

    void limitOnlineExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception;

    int expireCashLimit(LimitIncrementBean limitIncrementBean) throws Exception;

    int expireOnlineCashLimit(LimitIncrementBean limitIncrementBean) throws Exception;

    void cashLimitExpireOnAccount(LimitIncrementBean limitIncrementBean) throws Exception;

    void cashLimitOnlineExpireOnAccount(LimitIncrementBean limitIncrementBean) throws Exception;

    void cashLimitExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception;

    void cashLimitOnlineExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception;

    int updateTempLimitIncrementTable(StringBuffer cardNumber, String status, String requestId, int processId) throws Exception;
}
