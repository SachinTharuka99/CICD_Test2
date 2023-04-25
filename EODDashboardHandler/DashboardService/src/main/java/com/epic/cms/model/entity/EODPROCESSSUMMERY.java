package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "EODPROCESSSUMMERY")
public class EODPROCESSSUMMERY implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "PROCESSID")
    private Integer PROCESSID;

    @Column(name = "STARTTIME")
    private Date STARTTIME;

    @Column(name = "ENDTIME")
    private Date ENDTIME;

    @Column(name = "RUNNNGONPROCESSID")
    private String RUNNNGONPROCESSID;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "SUCCESSCOUNT")
    private Integer SUCCESSCOUNT;

    @Column(name = "FAILEDCOUNT")
    private Integer FAILEDCOUNT;

    @Column(name = "PROCESSPROGRESS")
    private String PROCESSPROGRESS;

    @Column(name = "SUBEODSTS")
    private String SUBEODSTS;

    @Column(name = "EODMODULE")
    private String EODMODULE;

}
