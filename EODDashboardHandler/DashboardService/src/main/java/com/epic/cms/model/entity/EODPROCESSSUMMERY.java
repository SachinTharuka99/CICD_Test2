package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "EODPROCESSSUMMERY")
public class EODPROCESSSUMMERY implements Serializable {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "PROCESSID")
    private Integer PROCESSID;

    @Column(name = "STARTTIME")
    private Timestamp STARTTIME;

    @Column(name = "ENDTIME")
    private Timestamp ENDTIME;

    @Column(name = "RUNNNGONPROCESSID")
    private Integer RUNNNGONPROCESSID;

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

}
