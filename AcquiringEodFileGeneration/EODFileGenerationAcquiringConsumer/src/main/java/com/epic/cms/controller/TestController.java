/**
 * Author : yasiru_l
 * Date : 06/27/2023
 * Time : 6:22 PM
 * Project Name : ecms_Acquiring_eod_file_generation_engine
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;

@RestController
@RequestMapping("eod-engine-acquiring")
public class TestController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    CommonRepo commonRepo;

    @GetMapping("/test/{processId}")
    public String post(@PathVariable("processId") final String processId) throws Exception {
        CreateEodId createDate = new CreateEodId();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        Configurations.EOD_ID = 23102900;//22062900;
        Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
        Configurations.EOD_DATE = createDate.getDateFromEODID(Configurations.EOD_ID);
        Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);
        Configurations.STARTING_EOD_STATUS = "INIT"; //INIT
        Configurations.PROCESS_STEP_ID = 6;
        Configurations.COMMIT_STATUS = true;

        ProcessBean processBean = commonRepo.getProcessDetails(Integer.parseInt(processId));
        kafkaTemplate.send(processBean.getKafkaTopic(), processId);
        return "Published successfully " + processBean.getProcessDes() + (":") + processId
                + " to the topic:" + processBean.getKafkaTopic();
    }
}
