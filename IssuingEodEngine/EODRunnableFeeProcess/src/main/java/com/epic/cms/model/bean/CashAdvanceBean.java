package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CashAdvanceBean {
    private Double totalCashAdvanceInterest;
    private Double totalCashAdvanceFee;
    private Double totalCashAdvanceAmount;
    //Added creditcard number as well @author Bilal
    private StringBuffer cardNumber;
    private String accountNo;
    private Date transactionDate;
    private String txnid;
}
