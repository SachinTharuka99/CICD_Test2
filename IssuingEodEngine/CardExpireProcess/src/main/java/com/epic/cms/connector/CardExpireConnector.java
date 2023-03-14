package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.CardBean;
import com.epic.cms.repository.CardExpireRepo;
import com.epic.cms.service.CardExpireService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class CardExpireConnector extends ProcessBuilder {

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CardExpireService cardExpireService;

    @Autowired
    CardExpireRepo cardExpireRepo;

    private ArrayList<CardBean> expiredCardList = new ArrayList<>();


    @Override
    public void concreteProcess() throws Exception {
        /**
         * Get the card list where EXPIRY DATE is less than the eod date and the
         * status are not in expired, closed as well as card replaced
         */
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CARD_EXPIRE;
            CommonMethods.eodDashboardProgressParametersReset();

            expiredCardList = cardExpireRepo.getExpiredCardList();
            if (expiredCardList != null) {
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = expiredCardList.size();

                for (CardBean cardBean : expiredCardList) {
                    cardExpireService.processCardExpire(cardBean);
                }
                //wait till all the threads are completed
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
            }
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = expiredCardList.size();
            Configurations.PROCESS_SUCCESS_COUNT = (Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - Configurations.PROCESS_FAILD_COUNT);

        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw ex;
        } finally {
            //addSummaries();
            //infoLogger.info(logManager.processSummeryStyles(summery));
            try {
                if (expiredCardList != null && expiredCardList.size() != 0) {
                    /** PADSS Change -
                     variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                    for (CardBean cardBean : expiredCardList) {
                        CommonMethods.clearStringBuffer(cardBean.getCardnumber());
                        CommonMethods.clearStringBuffer(cardBean.getMainCardNo());
                    }
                }
            } catch (Exception e) {
//                errorLogger.error("Exception ", e);
                LogManager.logError(e,errorLogger);
            }
        }
    }

    public void addSummaries() {
        summery.put("Number of cards to expired ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Number of success expired ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Number of failure expired ", Configurations.PROCESS_FAILD_COUNT);
    }
}
