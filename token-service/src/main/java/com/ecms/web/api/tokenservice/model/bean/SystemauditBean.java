package com.ecms.web.api.tokenservice.model.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class SystemauditBean {
    private Long systemauditid;
    private String ip;
    private String username;
    private String userrole;
    private String page;
    private String task;
    private String description;
    private String remarks;
    private String affectedkey;
    private String requestid;
    private String fields;
    private String oldvalue;
    private String newvalue;
    private Date createdtime;
    private boolean auditable;

}
