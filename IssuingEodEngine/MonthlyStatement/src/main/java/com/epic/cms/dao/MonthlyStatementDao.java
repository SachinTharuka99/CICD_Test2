/**
 * Author : yasiru_d
 * Date : 11/14/2022
 * Time : 5:34 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.CardTransactionBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.bean.StatementBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface MonthlyStatementDao {

    HashMap<String, ArrayList<CardBean>> getCardAccountListForBilling() throws Exception;
    void UpdateStatementDeatils(List<CardBean> CardBeanList, StatementBean stBean, String accountNum) throws Exception;
    boolean checkReplaceStatus(StringBuffer cardNo) throws Exception;
    StatementBean CheckBillingCycleChangeRequest(String accNo) throws Exception;
    List<StringBuffer> getAllOldCards(StringBuffer cardNo) throws Exception;
    void updateCloseCardFlag(StringBuffer CardNumbers) throws Exception;
    void updateBillingCycleRequestBCCP(String AccNo, String reqID) throws Exception;
    Date calculateDueDate(String AccNo) throws Exception;
    boolean isHoliday(java.util.Date today) throws Exception;
    int getThisStatementStartandEndEodId(StringBuffer cardNo) throws Exception;
    CardTransactionBean getCardTranactionSummeryBean(int StartEODID, int EndEODID, StringBuffer cardNo) throws Exception;
    int updateStatementIDByAccNoInEODTxn(String statementId, int startEodId, int endEodId, String accountNo) throws Exception;
    StatementBean getLastStatementDetails(StatementBean stBean) throws Exception;
    boolean insertBillingStatement(StatementBean stBean) throws SQLException, Exception;
    double getTotalStampDuty(String accNo) throws Exception;
    ArrayList<String> getBucketIdAndNODIA(String accNo) throws Exception;
    void updateNextBillingDate(StatementBean stBean) throws Exception;
    double calculateMinPayment(String AccNo, double ClosingBal, double availableCreadit, double minPaymentOverDue, double creditLimit) throws Exception;
    int checkMinPaymentDueCount(String accNo) throws Exception;
    boolean insertBillingLastStatementSummry(StatementBean stBean) throws Exception;
    boolean updateBillingLastStatementSummry(StatementBean StBean) throws Exception;
    double checkChequeReturns(StatementBean stBean) throws Exception;
}
