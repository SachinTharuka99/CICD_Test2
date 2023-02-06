/**
 * Author : lahiru_p
 * Date : 1/23/2023
 * Time : 11:46 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODFileGenerationAcquiringConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EODFileGenerationAcquiringConsumerApplication.class, args);
    }
}
