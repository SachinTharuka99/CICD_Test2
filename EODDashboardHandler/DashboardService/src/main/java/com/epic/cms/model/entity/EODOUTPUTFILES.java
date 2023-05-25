package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "EODOUTPUTFILES")
public class EODOUTPUTFILES implements Serializable {

    @EmbeddedId
    private EODOUTPUTFILESPK eodoutputfilesPk;

    @Column(name = "FILETYPE")
    private String FILETYPE;

    @Column(name = "NOOFRECORDS")
    private Integer NOOFRECORDS;

    @Column(name = "CREATEDTIME")
    private Date CREATEDTIME;

    @Column(name = "SUBFOLDER")
    private String SUBFOLDER;

}
