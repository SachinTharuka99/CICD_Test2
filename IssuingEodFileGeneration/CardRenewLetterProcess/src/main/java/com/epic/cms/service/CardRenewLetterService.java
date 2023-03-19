/**
 * Author : lahiru_p
 * Date : 11/22/2022
 * Time : 11:29 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.repository.CardRenewLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.errorLoggerEFGE;

@Service
public class CardRenewLetterService {

    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;

    @Autowired
    CardRenewLetterRepo cardRenewLetterRepo;

    @Autowired
    LetterService letterService;

    @Autowired
    LogManager logManager;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String[] startCardRenewLetterProcess(StringBuffer cardNo, int sequenceNo) {

        String[] fileNameAndPath = null;
        String maskedCardNo = "";
        String cardRenewalTemplateCode = Configurations.CARD_RENEWAL_LETTER_CODE;

        try {
            maskedCardNo = CommonMethods.cardNumberMask(new StringBuffer(cardNo));
            List<String> cardDetails = commonFileGenProcessRepo.getCardProductCardType(new StringBuffer(cardNo));
            fileNameAndPath = letterService.genaration(cardRenewalTemplateCode, "0", new StringBuffer(cardNo), cardDetails.get(1), Integer.toString(sequenceNo));

            cardDetails.add(3, "LETTER");
            cardDetails.add(4, "CARD RENEW");

            commonFileGenProcessRepo.InsertIntoDownloadTable(new StringBuffer(cardNo), fileNameAndPath[1], cardDetails);
            cardRenewLetterRepo.updateLettergenStatusInCardRenew(new StringBuffer(cardNo), "YES");

            Configurations.PROCESS_SUCCESS_COUNT++;

        } catch (Exception e) {
            logManager.logError("Failed Card Renew Letter Process " + maskedCardNo, e, errorLoggerEFGE);
            Configurations.PROCESS_FAILD_COUNT++;
        }
        return fileNameAndPath;
    }
}
