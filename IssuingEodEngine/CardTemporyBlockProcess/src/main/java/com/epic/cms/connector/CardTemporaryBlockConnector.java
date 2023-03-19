package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CardTemporaryBlockService;
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
public class CardTemporaryBlockConnector extends ProcessBuilder {

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CardBlockRepo cardTemporaryBlockRepo;

    @Autowired
    CardTemporaryBlockService cardTemporaryBlockService;

    @Autowired
    LogManager logManager;

    ArrayList<BlockCardBean> cardList = null;
    ProcessBean processBean = new ProcessBean();
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARD_TEMPORARY_BLOCK;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_CARD_TEMPORARY_BLOCK);

            if (processBean != null) {
                Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK = cardTemporaryBlockRepo.getBlockTheshholdPeriod("TEMPORARYBLKTHRESHOLD");
                cardList = cardTemporaryBlockRepo.getCardListFromMinPayment(Configurations.EOD_PENDING_STATUS, Configurations.NO_OF_MONTHS_FOR_TEMPORARY_BLOCK);

                if (cardList != null && cardList.size() > 0) {
                    for (BlockCardBean blockCardBean : cardList) {
                        cardTemporaryBlockService.processCardTemporaryBlock(blockCardBean, processBean);
                    }
                }
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                failedCount = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardList.size();
                Configurations.PROCESS_SUCCESS_COUNT = (cardList.size() - failedCount);
                Configurations.PROCESS_FAILD_COUNT = failedCount;
            }
        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw ex;
        } finally {
            logManager.logSummery(summery, infoLogger);
            try {
                /** PADSS Change -
                 variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                if (cardList != null && cardList.size() != 0) {
                    for (BlockCardBean temporyBlockCardBean : cardList) {
                        CommonMethods.clearStringBuffer(temporyBlockCardBean.getCardNo());
                    }
                    cardList = null;
                }
            } catch (Exception e2) {
                logManager.logError("Card Temporary Block process Error ", e2, errorLogger);
            }
        }
    }

    public void addSummaries() {
        if (cardList != null) {
            summery.put("Started Date", Configurations.EOD_DATE.toString());
            summery.put("No of Card effected", cardList.size());
            summery.put("No of Success Card", cardList.size() - failedCount);
            summery.put("No of fail Card", failedCount);
        } else {
            summery.put("No of Card effected", 0);
            summery.put("No of Success Card", 0);
            summery.put("No of fail Card", 0);
        }
    }
}
