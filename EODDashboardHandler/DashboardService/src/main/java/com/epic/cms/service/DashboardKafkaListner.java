/**
 * Author : rasintha_j
 * Date : 6/1/2023
 * Time : 8:49 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DashboardKafkaListner {

    @Autowired
    EODEngineDashboardService dashboardService;

    @KafkaListener(topics = "loadEodInfo", groupId = "group_loadEodInfo")
    public void loadEodInfoListner(String eodId) throws Exception {
        dashboardService.getCurrentDashboardEodId();
    }
}
