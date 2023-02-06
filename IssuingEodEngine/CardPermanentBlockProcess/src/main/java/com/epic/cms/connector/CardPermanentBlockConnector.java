package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CardPermanentBlockService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CardPermanentBlockConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CardPermanentBlockService cardPermanentBlockService;

    @Autowired
    CardBlockRepo cardPermanentBlockRepo;

    ArrayList<BlockCardBean> cardList = null;
    ProcessBean processBean = new ProcessBean();
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARD_PERMENANT_BLOCK;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_CARD_PERMENANT_BLOCK);

            if (processBean != null) {
                Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK = cardPermanentBlockRepo.getBlockTheshholdPeriod("PERMENANTBLKTHRESHOLD");
                cardList = cardPermanentBlockRepo.getCardListFromMinPayment(statusList.getCARD_TEMPORARY_BLOCK_Status(), Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK); //CATB

                if (cardList != null && cardList.size() > 0) {

                    for (BlockCardBean blockCardBean : cardList) {
                        cardPermanentBlockService.processCardPermanentBlock(blockCardBean, processBean);
                    }
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
                infoLogger.info("Thread Name Prefix: {}, Active count: {}, Pool size: {}, Queue Size: {}", taskExecutor.getThreadNamePrefix(), taskExecutor.getActiveCount(), taskExecutor.getPoolSize(), taskExecutor.getThreadPoolExecutor().getQueue().size());

                failedCount = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
                Configurations.PROCESS_SUCCESS_COUNT = (cardList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = failedCount;

            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Card Permanent Block process Error", e);
        } finally {
            addSummaries();
            infoLogger.info(logManager.processSummeryStyles(summery));
            try {
                /* PADSS Change -
                variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                if (cardList != null && cardList.size() != 0) {
                    for (BlockCardBean blockCardBean : cardList) {
                        CommonMethods.clearStringBuffer(blockCardBean.getCardNo());
                    }
                    cardList = null;
                }
            } catch (Exception e2) {
                errorLogger.error("Card Permanent Block process Error ", e2);
            }
        }
    }

    public void addSummaries() {
        if (cardList != null) {
            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("Number of transaction to sync", cardList.size());
            summery.put("Number of success transaction", cardList.size() - failedCount);
            summery.put("Number of failure transaction", failedCount);
        } else {
            summery.put("Number of transaction to sync", 0);
            summery.put("Number of success transaction", 0);
            summery.put("Number of failure transaction", 0);
        }
    }
}
