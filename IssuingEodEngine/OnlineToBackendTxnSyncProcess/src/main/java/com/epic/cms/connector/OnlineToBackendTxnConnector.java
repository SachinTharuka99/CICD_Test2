package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.service.OnlineToBackendTxnService;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;


@Service
public class OnlineToBackendTxnConnector extends ProcessBuilder {

    @Autowired
    OnlineToBackendTxnService onlineToBackendTxnService;

    @Autowired
    LogManager logManager;

    @Override
    public void concreteProcess() throws Exception {

        try {
            onlineToBackendTxnService.OnlineToBackend();

        } catch (Exception e) {
            logManager.logError("Online to Backend Txn Sync failed", e, errorLogger);
        }
    }

    @Override
    public void addSummaries() {

    }
}
