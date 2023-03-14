/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:40 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class DashboardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashboardServiceApplication.class, args);
    }
}
