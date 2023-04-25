package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "RECATMFILEINVALID")
public class RECATMFILEINVALID implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "FILEID", nullable = false)
    private String FILEID;

    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "LINENUMBER")
    private Integer LINENUMBER;

    @Column(name = "ERRORDESC")
    private String ERRORDESC;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;
    @Column(name = "LASTUPDATEDDATE")
    private Date LASTUPDATEDDATE;

}
