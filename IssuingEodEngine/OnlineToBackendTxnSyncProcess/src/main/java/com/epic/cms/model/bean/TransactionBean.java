package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
public class TransactionBean {

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
    private String onOfStatus;
    private String serviceCode;
    private String cardNo;
    private String expiryDate;
    private String txnCurrency;
    private String txnAmount;
    private String billingCurrency;
    private String billingAmount;
    private String settlementCurrency;
    private String settlementAmount;
    private String settlementTxnCount;
    private String settlementDate;
    private Timestamp settlementDateOnline;
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
    private String localTime;
    private String localDate;
    private Date createdTime;
    private String lastUpdatedTime;
    private String cardSequenceNo;
    private String fromAcc;
    private String toAccount;
    private String channelType;
    private String eodStatus;
    private String acceptorname;
    private String billaccountno;
    private Integer billproviderid;
    private String billrefno;
    private String caic;
    private String f60termtype;
    private String f62txnid;
    private Integer txnsubtype;
    private String backendtxntype;
    private BigDecimal cb_seq_no;

    private String cvm;
    private String visaCvv2Result;
    private String visaReqResReasonCode;
    private String listenerId;

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

    //purchase identifier for mVisa refund TC 06
    private String purchaseId;

    private String SECOND_PARTY_PAN;

    private int acqOrIssStatus; // 0 - not decided,1 - Issuing, 2 - Acquring, 3 - Both
    private int considerToEOD; //  0 - not decided, 1 - need to consider to eod, 2 - no need to consider to eod
}
