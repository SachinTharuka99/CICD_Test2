package com.epic.cms.model.entity;

import lombok.Data;
import org.springframework.data.relational.core.sql.In;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "EODERRORMERCHANT")
public class EODERRORMERCHANT implements Serializable {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "MID")
    private String MID;

    @Column(name = "ERRORPROCESSID")
    private Integer ERRORPROCESSID;

    @Column(name = "ERRORPROCESSNAME")
    private String ERRORPROCESSNAME;

    @Column(name = "PROCESSSTEPID")
    private Integer PROCESSSTEPID;

    @Column(name = "EODDATE")
    private Date EODDATE;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "ERRORREMARK")
    private String ERRORREMARK;

    @Column(name = "MERCHANTCUSTOMERNO")
    private String MERCHANTCUSTOMERNO;

}
