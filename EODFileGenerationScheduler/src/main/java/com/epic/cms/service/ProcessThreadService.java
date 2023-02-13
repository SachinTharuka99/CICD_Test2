/**
 * Author : lahiru_p
 * Date : 2/1/2023
 * Time : 9:38 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.connector.*;
import com.epic.cms.util.Configurations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class ProcessThreadService {
    @Async("ThreadPool_100")
    public void startProcessByProcessId(int processId) throws Exception {
        try{
            if(processId > 0){
                ProcessBuilder processBuilder = (ProcessBuilder) Configurations.processConnectorList.get(processId);
                processBuilder.startProcess();
            }else {
                errorLogger.error("Invalid Process Id ");
            }
        }catch (Exception e){
            throw e;
        }
    }
}
