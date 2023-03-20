/**
 * Author : lahiru_p
 * Date : 1/23/2023
 * Time : 3:25 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODFileGenEngineListener {

    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;

    @Autowired
    FileGenMainService fileGenMainService;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    CommonRepo commonRepo;

    @KafkaListener(topics = "eodEngineStatus", groupId = "group_eodEngineStatus")
    public void eodFileGenEngineListener(String eodId) throws Exception {
        if (eodId != null) {
            System.out.println("Start the EOD File Generation Module");
            int categoryId = 20;
            Configurations.IssuingOrAcquiring =1;
            int issuingOrAcquiring = Configurations.IssuingOrAcquiring;
            fileGenMainService.startEODFileGenEngine(categoryId, issuingOrAcquiring, Integer.parseInt(eodId));
        } else {
            throw new Exception("Invalid input from EoD Engine Status listener.");
        }

    }

    @KafkaListener(topics = "eodFileGenProcessStatus", groupId = "group_eodFileGenProcessStatus")
    public void eodFileGenEngineProcessStatusUpdator(String status) throws Exception {
        if (status.equalsIgnoreCase("true")) {
            Configurations.PROCESS_COMPLETE_STATUS = true;
        } else {
            throw new Exception("Invalid input from EoD File Generation Process Status listener.");
        }
    }

/*    @KafkaListener(topics = "eodFileGenStatus", groupId = "group_eodFileGenStatus")
    public void eodFileGenStatusUpdator(String status) throws Exception {
        if (status.equalsIgnoreCase(Configurations.FILE_GENERATION_COMPLETE)) {
            Configurations.FILE_GENERATION_COMPLETE_STATUS = true;
        } else if (status.equalsIgnoreCase(Configurations.FILE_GENERATION_COMPLETE_WITH_ERRORS)) {
            Configurations.FILE_GENERATION_COMPLETE_WITH_ERRORS_STATUS = true;
        } else {
            throw new Exception("Invalid input from EoD File Generation Status.");
        }
    }*/

    @KafkaListener(topics = "eodManualMode", groupId = "group_eodManualMode")
    public void runEodManualMode(String processId) throws Exception {
        try {
            ProcessBean processBean = commonRepo.getProcessDetails(Integer.parseInt(processId));
            kafkaTemplate.send(processBean.getKafkaTopic(), processId);
        } catch (Exception e) {
            throw new Exception("Invalid Process  from EoD Dashboard Handler.");
        }
    }
}
