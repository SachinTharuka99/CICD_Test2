package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SYSTEMUSER")
@Getter
@Setter
public class Systemuser implements Serializable {
    @Id
    @Column(name = "USERNAME")
    private String username;

    @Column(name = "FULLNAME")
    private String fullname;

    @Column(name = "PASSWORD")
    private String password;

    @ManyToOne
    @JoinColumn(name = "USERROLE")
    private Userrole userrole;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "INVALIDLOGINATTEMPT")
    private Byte invalidloginattempt;

    @Column(name = "EXPIRYDATE")
    private Date expirydate;

    @Column(name = "LASTLOGGEDDATE")
    private Date lastloggeddate;

    @ManyToOne
    @JoinColumn(name = "PASSWORDSTATUS")
    private Status passwordstatus;

    @ManyToOne
    @JoinColumn(name = "STATUS")
    private Status status;

    @Column(name = "CREATEDTIME")
    private Date createdtime;

    @Column(name = "LASTUPDATEDTIME")
    private Date lastupdatedtime;

    @Column(name = "LASTUPDATEDUSER")
    private String lastupdateduser;

    @ManyToOne
    @JoinColumn(name = "MAKER")
    private Systemuser maker;

    @ManyToOne
    @JoinColumn(name = "CHECKER")
    private Systemuser checker;

}
