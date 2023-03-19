package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.SnapShotService;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class SnapShotConnector extends ProcessBuilder {

    /**
     * This process check eod status update card snapshot table from card table and
     * update account snapshot table from card account table
     *
     */

    @Autowired
    SnapShotService snapShotService;

    @Autowired
    LogManager logManager;

    @Override
    public void concreteProcess() throws Exception {
        try {
            snapShotService.startDailySnapShotProcess();

        }catch (Exception e){
            logManager.logError("Failed SnapShot Process ", e, errorLogger);
        }
    }

    @Override
    public void addSummaries() {

    }
}
