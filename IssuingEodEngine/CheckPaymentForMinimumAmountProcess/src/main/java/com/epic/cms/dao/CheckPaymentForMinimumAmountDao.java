package com.epic.cms.dao;

import com.epic.cms.model.bean.LastStatementSummeryBean;

import java.util.List;

public interface CheckPaymentForMinimumAmountDao {

    List<LastStatementSummeryBean> getStatementCardList() throws Exception;

    String getAccountNoOnCard(StringBuffer cardNo) throws Exception;

    Boolean insertToMinPayTable(StringBuffer cardNo, double fee, double totalTransactions, java.sql.Date dueDate, String accNo, int statementDayEODID, double totalPayment, double paymentsBeforeDueDate) throws Exception;

    double getPaymentAmount(String accNO, int startEOD) throws Exception;

    double getTotalPaymentExceptDueDate(String accNO, int startEOD) throws Exception;
}
