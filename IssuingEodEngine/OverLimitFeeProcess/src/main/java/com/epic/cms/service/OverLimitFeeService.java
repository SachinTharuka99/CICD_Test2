package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;


@Service
public class OverLimitFeeService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addOverLimitFee(String accNumber, StringBuffer cardNumber, ProcessBean processBean, String processHeader, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            String maskedCardNumber = CommonMethods.cardNumberMask(cardNumber);
            try {
                details.put("Card Number", maskedCardNumber);
                details.put("Over limit fee", "Added");

                //add fee count
                commonRepo.addCardFeeCount(cardNumber, Configurations.OVER_LIMIT_FEE, 0);
                details.put("Process Status", "Passed");
                successCount.add(1);
                //Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                failCount.add(1);
               // Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardNumber), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                logError.error("OverLimit Fee process failed for cardNumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                details.put("Process Status", "Failed");

            } finally {
                logInfo.info(logManager.logDetails(details));
            }
            CommonMethods.clearStringBuffer(cardNumber);
        }
    }
}
