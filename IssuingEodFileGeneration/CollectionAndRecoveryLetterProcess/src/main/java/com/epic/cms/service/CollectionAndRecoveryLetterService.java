/**
 * Author : lahiru_p
 * Date : 11/22/2022
 * Time : 10:31 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.repository.CollectionAndRecoveryLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectionAndRecoveryLetterService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    CollectionAndRecoveryLetterRepo collectionAndRecoveryLetterRepo;
    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;
    @Autowired
    LetterService letterService;
    @Autowired
    LogManager logManager;

    public String[] startFirstReminderEligibleCardProcess(StringBuffer cardNumber, int sequenceNo, String remark) {
        String[] fileNameAndPath = null;
        String maskedCardNo = "";
        String accountNo;
        String collectionAndRecoveryFirstLatterTemplateCode = Configurations.FIRST_REMINDER_LETTER_CODE;
        try {
            maskedCardNo = CommonMethods.cardNumberMask(cardNumber);
            List<String> cardDetails = commonFileGenProcessRepo.getCardProductCardType(cardNumber);
            fileNameAndPath = letterService.genaration(collectionAndRecoveryFirstLatterTemplateCode, "0", cardNumber, cardDetails.get(1), Integer.toString(sequenceNo));

            cardDetails.add(3, "LETTER");
            cardDetails.add(4, "FIRST REMINDER");
            commonFileGenProcessRepo.InsertIntoDownloadTable(cardNumber, fileNameAndPath[1], cardDetails);
            //subDBCon.updateLettergenStatus(cardNumber, "YES");
            collectionAndRecoveryLetterRepo.updateTriggerCards(cardNumber);

            accountNo = collectionAndRecoveryLetterRepo.getAccountNoOnCard(cardNumber);
            collectionAndRecoveryLetterRepo.insertIntoDelinquentHistory(cardNumber, accountNo, remark);
            //TODO update lettergenstatus in triggercards
            //TODO delinquent history table
            Configurations.PROCESS_SUCCESS_COUNT++;
        } catch (Exception e) {
            logError.error("Collection & Recovery Letter Process Failed for First Reminder " + maskedCardNo, e);
            Configurations.PROCESS_FAILD_COUNT++;
        }
        return fileNameAndPath;
    }

    public String[] startSecondReminderEligibleCardProcess(StringBuffer cardNumber, int sequenceNo, String remark) {
        String[] fileNameAndPath = null;
        String maskedCardNo = "";
        String accountNo;

        String collectionAndRecoverySecondLatterTemplateCode = Configurations.SECOND_REMINDER_LETTER_CODE;

        try {
            maskedCardNo = CommonMethods.cardNumberMask(cardNumber);
            List<String> cardDetails = commonFileGenProcessRepo.getCardProductCardType(cardNumber);
            fileNameAndPath = letterService.genaration(collectionAndRecoverySecondLatterTemplateCode, "0", cardNumber, cardDetails.get(1), Integer.toString(sequenceNo));

            cardDetails.add(3, "LETTER");
            cardDetails.add(4, "SECOND REMINDER");
            commonFileGenProcessRepo.InsertIntoDownloadTable(cardNumber, fileNameAndPath[1], cardDetails);
            //subDBCon.updateLettergenStatus(cardNumber, "YES");
            collectionAndRecoveryLetterRepo.updateTriggerCards(cardNumber);

            accountNo = collectionAndRecoveryLetterRepo.getAccountNoOnCard(cardNumber);
            collectionAndRecoveryLetterRepo.insertIntoDelinquentHistory(cardNumber, accountNo, remark);
            //TODO update lettergenstatus in triggercards
            //TODO delinquent history table
            Configurations.PROCESS_SUCCESS_COUNT++;
        } catch (Exception e) {
            logError.error("Collection & Recovery Letter Process Failed for Second Reminder " + maskedCardNo, e);
            Configurations.PROCESS_FAILD_COUNT++;
        }
        return fileNameAndPath;
    }
}
