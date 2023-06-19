/**
 * Author : lahiru_p
 * Date : 2/1/2023
 * Time : 9:38 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProcessThreadService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;

    @Async("ThreadPool_100")
    public void startProcessByProcessId(int processId, String uniqueId) throws Exception {
        try {
            if (processId > 0) {
                FileGenProcessBuilder processBuilder = (FileGenProcessBuilder) Configurations.processConnectorList.get(processId);
                processBuilder.startProcess(processId, uniqueId);
            } else {
                logError.error("Invalid Process Id ");
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
