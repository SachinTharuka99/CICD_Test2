package com.epic.cms.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@Getter
@Setter
public class CommonVarList {

    @Value("${my.app.title}")
    public String title;

//    @Value("${my.app.title}")
//    public void setTitle(String title_NON) {
//        CommonVarList.title = title_NON;
//    }

    @Value("${my.app.email}")
    public String email;

//    @Value("${swap.Eod.Card.Balance}")
//    public String backendUrl;
}

