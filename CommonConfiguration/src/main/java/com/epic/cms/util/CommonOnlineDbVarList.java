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
public class CommonOnlineDbVarList {

    //Online Db
    @Value("${spring.datasource.online.url}")
    public String onlineUrl;

    @Value("${spring.datasource.online.username}")
    public String onlineUsername;

    @Value("${spring.datasource.online.password}")
    public String onlinePassword;

    @Value("${spring.datasource.online.driver-class-name}")
    public String onlineDriverClassName;

    //Online Db Pool
    @Value("${spring.datasource.online.hikari.connection-timeout}")
    public String onlineConnectionTimeout;

    @Value("${spring.datasource.online.hikari.minimum-idle}")
    public String onlineMinimumIdle;

    @Value("${spring.datasource.online.hikari.maximum-pool-size}")
    public String onlineMaximumPoolSize;

    @Value("${spring.datasource.online.hikari.idle-timeout}")
    public String onlineIdleTimeout;

    @Value("${spring.datasource.online.hikari.max-lifetime}")
    public String onlineMaxLifeTime;
}
