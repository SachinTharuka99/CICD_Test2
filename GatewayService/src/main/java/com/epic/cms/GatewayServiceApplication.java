/**
 * Author : lahiru_p
 * Date : 2/9/2023
 * Time : 11:22 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
