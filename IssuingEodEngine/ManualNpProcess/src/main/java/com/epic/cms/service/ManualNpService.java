package com.epic.cms.service;

import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.repository.ManualNpRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;


@Service
public class ManualNpService {
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    ManualNpRepo manualNpRepo;

    int selectedaccounts = 0;
    int successCounts = 0;
    int FailedCounts = 0;

    @Async("taskExecutor2")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void manualNpClassification(ArrayList<StringBuffer> accDetails) {

        if (!Configurations.isInterrupted) {
            selectedaccounts++;
            String accNo = accDetails.get(0).toString();
            StringBuffer cardNo = new StringBuffer(accDetails.get(1));
            String accStatus = accDetails.get(2).toString();
            int reqID = Integer.parseInt(accDetails.get(3).toString());
            DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
            String remark = "";

            try {
                if (accStatus.equalsIgnoreCase(status.getACCOUNT_NON_PERFORMING_STATUS())) {
                    manualNpRepo.updateNpStatusCardAccount(accNo, 2);
                    remark = "Account has Auto Non Performing to Manual Non Performing by the manual.";
                    manualNpRepo.insertIntoDelinquentHistory(cardNo, accNo, remark);

                } else {
                    String[] newriskClass;
                    double totalPayments = 0.0;
                    delinquentAccountBean = manualNpRepo.setDelinquentAccountDetails(cardNo);
                    delinquentAccountBean.setCardNumber(cardNo);
                    delinquentAccountBean.setAccStatus(accStatus);
                    delinquentAccountBean.setAccNo(accNo);

                    manualNpRepo.getNPDetailsFromLastBillingStatement(delinquentAccountBean, true);
                    delinquentAccountBean.setAccStatus(status.getACCOUNT_NON_PERFORMING_STATUS());

                    if (delinquentAccountBean.getNDIA() > 0) {
                        delinquentAccountBean.setDelinqstatus(status.getTO_PERFORMING_TO_NON_PERFORMING_STATUS());
                    } else {
                        delinquentAccountBean.setDelinqstatus(status.getONLY_MANUAL_NP_STATUS());
                        delinquentAccountBean.setNDIA(0);
                    }

                    double dueAmount = Double.parseDouble(delinquentAccountBean.getDueAmount());
                    totalPayments = manualNpRepo.getTotalPaymentSinceLastDue(accNo, Configurations.EOD_DATE, delinquentAccountBean.getLastStatementDate());
                    newriskClass = manualNpRepo.getRiskclassOnNdia(delinquentAccountBean.getNDIA());
                    delinquentAccountBean.setRiskClass(newriskClass[1]);
                    delinquentAccountBean.setMIA(Integer.parseInt(newriskClass[1]));
                    remark = "A fresh account for delinquent table with 0 NDIA by Manual NP";
                    delinquentAccountBean.setCardNumber(cardNo);
                    delinquentAccountBean.setDueAmount(Double.toString(dueAmount));

                    if (totalPayments > dueAmount) {
                        delinquentAccountBean.setRemainDue(0.0);
                    } else {
                        delinquentAccountBean.setRemainDue(dueAmount - totalPayments);
                    }

                    delinquentAccountBean.setAssignStatus(status.getSTATUS_NO());
                    delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                    delinquentAccountBean.setAssignee(Configurations.EOD_USER);
                    delinquentAccountBean.setDueDate(delinquentAccountBean.getDueDate());

                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                            Configurations.INTEREST_ON_THE_NP_GL, delinquentAccountBean.getNpInterest(), Configurations.DEBIT, null);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                            Configurations.OUTSTANDING_ON_THE_NP_GL, delinquentAccountBean.getNpOutstanding(), Configurations.DEBIT, null);
                    this.changeAccountStatus(delinquentAccountBean.getAccNo(), status.getACCOUNT_NON_PERFORMING_STATUS(), status.getONLINE_DEACTIVE_STATUS());

                    manualNpRepo.updateNpStatusCardAccount(accNo, 2);
                    delinquentAccountBean.setAccruedInterest(0.0);
                    delinquentAccountBean.setAccruedFees(0.0);
                    delinquentAccountBean.setAccruedOverLimit(0.0);
                    delinquentAccountBean.setAccruedlatePay(0.0);
                    delinquentAccountBean.setAccNo(accNo);
                    manualNpRepo.addDetailsForManualNPToDelinquentAccountTable(delinquentAccountBean);
                    remark = "Account has Performing to Manual Non Performing by the manual.";
                    manualNpRepo.insertIntoDelinquentHistory(cardNo, accNo, remark);
                }

                int q = manualNpRepo.updateManualNPtoComplete(reqID, status.getCOMMON_COMPLETED());
                if (q > 0) {
                    infoLogger.info(logManager.processHeaderStyle("Successfully updated id:" + reqID + " RQAC -> COMP"));
                } else {
                    infoLogger.info(logManager.processHeaderStyle("Failed to update id:" + reqID + " RQAC -> COMP"));
                }

                successCounts++;
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception ex) {
                FailedCounts++;
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardNo), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                errorLogger.error("Manual NP process failed when going to classified NP for account: " + accNo, ex);
            }
        }
    }
    @Async("taskExecutor2")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void manualNpDeClassification(ArrayList<StringBuffer> accDetails){

        if (!Configurations.isInterrupted) {
            selectedaccounts++;
            String accNo = accDetails.get(1).toString();
            StringBuffer cardNo = new StringBuffer(accDetails.get(0));
            String accStatus = accDetails.get(1).toString();
            int reqID = Integer.parseInt(accDetails.get(2).toString());
            int ndia = Integer.parseInt(accDetails.get(3).toString());
            DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
            delinquentAccountBean.setAccNo(accNo);
            delinquentAccountBean.setCardNumber(cardNo);
            delinquentAccountBean.setAccStatus(accStatus);
            String remark = "";

            try {
                String npRiskClass = manualNpRepo.getNPRiskClass();
                String[] bucketId = manualNpRepo.getNDIAOnRiskClass(npRiskClass);

                if (ndia >= Integer.parseInt(bucketId[1])) {
                    manualNpRepo.updateNpStatusCardAccount(accNo, 1);
                    remark = "Account has Manual Non Performing to Auto Non Performing by the manual declassification.";
                    manualNpRepo.insertIntoDelinquentHistory(cardNo, accNo, remark);
                    infoLogger.info(logManager.processStartEndStyle(accNo + ": " + remark));

                } else {
                    manualNpRepo.getNPDetailsForNpGl(accNo, delinquentAccountBean);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, cardNo,
                            Configurations.KNOCKOFF_NP_INTEREST_DECLASSIFIED_GL, delinquentAccountBean.getNpInterest(), Configurations.DEBIT, null);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, cardNo,
                            Configurations.KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL, delinquentAccountBean.getNpOutstanding(), Configurations.DEBIT, null);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, cardNo,
                            Configurations.KNOCKOFF_ACCRUED_INTEREST_DECLASSIFIED_GL, delinquentAccountBean.getAccruedInterest(), Configurations.DEBIT, null);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, cardNo,
                            Configurations.KNOCKOFF_ACCRUED_LATE_FEE_DECLASSIFIED_GL, delinquentAccountBean.getAccruedlatePay(), Configurations.DEBIT, null);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, cardNo,
                            Configurations.KNOCKOFF_ACCRUED_OVERLIMIT_FEE_DECLASSIFIED_GL, delinquentAccountBean.getAccruedOverLimit(), Configurations.DEBIT, null);
                    manualNpRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, cardNo,
                            Configurations.KNOCKOFF_ACCRUED_OTHER_FEES_DECLASSIFIED_GL, delinquentAccountBean.getAccruedFees(), Configurations.DEBIT, null);
                    this.changeAccountStatus(delinquentAccountBean.getAccNo(), status.getACCOUNT_ACTIVE_STATUS(), status.getONLINE_CARD_ACTIVE_STATUS());
                    manualNpRepo.updateNpStatusCardAccount(accNo, 0);
                    delinquentAccountBean.setAccStatus(status.getACCOUNT_ACTIVE_STATUS());

                    if (ndia == 0) {
                        delinquentAccountBean.setDelinqstatus(status.getTO_RESOLVE_STATUS());
                    } else {
                        delinquentAccountBean.setDelinqstatus(status.getTO_NON_PERFORMING_TO_PERFORMING_STATUS());
                    }
                    delinquentAccountBean.setNpDate(null);
                    delinquentAccountBean.setNpInterest(0.0);
                    delinquentAccountBean.setNpOutstanding(0.0);
                    delinquentAccountBean.setAccruedInterest(0.0);
                    delinquentAccountBean.setAccruedFees(0.0);
                    delinquentAccountBean.setAccruedOverLimit(0.0);
                    delinquentAccountBean.setAccruedlatePay(0.0);
                    manualNpRepo.updateDelinquentAccountForManualNP(accNo, delinquentAccountBean);
                    remark = "Account has Manual Non Performing to Performing by the manual.";
                    manualNpRepo.insertIntoDelinquentHistory(cardNo, accNo, remark);
                    infoLogger.info(logManager.processStartEndStyle(accNo + ": " + remark));
                }

                manualNpRepo.updateManualNPtoComplete(reqID, status.getCOMMON_COMPLETED());
                successCounts++;
                Configurations.PROCESS_SUCCESS_COUNT++;

            } catch (Exception ex) {
                FailedCounts++;
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cardNo), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                errorLogger.error("Manual NP process failed when going to De-classified NP for account: " + accNo, ex);
            }
        }
    }
    private int changeAccountStatus(String accNo, String backendStatus, int onlineStatus) throws Exception {
        int flag = 0;
        flag = manualNpRepo.updateAccountStatus(accNo, backendStatus);
        flag = manualNpRepo.updateOnlineAccountStatus(accNo, onlineStatus);
        return flag;
    }
}
