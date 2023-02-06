package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.InitialProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitialConnector extends ProcessBuilder {

    @Autowired
    InitialProcessService initialProcessService;

    @Override
    public void concreteProcess() throws Exception {
        initialProcessService.startInitialProcess();
    }
}
