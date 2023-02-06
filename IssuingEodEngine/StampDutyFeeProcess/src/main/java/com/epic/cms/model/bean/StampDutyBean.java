package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StampDutyBean {
    private String accountNumber;
    private StringBuffer cardNumber;
    private double persentage;
    private int currencycode;
    private double foriegnTxnAmount;
}
