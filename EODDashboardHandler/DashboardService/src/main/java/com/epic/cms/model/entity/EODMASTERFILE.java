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
@Table(name = "EODMASTERFILE")
public class EODMASTERFILE implements Serializable {

    @Id
    @Column(name = "FILEID", nullable = false)
    private String FILEID;

    @Column(name = "EODID")
    private Long EODID;

    @Column(name = "FILENAME")
    private String FILENAME;

    @Column(name = "STARTTIME")
    private Date STARTTIME;

    @Column(name = "ENDTIME")
    private Date ENDTIME;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "LASTUPDATEDDATE")
    private Date LASTUPDATEDDATE;

    @Column(name = "CREATETIME")
    private Date CREATETIME;

    @Column(name = "UPLOADTIME")
    private Date UPLOADTIME;

    @Column(name = "NOOFRECORDS")
    private String NOOFRECORDS;

    @Column(name = "NOOFTRANSACTION")
    private String NOOFTRANSACTION;

    @Column(name = "TRANSACTIONTYPE")
    private String TRANSACTIONTYPE;

    @Column(name = "CHECKSUM")
    private String CHECKSUM;

}
