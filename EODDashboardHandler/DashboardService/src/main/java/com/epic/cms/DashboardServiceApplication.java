/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:40 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@OpenAPIDefinition(info = @Info(title = "EOD Dashboard API", version = "1.0", description = "EOD Product Dashboard API Information"))
@ComponentScan(basePackages = {"com.epic.cms.*"})
@EnableDiscoveryClient
@EntityScan(basePackages = "com.epic.cms.model.*")
public class DashboardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashboardServiceApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
