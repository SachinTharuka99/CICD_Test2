package com.ecms.web.api.tokenservice.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "PASSWORDPOLICY")
@Getter
@Setter
public class Passwordpolicy {
    @Id
    @Column(name = "PASSWORDPOLICYCODE")
    private String passwordpolicycode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MINIMUMLENGTH")
    private int minimumlength;

    @Column(name = "MAXIMUMLENGTH")
    private int maximumlength;

    @Column(name = "MINIMUMSPECIALCHARACTERS")
    private int minimumspecialcharacters;

    @Column(name = "MINIMUMLOWERCASECHARACTERS")
    private int minimumlowercasecharacters;

    @Column(name = "MINIMUMUPPERCASECHARACTERS")
    private int minimumuppercasecharacters;

    @Column(name = "MINIMUMNUMERICALCHARACTERS")
    private int minimumnumericalcharacters;

    @Column(name = "REPEATCHARACTERSALLOW")
    private int repeatcharactersallow;

    @Column(name = "NOOFINVALIDLOGINATTEMPT")
    private int noofinvalidloginattempt;

    @Column(name = "PASSWORDEXPIRYPERIOD")
    private int passwordexpiryperiod;

    @Column(name = "NOOFHISTORYPASSWORD")
    private int noofhistorypassword;

    @Column(name = "PASSWORDEXPIRYNOTIFYPERIOD")
    private int passwordexpirynotifyperiod;

    @Column(name = "IDLEACCOUNTEXPIRYPERIOD")
    private int idleaccountexpiryperiod;

    @Column(name = "CREATEDTIME")
    private Date createdtime;

    @Column(name = "LASTUPDATEDTIME")
    private Date lastupdatedtime;

    @Column(name = "LASTUPDATEDUSER")
    private String lastupdateduser;

}
