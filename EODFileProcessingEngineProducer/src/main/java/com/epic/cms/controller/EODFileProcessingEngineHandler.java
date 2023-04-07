/**
 * Author :
 * Date : 4/5/2023
 * Time : 7:27 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.LogManager.infoLogger;

@RestController
@RequestMapping("/eod-file-processing-engine")
public class EODFileProcessingEngineHandler {

    @Autowired
    LogManager logManager;

    int processCategory = 3;

    @GetMapping("/start")
    public Map<String, Object> startFileProcessingEngine() throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {

        } catch (Exception ex) {

        }
        return null;
    }

    @GetMapping("/start/{processId}/{fileId}")
    public Map<String, Object> startFileProcessingEngine(@PathVariable("processId") final String processId, @PathVariable("fileId") final String fileId) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {

        } catch (Exception ex) {

        }
        return null;
    }
}
