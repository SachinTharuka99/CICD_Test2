package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class InterestDetailBean {
    private double interest;
    private Date statementEndDate;
    private double interestperiod;
}
