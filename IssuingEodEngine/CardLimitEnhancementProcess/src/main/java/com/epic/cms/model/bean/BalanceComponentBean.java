package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceComponentBean {
    private String customerId;
    private String accountNumber;
    private StringBuffer cardNumber;
    private double custOtbCredit;
    private double custOtbCash;
    private double custOtbCreditComp;
    private double custOtbCashComp;
    private double custAccOtbCredit;
    private double custAccOtbCash;
    private double custAccOtbCreditComp;
    private double custAccOtbCashComp;
    private double otbCredit;
    private double otbCash;
    private double tempCreditAmount;
    private double tempCashAmount;
    private double otbCreditComp;
    private double otbCashComp;
    private double tempCreditAmountComp;
    private double tempCashAmountComp;
    private double totalComp;
    private String cardCategory;
    private double incrementAmount;
    private String incrementType;
    private String incOrDec;
    private String requestId;
    private double payment;
    private double creditLimit;
    private double cashLimit;
    private double cumFinanceCharge;
    private double cumCashAdvance;
    private double cumTxn;
    private String txnType;
    private double txnAmount;
    private String startDate;
    private String endDate;
}
