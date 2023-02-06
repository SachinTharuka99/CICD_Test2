package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DelinquentAccountBean {
    private String accNo;
    private String cif;
    private String nameOnCard;
    private String nameInFull;
    private String idType;
    private String idNumber;
    private String accStatus;
    private String cardCategory;
    private int NDIA;
    private int MIA;
    private Date lastStatementDate;
    private String contactNo;
    private String email;
    private String address;
    private String delinqstatus;
    private StringBuffer cardNumber;
    private Date dueDate;
    private String assignStatus;
    private String supervisor;
    private String assignee;
    private String riskClass;
    private String dueAmount;
    private int isdueDate;
    private double npInterest;
    private double npOutstanding;
    private double accruedInterest;
    private double accruedFees;
    private double accruedOverLimit;
    private double accruedlatePay;
    private double provisionAmount;
    private Date npDate;
    private double remainDue;
    private boolean isEmpty;
}
