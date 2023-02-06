package com.epic.cms.dao;

import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.CashAdvanceBean;
import com.epic.cms.model.bean.LastStmtSummeryBean;

import java.sql.Date;
import java.util.List;

public interface RunnableFeeDao {
    List<CardBean> getAllActiveCards() throws Exception;

    List<CashAdvanceBean> findCashAdvances(StringBuffer cardNo) throws Exception;

    boolean updateNextAnniversaryDate(StringBuffer cardNumber) throws Exception;

    boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception;

    int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception;

    Boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws Exception;

    LastStmtSummeryBean getLastStatementSummaryInfor(StringBuffer cardNo) throws Exception;

    java.sql.Date getNextBillingDateForCard(StringBuffer cardNo) throws Exception;

    CardFeeBean getCardFeeProfileForCard(StringBuffer cardNumber, String feeCode) throws Exception;

    void insertToEODcardFee(CardFeeBean cardFeeBean, double amount, Date effectDate) throws Exception;

    void updateCardFeeCount(CardFeeBean cardFeeBean) throws Exception;

    boolean checkDuplicateCashAdvances(StringBuffer cardNo, String txnId, String feeCode) throws Exception;

    String getAccountNoOnCard(StringBuffer cardNo) throws Exception;

    double getTotalPayment(String accNo, int startEodId, int endEodId) throws Exception;
}
