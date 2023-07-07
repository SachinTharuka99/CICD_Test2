/**
 * Author : yasiru_l
 * Date : 6/30/2023
 * Time : 9:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.OutgoingCUPFileTransactionBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface OutgoingCUPFileDao {

    ArrayList<OutgoingCUPFileTransactionBean> getOutgoingStatementFileTransactionData() throws Exception;

    HashMap<String, String> getStatementFileBlockFields() throws Exception;
    HashMap<String, String> getOutgoingRejectReasonTable() throws Exception;

    OutgoingCUPFileTransactionBean getOriginalTxnInfoForReversalTxn(String originalTxnId) throws Exception;
    int insertOutgoingStatementFieldIdentity(String transactionId, int blockNumber, String tc, String[] blockFieldValues) throws Exception;

    int updateEodMerchantTransactionFileStatus(String txnId) throws Exception;

    int insertRejectOutgoingCUPTransaction(String eodId, String txnId, String rejectException) throws Exception;
    Set<String> getPendingOutgoingUPIStatementTxnIDList() throws Exception;

    StringBuffer getUPIStatementFileTxnFieldValues(String transactionId) throws Exception;

    int updateOutgoingUpiStatementFieldIdentityFileStatus(String txnId, String fileName) throws Exception;

    int insertOutgoingStatementFilePathToDownloadFile(String outgoinStatementFileName) throws Exception;
}