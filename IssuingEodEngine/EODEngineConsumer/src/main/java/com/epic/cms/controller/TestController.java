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
@RequestMapping("eod-engine/issuing")
public class TestController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    CommonRepo commonRepo;

    @GetMapping("/test/{processId}")
    public String post(@PathVariable("processId") final String processId) throws Exception {
        CreateEodId createDate = new CreateEodId();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        //Configurations.EOD_ID = 22101100;
        Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
        Configurations.EOD_DATE = createDate.getDateFromEODID(Configurations.EOD_ID);
        Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);
        Configurations.STARTING_EOD_STATUS = "INIT"; //EROR
        Configurations.PROCESS_STEP_ID = 6;
        Configurations.COMMIT_STATUS = true;

        ProcessBean processBean = commonRepo.getProcessDetails(Integer.parseInt(processId));
        kafkaTemplate.send(processBean.getKafkaTopic(), processId);
        return "Published successfully " + processBean.getProcessDes() + (":") + processId
                + " to the topic:" + processBean.getKafkaTopic();
    }
}
