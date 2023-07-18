/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:37 PM
 * Project Name : eod-engine
 */

package com.epic.cms.service;

import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODEngineScheduler {

    @Autowired
    EODEngineMainService eodEngineMainService;

    @KafkaListener(topics = "eodEngineProducer", groupId = "group_eodEngineProducer")
    public void eodEngineProducer(String eodID) throws Exception {
        int categoryId = 2;
        System.out.println("Start the EOD Engine Module");
        eodEngineMainService.startEodEngine(eodID);
    }

    @KafkaListener(topics = "eodEngineProcessStatus", groupId = "group_eodEngineProcessStatus")
    public void eodEngineProcessStatusUpdator(String status) throws Exception {
        if (status.equalsIgnoreCase("true")) {
            Configurations.PROCESS_COMPLETE_STATUS = true;
        } else {
            throw new Exception("Invalid input from eodEngineProcessStatus listener.");
        }
    }
}
