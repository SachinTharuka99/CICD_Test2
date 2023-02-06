/**
 * Author : sharuka_j
 * Date : 1/25/2023
 * Time : 7:03 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.*;

import java.sql.Date;
import java.util.ArrayList;

public interface AcquiringAdjustmentDao {
    ArrayList<AcqAdjustmentBean> getConfirmedAjustments() throws Exception;

    int insertToEodMerchantPayment(MerchantPayBean merchantPaymentBean, String adjustmentFlag) throws Exception;

    int insertToEodMerchantComission(String merchantCusNo,
                                     String merhantCusAcountNo, String mId, String merchantAccNo, String tId,
                                     String txnAmount, String merchantComission, String currency,
                                     String crDr, Date txnDate, String txnTypeCode, String batchNo,
                                     String txnId, String binStatus, String calMethod, String cardAssociation,
                                     String cardProduct, String segment, String originCardProduct, int adjustmentFlag) throws Exception;

    void insertToEODMerchantFee(MerchantFeeBean merchantFeeBean, String amount, java.util.Date effectDate) throws Exception;

    boolean getOnUsStatus(String txnId) throws Exception;

    int insertReversalTxnIntoTxnTable(String oldTxnId, String newTxnID) throws Exception;

    int insertReversalTxnIntoMerchantTxnTable(String oldTxnId, String newTxnID) throws Exception;

    int insertReversalCommission(String oldTxnId, String newTxnID) throws Exception;

    String getAccountNoOnCard(StringBuffer cardNo) throws Exception;

    String getCardAssociationFromBinRange(String cardNumber) throws Exception;

    MerchantDetailsBean getMerchanDetails(MerchantDetailsBean merchantDetailsBean) throws Exception;

    int insertToEODTransaction(StringBuffer cardnumber, String accountNo,
                               String mId, String tId, String txnAmount, int currencyType,
                               String crDr, java.util.Date settlementDate, java.util.Date txnDate, String txnType,
                               String batchNo, String txnId, String toAccNo, Double loyaltyPoint, String Description,
                               String countryCode, int onOffStatus, String poStringsEntryMode, String traceId, String authCode, int adjustemntFlag, String cardAssociation)
            throws Exception;

    int insertReversalTxnIntoTxnTable(AcqAdjustmentBean bean, String newTxnID, int onOffStatus) throws Exception;

    int insertIntoEodMerchantTransaction(EodTransactionBean eodTransactionBean, String status) throws Exception;

    int getBinType(String sixDigitBin, String eightDigitBin) throws Exception;

    void getPaymentAmount(String txnId, MerchantPayBean paymentBean) throws Exception;

    int updateAdjustmentToEdon(String id, String txnId) throws Exception;

    int setCardProductToEodMerTxn() throws Exception;
}
