package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.LimitIncrementBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.IncrementLimitExpireRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class IncrementLimitExpireService {

    @Autowired
    StatusVarList status;

    @Autowired
    IncrementLimitExpireRepo incrementLimitExpireRepo;

    @Autowired
    LogManager logManager;

    @Async("taskExecutor2")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void processCreditLimitExpire(LimitIncrementBean limitIncrementBean, ProcessBean processBean, int configProcess, String processHeader) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            String maskedCardNumber = CommonMethods.cardNumberMask(limitIncrementBean.getCardNumber());
            int failedCards = 0;
            int count = 0;
            try {
                details.put("Card Number", maskedCardNumber);
                // check credit increment
                if (limitIncrementBean.getIncrementType().equals(Configurations.CREDIT_INCREMENT)) {
                    details.put("Increment/Decrement Type", Configurations.CREDIT_INCREMENT);
                    details.put("Increment or Decremet", limitIncrementBean.getIncordec());
                    details.put("Amount", limitIncrementBean.getIncrementAmount());
                    //update card table
                    count = incrementLimitExpireRepo.expireCreditLimit(limitIncrementBean);
                    //update online card table
                    count = incrementLimitExpireRepo.expireOnlineCreditLimit(limitIncrementBean);
                    //check for the catd category
                    if (limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_MAIN)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_FD)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_AFFINITY)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                        //expire on account
                        incrementLimitExpireRepo.limitExpireOnAccount(limitIncrementBean);
                        incrementLimitExpireRepo.limitOnlineExpireOnAccount(limitIncrementBean);
                        //expire on customer
                        incrementLimitExpireRepo.limitExpireOnCustomer(limitIncrementBean);
                        incrementLimitExpireRepo.limitOnlineExpireOnCustomer(limitIncrementBean);
                    }
                }
                // check cash increment
                if (limitIncrementBean.getIncrementType().equals(Configurations.CASH_INCREMENT)) {
                    details.put("Increment/Decrement Type", Configurations.CASH_INCREMENT);
                    details.put("Increment or Decremet", limitIncrementBean.getIncordec());
                    details.put("Amount", limitIncrementBean.getIncrementAmount());
                    //update card table
                    count = incrementLimitExpireRepo.expireCashLimit(limitIncrementBean);
                    //update online card table
                    count = incrementLimitExpireRepo.expireOnlineCashLimit(limitIncrementBean);
                    if (limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_MAIN)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_FD)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_AFFINITY)
                            || limitIncrementBean.getCardcategorycode().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                        //expire on account
                        incrementLimitExpireRepo.cashLimitExpireOnAccount(limitIncrementBean);
                        incrementLimitExpireRepo.cashLimitOnlineExpireOnAccount(limitIncrementBean);
                        //expire on customer
                        incrementLimitExpireRepo.cashLimitExpireOnCustomer(limitIncrementBean);
                        incrementLimitExpireRepo.cashLimitOnlineExpireOnCustomer(limitIncrementBean);
                    }
                }
                //update templimitincrement table
                count = incrementLimitExpireRepo.updateTempLimitIncrementTable(limitIncrementBean.getCardNumber(), status.getCREDIT_LIMIT_ENHANCEMENT_EXPIRED(), limitIncrementBean.getRequestid(), Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE);

                details.put("Process Status", "Passed");
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                details.put("Process Status", "Failed");
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(limitIncrementBean.getCardNumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                failedCards++;
                Configurations.PROCESS_FAILD_COUNT++;
                logManager.logInfo("Increment Limit Expire process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), infoLogger);
                logManager.logError("Increment Limit Expire process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e, errorLogger);
            }
            logManager.logDetails(details, infoLogger);
            Configurations.Failed_Count_IncrementLimit += failedCards;
        }
    }
}
