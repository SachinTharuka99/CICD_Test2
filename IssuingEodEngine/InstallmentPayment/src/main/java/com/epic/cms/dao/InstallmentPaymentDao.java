package com.epic.cms.dao;

import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.InstallmentBean;
import com.epic.cms.model.bean.ManualNpRequestBean;

import java.util.Date;
import java.util.List;

public interface InstallmentPaymentDao {
    List<ManualNpRequestBean> getManualNpRequestDetails(int reqType, String status) throws Exception;

    int updateEasyPaymentRequestToAccelerate(String accNo, String tableName) throws Exception;

    List<DelinquentAccountBean> getDelinquentAccounts() throws Exception;

    double checkForPayment(String accNo, Date eodDate) throws Exception;

    String[] getRiskClassOnNdia(int noOfDates) throws Exception;

    String getNPRiskClass() throws Exception;

    String[] getNDIAOnRiskClass(String riskClass) throws Exception;

    double checkLeastMinimumPayment(String accNO) throws Exception;

    List<InstallmentBean> getBTOrLOCDetails(String tblName1, String tblName2) throws Exception;

    int insertInToEODTransactionOnlyVisaFalse(StringBuffer cardNumber, String accNo, double txnAmount, String curruncyCode, String test, String test0, String TXN_TYPE_SALE, String txnID, String description, String CrDr, int object, String cardAssociation) throws Exception ;

    int insertInToEODTransactionWithoutGL(StringBuffer cardnumber, String accountNo, Double txnAmount, String currencyType, String settlementDate, String txnDate, String txnType, String txnId, String description, String CrDr, int i, String cardAssociation) throws Exception;

    int updateEasyPaymentTableWithFirstInstallment(InstallmentBean easyPaymentBean, String tableName) throws Exception;

    int updateFeeToEDONInTransactionTable(StringBuffer cardNumber, String traceNumber, String transactionType) throws Exception;

    int updateEasyPaymentTable(InstallmentBean easyPaymentBean, String tableName) throws Exception;

    String getEodtxnDescription(String txnID) throws Exception;

    List<InstallmentBean> getEasyPaymentDetails() throws Exception;
}