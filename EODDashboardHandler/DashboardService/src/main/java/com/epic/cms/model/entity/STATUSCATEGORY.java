package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "STATUSCATEGORY")
public class STATUSCATEGORY implements Serializable {

    @Id
    @Column(name = "STATUSCATEGORYCODE")
    private String STATUSCATEGORYCODE;

    @Column(name = "DESCRIPTION")
    private String DESCRIPTION;

    @Column(name = "SORTKEY")
    private String SORTKEY;

    @Column(name = "LASTUPDATEDUSER")
    private String LASTUPDATEDUSER;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "CREATETIME")
    private Date CREATETIME;

}
