package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SYSTEMAUDIT")
@Getter
@Setter
public class Systemaudit {
    @Id
    @SequenceGenerator(name = "SequenceIdGenerator", sequenceName = "SYSTEMAUDIT_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SequenceIdGenerator")
    @Column(name = "SYSTEMAUDITID", unique = true, nullable = false, precision = 22, scale = 0)
    private Long systemauditid;

    @Column(name = "IP")
    private String ip;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "USERROLE")
    private String userrole;

    @Column(name = "PAGE")
    private String page;

    @Column(name = "TASK")
    private String task;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "AFFECTEDKEY")
    private String affectedkey;

    @Column(name = "REQUESTID")
    private String requestid;

    @Column(name = "FIELDS")
    private String fields;

    @Column(name = "OLDVALUE")
    private String oldvalue;

    @Column(name = "NEWVALUE")
    private String newvalue;

    @Column(name = "CREATEDTIME")
    private Date createdtime;


}
