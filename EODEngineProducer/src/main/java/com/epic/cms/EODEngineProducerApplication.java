/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 5:01 PM
 * Project Name : eod-engine
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
@EnableDiscoveryClient
public class EODEngineProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EODEngineProducerApplication.class, args);
    }

}
