package com.epic.cms.dao;

import com.epic.cms.model.bean.LoyaltyBean;

import java.util.ArrayList;
import java.util.Date;

public interface LoyaltyPointsCalculationDao {
    ArrayList<LoyaltyBean> getTodayBillingCardSet(Date eodDate) throws Exception;

    double getLastStmtClosingLoyalty(StringBuffer cardNo, String statementId) throws Exception;

    double getThisMonthPurchases(String accNo, int stmtEndEodID, int stmtStartEodID) throws Exception;

    double getThisMonthRedeem(StringBuffer cardNo, Date stmtStartDate, Date stmtEndDate, ArrayList<Integer> requestID)  throws Exception;

    double getAdjustLoyalty(String accNo, Date stmtStartDate, Date stmtEndDate) throws Exception;

    void updateBillingStatment(LoyaltyBean loyaltyBean) throws Exception;

    int updateLoyaltyRedeemRequest(ArrayList<Integer> requestID, String eodDoneStatus) throws Exception;

    boolean getLoyaltyConfigurations() throws Exception;
}
