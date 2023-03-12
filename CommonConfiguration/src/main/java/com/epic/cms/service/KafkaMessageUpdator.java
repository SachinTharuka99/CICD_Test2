/**
 * Author : shehan_m
 * Date : 1/23/2023
 * Time : 2:05 PM
 * Project Name : eod-engine
 */

package com.epic.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class KafkaMessageUpdator {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public Future<Boolean> producerWithReturn(Object obj, String topicName) throws InterruptedException {
        boolean isSuccess = false;
        if (true) {
            kafkaTemplate.send(topicName, String.valueOf(obj));
            Thread.sleep(2000);
            isSuccess = true;
        }
        return new AsyncResult<>(Boolean.valueOf(isSuccess));
    }

    public void producerWithNoReturn(Object obj, String topicName) {
        kafkaTemplate.send(topicName, String.valueOf(obj));
    }
}
