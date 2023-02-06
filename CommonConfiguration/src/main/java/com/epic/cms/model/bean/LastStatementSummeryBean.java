package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LastStatementSummeryBean {
    private StringBuffer cardno;
    private Double opaningBalance;
    private Double closingBalance;
    private Double minAmount;
    private Date dueDate;
    private Date statementStartDate;
    private Date statementEndDate;
    private String accNo;
    private int NDIA;// number of days in arias
    private Long closingloyaltypoint;
}
