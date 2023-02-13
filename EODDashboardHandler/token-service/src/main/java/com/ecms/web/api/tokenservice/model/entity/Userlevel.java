package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "USERLEVEL")
@Getter
@Setter
public class Userlevel implements Serializable {
    @Id
    @Column(name = "USERLEVELCODE")
    private Long userlevelcode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "STATUS")
    private String status;

}
