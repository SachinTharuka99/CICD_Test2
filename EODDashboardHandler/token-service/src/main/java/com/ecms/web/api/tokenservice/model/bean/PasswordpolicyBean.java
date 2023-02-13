package com.ecms.web.api.tokenservice.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordpolicyBean {
    private String passwordpolicycode;
    private String description;
    private int minimumlength;
    private int maximumlength;
    private int minimumspecialcharacters;
    private int minimumlowercasecharacters;
    private int minimumuppercasecharacters;
    private int minimumnumericalcharacters;
    private int repeatcharactersallow;
    private int noofinvalidloginattempt;
    private int passwordexpiryperiod;
    private int noofhistorypassword;
    private int passwordexpirynotifyperiod;
    private int idleaccountexpiryperiod;

}
