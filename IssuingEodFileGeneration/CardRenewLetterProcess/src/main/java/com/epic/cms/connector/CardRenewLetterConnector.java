/**
 * Author : lahiru_p
 * Date : 11/22/2022
 * Time : 11:27 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.repository.CardRenewLetterRepo;
import com.epic.cms.service.CardRenewLetterService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;
@Service
public class CardRenewLetterConnector extends FileGenProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    CardRenewLetterRepo cardRenewLetterRepo;

    @Autowired
    CardRenewLetterService cardRenewLetterService;

    @Override
    public void concreteProcess() throws Exception {

        ArrayList<StringBuffer> renewalCardList = new ArrayList<>();
        String[] fileNameAndPath = null;

        try {

            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARDRENEW_LETTER;
            CommonMethods.eodDashboardProgressParametersReset();

            renewalCardList = cardRenewLetterRepo.getRenewalCardsToGenerateLetters();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = renewalCardList.size();
            int sequenceNo = 0;

            for (int i = 0; i < renewalCardList.size(); i++) {
                fileNameAndPath = cardRenewLetterService.startCardRenewLetterProcess(renewalCardList.get(i), sequenceNo);
                sequenceNo++;
            }

            summery.put("Started Date ", Configurations.EOD_DATE.toString());
            summery.put("Total No of Effected Files ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("Process Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
            summery.put("Process Failed Count ", Configurations.PROCESS_FAILD_COUNT);
            summery.put("File Name and Path ", fileNameAndPath);

            infoLogger.info(logManager.processSummeryStyles(summery));

        }catch (Exception e){

            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("CardRenewLetterProcess Failed" , e);
            if(fileNameAndPath!= null){
                fileGenerationService.deleteExistFile(fileNameAndPath[0]);
            }
        } finally {
            try {
                if (!renewalCardList.isEmpty()) {
                    for (int i = 0; i < renewalCardList.size(); i++) {
                        CommonMethods.clearStringBuffer(renewalCardList.get(i));
                    }
                }
            } catch (Exception e) {
                errorLogger.error("Exception in Card Number Clearing ",e);
            }

        }
    }
}
