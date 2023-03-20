/**
 * Author : lahiru_p
 * Date : 2/9/2023
 * Time : 12:19 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODFileGenSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EODFileGenSchedulerApplication.class, args);
    }
}
