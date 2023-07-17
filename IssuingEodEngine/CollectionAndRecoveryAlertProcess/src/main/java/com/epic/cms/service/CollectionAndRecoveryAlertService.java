package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CollectionAndRecoveryAlertRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CollectionAndRecoveryAlertService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    CollectionAndRecoveryAlertRepo collectionAndRecoveryAlertRepo;
    @Autowired
    AlertService alert;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processCollectionAndRecoveryAlertService(StringBuffer cardNumber, String val, ProcessBean processBean, AtomicInteger faileCardCount) {
        if (!Configurations.isInterrupted) {
            boolean status = false;
            LinkedHashMap adjustDetails = new LinkedHashMap();
            try {
                cardNumber = new StringBuffer(cardNumber);

                adjustDetails.put("Card number", CommonMethods.cardNumberMask(cardNumber));
                adjustDetails.put("Last trigger point", val);

                String accNum = commonRepo.getAccountNoOnCard(cardNumber);

                if (Configurations.X_DAYS_BEFORE_1_DUE_DATE.equals(val)) {
                    status = commonRepo.getTriggerEligibleStatus(Configurations.X_DAYS_BEFORE_1_DUE_DATE, Configurations.EMAIL);
                    if (status) {
                        alert.alertGeneration(Configurations.X_DAYS_BEFORE_1_DUE_DATE_EMAIL_CODE, "0", val, cardNumber, Configurations.EMAIL_TEMPLATE, accNum);
                    }
                    status = commonRepo.getTriggerEligibleStatus(Configurations.X_DAYS_BEFORE_1_DUE_DATE, Configurations.SMS);
                    if (status) {
                        alert.alertGeneration(Configurations.X_DAYS_BEFORE_1_DUE_DATE_SMS_CODE, "0", val, cardNumber, Configurations.SMS_TEMPLATE, accNum);
                    }
                    adjustDetails.put("E-Mail Template Code", Configurations.X_DAYS_BEFORE_1_DUE_DATE_EMAIL_CODE);
                    adjustDetails.put("SMS Template Code", Configurations.X_DAYS_BEFORE_1_DUE_DATE_SMS_CODE);

                } else if (Configurations.IMMEDIATE_AFTER_1_DUE_DATE.equals(val)) {
                    status = commonRepo.getTriggerEligibleStatus(Configurations.IMMEDIATE_AFTER_1_DUE_DATE, Configurations.EMAIL);
                    if (status) {
                        alert.alertGeneration(Configurations.IMMEDIATE_AFTER_1_DUE_DATE_EMAIL_CODE, "0", val, cardNumber, Configurations.EMAIL_TEMPLATE, accNum);
                    }
                    status = commonRepo.getTriggerEligibleStatus(Configurations.IMMEDIATE_AFTER_1_DUE_DATE, Configurations.SMS);
                    if (status) {
                        alert.alertGeneration(Configurations.IMMEDIATE_AFTER_1_DUE_DATE_SMS_CODE, "0", val, cardNumber, Configurations.SMS_TEMPLATE, accNum);
                    }
                    adjustDetails.put("E-Mail Template Code", Configurations.IMMEDIATE_AFTER_1_DUE_DATE_EMAIL_CODE);
                    adjustDetails.put("SMS Template Code", Configurations.IMMEDIATE_AFTER_1_DUE_DATE_SMS_CODE);

                } else if (Configurations.IMMEDIATE_AFTER_2_DUE_DATE.equals(val)) {
                    status = commonRepo.getTriggerEligibleStatus(Configurations.IMMEDIATE_AFTER_2_DUE_DATE, Configurations.EMAIL);
                    if (status) {
                        alert.alertGeneration(Configurations.IMMEDIATE_AFTER_2_DUE_DATE_EMAIL_CODE, "0", val, cardNumber, Configurations.EMAIL_TEMPLATE, accNum);
                    }
                    status = commonRepo.getTriggerEligibleStatus(Configurations.IMMEDIATE_AFTER_2_DUE_DATE, Configurations.SMS);
                    if (status) {
                        alert.alertGeneration(Configurations.IMMEDIATE_AFTER_2_DUE_DATE_SMS_CODE, "0", val, cardNumber, Configurations.SMS_TEMPLATE, accNum);
                    }
                    adjustDetails.put("E-Mail Template Code", Configurations.IMMEDIATE_AFTER_2_DUE_DATE_EMAIL_CODE);
                    adjustDetails.put("SMS Template Code", Configurations.IMMEDIATE_AFTER_2_DUE_DATE_SMS_CODE);

                } else if (Configurations.IMMEDIATE_AFTER_3_DUE_DATE.equals(val)) {
                    status = commonRepo.getTriggerEligibleStatus(Configurations.IMMEDIATE_AFTER_3_DUE_DATE, Configurations.EMAIL);
                    if (status) {
                        alert.alertGeneration(Configurations.IMMEDIATE_AFTER_3_DUE_DATE_EMAIL_CODE, "0", val, cardNumber, Configurations.EMAIL_TEMPLATE, accNum);
                    }
                    status = commonRepo.getTriggerEligibleStatus(Configurations.IMMEDIATE_AFTER_3_DUE_DATE, Configurations.SMS);
                    if (status) {
                        alert.alertGeneration(Configurations.IMMEDIATE_AFTER_3_DUE_DATE_SMS_CODE, "0", val, cardNumber, Configurations.SMS_TEMPLATE, accNum);
                    }
                    adjustDetails.put("E-Mail Template Code", Configurations.IMMEDIATE_AFTER_3_DUE_DATE_EMAIL_CODE);
                    adjustDetails.put("SMS Template Code", Configurations.IMMEDIATE_AFTER_3_DUE_DATE_SMS_CODE);

                }

                collectionAndRecoveryAlertRepo.updateAlertGenStatus(cardNumber, val);

                //Configurations.successCardNoCount_CollectionAndRecoveryAlert++;
               // Configurations.PROCESS_SUCCESS_COUNT++;
                adjustDetails.put("Collection & Recovery Alert Process Status", "Passed");
            } catch (Exception e) {
                //Configurations.failedCardNoCount_CollectionAndRecoveryAlert++;
                faileCardCount.addAndGet(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardNumber), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                adjustDetails.put("Collection & Recovery Alert Process Status", "Failed");
                logError.error("Error Occurs, when running collection & recovery alert process for cardnumber " + CommonMethods.cardNumberMask(cardNumber) + " ", e);
            } finally {
                logInfo.info(logManager.logDetails(adjustDetails));
            }
        }
    }
}
