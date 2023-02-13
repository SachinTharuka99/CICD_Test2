package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "USERROLETYPE")
@Getter
@Setter
public class Userroletype implements Serializable {
    @Id
    @Column(name = "USERROLETYPECODE")
    private String userroletypecode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STATUS")
    private String status;

}