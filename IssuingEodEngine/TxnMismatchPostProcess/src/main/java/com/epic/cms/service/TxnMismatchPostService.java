package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;

@Service
public class TxnMismatchPostService {

    @Autowired
    LogManager logManager;

    @Autowired
    CommonRepo commonRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_100")
    public void processTxnMismatch(ArrayList<OtbBean> txnList, OtbBean bean, int iterator, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount)  {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            int failedCount = 0;
            try {
                for (OtbBean cardBean : txnList) {

                    cardBean.setCardnumber(commonRepo.getNewCardNumber(cardBean.getCardnumber()));

                    bean.setOtbcredit(Double.parseDouble("0.00"));
                    bean.setOtbcash(Double.parseDouble("0.00"));

                    try {

                        if (cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT)) {
                            cardBean.setOtbcredit(cardBean.getTxnAmount());
                            bean.setOtbcredit(cardBean.getTxnAmount());
                            cardBean.setTmpcredit(cardBean.getTxnAmount());
                            commonRepo.updateCardOtb(cardBean);
                            commonRepo.updateAccountOtb(bean);
                            commonRepo.updateCustomerOtb(bean);
                        } else if (cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_SALE) || cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_ORIGINATOR)) {
                            cardBean.setOtbcredit(cardBean.getTxnAmount());
                            cardBean.setTmpcredit(cardBean.getTxnAmount());
                            bean.setOtbcredit(cardBean.getTxnAmount());
                            commonRepo.updateCardOtb(cardBean);
                            commonRepo.updateAccountOtb(bean);
                            commonRepo.updateCustomerOtb(bean);

                        } else if (cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND)
                                || cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_MONEY_SEND)
                                || cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_MONEY_SEND_REVERSAL)) {
                            cardBean.setOtbcredit(cardBean.getTxnAmount());
                            bean.setOtbcredit(cardBean.getTxnAmount());
                            cardBean.setTmpcredit(cardBean.getTxnAmount());
                            commonRepo.updateCardOtb(cardBean);
                            commonRepo.updateAccountOtb(bean);
                            commonRepo.updateCustomerOtb(bean);
                        } else if (cardBean.getTxntype().equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)) {
                            cardBean.setOtbcredit(cardBean.getTxnAmount());
                            cardBean.setOtbcash(cardBean.getTxnAmount());
                            cardBean.setTmpcredit(cardBean.getTxnAmount());
                            cardBean.setTmpcash(cardBean.getTxnAmount());
                            bean.setOtbcredit(cardBean.getTxnAmount());
                            bean.setOtbcash(cardBean.getTxnAmount());
                            commonRepo.updateCardOtb(cardBean);
                            commonRepo.updateAccountOtb(bean);
                            commonRepo.updateCustomerOtb(bean);
                        }

                        if (txnList.size() == iterator) {

                            for (OtbBean card : txnList) {
                                details.put("Customer ID", bean.getCustomerid());
                                details.put("Account Number", bean.getAccountnumber());
                                details.put("Card Number", CommonMethods.cardNumberMask(card.getCardnumber()));
                                details.put("Transaction Type Code", card.getTxntype());
                                details.put("Transaction Description", card.getTxntypedesc());
                                details.put("Transaction Mismatch Amount", card.getTxnAmount());
                            }
                        }

                        successCount.add(1);
                        //Configurations.PROCESS_SUCCESS_COUNT++;
                    } catch (Exception ex) {
                        Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardBean.getCardnumber(), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                        logError.error("Transaction mismatch post process failed for account " + bean.getAccountnumber(), ex);
                        failCount.add(1);
                    }
                }
            } catch (Exception e) {
                logError.error("Transaction mismatch post process failed", e);
            } finally {
               logInfo.info(logManager.logDetails(details));
            }
            Configurations.failedCount_TxnMisMatchProcess = failedCount;
        }
    }
}

