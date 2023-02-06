package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EodTransactionBean {
    private String accountNo;
    private String adjustmentStatus;
    private String authCode;
    private String batchNo;
    private StringBuffer cardNo;
    private String countryNumCode;
    private String crDr;
    private String forexMarkupAmount;
    private int glStatus;
    private String mid;
    private int onlyVisaFalse;
    private int onOffStatus;
    private String paymentType;
    private String posEntryMode;
    private String sequenceNumber;
    private Date settlementDate;
    private int stmtId;
    private String status;
    private String tid;
    private String toAccNo;
    private String traceId;
    private String txnAmount;
    private Date txnDate;
    private String txnDescription;
    private String txnId;
    private String txnType;
    private String rrn;
    private String currencyType;
    private String mcc;
    private String bin;
    private String adjustmentFlag;
    private String billingAmount;
    private String fuelSurchargeAmount = "0";
    private String requestFrom;
    private String secondPartyPan;
    private int channelType;
    private String cardAssociation;
    private String cardProduct;

    private String listenerType;
}
