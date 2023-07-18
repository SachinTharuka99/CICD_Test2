package com.epic.cms.dao;

import com.epic.cms.model.bean.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OutgoingIPMFileGenDao {
    List<TransactionDataBean> getOutgoingIPMTransactionData();

    HashMap<String, String> getMasterOutgoingRejectReasonTable();

    HashMap<String, String> getCurrencyExponentTable();

    MPGSAdditionalDataBean getMPGSAdditionalData(String txnId);

    String getGCMSProductIDFromCardNumber(String cardNumber);

    IP0040T1Bean getIssuerAccountRangeDetails(String cardNumber, String cardProgram);

    IP0040T1Bean getIssuerAccountRangeDetailsWithoutCardProgram(String cardNumber);

    int insertToEODMasterOutgoingFieldIdentity(String decidedMTI, String txnId, List<String> dataElementValueList, Map<String, String> pdsMap, String cardProgramIdentifier, String gcmsProductID, String countryCodeAlpha, String region, String networkCode);

    int updateEodMerchantTransactionFileStatus(String txnId);

    int insertRejectMasterOutgoingTransaction(String eodId, String txnId, String rejectException);

    ArrayList<IRDCriteriaBean> getIRDCriteriaList(String irdCategory);

    ArrayList<String> getMatchingTxnsForIRDCriteria(String dynamicWhereClause, String ruleID);

    int updateIRDValue(String IRD, String txnId);

    int updateUndecidedIRDFileStatus();

    List<MasterOutgoingFieldIdentityBean> getPendingIPMTxnList();

    int updateMasterOutgoingFieldIdentityFileStatus(String txnId, String fileName);

    void insertIPMFileSummery(String ipmFileName, int txnCount);

    int insertOutputFiles(EodOuputFileBean outputfilebean, String fileType);
}
