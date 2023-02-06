package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.CRIBFileService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CRIBFileConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    CRIBFileService cribFileService;

    @Override
    public void concreteProcess() throws Exception {
        Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CRIB_FILE;
        CommonMethods.eodDashboardProgressParametersReset();
        cribFileService.startCribFileProcess();
    }
}
