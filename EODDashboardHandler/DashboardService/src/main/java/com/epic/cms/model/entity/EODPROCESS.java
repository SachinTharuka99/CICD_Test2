package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "EODPROCESS")
public class EODPROCESS implements Serializable {

    @Id
    @Column(name = "PROCESSID")
    private Integer PROCESSID;

    @Column(name = "DESCRIPTION")
    private String DESCRIPTION;

    @Column(name = "CRITICALSTATUS")
    private Integer CRITICALSTATUS;

    @Column(name = "ROLLBACKSTATUS")
    private Integer ROLLBACKSTATUS;

    @Column(name = "SHEDULEDATE")
    private Date SHEDULEDATE;

    @Column(name = "SHEDULETIME")
    private Integer SHEDULETIME;

    @Column(name = "FREQUENCYTYPE")
    private Integer FREQUENCYTYPE;

    @Column(name = "CONTINUESFREQUENCYTYPE")
    private Integer CONTINUESFREQUENCYTYPE;

    @Column(name = "CONTINUESFREQUENCY")
    private Integer CONTINUESFREQUENCY;

    @Column(name = "MULTIPLECYCLESTATUS")
    private Integer MULTIPLECYCLESTATUS;

    @Column(name = "PROCESSCATEGORYID")
    private Integer PROCESSCATEGORYID;

    @Column(name = "DEPENDANCYSTATUS")
    private Integer DEPENDANCYSTATUS;

    @Column(name = "RUNNINGONMAIN")
    private Integer RUNNINGONMAIN;

    @Column(name = "RUNNINGONSUB")
    private Integer RUNNINGONSUB;

    @Column(name = "PROCESSTYPE")
    private Integer PROCESSTYPE;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "SHEDULEDATETIME")
    private Timestamp SHEDULEDATETIME;

    @Column(name = "HOLIDAYACTION")
    private Integer HOLIDAYACTION;

    @Column(name = "LETTERGENARATIONSTATUS")
    private String LETTERGENARATIONSTATUS;

    @Column(name = "ISSUINGORACQUIRING")
    private Integer ISSUINGORACQUIRING;

    @Column(name = "EODMODULE")
    private String EODMODULE;

    @Column(name = "KAFKAGROUPID")
    private String KAFKAGROUPID;

    @Column(name = "KAFKATOPICNAME")
    private String KAFKATOPICNAME;

    @Column(name = "MAKER")
    private String MAKER;

    @Column(name = "CHECKER")
    private String CHECKER;

}
