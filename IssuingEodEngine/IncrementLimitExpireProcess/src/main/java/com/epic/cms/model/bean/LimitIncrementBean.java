package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitIncrementBean {
    private String customerid;
    private String accountnumber;
    private StringBuffer cardNumber;
    private String cardcategorycode;
    private double custotbcredit;
    private double custotbcash;
    private double accotbcredit;
    private double accotbcash;
    private double otbcredit;
    private double otbcash;
    private String incrementAmount;
    private String incrementType;
    private String incordec;
    private String requestid;
}
