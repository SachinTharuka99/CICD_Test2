package com.epic.cms.service;

import com.epic.cms.model.bean.BalanceComponentBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.CardLimitEnhancementRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
@Service
public class CardLimitEnhancementService {

    @Autowired
    LogManager logManager;

    @Autowired
    CardLimitEnhancementRepo cardLimitEnhancementRepo;

    @Autowired
    StatusVarList status;

    @Async("taskExecutor2")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void processCardLimitEnhancement(ArrayList<BalanceComponentBean> enhancementList, OtbBean bean) {
        if(!Configurations.isInterrupted){
            LinkedHashMap details = new LinkedHashMap();

            for (BalanceComponentBean componentBean : enhancementList) {
                try {
                    if (componentBean.getIncrementType().equals(Configurations.CREDIT_INCREMENT)) {
                        if (componentBean.getIncOrDec().equals(Configurations.LIMIT_INCREMENT)) {
                            componentBean.setOtbCredit(componentBean.getIncrementAmount());
                        } else if (componentBean.getIncOrDec().equals(Configurations.LIMIT_DECREMENT)) {
                            componentBean.setOtbCredit((-1) * (componentBean.getIncrementAmount()));
                        }

                        cardLimitEnhancementRepo.updateCardCreditLimit(componentBean.getCardNumber(), componentBean.getOtbCredit());
                        cardLimitEnhancementRepo.updateOnlineCardCreditLimit(componentBean.getCardNumber(), componentBean.getOtbCredit());

                        if (componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_MAIN)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                            cardLimitEnhancementRepo.updateAccountCreditLimit(bean.getAccountnumber(), componentBean.getOtbCredit());
                            cardLimitEnhancementRepo.updateOnlineAccountCreditLimit(bean.getAccountnumber(), componentBean.getOtbCredit());
                            cardLimitEnhancementRepo.updateCustomerCreditLimit(bean.getCustomerid(), componentBean.getOtbCredit());
                            cardLimitEnhancementRepo.updateOnlineCustomerCreditLimit(bean.getCustomerid(), componentBean.getOtbCredit());
                        }

                    } else if (componentBean.getIncrementType().equals(Configurations.CASH_INCREMENT)) {
                        if (componentBean.getIncOrDec().equals(Configurations.LIMIT_INCREMENT)) {
                            componentBean.setOtbCash(componentBean.getIncrementAmount());
                        } else if (componentBean.getIncOrDec().equals(Configurations.LIMIT_DECREMENT)) {
                            componentBean.setOtbCash((-1) * (componentBean.getIncrementAmount()));
                        }

                        cardLimitEnhancementRepo.updateCardCashLimit(componentBean.getCardNumber(), componentBean.getOtbCash());
                        cardLimitEnhancementRepo.updateOnlineCardCashLimit(componentBean.getCardNumber(), componentBean.getOtbCash());

                        if (componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_MAIN)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY)
                                || componentBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                            cardLimitEnhancementRepo.updateAccountCashLimit(bean.getAccountnumber(), componentBean.getOtbCash());
                            cardLimitEnhancementRepo.updateOnlineAccountCashLimit(bean.getAccountnumber(), componentBean.getOtbCash());
                            cardLimitEnhancementRepo.updateCustomerCashLimit(bean.getCustomerid(), componentBean.getOtbCash());
                            cardLimitEnhancementRepo.updateOnlineCustomerCashLimit(bean.getCustomerid(), componentBean.getOtbCash());
                        }

                    }

                    cardLimitEnhancementRepo.updateTempLimitIncrementTable(componentBean.getCardNumber(), status.getCREDIT_LIMIT_ENHANCEMENT_ACTIVE(), componentBean.getRequestId());

                    if (enhancementList.size() == Configurations.Iterator_Card_Limit_Enhancement) {

                        for (BalanceComponentBean card : enhancementList) {
                            details.put("Customer ID", bean.getCustomerid());
                            details.put("Account Number", bean.getAccountnumber());
                            details.put("Card Number", CommonMethods.cardNumberMask(card.getCardNumber()));
                            details.put("Card Type", card.getCardCategory());
                            details.put("Enhance Type", card.getIncrementType());
                            details.put("Enhance Amount", card.getIncrementAmount());
                            details.put("Increment Or Decrement", card.getIncOrDec());
                            details.put("Start Date", card.getStartDate());
                            details.put("End Date", card.getEndDate());
                            infoLogger.info(logManager.processDetailsStyles(details));
                            details.clear();
                        }

                    }
                    Configurations.PROCESS_SUCCESS_COUNT++;

                } catch (Exception ex) {
                    errorLogger.error("Fee post process failed for account " + bean.getAccountnumber(), ex);
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(bean.getCardnumber()), ex.getMessage(), Configurations.PROCESS_LIMIT_ENHANCEMENT, "Card Limit Enhancement Process", 0, CardAccount.ACCOUNT));
                    Configurations.PROCESS_FAILD_COUNT++;
                }
                Configurations.Iterator_Card_Limit_Enhancement++;

            }

        }

    }
}
