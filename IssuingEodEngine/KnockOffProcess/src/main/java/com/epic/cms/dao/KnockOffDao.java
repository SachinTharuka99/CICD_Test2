package com.epic.cms.dao;

import com.epic.cms.model.bean.OtbBean;

import java.util.ArrayList;

public interface KnockOffDao {

    ArrayList<OtbBean> getInitKnockOffCustAcc() throws Exception;

    ArrayList<OtbBean> getErrorKnockOffCustAcc() throws Exception;

    ArrayList<OtbBean> getKnockOffCardList(String customerid, String accountnumber) throws Exception;

    OtbBean getMainCard(String accountnumber) throws Exception;

    ArrayList<OtbBean> getPaymentList(StringBuffer cardnumber) throws Exception;

    OtbBean getEomKnockOffAmount(StringBuffer cardnumber) throws Exception;

    OtbBean getEodKnockOffAmount(StringBuffer cardnumber) throws Exception;

    int updateEodPayment(int id, double mainFinancialCharges, double mainCashAdvances, double mainTransactions, double supFinancialCharges, double supCashAdvances, double supTransactions, double forwardAmount, String status) throws Exception;

    int updateCardOtb(OtbBean cardBean) throws Exception;

    int updateEodClosingBalance(StringBuffer cardNumber, double closingBalance) throws Exception;

    int updateEOMCARDBALANCE(OtbBean cardBean) throws Exception;

    int updateEODCARDBALANCE(OtbBean cardBean) throws Exception;

    int updateCardComp(OtbBean cardBean) throws Exception;

    int updateAccountOtb(OtbBean otbBean) throws Exception;

    int updateCustomerOtb(OtbBean bean) throws Exception;

    int OnlineupdateCardOtb(OtbBean supCardBean) throws Exception;

    int OnlineupdateAccountOtb(OtbBean custAccBean) throws Exception;

    int OnlineupdateCustomerOtb(OtbBean custAccBean) throws Exception;
}
