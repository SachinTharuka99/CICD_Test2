package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.RunnableFeeDao;
import com.epic.cms.model.bean.CardBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.RunnableFeeService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.epic.cms.util.Configurations.FAILED_CARDS;

@Service
public class RunnableFeeConnector extends ProcessBuilder {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    RunnableFeeDao runnableFeeDao;
    @Autowired
    RunnableFeeService runnableFeeService;

    @Override
    public void concreteProcess() throws Exception {
        System.out.println("this is from EOD Runnable Fee Process concreteProcess()");

        LinkedHashMap summery = new LinkedHashMap();

        Configurations.SUMMARY_FOR_FEE_ANNIVERSARY_PROCESSED = 0;
        Configurations.SUMMARY_FOR_FEE_ANNIVERSARY = 0;
        Configurations.SUMMARY_FOR_FEE_CASHADVANCES = 0;
        Configurations.SUMMARY_FOR_FEE_LATEPAYMENTS = 0;
        Configurations.FAILED_CARDS = 0;

        List<CardBean> cardList = new ArrayList<>();
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_EOD_RUNNABLE_FEE;
            CommonMethods.eodDashboardProgressParametersReset();
            cardList = runnableFeeDao.getAllActiveCards();

            summery.put("No of cards to check for EOD fee", cardList.size() + "");
//            for (CardBean cardBean : cardList) {
//                runnableFeeService.addRunnableFees(cardBean);
//            }

            cardList.forEach(cardBean -> {
                runnableFeeService.addRunnableFees(cardBean);
            });

            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
            Configurations.PROCESS_SUCCESS_COUNT = ((Configurations.SUMMARY_FOR_FEE_ANNIVERSARY_PROCESSED + Configurations.SUMMARY_FOR_FEE_CASHADVANCES + Configurations.SUMMARY_FOR_FEE_LATEPAYMENTS) - (Configurations.FAILED_CARDS));
            Configurations.PROCESS_FAILD_COUNT = (Configurations.FAILED_CARDS);
            //Configurations.PROCESS_FAILD_COUNT = faileCardCount.get();

        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logError.error("Errors occurred while checking fee", ex);
        } finally {
            //logInfo.info(logManager.logSummery(summery));
            /* PADSS Change -
               variables handling card data should be nullified
               by replacing the value of variable with zero and call NULL function */
            for (CardBean cardBean : cardList) {
                CommonMethods.clearStringBuffer(cardBean.getCardnumber());
                CommonMethods.clearStringBuffer(cardBean.getMainCardNo());
            }
            cardList = null;
        }

    }

    @Override
    public void addSummaries() {
        summery.put("Anniversary Date Fee Checked for cards", Configurations.SUMMARY_FOR_FEE_ANNIVERSARY + "");
        summery.put("Anniversary Date Fee processed", Configurations.SUMMARY_FOR_FEE_ANNIVERSARY_PROCESSED + "");
        summery.put("Cash Advance fee processed", Configurations.SUMMARY_FOR_FEE_CASHADVANCES + "");
        summery.put("Late Payment fee processed", Configurations.SUMMARY_FOR_FEE_LATEPAYMENTS + "");
        summery.put("Failed No of cards", Configurations.FAILED_CARDS + "");
    }
}
