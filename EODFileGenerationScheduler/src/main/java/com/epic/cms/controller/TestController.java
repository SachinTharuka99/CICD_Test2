/**
 * Author : lahiru_p
 * Date : 2/9/2023
 * Time : 12:11 PM
 * Project Name : ECMS_EOD_PRODUCT
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
@RequestMapping("eod-fileGen/issuing")
public class TestController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    CommonRepo commonRepo;

    @GetMapping("/test/{eodId}")
    public String post(@PathVariable("eodId") final String eodId) throws Exception {
        CreateEodId createDate = new CreateEodId();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        Configurations.EOD_ID = Integer.parseInt(eodId);
        Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
        Configurations.EOD_DATE = createDate.getDateFromEODID(Configurations.EOD_ID);
        Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);
        Configurations.STARTING_EOD_STATUS = "INIT"; //EROR
        Configurations.COMMIT_STATUS = true;

        kafkaTemplate.send("eodEngineStatus", eodId);
        return "Published successfully " ;
    }
}
