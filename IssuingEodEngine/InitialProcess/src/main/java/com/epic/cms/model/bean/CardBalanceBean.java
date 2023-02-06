package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardBalanceBean {
    private StringBuffer cardNumber;
    private double cumFinanceCharge;
    private double cumCashAdvance;
    private double cumTxn;
    private double payment;
}
