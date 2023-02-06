/**
 * Author : lahiru_p
 * Date : 1/20/2023
 * Time : 1:23 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODAcquiringConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EODAcquiringConsumerApplication.class, args);
    }
}
