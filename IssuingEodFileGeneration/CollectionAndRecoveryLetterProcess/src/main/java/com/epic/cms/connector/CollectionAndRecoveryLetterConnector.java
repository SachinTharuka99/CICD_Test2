/**
 * Author : lahiru_p
 * Date : 11/22/2022
 * Time : 10:30 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.repository.CollectionAndRecoveryLetterRepo;
import com.epic.cms.service.CollectionAndRecoveryLetterService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
public class CollectionAndRecoveryLetterConnector extends FileGenProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    CollectionAndRecoveryLetterRepo collectionAndRecoveryLetterRepo;
    @Autowired
    CollectionAndRecoveryLetterService collectionAndRecoveryLetterService;

    @Override
    public void concreteProcess() throws Exception {
        String[] fileNameAndPath = null;
        boolean status = false;
        ArrayList<StringBuffer> cardList1;
        ArrayList<StringBuffer> cardList2;

        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS;
            CommonMethods.eodDashboardProgressParametersReset();

            cardList1 = collectionAndRecoveryLetterRepo.getFirstReminderEligibleCards();
            cardList2 = collectionAndRecoveryLetterRepo.getSecondReminderEligibleCards();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (cardList1.size() + cardList2.size());
            String remark;

            try {
                //check allocation rule method
                status = collectionAndRecoveryLetterRepo.getTriggerEligibleStatus(Configurations.TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE, Configurations.LETTER);
                if (status) {
                    remark = "LETTER HAS BEEN SENT AFTER 2ND DUE DATE";
                    int sequenceNo = 0;

                    for (StringBuffer cardNumber : cardList1) {
                        fileNameAndPath = collectionAndRecoveryLetterService.startFirstReminderEligibleCardProcess(cardNumber, sequenceNo, remark);
                        sequenceNo++;
                    }
                }
            } catch (Exception e) {
                logError.error("Collection & Recovery Letter Process Failed for First Reminder", e);
                if (fileNameAndPath != null) {
                    fileGenerationService.deleteExistFile(fileNameAndPath[0]);
                }
            }

            try {
                //check allocation rule method
                status = collectionAndRecoveryLetterRepo.getTriggerEligibleStatus(Configurations.TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE, Configurations.LETTER);
                if (status) {
                    remark = "LETTER HAS BEEN SENT AFTER 2ND DUE DATE";
                    int sequenceNo = 0;
                    for (StringBuffer cardNumber : cardList2) {
                        fileNameAndPath = collectionAndRecoveryLetterService.startSecondReminderEligibleCardProcess(cardNumber, sequenceNo, remark);
                        sequenceNo++;
                    }
                }
            } catch (Exception e) {
                logError.error("Collection & Recovery Letter Process Failed for Second Reminder", e);
                if (fileNameAndPath != null) {
                    fileGenerationService.deleteExistFile(fileNameAndPath[0]);
                }
            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logError.error("Failed Collection & Recovery Letter Process ", e);
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("Process Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Process Failed Count ", Configurations.PROCESS_FAILD_COUNT);
        summery.put("Process Status", "Success");
    }
}
