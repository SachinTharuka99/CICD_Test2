/**
 * Author :
 * Date : 4/5/2023
 * Time : 7:27 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.service.EODFileProcessingEngineMainService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import com.epic.cms.util.Util;
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
    public Map<String, Object> startFileProcessingEngine(@PathVariable("fileType") final String fileType, @PathVariable("fileId") final String fileId) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            Configurations.STARTING_EOD_STATUS = "INIT";
            if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                eodFileProcessingEngineMainService.startProcess(fileType, fileId);
                response.put(Util.STATUS_VALUE, Util.STATUS_SUCCESS);
            } else {
                System.out.println("eod is not in init status");
            }
        } catch (Exception ex) {
            response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);
        }
        return response;
    }
}
