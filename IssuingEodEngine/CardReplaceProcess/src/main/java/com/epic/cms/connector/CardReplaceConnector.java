package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.CardReplaceBean;
import com.epic.cms.repository.CardReplaceRepo;
import com.epic.cms.service.CardReplaceService;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class CardReplaceConnector extends ProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    CardReplaceService cardReplaceService;
    @Autowired
    CardReplaceRepo cardReplaceRepo;
    @Autowired
    StatusVarList status;
    @Autowired
    LogManager logManager;

    @Override
    public void concreteProcess() throws Exception {
        List<CardReplaceBean> cardListToReplace = new ArrayList<>();
        try {
            cardListToReplace = cardReplaceRepo.getCardListToReplace();
            Statusts.SUMMARY_FOR_CARDREPLACE =0;
            Statusts.SUMMARY_FOR_CARDREPLACE = cardListToReplace.size();
            summery.put("No of cards to be replaced", cardListToReplace.size() + "");

            if (cardListToReplace != null) {
                CommonMethods.eodDashboardProgressParametersReset();
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARD_REPLACE;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardListToReplace.size();

                //iterate card list one by one
                cardListToReplace.forEach(cardReplaceBean -> {
                    cardReplaceService.cardReplace(cardReplaceBean,Configurations.successCount,Configurations.failCount);
                });

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = Statusts.SUMMARY_FOR_CARDREPLACE;
                Configurations.PROCESS_SUCCESS_COUNT = Statusts.SUMMARY_FOR_CARDREPLACE_PROCESSED;
                Configurations.PROCESS_FAILD_COUNT = Statusts.SUMMARY_FOR_CARDREPLACE - Statusts.SUMMARY_FOR_CARDREPLACE_PROCESSED;
            }
            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                updateEodEngineDashboardProcessProgress();
                Thread.sleep(1000);
            }

        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw ex;
        } finally {
            try {
                if (cardListToReplace != null && cardListToReplace.size() != 0) {
                    /* variables handling card data should be nullified
                    by replacing the value of variable with zero and call NULL function */
                    for (CardReplaceBean cardReplaceBean : cardListToReplace) {
                        CommonMethods.clearStringBuffer(cardReplaceBean.getNewCardNo());
                        CommonMethods.clearStringBuffer(cardReplaceBean.getOldCardNo());
                    }
                    cardListToReplace = null;
                }
            } catch (Exception e) {
                logError.error("Failed Card Replace Process", e);
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Total no of cards to be replaced", Statusts.SUMMARY_FOR_CARDREPLACE);
        summery.put("Cards replaced", Configurations.successCount.size());
        summery.put("No of Success Card",Configurations.successCount.size());
        summery.put("Total Fails", Configurations.failCount.size());
    }
}
