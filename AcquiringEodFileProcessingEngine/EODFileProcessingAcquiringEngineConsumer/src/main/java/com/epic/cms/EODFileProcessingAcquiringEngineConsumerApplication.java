package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODFileProcessingAcquiringEngineConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EODFileProcessingAcquiringEngineConsumerApplication.class, args);
    }
}
