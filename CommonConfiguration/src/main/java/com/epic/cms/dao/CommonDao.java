package com.epic.cms.dao;

import com.epic.cms.model.bean.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface CommonDao {
    ProcessBean getProcessDetails(int processId) throws Exception;

    void insertToEodProcessSumery(int processId) throws Exception;

    void updateEodProcessSummery(int eodId, String status, int processId, int successCount, int failedCount, String progress) throws Exception;

    //int updateEodProcessSummery(int eodId, String status, int processId) throws Exception;

    StringBuffer getMainCardNumber(StringBuffer cardNo) throws Exception;

    HashMap<Integer, ArrayList<EodTransactionBean>> getAllSettledTxnFromTxn() throws Exception;

    int insertToEODTransaction(StringBuffer cardNumber, String accountNo,
                               String mId, String tId, String txnAmount, int currencyType,
                               String crDr, Date settlementDate, Date txnDate, String txnType,
                               String batchNo, String txnId, String toAccNo, Double loyaltyPoint, String Description,
                               String countryCode, int onOffStatus, String poStringsEntryMode, String traceId, String authCode, int adjustmentFlag, String requestFrom, String secondPartyPan, String fualSurchargeAmount, String mcc, String cardAssociation) throws Exception;

    int insertIntoEodMerchantTransaction(EodTransactionBean eodTransactionBean, String status) throws Exception;

    int updateTransactionToEDON(String txnId, StringBuffer cardNo) throws Exception;

    StringBuffer getNewCardNumber(StringBuffer cardNumber) throws Exception;

    void updateCardOtb(OtbBean cardBean) throws Exception;

    void updateAccountOtb(OtbBean bean) throws Exception;

    void updateCustomerOtb(OtbBean bean) throws Exception;

    double getTotalPaymentSinceLastDue(String accNo, Date eodDate, Date date) throws Exception;

    HashMap<String, Double> getDueAmountList(StringBuffer cardNumber) throws Exception;

    DelinquentAccountBean setDelinquentAccountDetails(StringBuffer cardNo) throws Exception;

    int insertToRECPAYMENTFILEINVALID(String fileid, BigDecimal linenumber, String errorMsg) throws Exception;

    int insertToRECATMFILEINVALID(String fileid, BigDecimal linenumber, String errorMsg) throws Exception;

    int updateEODPAYMENTFILE(String fileid) throws Exception;

    int updateEODPAYMENTFILE(int noofrecords, String status, String fileid) throws Exception;

    int updateEODATMFILE(String fileid) throws Exception;

    int updateEODATMFILE(int noofrecords, String status, String fileid) throws Exception;

    boolean isErrorProcess(int ProcessID) throws Exception;

    boolean isProcessCompletlyFail(int ProcessID) throws Exception;

    CardAccountCustomerBean getCardAccountCustomer(StringBuffer cardNo) throws Exception;

    void insertErrorEODCard(ErrorCardBean errorCardBean) throws Exception;

    int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception;

    boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception;

    boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws Exception;

    CardBean getCardDetails(StringBuffer cardNo) throws Exception;

    boolean getTriggerEligibleStatus(String triggerPoint, String smsOrEmail) throws Exception;

    void insertIntoDelinquentHistory(StringBuffer cardNumber, String accountNo, String remark) throws Exception;

    double getPaymentAmount(String accNo, int statementDayEODID, String initial_status) throws Exception;


    String getCardAssociationFromCardBin(String cardBin) throws Exception;

    int insertInToEODTransaction(StringBuffer cardNmber, String accountNo, String txnAmount, String currencyType, String settlementDate, String txnDate, String txnType, String txnId, String description, String CrDr, String adjStatus, String cardAssociation) throws Exception;

    int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception;

    Boolean updateEODProcessCount(String uniqueId) throws Exception;

    int insertOutputFiles(EodOuputFileBean outputFileBean, String fileType) throws Exception;

    void updateFileGenProcessSummery(String fileName, int eodId, String status, int processId, int processSuccessCount, int processFaildCount, String progress);

    public boolean checkForValidCard(StringBuffer cardNumber) throws Exception;

    String getLinuxFilePath(String fileCode) throws Exception;

    String getWindowsFilePath(String fileCode) throws Exception;

    ArrayList<String> getNameFields(String fileType) throws Exception;

    int getCurrentEodId(String initStatus, String errorStatus) throws Exception;

    String getEodStatusByEodID(int eodId) throws Exception;

    int getRuninngEODId(String initial_status) throws Exception;
}