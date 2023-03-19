/**
 * Author : sharuka_j
 * Date : 12/6/2022
 * Time : 9:13 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.EOMSupplementaryCardResetDao;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class EOMSupplementaryCardResetService {
    @Autowired
    LogManager logManager;

    @Autowired
    EOMSupplementaryCardResetDao eomSupplementaryCardResetDao;

    @Async("taskExecutor2")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void SupplementryResetThread(Object acclist) {
        if (!Configurations.isInterrupted) {
            double totalSupTempCredit = 0.00;
            double totalSupTempCash = 0.00;
            double tempX = 0.00;
            Object accountNo = acclist;
            ArrayList<StringBuffer> cardList = null;
            LinkedHashMap details = new LinkedHashMap();

            try {
                cardList = eomSupplementaryCardResetDao.getAllTheCardsForAccount(new StringBuffer(acclist.toString()));
                StringBuffer mainCardNo = cardList.get(0);
                HashMap<String, Double> mainCardBal = eomSupplementaryCardResetDao.getCardBalances(mainCardNo);
                HashMap<String, Double> supCardBal;
                if (mainCardBal.size() > 0) {
                    eomSupplementaryCardResetDao.UpdateEOMCardBalance(mainCardNo, mainCardNo, mainCardBal);
                }

                for (int j = 1; j < cardList.size(); j++) {
                    /**get balances from eodcardbalance table*/
                    HashMap<String, Double> cardBal = eomSupplementaryCardResetDao.getCardBalances(cardList.get(j));
                    /**get balances from backend card table*/
                    //TODO need to remove templimit inc/dec
                    HashMap<String, Double> cardTempBal = eomSupplementaryCardResetDao.getCardTempBalances(cardList.get(j));
                    totalSupTempCredit = totalSupTempCredit + cardTempBal.get("TEMPCREDITAMOUNT");
                    totalSupTempCash = totalSupTempCash + cardTempBal.get("TEMPCASHAMOUNT");

                    eomSupplementaryCardResetDao.UpdateEOMCardBalance(cardList.get(j), mainCardNo, cardBal);

                    /**update main card balances*/
                    mainCardBal.put("OpeningBal", mainCardBal.get("OpeningBal"));
                    mainCardBal.put("closingBal", mainCardBal.get("closingBal") - (cardBal.get("CREDITLIMIT") - cardBal.get("closingBal")));
                    mainCardBal.put("finCahrges", mainCardBal.get("finCahrges") + cardBal.get("finCahrges"));
                    mainCardBal.put("cashAdvanced", mainCardBal.get("cashAdvanced") + cardBal.get("cashAdvanced"));
                    mainCardBal.put("txns", mainCardBal.get("txns") + cardBal.get("txns"));

                    /**Reset suplimentary cards in eodcardBalance Table*/
                    eomSupplementaryCardResetDao.resetEodCardBallance(cardList.get(j));
                    /** Affect supp balance into main card balance*/
                    supCardBal = eomSupplementaryCardResetDao.getEOMCardBalanceFromSupplementary(cardList.get(j));
                    eomSupplementaryCardResetDao.UpdateEOMCardBalance(mainCardNo, mainCardNo, supCardBal);
                    /**update  supp balance  in EOMcardbalance to 0*/
                    eomSupplementaryCardResetDao.resetEOMCardBalance(cardList.get(j));
                }
                /**main card forward amount*/
                double mainCardFP = eomSupplementaryCardResetDao.calculateMainCardForwardPayments(mainCardNo);
                /**sup cards total foward amount*/
                double allSupCardFP = eomSupplementaryCardResetDao.calculateSupCardForwardPayments(mainCardNo);
                /**update backend cardTable(otbs,tempcredit,tempcash)*/
                eomSupplementaryCardResetDao.updateMainCardBal(mainCardNo, totalSupTempCredit, totalSupTempCash, allSupCardFP);
                /**update online otbCredit*/
                eomSupplementaryCardResetDao.updateMainCardBalOnline(mainCardNo, totalSupTempCredit, totalSupTempCash, allSupCardFP);
                /**updateEodPayment of sups status as EDON*/
                eomSupplementaryCardResetDao.updateSupplementaryEODPaymentsStatus(mainCardNo);
                /**insert new entry for each payment amouts to main card*/
                if (allSupCardFP > 0) {
                    eomSupplementaryCardResetDao.insertNewEntryToEodPayment(mainCardNo, allSupCardFP);
                }

                eomSupplementaryCardResetDao.updateEodCardBallance(mainCardNo, mainCardBal);

                HashMap<StringBuffer, double[]> OTBsAfterResetting = eomSupplementaryCardResetDao.calculateOTBsAfterResetting(cardList);
                /**reset supplimentary from card table in backend*/
                eomSupplementaryCardResetDao.resetSuplimentryBalanceInBackendCardTable(OTBsAfterResetting, mainCardNo);
                /**reset supplimentary from card table in online*/
                eomSupplementaryCardResetDao.resetSuplimentryBalanceInOnlineCardTable(OTBsAfterResetting, mainCardNo);

                details.put("Account Number", accountNo);
                details.put("Ststus", "Success");
                Configurations.PROCESS_SUCCESS_COUNT++;
                CommonMethods.clearStringBuffer(mainCardNo);
            } catch (Exception ex) {
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(accountNo.toString()), ex.toString(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                logManager.logError("Supplementary Card Reset process failed for accountnumber " + accountNo.toString(), ex, errorLogger);
                details.put("Account Number", accountNo);
                details.put("Ststus", "Fails");
            } finally {
                logManager.logDetails(details, infoLogger);
                if (cardList != null && cardList.size() != 0) {
                    for (int j = 1; j < cardList.size(); j++) {
                        CommonMethods.clearStringBuffer(cardList.get(j));
                    }
                    cardList = null;
                }
            }
        }
    }
}

