package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardFeeBean {
    private StringBuffer cardNumber;
    private String accNumber;
    private String accStatus;
    private String feeCode;
    private int feeCount;
    private int currCode;
    private String crOrDr;
    private double flatFee;
    private double minAmount;
    private double maxAmount;
    private double percentageAmount;
    private String combination;
    private double cashAmount;
    private String nextAnniversaryDate;
    private double otbCredit;
    private double creditLimit;
    private String txnId;

}
