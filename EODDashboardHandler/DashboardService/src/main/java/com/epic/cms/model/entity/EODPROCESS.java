package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
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
    private String CRITICALSTATUS;

    @Column(name = "ROLLBACKSTATUS")
    private String ROLLBACKSTATUS;

    @Column(name = "SHEDULEDATE")
    private Date SHEDULEDATE;

    @Column(name = "SHEDULETIME")
    private String SHEDULETIME;

    @Column(name = "FREQUENCYTYPE")
    private String FREQUENCYTYPE;

    @Column(name = "CONTINUESFREQUENCYTYPE")
    private String CONTINUESFREQUENCYTYPE;

    @Column(name = "CONTINUESFREQUENCY")
    private String CONTINUESFREQUENCY;

    @Column(name = "MULTIPLECYCLESTATUS")
    private String MULTIPLECYCLESTATUS;

    @Column(name = "PROCESSCATEGORYID")
    private String PROCESSCATEGORYID;

    @Column(name = "DEPENDANCYSTATUS")
    private String DEPENDANCYSTATUS;

    @Column(name = "RUNNINGONMAIN")
    private String RUNNINGONMAIN;

    @Column(name = "RUNNINGONSUB")
    private String RUNNINGONSUB;

    @Column(name = "PROCESSTYPE")
    private String PROCESSTYPE;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "SHEDULEDATETIME")
    private Date SHEDULEDATETIME;

    @Column(name = "HOLIDAYACTION")
    private String HOLIDAYACTION;

    @Column(name = "LETTERGENARATIONSTATUS")
    private String LETTERGENARATIONSTATUS;

    @Column(name = "KAFKATOPICNAME")
    private String KAFKATOPICNAME;

    @Column(name = "KAFKAGROUPID")
    private String KAFKAGROUPID;

    @Column(name = "ISSUINGORACQUIRING")
    private String ISSUINGORACQUIRING;

}
