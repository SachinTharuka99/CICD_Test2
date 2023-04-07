/**
 * Author : lahiru_p
 * Date : 3/19/2023
 * Time : 12:16 AM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate template(){
        return new RestTemplate();
    }
}
