package com.epic.cms.service;

import com.epic.cms.repository.SnapShotRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;


@Service
public class SnapShotService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    SnapShotRepo snapShotRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startDailySnapShotProcess() {
        LinkedHashMap details = new LinkedHashMap();
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_SNAPSHOT;
            CommonMethods.eodDashboardProgressParametersReset();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 4;

            /**
             Check whether the eod processes are complete
             */

            int status = snapShotRepo.checkEodComplete();

            if (status > 0) {
                details.put("EOD processes are not completed", "FAILED");
                logInfo.info(logManager.logDetails(details));

            } else {
                try {
                    /**
                     * update CARD_SNAPSHOT table with
                     * CARDNUMBER,CREDITLIMIT,CASHLIMIT,OTBCREDIT,OTBCASH,TEMPCREDITAMOUNT,TEMPCASHAMOUNT,CARDSTATUS,EODID
                     */
                    snapShotRepo.updateSnapShotTableOfCards();

                    details.put("CardSnapShot table updated", "COMPLETED");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("Exception ", e);
                }
                try {
                    /**
                     * update ACCOUNT_SNAPSHOT table with
                     * ACCOUNTNO,CREDITLIMIT,CASHLIMIT,OTBCASH,OTBCREDIT,STATUS,EODID
                     */
                    snapShotRepo.updateSnapShotTableOfAccounts();

                    details.put("AccountSnapShot table updated", "COMPLETED");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("Exception ", e);
                }
                try {
                    /**
                     * update ONLINECARDSNAPSHOT table with
                     * CARDNUMBER,CREDITLIMIT,CASHLIMIT,OTBCREDIT,OTBCASH,TEMPCREDITAMOUNT,TEMPCASHAMOUNT,CARDSTATUS,EODID
                     */
                    snapShotRepo.updateOnlineSnapShotTableOfCards();

                    details.put("OnlineCardSnapShot table updated", "COMPLETED");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("Exception ", e);
                }
                try {
                    /**
                     * update ONLINEACCOUNTSNAPSHOT table with
                     * ACCOUNTNO,CREDITLIMIT,CASHLIMIT,OTBCASH,OTBCREDIT,STATUS,EODID
                     */
                    snapShotRepo.updateOnlineSnapShotTableOfAccounts();

                    details.put("OnlineAccountSnapShot table updated", "COMPLETED");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("Exception ", e);
                }
            }
            logInfo.info(logManager.logDetails(details));

        } catch (Exception e) {
            logError.error("Exception ", e);
        }
    }
}
