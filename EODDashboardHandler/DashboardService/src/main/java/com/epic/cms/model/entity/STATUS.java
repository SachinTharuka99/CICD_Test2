package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "STATUS")
public class STATUS implements Serializable {

    @Id
    @Column(name = "STATUSCODE", nullable = false)
    private String STATUSCODE;

    @Column(name = "DESCRIPTION")
    private String DESCRIPTION;

    @ManyToOne
    @JoinColumn (name = "STATUSCATEGORY")
    private STATUSCATEGORY STATUSCATEGORY;

    @Column(name = "SORTKEY")
    private String SORTKEY;

    @ManyToOne
    @JoinColumn (name = "LASTUPDATEDUSER")
    private SYSTEMUSER LASTUPDATEDUSER;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "CREATETIME")
    private Date CREATETIME;

    @Column(name = "ONLINECODE")
    private String ONLINECODE;

}
