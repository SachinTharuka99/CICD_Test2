package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.SnapShotService;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public void concreteProcess() throws Exception {
        try {
            snapShotService.startDailySnapShotProcess();

        }catch (Exception e){
            logError.error("Failed SnapShot Process ", e);
        }
    }

    @Override
    public void addSummaries() {

    }
}
