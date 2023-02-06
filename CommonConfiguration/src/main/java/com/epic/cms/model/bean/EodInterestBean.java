package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EodInterestBean {
    private String accountno;
    private StringBuffer cardNumber;
    private double forwardamount;
    private double currentInterest;
    private double actualInterest;
    private double interestRate;
    private Date dueDate;
}
