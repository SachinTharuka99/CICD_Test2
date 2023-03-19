package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import org.springframework.stereotype.Service;

@Service
public class OutgoingIPMFileGenConnector extends ProcessBuilder {
    @Override
    public void concreteProcess() throws Exception {
        System.out.println("--inside MasterFileReadingConnector--");
    }

    @Override
    public void addSummaries() {

    }
}
