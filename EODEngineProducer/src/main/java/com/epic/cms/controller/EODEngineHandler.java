/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:38 PM
 * Project Name : eod-engine
 */

package com.epic.cms.controller;

import com.epic.cms.repository.EODEngineProducerRepo;
import com.epic.cms.service.EODEngineMainService;
import com.epic.cms.util.*;
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

    @GetMapping("/start")
    public Map<String, Object> startEODEngine() throws Exception {
        Map<String, Object> response = new HashMap<>();
        int categoryId = 2;
        int eodId =0;
        try {
            eodId =producerRepo.getNextRunningEodId();
            String EodIdString = String.valueOf(eodId);
            LogManager.processStartEndStyle("EOD-Engine Start for EODID:" + eodId);
            Configurations.STARTING_EOD_STATUS = producerRepo.getEODStatusFromEODID(EodIdString)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new InvalidEodId("Invalid EOD ID:" + EodIdString));

            boolean isFilesCompleted = producerRepo.checkUploadedFileStatus();

            isFilesCompleted = true;
            if (isFilesCompleted) {
                //run the main service thread
                eodEngineMainService.EODEngineMain(EodIdString, categoryId);
                response.put(Util.STATUS_VALUE, Util.STATUS_SUCCESS);
            } else {
                throw new UploadedFileNotCompleted("Uploaded file not completed.");
            }
        } catch (Exception ex) {
            response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);
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
