/**
 * Author : yasiru_l
 * Date : 11/21/2022
 * Time : 10:08 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.repository.CardApplicationConfirmationLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.*;

@Service
public class CardApplicationConfirmationLetterService {
    @Autowired
    LogManager logManager;

    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    LetterService letterService;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Autowired
    CardApplicationConfirmationLetterRepo cardApplicationConfirmationLetterRepo;
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public String[] getConfirmationLetter(StringBuffer confirmCard, int sequenceNo){

        String maskedCardNo = null;
        String[] fileNameAndPath = new String[2];

        try {
            maskedCardNo = "";
            String applicationConfirmTemplateCode = Configurations.APPLICATION_CONFIRMATION_LETTER_CODE;
            maskedCardNo = CommonMethods.cardNumberMask(confirmCard);
            List<String> cardDetils = commonFileGenProcessRepo.getCardProductCardType(new StringBuffer(confirmCard));
            fileNameAndPath = letterService.genaration(applicationConfirmTemplateCode, "0", confirmCard, cardDetils.get(1), Integer.toString(sequenceNo));

            cardDetils.add(3, "LETTER");
            cardDetils.add(4, "APPLICATION APPROVE");

            commonFileGenProcessRepo.InsertIntoDownloadTable(confirmCard, fileNameAndPath[1], cardDetils);
            cardApplicationConfirmationLetterRepo.updateLettergenStatus(confirmCard, "YES");

            Configurations.PROCESS_SUCCESS_COUNT++;

        } catch (Exception e) {
            Configurations.PROCESS_FAILD_COUNT++;
            logError.error("Card Application Confirmation Letter Process Failed for cardnumber: " + maskedCardNo, e);
        }

        return fileNameAndPath;
    }
}
