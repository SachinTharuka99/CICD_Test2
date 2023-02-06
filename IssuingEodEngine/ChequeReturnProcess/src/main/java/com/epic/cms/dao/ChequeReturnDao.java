package com.epic.cms.dao;

import com.epic.cms.model.bean.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ChequeReturnDao {
    List<ReturnChequePaymentDetailsBean> getChequeReturns() throws Exception;

    int updateChequeReturns(StringBuffer cardNo, String sequenceNumber, java.sql.Date returnDate) throws Exception;

    int updateChequeReturnsForEODPayment(StringBuffer cardNo, String sequenceNumber) throws Exception;

    StringBuffer getNewCardNumber(StringBuffer oldCardNumber) throws Exception;

    Map<StringBuffer, List<ReturnChequePaymentDetailsBean>> returnChequePaymentDetails() throws Exception;

    CardAccountCustomerBean getCardAccountCustomer(StringBuffer cardNo) throws Exception;

    ReturnChequePaymentDetailsBean getChequeKnockOffBean(StringBuffer cardNumber) throws Exception;

    OtbBean getEOMPendingKnockOffList(StringBuffer cardNumber) throws Exception;

    int updateEOMCARDBalanceKnockOn(OtbBean cardBean) throws Exception;

    int updateCustomerOtb(OtbBean bean) throws Exception;

    int updateAccountOtb(OtbBean otbBean) throws Exception;

    int updateCardOtb(OtbBean cardBean) throws Exception;

    int updateOnlineCustomerOtb(OtbBean bean) throws Exception;

    int updateOnlineAccountOtb(OtbBean otbBean) throws Exception;

    int updateOnlineCardOtb(OtbBean cardBean) throws Exception;

    int updateEODCARDBalanceKnockOn(OtbBean cardBean) throws Exception;

    InterestDetailBean getIntProf(String accountnumber) throws Exception;

    String getTxnIdForLastChequeByAccount(PaymentBean bean) throws SQLException;

    String getTxnIdForLastCheque(PaymentBean bean) throws SQLException;

    boolean checkDuplicateChequeReturnEntry(StringBuffer cardnumber, Double txnAmount, String txnId, String traceid, String seqNo) throws Exception;

    int insertReturnChequeToEODTransaction(StringBuffer cardnumber, String accountNo, Double txnAmount, String txnId, String traceid, String seqNo, String cardAssociation) throws Exception;

    int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception;

    boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception;

    Boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws Exception;

    int updatePaymentStatus(StringBuffer cardno, String status, String seqNo) throws Exception;

    int updateTransactionEODStatus(StringBuffer newCardNo, StringBuffer OldCardNo, String status, String seqNo) throws Exception;

    int updateChequeStatusForEODTxn(PaymentBean payBean, String accountNo) throws Exception;

    int updateChequePaymentStatus(int id, String status) throws Exception;

    String getAccountNoOnCard(StringBuffer cardNo) throws Exception;

    double getPaymentAmountBetweenDueDate(String accNO, int startEOD, String status, String dueDate) throws Exception;

    EodInterestBean getEodInterestForCard(StringBuffer cardNo) throws Exception;

    int updateEodInterestForCard(StringBuffer cardNo, double interest) throws Exception;

    Boolean getFeeCodeIfThereExists(StringBuffer cardNumber, String feeCode) throws Exception;

    boolean restoreMinimumPayment(StringBuffer cardNo) throws SQLException;

    BlockCardBean getCardBlockOldCardStatus(StringBuffer cardNumber) throws Exception;

    int updateCardStatus(StringBuffer cardNumber, String status) throws Exception;

    String[] getRiskClassOnNdia(int noOfDates) throws Exception;

    int updateDelinquencyStatus(StringBuffer cardNo, String delinqClass, int ndia) throws Exception;

    Boolean insertToMinPayTableOld(StringBuffer cardNo, double fee, double totalTransactions, java.sql.Date dueDate, double paymentAmount) throws Exception;
}
