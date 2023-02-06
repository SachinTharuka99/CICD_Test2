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
public class CommonBackendDbVarList {
    //Backend Db
    @Value("${spring.datasource.backend.url}")
    public String backendUrl;

    @Value("${spring.datasource.backend.username}")
    public String backendUsername;

    @Value("${spring.datasource.backend.password}")
    public String backendPassword;

    @Value("${spring.datasource.backend.driver-class-name}")
    public String backendDriverClassName;

    //Backend Db Pool
    @Value("${spring.datasource.backend.hikari.connection-timeout}")
    public String backendConnectionTimeout;

    @Value("${spring.datasource.backend.hikari.minimum-idle}")
    public String backendMinimumIdle;

    @Value("${spring.datasource.backend.hikari.maximum-pool-size}")
    public String backendMaximumPoolSize;

    @Value("${spring.datasource.backend.hikari.idle-timeout}")
    public String backendIdleTimeout;

    @Value("${spring.datasource.backend.hikari.max-lifetime}")
    public String backendMaxLifeTime;
}
