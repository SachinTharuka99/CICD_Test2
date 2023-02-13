package com.ecms.web.api.tokenservice.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class SystemuserBean implements Serializable {
    private String username;
    private String fullname;
    private String password;
    private String userrole;
    private long userlevel;
    private String email;
    private Byte invalidloginattempt;
    private Date expirydate;
    private Date lastloggeddate;
    private String passwordstatus;
    private String status;
    private Date createdtime;
    private Date lastupdatedtime;
    private String lastupdateduser;
    private boolean updateflag;
    private String ip;
    private String message;

}
