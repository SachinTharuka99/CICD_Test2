package com.epic.cms.service;

import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CardExpireRepo;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

@Service
public class CardExpireService {

    @Autowired
    CardBlockRepo cardBlockRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CardExpireRepo cardExpireRepo;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processCardExpire(CardBean cardBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                String cardStatus = cardBean.getCardStatus();

                /**set card status to expire in back end DB*/
                cardExpireRepo.setCardStatusToExpire(cardBean.getCardnumber());

                /**set card status to expire in online DB*/
                cardExpireRepo.setOnlineCardStatusToExpire(cardBean.getCardnumber());

                /**de-activate existing data for the particular card number*/
                cardBlockRepo.deactivateCardBlock(cardBean.getCardnumber());

                /**insert card status change in back end DB*/
                cardExpireRepo.insertToCardBlock(cardBean.getCardnumber(), cardStatus);

                /**insert card status change in online DB*/
                cardBlockRepo.insertToOnlineCardBlock(cardBean.getCardnumber(), statusList.getONLINE_CARD_EXPIRED_STATUS());

                details.put("Card Number", CommonMethods.cardNumberMask(cardBean.getCardnumber()));
                details.put("Expire date", cardBean.getExpiryDate());
                details.put("Old Status", cardBean.getCardStatus());
                details.put("New Status", statusList.getCARD_EXPIRED_STATUS());

            } catch (Exception e) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardBean.getCardnumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                logError.error("Card expire process failed for card number " + CommonMethods.cardNumberMask(cardBean.getCardnumber()), e);
                Configurations.PROCESS_FAILED_COUNT.set(Configurations.PROCESS_FAILED_COUNT.getAndIncrement());
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
