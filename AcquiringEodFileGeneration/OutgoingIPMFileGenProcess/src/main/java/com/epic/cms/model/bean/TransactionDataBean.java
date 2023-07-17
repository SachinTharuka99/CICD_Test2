/**
 * Author : lahiru_p
 * Date : 7/11/2023
 * Time : 2:08 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDataBean {
    //From Merchant Detail Tables
    private String merchantName;
    private String merchantCity;
    private String merchantCountryCode;
    private String merchantCategoryCode;
    private String merchantZipCode;
    private String merchantStateCode;
    private String merchantTelephoneNumber;

    //From EODMERCHANTTRANSACTION, TRANSACTION Table data
    private String txnId;
    private String mti;
    private String responseMti;
    private String processingCode;
    private String offMti;
    private String offResponseMti;
    private String offProcessingCode;
    private String nii;
    private String txnTypeCode;
    private String bin;
    private String requestFrom;
    private String listenerType;
    private String onOffStatus;
    private String serviceCode;
    private StringBuffer cardno;
    private String expiryDate;
    private String txnCurrency;
    private String txnAmount;
    private String billingCurrency;
    private String billingAmount;
    private String settlementCurrency;
    private String settlementAmount;
    private String settlementTxnCount;
    private String settlementDate;
    private String batchNo;
    private String tid;
    private String mid;
    private String mcc;
    private String countryCode;
    private String traceNo;
    private String invoiceNo;
    private String rrn;
    private String authCode;
    private String responseCode;
    private String status;
    private String posConditionCode;
    private String posEntryMode;
    private String aiic;
    private String fiic;
    private String tag5f2a;
    private String tag9a;
    private String tag9c;
    private String tag9f34;
    private String tag9f02;
    private String tag9f03;
    private String tag9f1a;
    private String tag9f1e;
    private String tag9f27;
    private String tag9f33;
    private String tag9f35;
    private String tag9f41;
    private String txnTime;
    private String txnDate;
    private String cardSequenceNumber;
    private String fromAccount;
    private String toAccount;
    private String channelType;
    private String eodStatus;
    private String acceptorName;
    private String billAccountNo;
    private String billProviderId;
    private String billRefNo;
    private String caic;
    private String f60termtype;
    private String f62txnid;
    private String txnSubType;
    private String chqBankName;
    private String chqBranchName;
    private String chqNo;
    private String chqReturnDate;
    private String paymentInitType;
    private String paymentMode;
    private String visaTxnStatus;
    private String backendTxnType;
    private String autoSettlementStatus;
    private String onlineCreatedTime;
    private String remarks;
    private String cbSeqNo;
    private String eci;
    private String currencyType;
    private String crdr;
    private String transactionDate;
    private String transactionType;
    private String transactionId;
    private String toAccountNo;
    private String countryNumCode;
    private String paymentType;
    private String sequenceNumber;
    private String traceId;

    //newly added fields from online side
    private String cvm;
    private String visaCvv2Result;
    private String visaReqResReasonCode;

    //for TCR 05
    private String transactionIdentifier; //TCR 05 from online side
    private String authorizedAmount;
    private String authorizationCurrencyCode;
    private String authorizationResponseCode;
    private String totalAuthorizedAmount;

    private boolean representmentStatus;
    private String originalTxnId; // for reversal transaction only (original transaction id)
    private String mvv; //merchant verification value

    //emv chip data
    private String EMV_9F33; //Terminal Capabilities
    private String EMV_95;   //Terminal Verification Results
    private String EMV_82;   //Application Interchange Profile
    private String EMV_9A;   //Transaction Date
    private String EMV_9C;   //Transaction Type
    private String EMV_5F2A; //Transaction Currency Code
    private String EMV_9F02; //Amount, Authorised (Numeric)
    private String EMV_9F03; //Amount, Other (Numeric)
    private String EMV_9F10; //Issuer Application Data
    private String EMV_9F1A; //Terminal Country Code
    private String EMV_9F26; //Application Cryptogram
    private String EMV_9F36; //Application Transaction Counter (ATC)
    private String EMV_9F37; //Unpredictable Number
    private String EMV_9F6E; //Form Factor Indicator
    private String EMV_9F27; //Cryptogram Information Data
    private String EMV_9F34; //Cardholder Verification Method(CVM) Result
    private String EMV_84; //Dedicated File Name

    private String terminalCapability; // 2-Magnetic Strip, 3-Chip Capable, 4-Proximity Read Capable
    private String purchaseIdentifier;
    private String SECOND_PARTY_PAN;

    private String MC_F63_DATA;
    private String F15_SETTLE_DATE;
}
