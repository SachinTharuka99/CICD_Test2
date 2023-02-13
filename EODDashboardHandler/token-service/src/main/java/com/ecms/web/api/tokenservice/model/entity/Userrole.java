package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "USERROLE")
@Getter
@Setter
public class Userrole implements Serializable {
    @Id
    @Column(name = "USERROLECODE")
    private String userrolecode;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne
    @JoinColumn(name = "USERROLETYPE")
    private Userroletype userroletype;

    @ManyToOne
    @JoinColumn(name = "USERLEVEL")
    private Userlevel userlevel;

    @ManyToOne
    @JoinColumn(name = "STATUS")
    private Status status;

}
