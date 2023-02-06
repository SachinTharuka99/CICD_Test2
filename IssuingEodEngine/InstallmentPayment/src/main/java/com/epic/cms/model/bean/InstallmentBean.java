package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstallmentBean {
    private int currentCount;
    private StringBuffer cardNumber;
    private String txnID;
    private String txnAmount;
    private String totalFEeAmount;
    private String Status;
    private String instalmentAmount;
    private String curruncyCode;
    private String interestRate;
    private String accNo;
    private int remainingCount;
    private int lastEodTxnId;
    private String nxtTxnDate;
    private int runningStatus;
    private String acptDate;
    private String txnDescription;
    private int duration;
    private String effectivedate;
    private String feeApplyFirstMonth;
    private String includeFirstMonth;
    private String feeType;
    private String accelarateStatus;
    private String requestID;
    private String traceNumber; // trace number which initiated when online fee call from manual api.
}
