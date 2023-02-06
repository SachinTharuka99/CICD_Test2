package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODEngineConsumerApplication {


    public static void main(String[] args) {
        SpringApplication.run(EODEngineConsumerApplication.class, args);
    }
}
