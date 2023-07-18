/**
 * Author : lahiru_p
 * Date : 2/9/2023
 * Time : 11:22 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "API Gateway", version = "1.0", description = "Documentation API Gateway v1.0"))
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(r -> r.path("/DashboardService/v3/api-docs").and().method(HttpMethod.GET).uri("lb://DashboardService"))
                .route(r -> r.path("/eod-dashboard/eod-engine/starteodid").and().method(HttpMethod.POST).uri("lb://DashboardService"))
                .build();
    }
}
