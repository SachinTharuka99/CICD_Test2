/**
 * Author :
 * Date : 12/8/2022
 * Time : 9:47 AM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.controller;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;

@RestController
@RequestMapping("eod-file-processing-engine")
public class TestController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    CommonRepo commonRepo;

    @GetMapping("/test/{topic}/{processId}/{eodId}")
    public String post(@PathVariable("topic") final String topic, @PathVariable("processId") final String fileId, @PathVariable("eodId") final int eodId) throws Exception {
        CreateEodId createDate = new CreateEodId();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        Configurations.EOD_ID = eodId;
        Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
        Configurations.EOD_DATE = createDate.getDateFromEODID(Configurations.EOD_ID);
        Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);
        Configurations.STARTING_EOD_STATUS = "INIT"; //EROR
        Configurations.PROCESS_STEP_ID = 6;
        Configurations.COMMIT_STATUS = true;

        //config loggers
        LogManager.init();

//        ProcessBean processBean = commonRepo.getProcessDetails(Integer.parseInt(processId));
//        kafkaTemplate.send(processBean.getKafkaTopic(), processId);
//        return "Published successfully " + processBean.getProcessDes() + (":") + processId
//                + " to the topic:" + processBean.getKafkaTopic();

        String topicName = "ATMFileClearing";
        String processDes = "ATM File Clearing Process";
        kafkaTemplate.send(topic, fileId);
        return "Published successfully " + processDes + "" + (":") + fileId
                + " to the topic:" + topic;
    }

}
