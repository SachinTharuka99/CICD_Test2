package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.CardFeeDao;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.service.CardFeeService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class CardFeeConnector extends ProcessBuilder {

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CardFeeService cardFeeService;

    @Autowired
    CardFeeDao cardFeeDao;

    @Override
    public void concreteProcess() throws Exception {
//        System.out.println("this is from Adjustment Process concreteProcess()");

//        LinkedHashMap summery = new LinkedHashMap();

        Configurations.PROCESS_SUCCESS_COUNT = 0;
        Configurations.PROCESS_FAILD_COUNT = 0;

        List<CardFeeBean> cardRecordList = new ArrayList<CardFeeBean>();
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CARD_FEE;
            Configurations.PROCESS_STEP_ID = 50;
            CommonMethods.eodDashboardProgressParametersReset();

            //get the card list
            cardRecordList = cardFeeDao.getCardFeeCountList();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardRecordList.size();

            if (cardRecordList != null && cardRecordList.size() > 0) {
                for (CardFeeBean cardBean : cardRecordList) {
                    cardFeeService.cardFeeCalculate(cardBean);
                }
            } else {
                summery.put("Fee not found", 0 + "");
            }
            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardRecordList.size();
        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw ex;
        } finally {
//            infoLogger.info(logManager.processDetailsStyles(summery));
            /** PADSS Change -
             variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
            for (CardFeeBean cardFeeBean : cardRecordList) {
                CommonMethods.clearStringBuffer(cardFeeBean.getCardNumber());
            }
            cardRecordList = null;
        }

    }

    @Override
    public void addSummaries() {
        summery.put("Number of accounts to fee post ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Number of success fee post ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - Configurations.PROCESS_FAILD_COUNT);
        summery.put("Number of failure fee post ", Configurations.PROCESS_FAILD_COUNT);
    }
}
