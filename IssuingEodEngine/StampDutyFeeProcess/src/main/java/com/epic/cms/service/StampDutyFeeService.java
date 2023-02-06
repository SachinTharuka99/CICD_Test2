package com.epic.cms.service;

import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.StampDutyBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.StampDutyFeeRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class StampDutyFeeService {

    @Autowired
    LogManager logManager;

    @Autowired
    StampDutyFeeRepo stampDutyFeeRepo;

    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void StampDutyFee(StampDutyBean stampDutyAcoountBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            ArrayList<StampDutyBean> statementCardList = null;

            try {
                try {
                    ArrayList<StringBuffer> oldcardnumbers = new ArrayList<>();

                    // creating back end DB Connection
                    int startEodID = stampDutyFeeRepo.getStartEodId(stampDutyAcoountBean.getAccountNumber());

                    statementCardList = stampDutyFeeRepo.getStatementCardList(stampDutyAcoountBean.getAccountNumber());
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += statementCardList.size();

                    cards:
                    for (StampDutyBean stampDutyCardBean : statementCardList) {
                        try {
                            oldcardnumbers = stampDutyFeeRepo.getOldCardNumbers(stampDutyCardBean.getCardNumber());
                            String inClauseString = CommonMethods.getInClauseString(oldcardnumbers);

                            double totalForeignTxns = stampDutyFeeRepo.getTotalForeignTxns(inClauseString, startEodID);
                            stampDutyCardBean.setForiegnTxnAmount(totalForeignTxns);
                            double stampDutyFee = CommonMethods.calcStampDutyFee(totalForeignTxns, stampDutyCardBean.getPersentage());

                            if (stampDutyFee > 0) {
                                CardFeeBean cardFeeBean = new CardFeeBean();
                                cardFeeBean.setAccNumber(stampDutyAcoountBean.getAccountNumber());
                                cardFeeBean.setCardNumber(stampDutyCardBean.getCardNumber());
                                cardFeeBean.setFeeCode(Configurations.STAMP_DUTY_FEE);
                                cardFeeBean.setCurrCode(stampDutyCardBean.getCurrencycode());
                                cardFeeBean.setCrOrDr("DR");
                                cardFeeBean.setCashAmount(stampDutyCardBean.getForiegnTxnAmount());
                                stampDutyFeeRepo.insertToEODcardFee(cardFeeBean, stampDutyFee, DateUtil.getSqldate(Configurations.EOD_DATE), null);

                                details.put("Account Number", stampDutyAcoountBean.getAccountNumber());
                                details.put("Card Number", CommonMethods.cardNumberMask(stampDutyCardBean.getCardNumber()));
                                details.put("Total Overseas Transaction Amount", totalForeignTxns);
                                details.put("Stamp Duty Fee", stampDutyFee);
                                infoLogger.info(LogManager.processDetailsStyles(details));

                                CommonMethods.clearStringBuffer(cardFeeBean.getCardNumber());
                                cardFeeBean = null;
                            }
                            details.put("Process Status", "Passed");
                            Configurations.PROCESS_SUCCESS_COUNT++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            errorLogger.error("Stampduty Fee process failed for account " + stampDutyAcoountBean.getAccountNumber(), e);
                            details.put("Process Status", "Failed");
                            Configurations.PROCESS_FAILD_COUNT++;
                            break cards;
                        } finally {
                            for (StringBuffer sb : oldcardnumbers) {
                                CommonMethods.clearStringBuffer(sb);
                            }
                            oldcardnumbers = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errorLogger.error("Stampduty Fee process failed for account " + stampDutyAcoountBean.getAccountNumber(), e);
                    details.put("Process Status", "Failed");
                    Configurations.PROCESS_FAILD_COUNT++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLogger.error("Stampduty Fee process thread: exception in connection borrowing" + ex);
                details.put("Process Status", "Failed");
                Configurations.PROCESS_FAILD_COUNT++;
            }
            infoLogger.info(LogManager.processDetailsStyles(details));
        }
    }
}
