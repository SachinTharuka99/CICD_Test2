/**
 * Author : sharuka_j
 * Date : 12/6/2022
 * Time : 8:59 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import java.util.ArrayList;
import java.util.HashMap;

public interface EOMSupplementaryCardResetDao {
    ArrayList getEligibleAccounts() throws Exception;

    ArrayList<StringBuffer> getAllTheCardsForAccount(StringBuffer accNo) throws Exception;

    HashMap<String, Double> getCardBalances(StringBuffer cardNo) throws Exception;

    void UpdateEOMCardBalance(StringBuffer cardNo, StringBuffer mainCardNo, HashMap<String, Double> CardBal) throws Exception;

    HashMap<String, Double> getCardTempBalances(StringBuffer cardNo) throws Exception;

    void resetEodCardBallance(StringBuffer cardNo) throws Exception;

    HashMap<String, Double> getEOMCardBalanceFromSupplementary(StringBuffer cardNo) throws Exception;

    int resetEOMCardBalance(StringBuffer cardNo) throws Exception;

    double calculateMainCardForwardPayments(StringBuffer mainCardNumber) throws Exception;

    double calculateSupCardForwardPayments(StringBuffer mainCard) throws Exception;

    void updateMainCardBal(StringBuffer mainCardNumber, Double totalSupTempCredit, Double totalSupTempCash, Double supFowardPayments) throws Exception;

    void updateMainCardBalOnline(StringBuffer mainCardNumber, Double totalSupTempCredit, Double totalSupTempCash, Double supFowardPayments) throws Exception;

    int updateSupplementaryEODPaymentsStatus(StringBuffer mainCardNo) throws Exception;

    int insertNewEntryToEodPayment(StringBuffer mainCardNo, double allSupCardFP) throws Exception;

    void updateEodCardBallance(StringBuffer cardNo, HashMap<String, Double> cardBal) throws Exception;

    HashMap<StringBuffer, double[]> calculateOTBsAfterResetting(ArrayList<StringBuffer> cardList) throws Exception;

    void resetSuplimentryBalanceInBackendCardTable(HashMap<StringBuffer, double[]> map, StringBuffer mainCardNo) throws Exception;

    void resetSuplimentryBalanceInOnlineCardTable(HashMap<StringBuffer, double[]> map, StringBuffer mainCardNo) throws Exception;

    int updatePreviousEODErrorCardDetails(String prevEODID, int stepId) throws Exception;

    int updateEodProcessSummery(int eodId, String status, int processId) throws Exception;
}
