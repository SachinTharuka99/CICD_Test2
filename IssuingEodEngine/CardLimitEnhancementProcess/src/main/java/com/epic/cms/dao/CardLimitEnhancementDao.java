package com.epic.cms.dao;

import com.epic.cms.model.bean.BalanceComponentBean;
import com.epic.cms.model.bean.OtbBean;

import java.util.ArrayList;

public interface CardLimitEnhancementDao {
    ArrayList<OtbBean> getInitLimitEnhanceCustAcc() throws Exception;

    ArrayList<OtbBean> getErrorLimitEnhanceCustAcc() throws Exception;

    ArrayList<BalanceComponentBean> getLimitEnhanceReqConCardList(String customerId, String accountNumber) throws Exception;

    void updateCardCreditLimit(StringBuffer cardNumber, double otbCredit)  throws Exception;

    void updateOnlineCardCreditLimit(StringBuffer cardNumber, double otbCredit) throws Exception;

    void updateAccountCreditLimit(String accountNumber, double otbCredit) throws Exception;

    void updateOnlineAccountCreditLimit(String accountNumber, double otbCredit) throws Exception;

    void updateCustomerCreditLimit(String customerId, double otbCredit) throws Exception;

    void updateOnlineCustomerCreditLimit(String customerId, double otbCredit) throws Exception;

    void updateCardCashLimit(StringBuffer cardNumber, double otbCash) throws Exception;

    void updateOnlineCardCashLimit(StringBuffer cardNumber, double otbCash) throws Exception;

    void updateAccountCashLimit(String accountNumber, double otbCash) throws Exception;

    void updateOnlineAccountCashLimit(String accountNumber, double otbCash) throws Exception;

    void updateCustomerCashLimit(String customerId, double otbCash) throws Exception;

    void updateOnlineCustomerCashLimit(String customerId, double otbCash) throws Exception;

    void updateTempLimitIncrementTable(StringBuffer cardNumber, String status, String requestId);
}
