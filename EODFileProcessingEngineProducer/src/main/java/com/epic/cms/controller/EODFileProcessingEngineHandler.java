/**
 * Author :
 * Date : 4/5/2023
 * Time : 7:27 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.ResponseBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EODFileProcessingEngineProducerRepo;
import com.epic.cms.service.EODFileProcessingEngineMainService;
import com.epic.cms.service.KafkaMessageUpdator;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eod-file-processing-engine")
public class EODFileProcessingEngineHandler {

    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    EODFileProcessingEngineMainService eodFileProcessingEngineMainService;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;

    int processCategory = 3;

    @GetMapping("/start")
    public Map<String, Object> startFileProcessingEngine() throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            Configurations.STARTING_EOD_STATUS = "INIT";
            if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                eodFileProcessingEngineMainService.startProcess();//eod-file-processing-engine
                response.put(Util.STATUS_VALUE, Util.STATUS_SUCCESS);
            } else {
                System.out.println("eod is not in init status");
            }

        } catch (Exception ex) {
            response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);
        }
        return response;
    }

    @GetMapping("/start/{fileType}/{fileId}")
    public ResponseBean startFileProcessingEngine(@PathVariable("fileType") final String fileType, @PathVariable("fileId") final String fileId) throws Exception {
        ResponseBean responseBean = new ResponseBean();
        int eodId = 0;
        try {
            //Configurations.STARTING_EOD_STATUS = "INIT";
            eodId = commonRepo.getRuninngEODId(status.getINITIAL_STATUS());
            if (eodId != 0) {
                kafkaMessageUpdator.producerWithNoReturn(eodId, "eodIdUpdator");
                eodFileProcessingEngineMainService.startProcess(fileType, fileId);

                responseBean.setContent(fileType+ ": " +fileId);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg("eod is not in init status");
            }
        } catch (Exception ex) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
        }
        return responseBean;
    }
}
