package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AlertBean {
    private int alertType;
    private String message;
    private String mobileNo;
    private String email;
    private StringBuffer cardNumber;
    private String maskedCardNumber;
    private int status;
    private Date createdTime;
    private Date lastUpdatedTime;
}
