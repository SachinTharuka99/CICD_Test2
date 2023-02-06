package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class OverLimitFeeService {

    @Autowired
    LogManager logManager;

    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addOverLimitFee(String accNumber, StringBuffer cardNumber, ProcessBean processBean, String processHeader) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            String maskedCardNumber = CommonMethods.cardNumberMask(cardNumber);
            try {
                details.put("Card Number", maskedCardNumber);
                details.put("Over limit fee", "Added");

                //add fee count
                commonRepo.addCardFeeCount(cardNumber, Configurations.OVER_LIMIT_FEE, 0);

                details.put("Process Status", "Passed");
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardNumber), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                infoLogger.info("OverLimit Fee process failed for cardNumber " + CommonMethods.cardInfo(maskedCardNumber, processBean));
                errorLogger.error("OverLimit Fee process failed for cardNumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                details.put("Process Status", "Failed");

            }
            infoLogger.info(logManager.processDetailsStyles(details));
            CommonMethods.clearStringBuffer(cardNumber);
        }
    }
}
