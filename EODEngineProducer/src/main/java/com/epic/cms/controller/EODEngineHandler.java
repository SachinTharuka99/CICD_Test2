/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:38 PM
 * Project Name : eod-engine
 */

package com.epic.cms.controller;

import com.epic.cms.dao.EODEngineProducerDao;
import com.epic.cms.repository.EODEngineProducerRepo;
import com.epic.cms.service.EODEngineMainService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.Util;
import com.epic.cms.util.exception.InvalidEodId;
import com.epic.cms.util.exception.UploadedFileNotCompleted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.LogManager.infoLogger;

@RestController
@RequestMapping("eod-engine")
public class EODEngineHandler {

    @Autowired
    @Qualifier("EODEngineProducerRepo")
    EODEngineProducerRepo producerRepo;

    @Autowired
    EODEngineMainService eodEngineMainService;

    @GetMapping("/start/{eodid}")
    public Map<String, Object> startEODEngine(@PathVariable("eodid") final String eodId) throws Exception {
        Map<String, Object> response = new HashMap<>();
        int categoryId = 2;
        try {
            infoLogger.info(LogManager.processStartEndStyle("EOD-Engine Start for EODID:" + eodId));
            Configurations.STARTING_EOD_STATUS = producerRepo.getEODStatusFromEODID(eodId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new InvalidEodId("Invalid EOD ID:" + eodId));

            boolean isFilesCompleted = producerRepo.checkUploadedFileStatus();
            //run the main service thread
            eodEngineMainService.EODEngineMain(eodId, categoryId);

            if (isFilesCompleted) {
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
