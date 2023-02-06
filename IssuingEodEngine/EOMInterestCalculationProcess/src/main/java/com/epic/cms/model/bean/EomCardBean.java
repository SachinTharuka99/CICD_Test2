package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EomCardBean {

    private String accNo;
    private StringBuffer cardNo;
    private String accStatus;
    private Double interestRate;
    private int interestPeriod;

}
