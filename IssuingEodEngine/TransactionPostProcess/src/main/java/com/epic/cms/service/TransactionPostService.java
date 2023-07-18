/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:55 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.TransactionPostDao;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;


@Service
public class TransactionPostService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    TransactionPostDao transactionPostDao;
    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void transactionList(OtbBean bean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            ArrayList<OtbBean> txnList;
            LinkedHashMap details = new LinkedHashMap();
            try {
                txnList = transactionPostDao.getTxnAmount(bean.getAccountnumber());
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += txnList.size();
                int iterator = 1;

                cards:
                for (OtbBean cardBean : txnList) {

                    try {

                        cardBean.setCardnumber(commonRepo.getNewCardNumber(cardBean.getCardnumber()));
                        /** payments */
                        if (cardBean.getPayment() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() - cardBean.getPayment());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() - cardBean.getPayment());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() - cardBean.getPayment());
                            transactionPostDao.updateCardTemp(cardBean.getCardnumber(), cardBean.getPayment());
                        }
                        /** sale and mvisa originator tran */
                        if (cardBean.getSale() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() + cardBean.getSale());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() + cardBean.getSale());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() + cardBean.getSale());
                        }
                        /** cash advance */
                        if (cardBean.getCashadavance() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() + cardBean.getCashadavance());
                            bean.setOtbcash(bean.getOtbcash() + cardBean.getCashadavance());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() + cardBean.getCashadavance());
                            cardBean.setOtbcash(cardBean.getCashadavance());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() + cardBean.getCashadavance());
                            cardBean.setTmpcash(cardBean.getCashadavance());
                        }
                        /** easy payment reversal */
                        if (cardBean.getEasypayrev() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() - cardBean.getEasypayrev());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() - cardBean.getEasypayrev());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() - cardBean.getEasypayrev());
                        }
                        /** easy payment */
                        if (cardBean.getEasypay() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() + cardBean.getEasypay());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() + cardBean.getEasypay());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() + cardBean.getEasypay());
                        }
                        /** easy payment fee */
                        if (cardBean.getEasypayfee() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() + cardBean.getEasypayfee());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() + cardBean.getEasypayfee());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() + cardBean.getEasypayfee());
                        }
                        /** if mvisa refund transaction */
                        if (cardBean.getMvisaRefund() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() - cardBean.getMvisaRefund());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() - cardBean.getMvisaRefund());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() - cardBean.getMvisaRefund());
                            cardBean.setAccountnumber(bean.getAccountnumber());
                            cardBean.setCustomerid(bean.getCustomerid());

                            /** update online side tempcredit, otb(card,account,customer) since online only put a tran
                             data to online tran table for mvisa refund*/
                            transactionPostDao.updateCardTemp(cardBean.getCardnumber(), cardBean.getMvisaRefund());

                            transactionPostDao.updateCardOtbCredit(cardBean);
                            transactionPostDao.updateAccountOtbCredit(cardBean);
                            transactionPostDao.updateCustomerOtbCredit(cardBean);
                        }
                        /** reversal */
                        if (cardBean.getReversal() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() - cardBean.getReversal());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() - cardBean.getReversal());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() - cardBean.getReversal());
                        }
                        /** refund */
                        if (cardBean.getRefund() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() - cardBean.getRefund());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() - cardBean.getRefund());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() - cardBean.getRefund());
                        }
                        /** if mastercard money send transaction,*/
                        if (cardBean.getMoneysend() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() - cardBean.getMoneysend());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() - cardBean.getMoneysend());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() - cardBean.getMoneysend());

                            /**update online side tempcredit since online only adjust otb */
                            transactionPostDao.updateCardTemp(cardBean.getCardnumber(), cardBean.getMoneysend());

                        }
                        /** if mastercard money send reversal transaction,*/
                        if (cardBean.getMoneysendreversal() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() + cardBean.getMoneysendreversal());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() + cardBean.getMoneysendreversal());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() + cardBean.getMoneysendreversal());

                            //update online side tempcredit since online only adjust otb
                            transactionPostDao.updateCardTemp(cardBean.getCardnumber(), cardBean.getMoneysendreversal() * (-1));

                        }
                        /** aft tran */
                        if (cardBean.getAft() > 0) {
                            bean.setOtbcredit(bean.getOtbcredit() + cardBean.getAft());
                            cardBean.setOtbcredit(cardBean.getOtbcredit() + cardBean.getAft());
                            cardBean.setTmpcredit(cardBean.getTmpcredit() + cardBean.getAft());
                        }
                        /** update temp credit,temp cash, otb credit, otb cash*/
                        transactionPostDao.updateCardByPostedTransactions(cardBean);

                        /** update eodcardbalance */
                        transactionPostDao.updateEODCARDBALANCEByTxn(cardBean);

                        if (txnList.size() == iterator) {
                            transactionPostDao.updateEODTRANSACTION(bean.getAccountnumber());
                            //update account and customer tables
                            transactionPostDao.updateAccountOtb(bean);
                            transactionPostDao.updateCustomerOtb(bean);

                            for (OtbBean card : txnList) {
                                details.put("Customer ID", bean.getCustomerid());
                                details.put("Account Number", bean.getAccountnumber());
                                details.put("Card Number", CommonMethods.cardNumberMask(card.getCardnumber()));
                                details.put("Payment Amount ", card.getPayment());
                                details.put("Sale Amount", card.getSale());
                                details.put("Cash Advance Amount", card.getCashadavance());
                                details.put("Installment Reversal Amount", card.getEasypayrev());
                                details.put("Installment Amount", card.getEasypay());
                                details.put("Installment Process Fee Amount", card.getEasypayfee());
                                details.put("Refund", card.getRefund());
                                details.put("Reversal", card.getReversal());
                                details.put("mVisa Refund", card.getMvisaRefund());
                                details.put("Aft Amount", card.getAft());
                            }
                        }
                        successCount.add(1);
                        //Configurations.PROCESS_SUCCESS_COUNT++;

                    } catch (Exception ex) {
                        Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardBean.getCardnumber()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                        logError.error("Transaction post process failed for account " + bean.getAccountnumber(), ex);
                        //Configurations.PROCESS_FAILD_COUNT++;
                        failCount.add(1);
                        break;
                    }
                    iterator++;
                }
            } catch (Exception e) {
                logError.error("Transaction post process failed for account " + bean.getAccountnumber(), e);
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
