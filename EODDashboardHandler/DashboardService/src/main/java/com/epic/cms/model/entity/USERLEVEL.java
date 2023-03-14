package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "USERLEVEL")
public class USERLEVEL implements Serializable {

    @Id
    @Column(name = "LEVELID")
    private String LEVELID;

    @Column(name = "DESCRIPTION")
    private String DESCRIPTION;

    @ManyToOne
    @JoinColumn(name = "STATUS")
    private STATUS STATUS;

    @Column(name = "LASTUPDATEDTIME")
    private Date LASTUPDATEDTIME;

    @Column(name = "CREATETIME")
    private Date CREATETIME;

    @ManyToOne
    @JoinColumn(name = "LASTUPDATEDUSER")
    private SYSTEMUSER LASTUPDATEDUSER;

}
