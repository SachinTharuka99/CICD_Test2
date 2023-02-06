package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualNpRequestBean {
    private String accNumber;
    private String accStatus;
    private StringBuffer cardNumber;
    private int requestId;
    private int ndia;
}
