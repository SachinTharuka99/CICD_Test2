package com.ecms.web.api.tokenservice.model.bean;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserroleBean {
    private String userrolecode;
    private String description;
    private String userroletype;
    private String userroletypedesc;
    private Long userlevel;
    private String userleveldesc;
    private String status;
    private String statusdesc;
    private Date createdtime;
    private Date lastupdatedtime;
    private String lastupdateduser;

}
