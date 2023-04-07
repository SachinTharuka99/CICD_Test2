package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "STATUSCATEGORY")
@Getter
@Setter
public class Statuscategory implements Serializable {
    @Id
    @Column(name = "STATUSCATEGORYCODE")
    private String statuscategorycode;

    @Column(name = "DESCRIPTION")
    private String description;

}
