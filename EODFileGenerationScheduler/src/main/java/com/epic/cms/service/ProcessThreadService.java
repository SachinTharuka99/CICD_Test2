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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.errorLoggerEFGE;

@Service
public class ProcessThreadService {

    @Autowired
    LogManager logManager;

    @Async("ThreadPool_100")
    public void startProcessByProcessId(int processId, String uniqueId) throws Exception {
        try{
            if(processId > 0){
                FileGenProcessBuilder processBuilder = (FileGenProcessBuilder) Configurations.processConnectorList.get(processId);
                processBuilder.startProcess(processId, uniqueId);
            }else {
                logManager.logError("Invalid Process Id ", errorLoggerEFGE);
                logManager.logError("Invalid Process Id ",errorLogger);
            }
        }catch (Exception e){
            throw e;
        }
    }
}
