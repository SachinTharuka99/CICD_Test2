package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.EodPaymentUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EODPaymentUpdateConnector extends ProcessBuilder {

    @Autowired
    EodPaymentUpdateService eodPaymentUpdateService;

    @Override
    public void concreteProcess() throws Exception {
        eodPaymentUpdateService.startEODPaymentUpdate();
    }
}
