package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.KnockOffRepo;
import com.epic.cms.util.*;
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
public class KnockOffService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    LogManager logManager;
    @Autowired
    KnockOffRepo knockOffRepo;
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    CommonRepo commonRepo;

//    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void knockOff(OtbBean custAccBean, ArrayList<OtbBean> cardList, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            ArrayList<OtbBean> paymentList = new ArrayList<OtbBean>();
            int cardIteration = 1;

            cardList = knockOffRepo.getKnockOffCardList(custAccBean.getCustomerid(), custAccBean.getAccountnumber());
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += cardList.size();
            OtbBean mainCardBean = knockOffRepo.getMainCard(custAccBean.getAccountnumber());

            double maineodclosingbalance = 0.00;

            for (OtbBean supCardBean : cardList) {
                try {
                    boolean eom = false;
                    boolean eod = false;
                    int paymentIteration = 1;
                    boolean isFirstIteration = true;
                    double supeodclosingbalance = 0.00;

                    paymentList = knockOffRepo.getPaymentList(supCardBean.getCardnumber());

                    OtbBean eomBean = knockOffRepo.getEomKnockOffAmount(supCardBean.getCardnumber());

                    OtbBean eodBean = knockOffRepo.getEodKnockOffAmount(supCardBean.getCardnumber());

                    if (eomBean != null) {
                        eom = true;
                    }

                    if (eodBean != null) {
                        eod = true;
                    }

                    for (OtbBean paymentBean : paymentList) {

                        double amount = paymentBean.getPayment();
                        double mainFinancialCharges = 0.00;
                        double mainCashAdvances = 0.00;
                        double mainTransactions = 0.00;
                        double supFinancialCharges = 0.00;
                        double supCashAdvances = 0.00;
                        double supTransactions = 0.00;

                        if (eom) {
                            if (eomBean.getFinacialcharges() > 0 && paymentBean.getPayment() >= eomBean.getFinacialcharges()) {
                                // NO == NO
                                if (paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                                    mainCardBean.setOtbcredit(mainCardBean.getOtbcredit() - eomBean.getFinacialcharges());
                                    supCardBean.setOtbcredit(supCardBean.getOtbcredit() + eomBean.getFinacialcharges());
                                    supeodclosingbalance = supeodclosingbalance + eomBean.getFinacialcharges();
                                }

                                mainCardBean.setTmpcredit(mainCardBean.getTmpcredit() - eomBean.getFinacialcharges());
                                mainFinancialCharges = eomBean.getFinacialcharges();
                                paymentBean.setPayment(paymentBean.getPayment() - eomBean.getFinacialcharges());
                                eomBean.setFinacialcharges(0.00);

                            } else if (paymentBean.getPayment() > 0 && paymentBean.getPayment() < eomBean.getFinacialcharges()) {
                                // NO == NO
                                if (paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                                    mainCardBean.setOtbcredit(mainCardBean.getOtbcredit() - paymentBean.getPayment());
                                    supCardBean.setOtbcredit(supCardBean.getOtbcredit() + paymentBean.getPayment());
                                    supeodclosingbalance = supeodclosingbalance + paymentBean.getPayment();
                                }

                                mainCardBean.setTmpcredit(mainCardBean.getTmpcredit() - paymentBean.getPayment());
                                mainFinancialCharges = paymentBean.getPayment();
                                eomBean.setFinacialcharges(eomBean.getFinacialcharges() - paymentBean.getPayment());
                                paymentBean.setPayment(0.00);

                            }

                            if (eomBean.getCumcashadvance() > 0 && paymentBean.getPayment() >= eomBean.getCumcashadvance()) {
                                custAccBean.setOtbcash(custAccBean.getOtbcash() - eomBean.getCumcashadvance());
                                // NO == NO
                                if (paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                                    mainCardBean.setOtbcredit(mainCardBean.getOtbcredit() - eomBean.getCumcashadvance());
                                    supCardBean.setOtbcredit(supCardBean.getOtbcredit() + eomBean.getCumcashadvance());
                                    supeodclosingbalance = supeodclosingbalance + eomBean.getCumcashadvance();
                                }

                                mainCardBean.setOtbcash(mainCardBean.getOtbcash() - eomBean.getCumcashadvance());
                                mainCardBean.setTmpcredit(mainCardBean.getTmpcredit() - eomBean.getCumcashadvance());
                                mainCardBean.setTmpcash(mainCardBean.getTmpcash() - eomBean.getCumcashadvance());
                                mainCashAdvances = eomBean.getCumcashadvance();
                                paymentBean.setPayment(paymentBean.getPayment() - eomBean.getCumcashadvance());
                                eomBean.setCumcashadvance(0.00);

                            } else if (paymentBean.getPayment() > 0 && paymentBean.getPayment() < eomBean.getCumcashadvance()) {
                                custAccBean.setOtbcash(custAccBean.getOtbcash() - paymentBean.getPayment());
                                // NO == NO
                                if (paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                                    mainCardBean.setOtbcredit(mainCardBean.getOtbcredit() - paymentBean.getPayment());
                                    supCardBean.setOtbcredit(supCardBean.getOtbcredit() + paymentBean.getPayment());
                                    supeodclosingbalance = supeodclosingbalance + paymentBean.getPayment();
                                }

                                mainCardBean.setOtbcash(mainCardBean.getOtbcash() - paymentBean.getPayment());
                                mainCardBean.setTmpcredit(mainCardBean.getTmpcredit() - paymentBean.getPayment());
                                mainCardBean.setTmpcash(mainCardBean.getTmpcash() - paymentBean.getPayment());

                                mainCashAdvances = paymentBean.getPayment();
                                eomBean.setCumcashadvance(eomBean.getCumcashadvance() - paymentBean.getPayment());
                                paymentBean.setPayment(0.00);
                            }

                            if (eomBean.getCumtransactions() > 0 && paymentBean.getPayment() >= eomBean.getCumtransactions()) {
                                // NO == NO
                                if (paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                                    mainCardBean.setOtbcredit(mainCardBean.getOtbcredit() - eomBean.getCumtransactions());
                                    supCardBean.setOtbcredit(supCardBean.getOtbcredit() + eomBean.getCumtransactions());
                                    supeodclosingbalance = supeodclosingbalance + eomBean.getCumtransactions();
                                }

                                mainCardBean.setTmpcredit(mainCardBean.getTmpcredit() - eomBean.getCumtransactions());
                                mainTransactions = eomBean.getCumtransactions();
                                paymentBean.setPayment(paymentBean.getPayment() - eomBean.getCumtransactions());
                                eomBean.setCumtransactions(0.00);

                            } else if (paymentBean.getPayment() > 0 && paymentBean.getPayment() < eomBean.getCumtransactions()) {
                                // NO == NO
                                if (paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                                    mainCardBean.setOtbcredit(mainCardBean.getOtbcredit() - paymentBean.getPayment());
                                    supCardBean.setOtbcredit(supCardBean.getOtbcredit() + paymentBean.getPayment());
                                    supeodclosingbalance = supeodclosingbalance + paymentBean.getPayment();
                                }

                                mainCardBean.setTmpcredit(mainCardBean.getTmpcredit() - paymentBean.getPayment());
                                mainTransactions = paymentBean.getPayment();
                                eomBean.setCumtransactions(eomBean.getCumtransactions() - paymentBean.getPayment());
                                paymentBean.setPayment(0.00);
                            }

                            if (eomBean.getFinacialcharges() == 0 && eomBean.getCumcashadvance() == 0 && eomBean.getCumtransactions() == 0) {
                                eom = false;
                            }
                        }

                        if (eod && paymentBean.getPayment() > 0) {

                            if (isFirstIteration && paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.YES_STATUS)) {
                                supCardBean = mainCardBean;
                            }

                            if (eodBean.getFinacialcharges() > 0 && paymentBean.getPayment() >= eodBean.getFinacialcharges()) {
                                supCardBean.setTmpcredit(supCardBean.getTmpcredit() - eodBean.getFinacialcharges());
                                supFinancialCharges = eodBean.getFinacialcharges();
                                paymentBean.setPayment(paymentBean.getPayment() - eodBean.getFinacialcharges());
                                eodBean.setFinacialcharges(0.00);

                            } else if (paymentBean.getPayment() > 0 && paymentBean.getPayment() < eodBean.getFinacialcharges()) {
                                supCardBean.setTmpcredit(supCardBean.getTmpcredit() - paymentBean.getPayment());
                                supFinancialCharges = paymentBean.getPayment();
                                eodBean.setFinacialcharges(eodBean.getFinacialcharges() - paymentBean.getPayment());
                                paymentBean.setPayment(0.00);
                            }

                            if (eodBean.getCumcashadvance() > 0 && paymentBean.getPayment() >= eodBean.getCumcashadvance()) {
                                supCardBean.setTmpcredit(supCardBean.getTmpcredit() - eodBean.getCumcashadvance());
                                supCardBean.setTmpcash(supCardBean.getTmpcash() - eodBean.getCumcashadvance());
                                custAccBean.setOtbcash(custAccBean.getOtbcash() - eodBean.getCumcashadvance());
                                supCardBean.setOtbcash(supCardBean.getOtbcash() - eodBean.getCumcashadvance());
                                supCashAdvances = eodBean.getCumcashadvance();
                                paymentBean.setPayment(paymentBean.getPayment() - eodBean.getCumcashadvance());
                                eodBean.setCumcashadvance(0.00);

                            } else if (paymentBean.getPayment() > 0 && paymentBean.getPayment() < eodBean.getCumcashadvance()) {
                                supCardBean.setTmpcredit(supCardBean.getTmpcredit() - paymentBean.getPayment());
                                supCardBean.setTmpcash(supCardBean.getTmpcash() - paymentBean.getPayment());
                                custAccBean.setOtbcash(custAccBean.getOtbcash() - paymentBean.getPayment());
                                supCardBean.setOtbcash(supCardBean.getOtbcash() - paymentBean.getPayment());
                                supCashAdvances = paymentBean.getPayment();
                                eodBean.setCumcashadvance(eodBean.getCumcashadvance() - paymentBean.getPayment());
                                paymentBean.setPayment(0.00);
                            }

                            if (eodBean.getCumtransactions() > 0 && paymentBean.getPayment() >= eodBean.getCumtransactions()) {
                                supCardBean.setTmpcredit(supCardBean.getTmpcredit() - eodBean.getCumtransactions());
                                supTransactions = eodBean.getCumtransactions();
                                paymentBean.setPayment(paymentBean.getPayment() - eodBean.getCumtransactions());
                                eodBean.setCumtransactions(0.00);

                            } else if (paymentBean.getPayment() > 0 && paymentBean.getPayment() < eodBean.getCumtransactions()) {
                                supCardBean.setTmpcredit(supCardBean.getTmpcredit() - paymentBean.getPayment());
                                supTransactions = paymentBean.getPayment();
                                eodBean.setCumtransactions(eodBean.getCumtransactions() - paymentBean.getPayment());
                                paymentBean.setPayment(0.00);
                            }

                            if (eodBean.getFinacialcharges() == 0 && eodBean.getCumcashadvance() == 0 && eodBean.getCumtransactions() == 0) {
                                eod = false;
                            }
                        }

                        String status = statusVarList.getEOD_PENDING_STATUS();

                        if (paymentBean.getPayment() == 0) {
                            status = statusVarList.getEOD_DONE_STATUS();
                        }
                        knockOffRepo.updateEodPayment(paymentBean.getId(), mainFinancialCharges, mainCashAdvances, mainTransactions, supFinancialCharges, supCashAdvances, supTransactions, paymentBean.getPayment(), status);

                        if (paymentIteration == paymentList.size() && paymentBean.getIsPrimary().equalsIgnoreCase(Configurations.NO_STATUS)) {
                            knockOffRepo.updateCardOtb(supCardBean);
                            knockOffRepo.OnlineupdateCardOtb(supCardBean);
                            if (supeodclosingbalance > 0) {
                                maineodclosingbalance = maineodclosingbalance + supeodclosingbalance;
                                knockOffRepo.updateEodClosingBalance(supCardBean.getCardnumber(), supeodclosingbalance);
                            }
                        }
                        paymentIteration++;

                        details.put("Customer ID", custAccBean.getCustomerid());
                        details.put("Account Number", custAccBean.getAccountnumber());
                        details.put("Main Card Number", CommonMethods.cardNumberMask(mainCardBean.getCardnumber()));
                        details.put("Card Number", CommonMethods.cardNumberMask(supCardBean.getCardnumber()));
                        details.put("Payment Amount", amount);
                        details.put("Knocked Off EOM Financial Charges", mainFinancialCharges);
                        details.put("Knocked Off EOM Cash Advance", mainCashAdvances);
                        details.put("Knocked Off EOM Sales", mainTransactions);
                        details.put("Knocked Off EOD Financial Charges", supFinancialCharges);
                        details.put("Knocked Off EOD Cash Advance", supCashAdvances);
                        details.put("Knocked Off EOD Sales", supTransactions);
                        details.put("Forward Amount", paymentBean.getPayment());
                    }

                    if (eomBean != null) {
                        knockOffRepo.updateEOMCARDBALANCE(eomBean);
                    }

                    if (eodBean != null) {
                        knockOffRepo.updateEODCARDBALANCE(eodBean);
                    }

                    if (cardIteration == cardList.size()) {
                        if (maineodclosingbalance > 0) {
                            knockOffRepo.updateEodClosingBalance(mainCardBean.getCardnumber(), ((-1) * maineodclosingbalance));
                        }
                        knockOffRepo.updateCardComp(mainCardBean);
                        knockOffRepo.OnlineupdateCardOtb(mainCardBean);
                        knockOffRepo.updateAccountOtb(custAccBean);
                        knockOffRepo.updateCustomerOtb(custAccBean);
                        knockOffRepo.OnlineupdateAccountOtb(custAccBean);
                        knockOffRepo.OnlineupdateCustomerOtb(custAccBean);
                    }
                    cardIteration++;
                    successCount.add(1);

                } catch (Exception e) {
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(custAccBean.getCardnumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                    logError.error("Knock off process failed for account " + custAccBean.getAccountnumber(), e);
                    failCount.add(1);
                    break;
                } finally {
                    logInfo.info(logManager.logDetails(details));

                    if (paymentList != null && paymentList.size() != 0) {
                        for (OtbBean paymentBean : paymentList) {
                            CommonMethods.clearStringBuffer(paymentBean.getCardnumber());
                            CommonMethods.clearStringBuffer(paymentBean.getMaincardno());
                        }
                        paymentList = null;
                    }
                }
            }
        }
    }
}
