package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.OnlineToBackendTxnService;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;


@Service
public class OnlineToBackendTxnConnector extends ProcessBuilder {

    @Autowired
    OnlineToBackendTxnService onlineToBackendTxnService;

    @Autowired
    LogManager logManager;

    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public void concreteProcess() throws Exception {

        try {
            onlineToBackendTxnService.OnlineToBackend();

        } catch (Exception e) {
            logError.error("Online to Backend Txn Sync failed", e);
        }
    }

    @Override
    public void addSummaries() {

    }
}
