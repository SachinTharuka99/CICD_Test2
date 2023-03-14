package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "SYSTEMUSER")
public class SYSTEMUSER implements Serializable {

    @Id
    @Column(name = "USERNAME")
    private String USERNAME;

    @Column(name = "PASSWORD")
    private String PASSWORD;

    @ManyToOne
    @JoinColumn(name = "USERROLECODE")
    private USERROLE USERROLECODE;

    @ManyToOne
    @JoinColumn (name = "STATUS")
    private STATUS STATUS;

    @Column(name = "EXPIRYDATE")
    private Date EXPIRYDATE;

    @ManyToOne
    @JoinColumn(name = "DUALAUTHUSERROLE")
    private USERROLE DUALAUTHUSERROLE;

    @Column(name = "DUALAUTHUSERNAME")
    private String DUALAUTHUSERNAME;

    @Column(name = "LOGINATTEMPT")
    private String LOGINATTEMPT;

    @Column(name = "SESSIONID")
    private String SESSIONID;

    @Column(name = "TITLE")
    private String TITLE;

    @Column(name = "INITIALS")
    private String INITIALS;

    @Column(name = "FIRSTNAME")
    private String FIRSTNAME;

    @Column(name = "MIDDLENAME")
    private String MIDDLENAME;

    @Column(name = "LASTNAME")
    private String LASTNAME;

    @Column(name = "BANKNAME")
    private String BANKNAME;

    @Column(name = "BRANCHNAME")
    private String BRANCHNAME;

    @Column(name = "DESIGNATION")
    private String DESIGNATION;

    @Column(name = "SERVICEID")
    private String SERVICEID;

    @Column(name = "ADDRESS1")
    private String ADDRESS1;

    @Column(name = "ADDRESS2")
    private String ADDRESS2;

    @Column(name = "ADDRESS3")
    private String ADDRESS3;

    @Column(name = "CITY")
    private String CITY;

    @Column(name = "CONTACTNUMBER")
    private String CONTACTNUMBER;

    @Column(name = "NIC")
    private String NIC;

    @Column(name = "EMAILADDRESS")
    private String EMAILADDRESS;

    @Column(name = "GENDER")
    private String GENDER;

    @Column(name = "DATEOFBIRTH")
    private Date DATEOFBIRTH;

    @Column(name = "REPORTEDOFFICER")
    private String REPORTEDOFFICER;

    @ManyToOne
    @JoinColumn(name = "LASTUPDATEDUSER")
    private SYSTEMUSER LASTUPDATEDUSER;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTLOGGEDDATETIME")
    private Date LASTLOGGEDDATETIME;

    @Column(name = "ISEMAILSENT")
    private String ISEMAILSENT;

    @Column(name = "INITIALLOGINSTATUS")
    private String INITIALLOGINSTATUS;

    @Column(name = "ISLOCKEDFORAUTH")
    private String ISLOCKEDFORAUTH;

    @Column(name = "PASSWORDRESETTIME")
    private Date PASSWORDRESETTIME;

}
