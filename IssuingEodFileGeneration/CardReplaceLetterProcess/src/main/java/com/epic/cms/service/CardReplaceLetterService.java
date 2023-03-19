/**
 * Author : yasiru_l
 * Date : 11/22/2022
 * Time : 4:39 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.repository.CardReplaceLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.*;

@Service
public class CardReplaceLetterService {

    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    LetterService letterService;
    @Autowired
    CardReplaceLetterRepo cardReplaceLetterRepo;

    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;

    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public String[] replaceCardExceptProductChangeCard(StringBuffer replaceCard, int sequenceNo) {

        String[] fileNameAndPath = null;
        String maskedCardNo = null;

        try {
            maskedCardNo = "";
            String applicationConfirmTemplateCode = Configurations.CARD_REPLACEMENT_LETTER_CODE;
            maskedCardNo = CommonMethods.cardNumberMask(replaceCard);
            List<String> cardDetils = commonFileGenProcessRepo.getCardProductCardType(replaceCard);
            fileNameAndPath = letterService.genaration(applicationConfirmTemplateCode, "0", replaceCard, cardDetils.get(1), Integer.toString(sequenceNo));

            cardDetils.add(3, "LETTER");
            cardDetils.add(4, "CARD REPLACE");

            commonFileGenProcessRepo.InsertIntoDownloadTable(replaceCard, fileNameAndPath[1].toString(), cardDetils);
            cardReplaceLetterRepo.updateLettergenStatusInCardReplace(replaceCard, "YES");

            Configurations.PROCESS_SUCCESS_COUNT++;

        } catch (Exception e) {
            logManager.logError("Failed Card Replace Letter Process " + maskedCardNo, e, errorLoggerEFGE);
            Configurations.PROCESS_FAILD_COUNT++;

        }
        return fileNameAndPath;
    }

    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public String[] forProductChangeCards(StringBuffer productChangeCard, int sequenceNo) {

        String[] fileNameAndPath = null;
        String maskedCardNo = null;

        try {
            maskedCardNo = "";
            String productChangeTemplateCode = Configurations.PRODUCT_CHANGE_LETTER_CODE;
            maskedCardNo = CommonMethods.cardNumberMask(productChangeCard);
            List<String> cardDetils = cardReplaceLetterRepo.getCardProductCardTypeForProductChangeCards(productChangeCard);
            fileNameAndPath = letterService.genaration(productChangeTemplateCode, "0", productChangeCard, cardDetils.get(1), Integer.toString(sequenceNo));

            cardDetils.add(3, "LETTER");
            cardDetils.add(4, "PRODUCT CHANGE");

            commonFileGenProcessRepo.InsertIntoDownloadTable(productChangeCard, fileNameAndPath[1].toString(), cardDetils);
            cardReplaceLetterRepo.updateLettergenStatusInProductChange(productChangeCard, "YES");

            Configurations.PROCESS_SUCCESS_COUNT++;

        } catch (Exception e) {
            logManager.logError("Failed Card Replace Letter Process " + maskedCardNo, e, errorLoggerEFGE);
            Configurations.PROCESS_FAILD_COUNT++;
        }
        return fileNameAndPath;
    }
}
