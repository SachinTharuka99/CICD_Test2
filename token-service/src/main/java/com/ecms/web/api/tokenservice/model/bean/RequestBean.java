package com.ecms.web.api.tokenservice.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBean {
    private String client_ip;
    private String token;
    private String userrole;
    private String username;
    private Object requestBody;

}
