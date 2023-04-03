package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

@Data
@Entity
@Table(name = "EODERRORCARDS")
public class EODERRORCARDS {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "CARDNO")
    private String CARDNO;

    @Column(name = "ACCOUNTNO")
    private String ACCOUNTNO;

    @Column(name = "CUSTOMERID")
    private String CUSTOMERID;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "ERRORPROCESSID")
    private String ERRORPROCESSID;

    @Column(name = "ERRORPROCESSNAME")
    private String ERRORPROCESSNAME;

    @Column(name = "ERRORREMARK")
    private String ERRORREMARK;

    @Column(name = "EODDATE")
    private Date EODDATE;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "ISPROCESSFAIL")
    private String ISPROCESSFAIL;

    @Column(name = "PROCESSSTEPID")
    private String PROCESSSTEPID;

}
