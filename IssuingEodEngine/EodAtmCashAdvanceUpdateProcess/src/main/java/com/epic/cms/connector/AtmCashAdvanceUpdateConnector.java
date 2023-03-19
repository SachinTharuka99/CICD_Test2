package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.AtmCashAdvanceUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AtmCashAdvanceUpdateConnector  extends ProcessBuilder {

    @Autowired
    AtmCashAdvanceUpdateService atmCashAdvanceUpdateService;

    @Override
    public void concreteProcess() throws Exception {
        atmCashAdvanceUpdateService.startEodAtmCashAdvanceUpdate();
    }

    @Override
    public void addSummaries() {

    }
}
