package com.epic.cms.dao;

import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.ProcessBean;

import java.util.HashMap;

public interface ManualNpDao {

    HashMap<String, String[]> getManualNpRequestDetails(int reqType, String Status) throws Exception;
    int updateNpStatusCardAccount(String accNo, int npstatus) throws Exception;
    int insertIntoDelinquentHistory(StringBuffer cardNumber, String accNo, String remark) throws Exception;
    int getNPDetailsFromLastBillingStatement(DelinquentAccountBean delinquentAccountBean, boolean manualNp) throws Exception;
    DelinquentAccountBean setDelinquentAccountDetails(StringBuffer cardNo) throws Exception;
    double getTotalPaymentSinceLastDue(String accountNum, java.util.Date EOD_DATE, java.util.Date dueDate) throws Exception;
    String[] getRiskclassOnNdia(int noOfDates) throws Exception;
    int insertIntoEodGLAccount(int eodID, java.util.Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception;
    int addDetailsForManualNPToDelinquentAccountTable(DelinquentAccountBean delinquentAccountBean) throws Exception;
    int updateManualNPtoComplete(int reqID, String status) throws Exception;
    String getNPRiskClass() throws Exception;
    String[] getNDIAOnRiskClass(String riskClass) throws Exception;
    int getNPDetailsForNpGl(String accNo, DelinquentAccountBean delinquentAccountBean) throws Exception;
    int updateDelinquentAccountForManualNP(String accNo, DelinquentAccountBean bean) throws Exception;
    int updateAccountStatus(String accNo, String status) throws Exception;
    int updateOnlineAccountStatus(String accNo, int status) throws Exception;
    ProcessBean getProcessDetails (int processId) throws Exception;
}
