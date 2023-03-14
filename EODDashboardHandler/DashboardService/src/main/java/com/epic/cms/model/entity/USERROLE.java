package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "USERROLE")
public class USERROLE implements Serializable {

    @Id
    @Column(name = "USERROLE", nullable = false)
    private String USERROLE;

    @Column(name = "DESCRIPTION")
    private String DESCRIPTION;

    @ManyToOne
    @JoinColumn(name = "STATUS")
    private STATUS STATUS;

    @Column(name = "FIELD1")
    private String FIELD1;

    @Column(name = "FIELD2")
    private String FIELD2;

    @Column(name = "FIELD3")
    private String FIELD3;

    @Column(name = "FIELD4")
    private String FIELD4;

    @Column(name = "FIELD5")
    private String FIELD5;

    @ManyToOne
    @JoinColumn(name = "LASTUPDATEDUSER")
    private SYSTEMUSER LASTUPDATEDUSER;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "CREATETIME")
    private Date CREATETIME;

    @ManyToOne
    @JoinColumn(name = "LEVELID")
    private USERLEVEL LEVELID;

}
