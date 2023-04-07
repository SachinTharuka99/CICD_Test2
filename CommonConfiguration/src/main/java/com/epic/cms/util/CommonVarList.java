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

    @Value("${my.app.email}")
    public String email;

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    public String consumerUrl;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    public String producerUrl;
}

