/**
 * Author : lahiru_p
 * Date : 3/13/2023
 * Time : 1:02 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardKafkaConsumer {
    @Autowired
    SimpMessagingTemplate template;

    @KafkaListener(topics = "logTopic", groupId = "msg-group-id", containerFactory = "kafkaListenerContainerFactory")
    public void listenSenderEmail(String data) {
        template.convertAndSend("/topic/message", data);
    }
}
