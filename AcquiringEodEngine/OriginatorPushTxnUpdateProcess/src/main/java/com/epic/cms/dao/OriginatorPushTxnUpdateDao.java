/**
 * Author : rasintha_j
 * Date : 6/20/2023
 * Time : 10:37 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.EodTransactionBean;

import java.util.ArrayList;
import java.util.HashMap;

public interface OriginatorPushTxnUpdateDao {
    ArrayList<EodTransactionBean> getAllOriginatorPushTxn() throws Exception;
    String getForexPercentage() throws Exception;
    HashMap<String, String> getFinancialStatus() throws Exception;
    int insertToEODTransaction(StringBuffer cardnumber, String accountNo,
                               String mId, String tId, String txnAmount, int currencyType,
                               String crDr, java.util.Date settlementDate, java.util.Date txnDate, String txnType,
                               String batchNo, String txnId, String toAccNo, Double loyaltyPoint, String Description,
                               String countryCode, int onOffStatus, String poStringsEntryMode, String traceId, String authCode, int adjustmentFlag, String requestFrom, String secondPartyPan, String fualSurchargeAmount, String mcc, String cardAssociation)
            throws Exception;

    int updateTransactionToEDON(String txnId, StringBuffer cardNo) throws Exception;
    String getAccountNoOnCard(StringBuffer cardNo) throws Exception;
}
