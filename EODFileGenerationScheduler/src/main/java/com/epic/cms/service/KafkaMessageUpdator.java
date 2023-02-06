/**
 * Author : lahiru_p
 * Date : 1/26/2023
 * Time : 9:03 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class KafkaMessageUpdator {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Async
    public void updateDashboardProcessStatus(String topicName, Object obj) throws Exception{
        kafkaTemplate.send(topicName, String.valueOf(obj));
    }
}
