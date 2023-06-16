/**
 * Author : yasiru_l
 * Date : 11/22/2022
 * Time : 4:38 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.repository.CardReplaceLetterRepo;
import com.epic.cms.service.CardReplaceLetterService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
public class CardReplaceLetterConnector extends FileGenProcessBuilder {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    CardReplaceLetterRepo cardReplaceLetterRepo;
    @Autowired
    CardReplaceLetterService cardReplaceLetterService;
    String[] fileNameAndPath = null;

    @Override
    public void concreteProcess() throws Exception {

        ArrayList<StringBuffer> productChangeCardList = null;
        ArrayList<StringBuffer> replaceCardList = null;

        try {
            replaceCardList = cardReplaceLetterRepo.getReplacedToGenerateLetters();

            try {
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARDREPLACE_LETTER;
                CommonMethods.eodDashboardProgressParametersReset();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += replaceCardList.size();
                int sequenceNo = 0;

                for (int i = 0; i < replaceCardList.size(); i++) {
                    fileNameAndPath = cardReplaceLetterService.replaceCardExceptProductChangeCard(replaceCardList.get(i), sequenceNo);
                    sequenceNo++;
                }

                productChangeCardList = cardReplaceLetterRepo.getProductChangedCardsToGenerateLetters();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += productChangeCardList.size();

                int sequenceNo2 = 0;
                for (int i = 0; i < productChangeCardList.size(); i++) {
                    fileNameAndPath = cardReplaceLetterService.forProductChangeCards(productChangeCardList.get(i), sequenceNo2);
                    sequenceNo2++;
                }

            } catch (Exception e) {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                logError.error("Error in Card Replace Letter Process ", e);
                if (fileNameAndPath != null) {
                    fileGenerationService.deleteExistFile(fileNameAndPath[0]);
                }
            }

        } finally {
            logInfo.info(logManager.logSummery(summery));
            try {
                if (productChangeCardList != null && productChangeCardList.size() != 0) {
                    for (StringBuffer sb : productChangeCardList) {
                        CommonMethods.clearStringBuffer(sb);
                    }
                    productChangeCardList = null;
                }
                if (replaceCardList != null && replaceCardList.size() != 0) {
                    for (StringBuffer cardNo : replaceCardList) {
                        CommonMethods.clearStringBuffer(cardNo);
                    }
                    replaceCardList = null;
                }
            } catch (Exception e) {
                logError.error("Exception in Card Number Clearing ", e);
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("Total No of Effected Letters ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Letter Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Letter Failed Count ", Configurations.PROCESS_FAILD_COUNT);
    }
}
