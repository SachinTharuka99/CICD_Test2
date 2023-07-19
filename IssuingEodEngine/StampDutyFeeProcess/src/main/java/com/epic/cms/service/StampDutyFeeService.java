package com.epic.cms.service;

import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.StampDutyBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.StampDutyFeeRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;


@Service
public class StampDutyFeeService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    StampDutyFeeRepo stampDutyFeeRepo;
    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void StampDutyFee(StampDutyBean stampDutyAccountBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            ArrayList<StampDutyBean> statementCardList = null;
            ArrayList<StringBuffer> oldcardnumbers = new ArrayList<>();

            try {
                // creating back end DB Connection
                int startEodID = stampDutyFeeRepo.getStartEodId(stampDutyAccountBean.getAccountNumber());

                statementCardList = stampDutyFeeRepo.getStatementCardList(stampDutyAccountBean.getAccountNumber());
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += statementCardList.size();

                for (StampDutyBean stampDutyCardBean : statementCardList) {
                    try {
                        oldcardnumbers = stampDutyFeeRepo.getOldCardNumbers(stampDutyCardBean.getCardNumber());
                        String inClauseString = CommonMethods.getInClauseString(oldcardnumbers);

                        double totalForeignTxns = stampDutyFeeRepo.getTotalForeignTxns(inClauseString, startEodID);
                        stampDutyCardBean.setForiegnTxnAmount(totalForeignTxns);
                        double stampDutyFee = CommonMethods.calcStampDutyFee(totalForeignTxns, stampDutyCardBean.getPersentage());

                        if (stampDutyFee > 0) {
                            CardFeeBean cardFeeBean = new CardFeeBean();
                            cardFeeBean.setAccNumber(stampDutyAccountBean.getAccountNumber());
                            cardFeeBean.setCardNumber(stampDutyCardBean.getCardNumber());
                            cardFeeBean.setFeeCode(Configurations.STAMP_DUTY_FEE);
                            cardFeeBean.setCurrCode(stampDutyCardBean.getCurrencycode());
                            cardFeeBean.setCrOrDr("DR");
                            cardFeeBean.setCashAmount(stampDutyCardBean.getForiegnTxnAmount());
                            stampDutyFeeRepo.insertToEODcardFee(cardFeeBean, stampDutyFee, DateUtil.getSqldate(Configurations.EOD_DATE), null);

                            details.put("Account Number", stampDutyAccountBean.getAccountNumber());
                            details.put("Card Number", CommonMethods.cardNumberMask(stampDutyCardBean.getCardNumber()));
                            details.put("Total Overseas Transaction Amount", totalForeignTxns);
                            details.put("Stamp Duty Fee", stampDutyFee);

                            CommonMethods.clearStringBuffer(cardFeeBean.getCardNumber());
                            cardFeeBean = null;
                        }
                        details.put("Process Status", "Passed");
                        successCount.add(1);
                    } catch (Exception e) {
                        throw e;
                    }
                }
            } catch (Exception e) {
                logError.error("Stamp Duty Fee process failed for account " + stampDutyAccountBean.getAccountNumber(), e);
                details.put("Process Status", "Failed");
                failCount.add(1);
            } finally {
                for (StringBuffer sb : oldcardnumbers) {
                    CommonMethods.clearStringBuffer(sb);
                }
                oldcardnumbers = null;

                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
