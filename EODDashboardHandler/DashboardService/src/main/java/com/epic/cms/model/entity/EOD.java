package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "EOD")
public class EOD implements Serializable {

    @Id
    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "STARTTIME")
    private Date STARTTIME;

    @Column(name = "ENDTIME")
    private Date ENDTIME;

    @ManyToOne
    @JoinColumn(name = "STATUS")
    private STATUS STATUS;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "NEXTEODSTARTTIME")
    private Date NEXTEODSTARTTIME;

    @Column(name = "NEXTSUBEODSTARTTIME")
    private Date NEXTSUBEODSTARTTIME;

    @Column(name = "SUBEODSTATUS")
    private String SUBEODSTATUS;

    @Column(name = "NOOFSUCCESSPROCESS")
    private Integer NOOFSUCCESSPROCESS;

    @Column(name = "NOOFERRORPAROCESS")
    private Integer NOOFERRORPAROCESS;

    @Column(name = "SUBEODPARTCOMP")
    private String SUBEODPARTCOMP;

    @Column(name = "FILEGENERATIONSTATUS")
    private String FILEGENERATIONSTATUS;

    @Column(name = "ESTATEMENTFLAG")
    private String ESTATEMENTFLAG;

}
