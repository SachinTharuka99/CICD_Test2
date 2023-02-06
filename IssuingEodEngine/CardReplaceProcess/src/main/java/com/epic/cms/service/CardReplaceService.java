package com.epic.cms.service;

import com.epic.cms.model.bean.CardReplaceBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.repository.CardReplaceRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class CardReplaceService {

    @Autowired
    LogManager logManager;

    @Autowired
    CardReplaceRepo cardReplaceRepo;

    @Autowired
    StatusVarList status;

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void cardReplace(CardReplaceBean cardReplaceBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                details.put("replacing card", CommonMethods.cardNumberMask(cardReplaceBean.getOldCardNo()));
                cardReplaceRepo.updateBackendOldCardFromNewCard(cardReplaceBean);
                details.put("new card", CommonMethods.cardNumberMask(cardReplaceBean.getNewCardNo()));

                /** CARD_REPLACE ( update status as EDON) */
                cardReplaceRepo.updateCardReplaceStatus(cardReplaceBean.getNewCardNo());
                /**
                 * For online db, Update the ECMS_ONLINE_CARD_BLOCK
                 * table card numbers with new card numbers *
                 */
                cardReplaceRepo.updateOnlineOldCardFromNewCard(cardReplaceBean);

                Statusts.SUMMARY_FOR_CARDREPLACE_PROCESSED++;
                Configurations.PROCESS_SUCCESS_COUNT++;
                infoLogger.info(logManager.processDetailsStyles(details));
                details.clear();

            } catch (Exception e) {
                errorLogger.error("Card Replace Process Error for Card - " + CommonMethods.cardNumberMask(cardReplaceBean.getOldCardNo()), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardReplaceBean.getOldCardNo()), e.getMessage(), Configurations.PROCESS_ID_CARD_REPLACE, "Card Replace", 0, CardAccount.CARD));
                Configurations.PROCESS_FAILD_COUNT++;
            }
        }
    }
}
