/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:37 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.RiskCalculationBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public interface RiskCalculationDao {
    ArrayList<DelinquentAccountBean> getDelinquentAccounts() throws Exception;

    boolean isManualNp(String accNo) throws Exception;

    double checkForPayment(String accNo, java.util.Date EOD_DATE) throws Exception;

    String[] getRiskclassOnNdia(int noOfDates) throws Exception;

    String getNPRiskClass() throws Exception;

    String[] getNDIAOnRiskClass(String riskClass) throws Exception;

    int getNPDetailsFromLastBillingStatement(DelinquentAccountBean delinquentAccountBean, boolean manualNp) throws Exception;

    int insertIntoEodGLAccount(int eodID, java.util.Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception;

    int updateNpStatusCardAccount(String accNo, int npstatus) throws Exception;

    double getTotalPaymentSinceLastDue(String cardNumber, java.util.Date EOD_DATE, java.util.Date dueDate) throws Exception;

    int addDetailsToDelinquentAccountTable(DelinquentAccountBean delinquentAccountBean) throws Exception;

    int insertIntoDelinquentHistory(StringBuffer cardNumber, String accNo, String remark) throws Exception;

    ArrayList<RiskCalculationBean> getRiskCalculationCardList() throws Exception;

    int updateProvisionInDELINQUENTACCOUNT(BigDecimal provisionAmount, String accNo) throws Exception;

    ArrayList<Object> getLastStatementDate(StringBuffer cardNumber) throws Exception;

    HashMap<String, Date> getDueDateList(StringBuffer cardNumber) throws Exception;

    ArrayList<Object> getMinimumPaymentExistStatementDate(StringBuffer cardNo, int monthNo) throws Exception;

    int updateOnlineAccountStatus(String accNo, int status) throws Exception;

    int updateAccountStatus(String accNo, String status) throws Exception;

    double checkLeastMinimumPayment(String accNo) throws Exception;

    int insertIntoEodGLAccountBigDecimal(int eodID
            , java.util.Date glDate, StringBuffer cardNo, String glType, BigDecimal amount, String cdStatus, String payType) throws Exception;

    int updateMinimumPayment(StringBuffer cardNo, HashMap<String, Double> dueAmountList
            , HashMap<String, java.util.Date> dueDateList, int dueCount) throws Exception;

    Date getDueDateOnRiskClass(int monthNo, StringBuffer cardNo) throws Exception;

    DelinquentAccountBean setDelinquentAccountDetails(StringBuffer cardNo) throws Exception;

    double getMinPaymentFromBilling(String accNumber) throws Exception;

    List<BigDecimal> getDelinquentAccountDetailsAsList(String accNo) throws Exception;

    int updateAllDELINQUENTACCOUNTnpdetails(double npInterest, double npOutstanding, double accruedInterest, double accruedOverLimitFees,
                                            double accruedLatePayFees, double otherFees, String accNo) throws Exception;

    int updateAllDELINQUENTACCOUNTnpdetails(BigDecimal npInterest, BigDecimal npOutstanding, BigDecimal accruedInterest,
                                            BigDecimal accruedOverLimitFees, BigDecimal accruedLatePayFees, BigDecimal otherFees, String accNo) throws Exception;
}
