package com.epic.cms.dao;

import com.epic.cms.model.bean.CardFeeBean;

import java.sql.Date;
import java.util.List;

public interface CardFeeDao {
    List<CardFeeBean> getCardFeeCountList() throws Exception;

    CardFeeBean getCardFeeCountForCard(StringBuffer cardNo, String accountNo, String feeCode) throws Exception;

    Date getNextBillingDateForCard(StringBuffer cardNo) throws Exception;

    void insertToEODCardFee(CardFeeBean cardFeeBean, double amount, Date effectDate) throws Exception;

    void updateCardFeeCount(CardFeeBean cardFeeBean) throws Exception;

    int updateDELINQUENTACCOUNTNpDetails(double accruedInterest, double accruedOverLimitFees, double accruedLatePayFees, double otherFees, String accNo) throws Exception;
}
