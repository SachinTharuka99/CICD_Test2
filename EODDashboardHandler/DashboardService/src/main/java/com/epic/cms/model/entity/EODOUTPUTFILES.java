package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "EODOUTPUTFILES")
public class EODOUTPUTFILES implements Serializable {

    @Column(name = "FILETYPE", nullable = false)
    private String FILETYPE;

    @Id
    @Column(name = "FILENAME", nullable = false)
    private String FILENAME;

    @Column(name = "EODID", unique = true)
    private Long EODID;

    @Column(name = "NOOFRECORDS")
    private Integer NOOFRECORDS;

    @Column(name = "CREATEDTIME", nullable = false)
    private Date CREATEDTIME;

    @Column(name = "SUBFOLDER")
    private String SUBFOLDER;

}
