/**
 * Author : yasiru_l
 * Date : 12/5/2022
 * Time : 11:37 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.CardAccountCustomerBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.repository.CardApplicationRejectLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.repository.CommonRepo;
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
public class CardApplicationRejectLetterService {

    @Autowired
    CardApplicationRejectLetterRepo cardApplicationRejectLetterRepo;

    @Autowired
    LetterService letterService;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;

    @Autowired
    LogManager logManager;

    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public String[] processCardApplicationReject(String aplicationId, int sequenceNo) throws Exception {

        String[] fileNameAndPath = new String[2];

        try {
            String applictionRejectTemplateCode = Configurations.APPLICATION_REJECTION_LETTER_CODE;
            List<String> cardDetails = commonFileGenProcessRepo.getCardProductCardTypeByApplicationId(aplicationId);
            fileNameAndPath = letterService.genaration(applictionRejectTemplateCode, aplicationId, new StringBuffer("0"), cardDetails.get(1), Integer.toString(sequenceNo));

            cardDetails.add(3, "LETTER");
            cardDetails.add(4, "APPLICATION REJECT");

            StringBuffer cardNo = cardApplicationRejectLetterRepo.getCardNo(aplicationId);
            commonFileGenProcessRepo.InsertIntoDownloadTable(cardNo, fileNameAndPath[1], cardDetails);
            cardApplicationRejectLetterRepo.updateLettergenStatus(cardNo, "YES");

            Configurations.PROCESS_SUCCESS_COUNT++;

        }catch (Exception e){
            CardAccountCustomerBean cBean = commonRepo.getCardAccountCustomer(new StringBuffer(aplicationId));
            ErrorCardBean errorBean = new ErrorCardBean();
            errorBean.setCardNo(cBean.getCardNumber());
            errorBean.setAccountNo(cBean.getAccountNumber());
            errorBean.setCustomerID(cBean.getCustomerId());
            errorBean.setEodID(Configurations.EOD_ID);
            errorBean.setProcessId(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            errorBean.setIsProcessFails(0);

            Configurations.PROCESS_FAILD_COUNT++;
            logManager.logError("Card Application Rejected Letter Process Failed " +  e, errorLoggerEFGE);

        }
        return fileNameAndPath;
    }
}
