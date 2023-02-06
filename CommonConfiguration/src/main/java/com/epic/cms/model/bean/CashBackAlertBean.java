package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CashBackAlertBean {
    private int reqId;
    private String accNo;
    private StringBuffer mainCardNo;
    private double cashBackAmount;
    private Date statmentEndDate;
    private boolean isCBNull;
    private String statementId;
    private boolean isMinPayAvl;
}
