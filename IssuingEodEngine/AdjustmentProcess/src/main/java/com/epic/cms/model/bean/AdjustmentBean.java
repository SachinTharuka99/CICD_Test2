package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AdjustmentBean {
    private String id;
    private double adjustAmount;
    private Date adjustDate;
    private String adjustDes;
    private String txnType;
    private String crDr;
    private StringBuffer cardNumber;
    private String accNo;
    private String adjustType;
    private String curruncyType;
    private String paymentType;
    private String txnId;
    private String adjustTxnType;
    private String sequenceNo;
    private String traceNo;
}
