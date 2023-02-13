package com.ecms.web.api.tokenservice.model.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Getter
@Setter
public class ResponseBean {

    String responseCode;
    String responseMsg;
    Object content;

}
