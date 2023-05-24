/**
 * Author :
 * Date : 4/14/2023
 * Time : 8:32 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
@EnableDiscoveryClient
public class EODFileProcessingProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EODFileProcessingProducerApplication.class, args);
    }
}
