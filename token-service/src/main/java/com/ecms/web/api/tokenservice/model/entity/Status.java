package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "STATUS")
@Getter
@Setter
public class Status implements Serializable {
    @Id
    @Column(name = "STATUSCODE")
    private String statuscode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STATUSCATEGORY")
    private String statuscategory;

    @Column(name = "ONLINECODE")
    private Long onlinecode;

    @Column(name = "CREATEDTIME")
    private Date createdtime;

    @Column(name = "LASTUPDATEDTIME")
    private Date lastupdatedtime;

    @Column(name = "LASTUPDATEDUSER")
    private String lastupdateduser;

}
