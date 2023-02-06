/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:36 PM
 * Project Name : eod-engine
 */

package com.epic.cms.service;

import com.epic.cms.util.Configurations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EODEngineListener {

    @KafkaListener(topics = "processStatus", groupId = "group_processStatus")
    public void checkProcessCompleteListener(String statusFlag) throws Exception {
        if (statusFlag.equalsIgnoreCase("true")) {
            Configurations.PROCESS_COMPLETE_STATUS = true;
        }
    }

    @KafkaListener(topics = "softStopStatus", groupId = "group_softStopStatus")
    public void checkProcessSoftStopListener(String statusFlag) throws Exception {
        if (statusFlag.equalsIgnoreCase("true")) {
            Configurations.EOD_ENGINE_SOFT_STOP = true;
        }
    }
}
