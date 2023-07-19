/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:38 PM
 * Project Name : eod-engine
 */

package com.epic.cms.controller;

import com.epic.cms.repository.EODEngineProducerRepo;
import com.epic.cms.service.EODEngineMainService;
import com.epic.cms.service.KafkaMessageUpdator;
import com.epic.cms.util.*;
import com.epic.cms.util.exception.FileProcessingNotCompletedException;
import com.epic.cms.util.exception.InvalidEODEngineStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eod-engine")
public class EODEngineHandler {

    @Autowired
    @Qualifier("EODEngineProducerRepo")
    EODEngineProducerRepo producerRepo;

    @Autowired
    EODEngineMainService eodEngineMainService;

    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;

    @Autowired
    StatusVarList status;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @GetMapping("/start")
    public synchronized Map<String, Object> startEODEngine() throws Exception {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> nextRunningEodInfo;
        String eodId = null;
        String eodStatus = null;
        boolean fileProcessingStatus = true;
        try {
            logManager.logDashboardInfo("EOD Engine going to be started...");
            nextRunningEodInfo = producerRepo.getNextRunningEodInfo();
            if (!nextRunningEodInfo.isEmpty()) {
                for (Map.Entry<String, String> entry : nextRunningEodInfo.entrySet()) {
                    eodId = entry.getKey();
                    eodStatus = entry.getValue();
                    Configurations.STARTING_EOD_STATUS = eodStatus;
                }
                if (!eodStatus.equals(status.getINPROGRESS_STATUS())) {//INPR
                    if (eodStatus.equals(status.getINITIAL_STATUS())) {//INIT
                        //check file processing status
                        //fileProcessingStatus = producerRepo.checkUploadedFileStatus();
                    }
                    if (fileProcessingStatus) {//check file processing status
                        kafkaMessageUpdator.producerWithNoReturn(Configurations.STARTING_EOD_STATUS, "eodStartStatus");//set starting EOD status on consumer side
                        eodEngineMainService.startEodEngine(eodId);//run the eod engine main service thread
                        //response.put(Util.STATUS_VALUE, Util.STATUS_SUCCESS);
                    } else {
                        throw new FileProcessingNotCompletedException("Cannot be started. File processing not completed for EOD ID: " + eodId);
                    }
                } else {
                    throw new EODEngineStartFailException("Cannot be started. EOD Engine is already running for EOD ID: " + eodId);
                }
            } else {
                throw new EODEngineStartFailException("Cannot be started. EOD ID not found");
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
//            logInfo.info(logManager.processStartEndStyle("EOD-Engine completed for EOD ID:" + eodId));
        }
        return response;
    }

    @GetMapping("/stop/soft/{eodid}/{flag}")
    public Map<String, Object> startEODEngine(@PathVariable("eodid") final String eodId,
                                              @PathVariable("flag") final String flag) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            if (flag.equalsIgnoreCase("true")) {
                Configurations.EOD_ENGINE_SOFT_STOP = true;
            }
            response.put(Util.STATUS_VALUE, Util.STATUS_SUCCESS);
        } catch (Exception ex) {
            response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);
        }
        return response;
    }
}
