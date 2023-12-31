package com.epic.cms.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "EODPROCESSFLOW")
public class EODPROCESSFLOW implements Serializable {

    @Id
    @Column(name = "STEPID")
    private Integer STEPID;

    @Column(name = "PROCESSID")
    private Integer PROCESSID;

    @Column(name = "PROCESSCATEGORYID")
    private Integer PROCESSCATEGORYID;

    @Column(name = "STATUS")
    private String STATUS;

    @Column(name = "MAKER")
    private String MAKER;

    @Column(name = "CHECKER")
    private String CHECKER;


}
