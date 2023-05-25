package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "EOD")
public class EOD implements Serializable {

    @Id
    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "STARTTIME")
    private Timestamp STARTTIME;

    @Column(name = "ENDTIME")
    private Timestamp ENDTIME;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "NEXTEODSTARTTIME")
    private Timestamp NEXTEODSTARTTIME;

    @Column(name = "NEXTSUBEODSTARTTIME")
    private Timestamp NEXTSUBEODSTARTTIME;

    @Column(name = "SUBEODSTATUS")
    private String SUBEODSTATUS;

    @Column(name = "NOOFSUCCESSPROCESS")
    private Integer NOOFSUCCESSPROCESS;

    @Column(name = "NOOFERRORPAROCESS")
    private Integer NOOFERRORPAROCESS;

//    @Column(name = "SUBEODPARTCOMP")
//    private String SUBEODPARTCOMP;

    @Column(name = "FILEGENERATIONSTATUS")
    private String FILEGENERATIONSTATUS;

//    @Column(name = "ESTATEMENTFLAG")
//    private String ESTATEMENTFLAG;

}
