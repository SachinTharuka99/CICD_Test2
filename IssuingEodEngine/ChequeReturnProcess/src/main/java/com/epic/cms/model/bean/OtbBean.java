package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OtbBean {
    private String accountnumber;
    private StringBuffer cardnumber;
    private String customerid;
    private double otbcredit;
    private double otbcash;
    private double finacialcharges;
    private double cumpayment;
    private double cumcashadvance;
    private double cumtransactions;
    private double tmpcredit;
    private double tmpcash;
    private double txnAmount;
    private String txntype;
    private String txntypedesc;
    private double payment;
    private double sale;
    private double cashadavance;
    private String isPrimary;
    private StringBuffer maincardno;
    private int id;
    private double easypayrev;
    private double easypay;
    private double easypayfee;
    private double mvisaRefund;
    private double refund;
    private double reversal;
    private double moneysend;
    private double moneysendreversal;
}
