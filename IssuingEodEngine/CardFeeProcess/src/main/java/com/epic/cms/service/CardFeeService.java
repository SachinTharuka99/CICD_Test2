package com.epic.cms.service;

import com.epic.cms.dao.CardFeeDao;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CardFeeService {

    @Autowired
    public StatusVarList status;

    @Autowired
    public CardFeeDao cardFeeDao;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void cardFeeCalculate(CardFeeBean cardBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap detail = new LinkedHashMap();
            try {
                CardFeeBean cardFeeBean = cardFeeDao.getCardFeeCountForCard(cardBean.getCardNumber(), cardBean.getAccNumber(), cardBean.getFeeCode());
                double perc_amount = cardFeeBean.getPercentageAmount() * cardBean.getCashAmount() / 100;
                //Add effective date.
                double fee = cardFeeBean.getFlatFee();
                String combination = cardFeeBean.getCombination();
                double amount = CommonMethods.getAmountFromCombination(perc_amount, fee * cardFeeBean.getFeeCount(), combination);
                if (amount >= cardFeeBean.getMaxAmount()) {
                    amount = cardFeeBean.getMaxAmount();
                } else if (amount <= cardFeeBean.getMinAmount()) {
                    amount = cardFeeBean.getMinAmount();
                }

                //Update the fee amount X count
                if (cardBean.getFeeCount() == 0) {
                    cardBean.setFeeCount(cardFeeBean.getFeeCount());
                }

                detail.put("card", CommonMethods.cardNumberMask(cardBean.getCardNumber()));
                detail.put("fee type", cardBean.getFeeCode());
                detail.put("fee count", cardBean.getFeeCount());
                detail.put("flat fee", fee);
                detail.put("fee MIN/MAX/CMB", combination);
                detail.put("final amount", amount);

                try {
                    Date effectDate = CommonMethods.getSqldate(Configurations.EOD_DATE);
                    if (cardBean.getFeeCode().equals(Configurations.LATE_PAYMENT_FEE)) {
                        Date nextBillingDate = cardFeeDao.getNextBillingDateForCard(cardBean.getCardNumber());
                        effectDate = nextBillingDate;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        if (effectDate.toString().equals(sdf.format(Configurations.EOD_DATE))) {
                            cardFeeDao.insertToEODCardFee(cardFeeBean, amount, effectDate);
                            cardFeeDao.updateCardFeeCount(cardFeeBean);
                            //according to NP CR, accrued interest in each cycle will be updated with delenquebtaccount table accruedfees field.
                            //this will used to segregate np fee gl & normal fee gl.payment will be setoff with those fields.
                            if (cardBean.getAccStatus().equalsIgnoreCase(status.getACCOUNT_NON_PERFORMING_STATUS())) {
                                cardFeeDao.updateDELINQUENTACCOUNTNpDetails(0.0, 0.0, amount, 0.0, cardBean.getAccNumber());
                            }
                            //Statusts.SUMMARY_FOR_FEE_UPDATE++;
                        }
                    } else { //Can add else if statements here if needed to insert data on effective date only!
                        cardFeeDao.insertToEODCardFee(cardFeeBean, amount, effectDate);
                        cardFeeDao.updateCardFeeCount(cardFeeBean);
                        //according to NP CR, accrued interest in each cycle will be updated with delenquebtaccount table accruedfees field.
                        //this will used to segregate np fee gl & normal fee gl. payment will be setoff with those fields.

                        if (cardBean.getAccStatus().equalsIgnoreCase(status.getACCOUNT_NON_PERFORMING_STATUS())) {
                            if (cardBean.getFeeCode().equals(Configurations.OVER_LIMIT_FEE)) {
                                cardFeeDao.updateDELINQUENTACCOUNTNpDetails(0.0, amount, 0.0, 0.0, cardBean.getAccNumber());
                            } else if (!cardBean.getFeeCode().equals(Configurations.LATE_PAYMENT_FEE)) {
                                cardFeeDao.updateDELINQUENTACCOUNTNpDetails(0.0, 0.0, 0.0, amount, cardBean.getAccNumber());
                            }
                        }
                        //Statusts.SUMMARY_FOR_FEE_UPDATE++;
                    }
                    //infoLogger.info(logManager.processDetailsStyles(detail));
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception ex) {
                    //errorLogger.error("Exceptions occurred for: " + CommonMethods.cardNumberMask(cardBean.getCardNumber()), ex);
                    LogManager.logError("Exceptions occurred for: " + CommonMethods.cardNumberMask(cardBean.getCardNumber()), ex, errorLogger);
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardFeeBean.getCardNumber(), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                    Configurations.PROCESS_FAILD_COUNT++;
                }

            } catch (Exception ex) {
                //errorLogger.error("Error occurred while processing card number: " + CommonMethods.cardNumberMask(cardBean.getCardNumber()) + " | " + ex);
                LogManager.logError("Error occurred while processing card number: " + CommonMethods.cardNumberMask(cardBean.getCardNumber()), ex, errorLogger);
                Configurations.PROCESS_FAILD_COUNT++;
            } finally {
                LogManager.logDetails(detail, infoLogger);
            }
        }
    }
}
