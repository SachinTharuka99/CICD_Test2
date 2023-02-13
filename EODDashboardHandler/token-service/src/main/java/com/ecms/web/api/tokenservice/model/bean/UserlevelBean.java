package com.ecms.web.api.tokenservice.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class UserlevelBean {
    private Long userlevelcode;
    private String description;
    private String status;
    private Date createdtime;
    private Date lastupdatedtime;
    private String lastupdateduser;

}
