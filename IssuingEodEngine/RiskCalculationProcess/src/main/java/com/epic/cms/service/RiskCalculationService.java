/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:35 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.RiskCalculationDao;
import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.bean.RiskCalculationBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RiskCalculationService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    LogManager logManager;
    @Autowired
    RiskCalculationDao riskCalculationDao;
    @Autowired
    StatusVarList statusVarList;
    int noOfNewCards = 0;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void riskCalculationProcess(DelinquentAccountBean delinquentAccountBean, int configProcess, ProcessBean processBean, AtomicInteger faileCardCount) {
        if (!Configurations.isInterrupted) {
            String maskedCardNumber = CommonMethods.cardNumberMask(delinquentAccountBean.getCardNumber());
            String riskClass = delinquentAccountBean.getRiskClass();

            LinkedHashMap details = new LinkedHashMap();
            LinkedHashMap detailsProvision = new LinkedHashMap();
            ArrayList<String> logDetails = new ArrayList<String>();
            boolean isManualNP = false;
            try {
                isManualNP = riskCalculationDao.isManualNp(delinquentAccountBean.getAccNo());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                if (!(delinquentAccountBean.getRemainDue() > 0)) {
                    delinquentAccountBean.setRemainDue(Double.parseDouble(delinquentAccountBean.getDueAmount()));
                }

                ArrayList<Object> lastStmtDetails;
                details.put("Account Number", delinquentAccountBean.getAccNo());
                details.put("Card Number", maskedCardNumber);
                lastStmtDetails = this.getLastStmtdate(delinquentAccountBean.getCardNumber());
                boolean isDueDate = checkForDueDate((Date) lastStmtDetails.get(2));
                String remark = null;
                double payment = riskCalculationDao.checkForPayment(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                boolean isPaymentOnCurrentDay;
                //Check weather a due date or not for the account
                if (isDueDate) {
                    details.put("Is Due Date", "Yes");
                    delinquentAccountBean.setIsdueDate(1);
                    isPaymentOnCurrentDay = checkForPayment(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                    delinquentAccountBean.setNDIA(delinquentAccountBean.getNDIA() + 1);

                    if (isPaymentOnCurrentDay) {
                        boolean minimumPaymentClearStatus = this.isMinimumPaymentTableClear(delinquentAccountBean.getCardNumber());
                        //total due amount is settled by the today payment
                        boolean resolveStatus = checkForResolveStatus(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), delinquentAccountBean.getRiskClass());
                        //Get total payments since last due date
                        double totalPayments;
                        boolean enoughPaymentForKnockOff = checkPaymentEnoughForKnockOff(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);

                        totalPayments = riskCalculationDao.checkForPayment(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);

                        if (delinquentAccountBean.getAccStatus().equalsIgnoreCase(statusVarList.ACCOUNT_NON_PERFORMING_STATUS)) {
                            this.generateGLForPaymentsKnockOff(minimumPaymentClearStatus, resolveStatus, delinquentAccountBean,
                                    delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), payment);
                        }
                        logDetails = this.updateDelinquentOnPayments(isManualNP, resolveStatus, totalPayments, delinquentAccountBean, minimumPaymentClearStatus);
                        this.generateProvisionGLWhenPaymentRecieved(delinquentAccountBean, delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), payment);
                    } else {
                        logDetails.add(delinquentAccountBean.getRiskClass());
                        //not 9th bucket(V1.09)
                        if (!riskClass.equals(statusVarList.getRISK_CLASS_NINE())) {
                            String[] newriskClass;
                            String nextRiskClass;
                            String oldRiskClass = delinquentAccountBean.getRiskClass();
                            newriskClass = riskCalculationDao.getRiskclassOnNdia(delinquentAccountBean.getNDIA());
                            delinquentAccountBean.setDueDate((Date) lastStmtDetails.get(2));
                            nextRiskClass = newriskClass[1];
                            String npRiskClass = riskCalculationDao.getNPRiskClass();
                            String[] bucketId = riskCalculationDao.getNDIAOnRiskClass(npRiskClass);

                            //changed by the CR. account should be update NP when passed the ndia 90.
                            if (delinquentAccountBean.getNDIA() == Integer.parseInt(bucketId[1]) && !isManualNP) {
                                riskCalculationDao.getNPDetailsFromLastBillingStatement(delinquentAccountBean, false);
                                delinquentAccountBean.setAccStatus(statusVarList.ACCOUNT_NON_PERFORMING_STATUS);
                                delinquentAccountBean.setDelinqstatus(statusVarList.TO_PERFORMING_TO_NON_PERFORMING_STATUS);
                                //insert GL when account got NP(NpP Interest , Capital & interest(Outstanding))
                                riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                        Configurations.INTEREST_ON_THE_NP_GL, delinquentAccountBean.getNpInterest(), Configurations.DEBIT, null);
                                riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                        Configurations.OUTSTANDING_ON_THE_NP_GL, delinquentAccountBean.getNpOutstanding(), Configurations.DEBIT, null);
                                //deacteve the account status online and backend
                                this.changeAccountStatus(delinquentAccountBean.getAccNo(), statusVarList.ACCOUNT_NON_PERFORMING_STATUS, statusVarList.ONLINE_DEACTIVE_STATUS);
                                //change the NPSTATUS to automatic NP (1)
                                riskCalculationDao.updateNpStatusCardAccount(delinquentAccountBean.getAccNo(), 1);
                            }
                            String newRiskClass = nextRiskClass;
                            remark = "Risk class change from " + oldRiskClass + " to " + newRiskClass;
                            delinquentAccountBean.setRiskClass(nextRiskClass);
                            //added start
                            delinquentAccountBean.setMIA(Integer.parseInt(newriskClass[1]));
                            String newDelinquentStaus = this.getNEwDelinquentStatus(newriskClass[1], delinquentAccountBean.getRiskClass());
                            delinquentAccountBean.setDelinqstatus(newDelinquentStaus);
                            delinquentAccountBean.setLastStatementDate((Date) lastStmtDetails.get(0));
                            delinquentAccountBean.setDueAmount(lastStmtDetails.get(1).toString());
                            double totalPayments;
                            double newDueAmount = 0;
                            totalPayments = riskCalculationDao.getTotalPaymentSinceLastDue(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE, (Date) lastStmtDetails.get(0));
                            newDueAmount = Double.parseDouble(delinquentAccountBean.getDueAmount()) - totalPayments;
                            delinquentAccountBean.setRemainDue(newDueAmount);
                            //end
                            delinquentAccountBean.setAssignStatus(statusVarList.STATUS_NO);
                            delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                            delinquentAccountBean.setAssignee(Configurations.EOD_USER);
                            riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);
                            commonRepo.insertIntoDelinquentHistory(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), remark);
                        }
                        logDetails.add(delinquentAccountBean.getRiskClass());
                        logDetails.add(delinquentAccountBean.getDelinqstatus());
                        logDetails.add(Integer.toString(delinquentAccountBean.getNDIA()));
                        logDetails.add(delinquentAccountBean.getDueAmount());
                        logDetails.add(delinquentAccountBean.getAccStatus());
                    }

                } else {
                    String nextRiskClass;
                    delinquentAccountBean.setIsdueDate(0);
                    isPaymentOnCurrentDay = checkForPayment(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                    delinquentAccountBean.setNDIA(delinquentAccountBean.getNDIA() + 1);

                    if (isPaymentOnCurrentDay) {
                        //Get total payments since last due date
                        double totalPayments;
                        boolean minimumPaymentClearStatus = this.isMinimumPaymentTableClear(delinquentAccountBean.getCardNumber());
                        //total due amount is settled by the today payment
                        boolean resolveStatus = checkForResolveStatus(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), delinquentAccountBean.getRiskClass());
                        totalPayments = riskCalculationDao.checkForPayment(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                        boolean enoughPaymentForKnockOff = checkPaymentEnoughForKnockOff(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);

                        //generate payment knockoff gl for NP accounts. & update remaining NP balance in delinquentaccount (etc.npinterest,npoutstanding ...)
                        //this should be happen before reducing the ndia. beacause provision should be knock off with current ndia ratio(0.25 etc) before ndia to get reduce.
                        if (delinquentAccountBean.getAccStatus().equalsIgnoreCase(statusVarList.getACCOUNT_NON_PERFORMING_STATUS())) {
                            this.generateGLForPaymentsKnockOff(minimumPaymentClearStatus, resolveStatus, delinquentAccountBean,
                                    delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), payment);
                        }
                        logDetails = this.updateDelinquentOnPayments(isManualNP, resolveStatus, totalPayments, delinquentAccountBean, minimumPaymentClearStatus);
                        this.generateProvisionGLWhenPaymentRecieved(delinquentAccountBean, delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), payment);
                    } else {
                        logDetails.add(delinquentAccountBean.getRiskClass());
                        //not 9th bucket(V1.09)
                        if (!riskClass.equals(statusVarList.getRISK_CLASS_NINE())) {
                            String[] newriskClass;
                            newriskClass = riskCalculationDao.getRiskclassOnNdia(delinquentAccountBean.getNDIA());
                            delinquentAccountBean.setRiskClass(newriskClass[1]);

                            delinquentAccountBean.setDueDate((Date) lastStmtDetails.get(2));
                            nextRiskClass = newriskClass[1];
                            String npRiskClass = riskCalculationDao.getNPRiskClass();
                            String[] bucketId = riskCalculationDao.getNDIAOnRiskClass(npRiskClass);

                            //added from new CR 2019/09/16
                            //changed by the CR. account should be update NP when passed the ndia 90.
                            if (delinquentAccountBean.getNDIA() == Integer.parseInt(bucketId[1]) && !isManualNP) {
                                riskCalculationDao.getNPDetailsFromLastBillingStatement(delinquentAccountBean, false);
                                delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_NON_PERFORMING_STATUS());
                                delinquentAccountBean.setDelinqstatus(statusVarList.getTO_PERFORMING_TO_NON_PERFORMING_STATUS());
                                //insert GL when account got NP(NpP Interest , Capital & interest(Outstanding))
                                riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                        Configurations.INTEREST_ON_THE_NP_GL, delinquentAccountBean.getNpInterest(), Configurations.DEBIT, null);
                                riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                        Configurations.OUTSTANDING_ON_THE_NP_GL, delinquentAccountBean.getNpOutstanding(), Configurations.DEBIT, null);

                                //deacteve the account status online and backend
                                this.changeAccountStatus(delinquentAccountBean.getAccNo(), statusVarList.getACCOUNT_NON_PERFORMING_STATUS(), statusVarList.getONLINE_DEACTIVE_STATUS());
                                //change the NPSTATUS to automatic NP (1)
                                riskCalculationDao.updateNpStatusCardAccount(delinquentAccountBean.getAccNo(), 1);
                            }

                            delinquentAccountBean.setMIA(Integer.parseInt(newriskClass[1]));

                            String newDelinquentStaus = this.getNEwDelinquentStatus(newriskClass[1], delinquentAccountBean.getRiskClass());
                            delinquentAccountBean.setDelinqstatus(newDelinquentStaus);

                            delinquentAccountBean.setLastStatementDate((Date) lastStmtDetails.get(0));

                            delinquentAccountBean.setDueAmount(lastStmtDetails.get(1).toString());
                            double totalPayments;
                            double newDueAmount = 0;
                            totalPayments = riskCalculationDao.getTotalPaymentSinceLastDue(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE, (Date) lastStmtDetails.get(0));
                            newDueAmount = Double.parseDouble(delinquentAccountBean.getDueAmount()) - totalPayments;
                            delinquentAccountBean.setRemainDue(newDueAmount);
                            delinquentAccountBean.setAssignStatus(statusVarList.getSTATUS_NO());
                            delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                            delinquentAccountBean.setAssignee(Configurations.EOD_USER);

                            riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);
                        }
                        logDetails.add(delinquentAccountBean.getRiskClass());
                        logDetails.add(delinquentAccountBean.getDelinqstatus());
                        logDetails.add(Integer.toString(delinquentAccountBean.getNDIA()));
                        logDetails.add(delinquentAccountBean.getDueAmount());
                        logDetails.add(delinquentAccountBean.getAccStatus());
                    }
                }

                //Provisioning GL
                double oldProvisionAmount = 0.0;
                double provisionGLAmount = 0.0;
                double npCapital = 0.0;
                double remainingCapital = 0.0;
                double calculateProvision = 0.0;
                boolean isTrue = false;

                if (delinquentAccountBean.getNDIA() >= 120) {
                    //if payment done, provision was handled by the 'this.generateProvisionGLWhenPaymentRecieved' method.
                    if (!isPaymentOnCurrentDay) {
                        BigDecimal npOutstandingBig = BigDecimal.valueOf(delinquentAccountBean.getNpOutstanding());
                        BigDecimal npInterestBig = BigDecimal.valueOf(delinquentAccountBean.getNpInterest());
                        BigDecimal remainingCapitalBigDec = new BigDecimal("0.0");
                        BigDecimal provisionAmountStrBig = new BigDecimal("0.0");
                        //npCapital--> BALANCE AFTER PAYMENT KNOCKOFF
                        remainingCapitalBigDec = npOutstandingBig.subtract(npInterestBig);

                        String provisionAmountStr = String.valueOf(delinquentAccountBean.getProvisionAmount());
                        BigDecimal oldProvisionAmountBigDec = new BigDecimal(provisionAmountStr);

                        double provisionPercentage = 0.0;
                        if (delinquentAccountBean.getNDIA() >= 120 && delinquentAccountBean.getNDIA() <= 179) {
                            provisionPercentage = Configurations.PROVISION_PERCENTAGE_NDIA_120_179;
                        } else if (delinquentAccountBean.getNDIA() >= 180 && delinquentAccountBean.getNDIA() <= 239) {
                            provisionPercentage = Configurations.PROVISION_PERCENTAGE_NDIA_180_239;
                        } else if (delinquentAccountBean.getNDIA() > 239) {
                            provisionPercentage = Configurations.PROVISION_PERCENTAGE_NDIA_OVER_239;
                        }

                        BigDecimal provisionPercentageBigDec = new BigDecimal(provisionPercentage).
                                divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);

                        BigDecimal calculateProvisionBigDec = new BigDecimal("0.0");
                        calculateProvisionBigDec = remainingCapitalBigDec.multiply(provisionPercentageBigDec, MathContext.DECIMAL64);
                        calculateProvisionBigDec = calculateProvisionBigDec.setScale(2, RoundingMode.DOWN);
                        calculateProvision = calculateProvisionBigDec.doubleValue();

                        BigDecimal provisionGLAmountBigDec = new BigDecimal("0.0");
                        provisionGLAmountBigDec = calculateProvisionBigDec.subtract(oldProvisionAmountBigDec);
                        provisionGLAmountBigDec = provisionGLAmountBigDec.setScale(2, RoundingMode.DOWN);
                        String provisionGLAmountString = provisionGLAmountBigDec.toString();
                        provisionGLAmount = provisionGLAmountBigDec.doubleValue();
                        //to used npcapital in log summary.
                        npCapital = remainingCapitalBigDec.setScale(2, RoundingMode.DOWN).doubleValue();
                        riskCalculationDao.updateProvisionInDELINQUENTACCOUNT(calculateProvisionBigDec, delinquentAccountBean.getAccNo());
                        if (provisionGLAmount > 0) {
                            riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                    Configurations.PROVISION_GL, provisionGLAmount, Configurations.DEBIT, null);
                            logInfo.info("Inserted GL entries for provision amount for acc no : " + delinquentAccountBean.getAccNo());
                        }
                        isTrue = true;
                    }
                }

                if (isTrue) {
                    detailsProvision.put("Account No", delinquentAccountBean.getAccNo());
                    detailsProvision.put("Old Provision Amount", oldProvisionAmount);
                    detailsProvision.put("NP Capital", npCapital);
                    detailsProvision.put("Provision GL Amount", provisionGLAmount);
                    detailsProvision.put("New Provision Amount", calculateProvision);
                    logInfo.info(logManager.logDetails(detailsProvision));
                    detailsProvision.clear();
                }
                //Configurations.PROCESS_SUCCESS_COUNT++;

                details.put("Old Risk Class", logDetails.get(0));
                details.put("New Risk Class", logDetails.get(1));
                details.put("New Delinquent Status", logDetails.get(2));
                details.put("Number of days in areas", logDetails.get(3));
                details.put("Due Amount", logDetails.get(4));
                details.put("Account Status", logDetails.get(5));
                details.put("Process Status", "Passed");
            } catch (Exception e) {

                details.put("Process Status", "Failed");
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(delinquentAccountBean.getCardNumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                logError.error("RISK_CALCULATION_PROCESS Process for existing cards failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                //Configurations.PROCESS_FAILD_COUNT++;
                faileCardCount.addAndGet(1);
            } finally {
                logInfo.info(logManager.logDetails(details));
                details.clear();
            }
            if (Configurations.PROCESS_FAILD_COUNT > 0) {
                logInfo.info(logManager.logStartEnd("RISK_CALCULATION_PROCESS Process completed for existing cards with errors"));
            } else {
                logInfo.info(logManager.logStartEnd("RISK_CALCULATION_PROCESS Process completed for existing cards without errors"));
            }
        }
    }

    private ArrayList<Object> getLastStmtdate(StringBuffer cardNumber) throws Exception {
        ArrayList<Object> lastStmtDetails;// = new ArrayList<Object>();
        lastStmtDetails = riskCalculationDao.getLastStatementDate(cardNumber);
        return lastStmtDetails;
    }


    //Check for the due date
    private boolean checkForDueDate(Date dueDate) throws Exception {
        boolean isDueDate = false;

        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);
        String S1 = new SimpleDateFormat("MM/dd/yyyy").format(dueDate);
        String S2 = new SimpleDateFormat("MM/dd/yyyy").format(eodDate);
        if (S1.equals(S2)) {
            isDueDate = true;
        }

        return isDueDate;
    }

    private boolean checkForPayment(String accNO, Date EOD_DATE) throws Exception {
        boolean isPaymentOnCurrentDay = false;
        double payment;

        payment = riskCalculationDao.checkForPayment(accNO, EOD_DATE);

        if (payment > 0) {
            isPaymentOnCurrentDay = true;
        }
        return isPaymentOnCurrentDay;

    }

    private boolean isMinimumPaymentTableClear(StringBuffer cardNumber) throws Exception {
        double dueAmount = 0;
        int monthNO = 0;
        HashMap<String, Double> dueAmountList = null;
        dueAmountList = commonRepo.getDueAmountList(cardNumber);
        if (dueAmountList != null && dueAmountList.size() > 0) {
            for (int i = 12; i > 0; i--) {
                dueAmount = dueAmountList.get("M" + i);
                if (dueAmount > 0) {
                    monthNO = i;
                    break;
                }
            }
        }

        return monthNO == 0;

    }

    private boolean checkForResolveStatus(StringBuffer cardNumber, String accNo, String riskClass) throws Exception {
        int monthNo = getLastMinimumPaymentMonth(cardNumber);
        double dueAmount = 0.0, payments = 0.0;
        if (monthNo > 0) {
            ArrayList<Object> lastStmtDetails = riskCalculationDao.getMinimumPaymentExistStatementDate(cardNumber, monthNo);

            payments = riskCalculationDao.getTotalPaymentSinceLastDue(accNo, Configurations.EOD_DATE, (Date) lastStmtDetails.get(0));

            dueAmount = (Double) lastStmtDetails.get(1);
        }
        //according to the NP CR V1.03 minimum payment should be 5% if account get NP
        return payments >= dueAmount;
    }

    private int getLastMinimumPaymentMonth(StringBuffer cardNumber) throws Exception {
        double dueAmount = 0;
        int monthNO = 0;
        HashMap<String, Double> dueAmountList = null;
        dueAmountList = commonRepo.getDueAmountList(cardNumber);
        if (dueAmountList != null && dueAmountList.size() > 0) {
            for (int i = 12; i > 0; i--) {
                dueAmount = dueAmountList.get("M" + i);
                if (dueAmount > 0) {
                    monthNO = i;
                    break;
                }
            }
        }
        return monthNO;
    }

    private boolean checkPaymentEnoughForKnockOff(String accNO, Date EOD_DATE) throws Exception {
        boolean isPaymentOnCurrentDay = false;
        double payment, m1Amount;

        payment = riskCalculationDao.checkForPayment(accNO, EOD_DATE);
        m1Amount = riskCalculationDao.checkLeastMinimumPayment(accNO);

        if (payment > m1Amount) {
            isPaymentOnCurrentDay = true;
        }
        return isPaymentOnCurrentDay;

    }

    public void generateGLForPaymentsKnockOff(boolean minimumTableClear, boolean resolveStatus, DelinquentAccountBean delinquentAccountBean,
                                              StringBuffer cardNumber, String accNo, double paymentAmount) throws Exception {

        LinkedHashMap details2 = new LinkedHashMap();

        List<BigDecimal> balanceAfterSetOffBig = new ArrayList<BigDecimal>();
        List<BigDecimal> KnockOffListBig = new ArrayList<BigDecimal>();
        try {
            BigDecimal remainingBalanceBig = new BigDecimal("0.0");
            BigDecimal remainingTotalFeesBig = new BigDecimal("0.0");

            BigDecimal remainingNPInterestBig = new BigDecimal("0.0");
            BigDecimal remainingNPCapitalBig = new BigDecimal("0.0");
            BigDecimal remainingNPOutstandingBig = new BigDecimal("0.0");
            BigDecimal remainingAccruedInterestBig = new BigDecimal("0.0");
            BigDecimal remainingAccruedLatePayBig = new BigDecimal("0.0");
            BigDecimal remainingAccruedOverLimitBig = new BigDecimal("0.0");
            BigDecimal remainingAccruedOtherFeesBig = new BigDecimal("0.0");

            BigDecimal knocOffNPInterestBig = new BigDecimal("0.0");
            BigDecimal knocOffNPCapitalBig = new BigDecimal("0.0");
            BigDecimal knocOffNPOutstandingBig = new BigDecimal("0.0");
            BigDecimal knocOffAccruedInterestBig = new BigDecimal("0.0");
            BigDecimal knocOffAccruedLatePayBig = new BigDecimal("0.0");
            BigDecimal knocOffAccruedOverLimitBig = new BigDecimal("0.0");
            BigDecimal knocOffAccruedOtherFeesBig = new BigDecimal("0.0");

            BigDecimal totalAccruedBig = new BigDecimal("0.0");

            boolean npDeClassified = false;

            // payment knock-off order should be same as below
            // 1.NPINTEREST 2.NPCAPITAL 3. ACCRUEDINTEREST 4. ACCRUEDLATEPAYMENT 5. ACCRUEDOVERLIMIT 6. ACCRUEDOTHERFEES
            // according to this order paymentKnockOffList,balanceAfterSetOff & KnockOffList has been created.
            // if you want to chnage the order those 3 list should be change accordingly.
            List<BigDecimal> paymentKnockOffList = riskCalculationDao.getDelinquentAccountDetailsAsList(accNo);

            details2.put("Acc No", accNo);
            details2.put("NDIA", delinquentAccountBean.getNDIA());
            details2.put("NP Interest", paymentKnockOffList.get(0));
            details2.put("NP Capital", paymentKnockOffList.get(1));
            details2.put("NP Accrued Interest", paymentKnockOffList.get(2));
            details2.put("NP Accrued Late Payment", paymentKnockOffList.get(3));
            details2.put("NP Accrued Over Limit", paymentKnockOffList.get(4));
            details2.put("NP Accrued Other Fees", paymentKnockOffList.get(5));
            details2.put("Payment Amount", paymentAmount);
            details2.put("*************", "*************");

            remainingBalanceBig = new BigDecimal(paymentAmount);

            boolean isBreak = true;

            for (BigDecimal amount : paymentKnockOffList) {
                if (isBreak) {
                    if (remainingBalanceBig.compareTo(amount) > 0) {
                        balanceAfterSetOffBig.add(BigDecimal.ZERO);
                        KnockOffListBig.add(amount);
                        remainingBalanceBig = remainingBalanceBig.subtract(amount);
                    } else {
                        balanceAfterSetOffBig.add(amount.subtract(remainingBalanceBig));
                        KnockOffListBig.add(remainingBalanceBig);
                        remainingBalanceBig = BigDecimal.ZERO;
                        isBreak = false;
                    }
                } else {
                    balanceAfterSetOffBig.add(amount);
                    KnockOffListBig.add(BigDecimal.ZERO);
                }
            }

            for (int i = 0; i < balanceAfterSetOffBig.size(); i++) {
                switch (i) {
                    case 0:
                        remainingNPInterestBig = balanceAfterSetOffBig.get(i);
                        break;
                    case 1:
                        remainingNPCapitalBig = balanceAfterSetOffBig.get(i);
                        break;
                    case 2:
                        remainingAccruedInterestBig = balanceAfterSetOffBig.get(i);
                        break;
                    case 3:
                        remainingAccruedLatePayBig = balanceAfterSetOffBig.get(i);
                        break;
                    case 4:
                        remainingAccruedOverLimitBig = balanceAfterSetOffBig.get(i);
                        break;
                    case 5:
                        remainingAccruedOtherFeesBig = balanceAfterSetOffBig.get(i);
                        break;
                }
            }

            for (int i = 0; i < KnockOffListBig.size(); i++) {
                switch (i) {
                    case 0:
                        knocOffNPInterestBig = KnockOffListBig.get(i);
                        break;
                    case 1:
                        knocOffNPCapitalBig = KnockOffListBig.get(i);
                        break;
                    case 2:
                        knocOffAccruedInterestBig = KnockOffListBig.get(i);
                        break;
                    case 3:
                        knocOffAccruedLatePayBig = KnockOffListBig.get(i);
                        break;
                    case 4:
                        knocOffAccruedOverLimitBig = KnockOffListBig.get(i);
                        break;
                    case 5:
                        knocOffAccruedOtherFeesBig = KnockOffListBig.get(i);
                        break;
                }
            }

            details2.put("Knock Off NP Interest", knocOffNPInterestBig);
            details2.put("Knock Off NP Capital", knocOffNPCapitalBig);
            details2.put("Knock Off NP Accrued Interest", knocOffAccruedInterestBig);
            details2.put("Knock Off NP Accrued Late Payment", knocOffAccruedLatePayBig);
            details2.put("Knock Off NP Accrued Over Limit", knocOffAccruedOverLimitBig);
            details2.put("Knock Off NP Accrued Other Fees", knocOffAccruedOtherFeesBig);

            remainingNPOutstandingBig = remainingNPInterestBig.add(remainingNPCapitalBig);

            knocOffNPOutstandingBig = knocOffNPInterestBig.add(knocOffNPCapitalBig);

            remainingTotalFeesBig = remainingAccruedLatePayBig.add(remainingAccruedOverLimitBig).add(remainingAccruedOtherFeesBig);

            delinquentAccountBean.setNpInterest(remainingNPInterestBig.doubleValue());
            delinquentAccountBean.setNpOutstanding(remainingNPOutstandingBig.doubleValue());

            if (minimumTableClear || resolveStatus) {
                npDeClassified = true;
            }

            if (npDeClassified) {
                riskCalculationDao.updateAllDELINQUENTACCOUNTnpdetails(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, accNo);
                delinquentAccountBean.setNpDate(null);
                delinquentAccountBean.setNpInterest(0.0);
                delinquentAccountBean.setNpOutstanding(0.0);

                details2.put("Knock Off Status", "Passed");
                logInfo.info("Successfully updated remaining balance of NP Details after payment of " + paymentAmount + " for accNo : " + accNo);
                logInfo.info(logManager.logDetails(details2));
                details2.clear();

                //Generate gl when receiving payment for np accounts. (NP CR 2019/09/25)
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NPINTEREST_GL, knocOffNPInterestBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NPOUTSTANDING_GL, knocOffNPOutstandingBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_INTEREST_GL, knocOffAccruedInterestBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_LATE_FEE_GL, knocOffAccruedLatePayBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_OVERLIMIT_FEE_GL, knocOffAccruedOverLimitBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_OTHER_FEES_GL, knocOffAccruedOtherFeesBig, Configurations.DEBIT, null);

                //Generate gl when receiving payment with NP Declassified for np accounts. (NP CR 2019/09/25)
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NP_INTEREST_DECLASSIFIED_GL, remainingNPInterestBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL, remainingNPOutstandingBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_INTEREST_DECLASSIFIED_GL, remainingAccruedInterestBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_LATE_FEE_DECLASSIFIED_GL, remainingAccruedLatePayBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_OVERLIMIT_FEE_DECLASSIFIED_GL, remainingAccruedOverLimitBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_OTHER_FEES_DECLASSIFIED_GL, remainingAccruedOtherFeesBig, Configurations.DEBIT, null);

                //generate nonperforming accrued balances into performing accounts(Performing loan)
                //eg. Credit-> NP Credit Cards, Debit -> Performing Loan
                totalAccruedBig = paymentKnockOffList.get(2).add(paymentKnockOffList.get(3))
                        .add(paymentKnockOffList.get(4)).add(paymentKnockOffList.get(5));

                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL, totalAccruedBig, Configurations.DEBIT, null);

            } else {

                riskCalculationDao.updateAllDELINQUENTACCOUNTnpdetails(remainingNPInterestBig, remainingNPOutstandingBig,
                        remainingAccruedInterestBig, remainingAccruedOverLimitBig,
                        remainingAccruedLatePayBig, remainingAccruedOtherFeesBig, accNo);

                details2.put("Knock Off Status", "Passed");
                logInfo.info("Successfully updated remaining balance of NP Details after payment of " + paymentAmount + " for accNo : " + accNo);
                logInfo.info(logManager.logDetails(details2));
                details2.clear();

                //Generate gl when receiving payment for np accounts. (NP CR 2019/09/25)
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NPINTEREST_GL, knocOffNPInterestBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NPOUTSTANDING_GL, knocOffNPOutstandingBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_INTEREST_GL, knocOffAccruedInterestBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_LATE_FEE_GL, knocOffAccruedLatePayBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_OVERLIMIT_FEE_GL, knocOffAccruedOverLimitBig, Configurations.DEBIT, null);
                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_ACCRUED_OTHER_FEES_GL, knocOffAccruedOtherFeesBig, Configurations.DEBIT, null);

                //generate nonperforming accrued balances into performing accounts(Performing loan)
                //eg. Credit-> NP Credit Cards, Debit -> Performing Loan
                totalAccruedBig = knocOffAccruedInterestBig.add(knocOffAccruedLatePayBig)
                        .add(knocOffAccruedOverLimitBig).add(knocOffAccruedOtherFeesBig);

                riskCalculationDao.insertIntoEodGLAccountBigDecimal(Configurations.EOD_ID, Configurations.EOD_DATE, cardNumber,
                        Configurations.KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL, totalAccruedBig, Configurations.DEBIT, null);

            }

            logInfo.info("Inserted GL entries for payment knock off for accNo : " + accNo);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void generateProvisionGLWhenPaymentRecieved(DelinquentAccountBean delinquentAccountBean,
                                                       StringBuffer cardNumber, String accNo, double paymentAmount) throws Exception {
        try {
            //provision knock off with payments.
            LinkedHashMap details3 = new LinkedHashMap();

            double oldProvisionAmount = delinquentAccountBean.getProvisionAmount();

            if (oldProvisionAmount > 0) {
                double provisionGLAmount = 0.0;
                double calculateProvision = 0.0;

                double npCapital = 0.0;
                double remainingCapital = 0.0;
                boolean isTrue = false;
                String provisionAmountStr = String.valueOf(delinquentAccountBean.getProvisionAmount());
                BigDecimal oldProvisionAmountBigDec = new BigDecimal(provisionAmountStr);
                npCapital = delinquentAccountBean.getNpOutstanding() - delinquentAccountBean.getNpInterest();
                String npCapitalString = String.valueOf(npCapital);
                BigDecimal remainingCapitalBigDec = new BigDecimal(npCapitalString);
                remainingCapital = remainingCapitalBigDec.doubleValue();

                if (delinquentAccountBean.getNDIA() >= 120) {
                    double provisionPercentage = 0.0;
                    if (delinquentAccountBean.getNDIA() >= 120 && delinquentAccountBean.getNDIA() <= 179) {
                        provisionPercentage = Configurations.PROVISION_PERCENTAGE_NDIA_120_179;
                    } else if (delinquentAccountBean.getNDIA() >= 180 && delinquentAccountBean.getNDIA() <= 239) {
                        provisionPercentage = Configurations.PROVISION_PERCENTAGE_NDIA_180_239;
                    } else if (delinquentAccountBean.getNDIA() > 239) {
                        provisionPercentage = Configurations.PROVISION_PERCENTAGE_NDIA_OVER_239;
                    }

                    BigDecimal provisionPercentageBigDec = new BigDecimal(provisionPercentage).
                            divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);

                    BigDecimal calculateProvisionBigDec = new BigDecimal("0.0");
                    calculateProvisionBigDec = remainingCapitalBigDec.multiply(provisionPercentageBigDec, MathContext.DECIMAL64);
                    calculateProvisionBigDec = calculateProvisionBigDec.setScale(2, RoundingMode.DOWN);
                    calculateProvision = calculateProvisionBigDec.doubleValue();

                    BigDecimal provisionGLAmountBigDec = new BigDecimal("0.0");
                    provisionGLAmountBigDec = oldProvisionAmountBigDec.subtract(calculateProvisionBigDec);
                    provisionGLAmountBigDec = provisionGLAmountBigDec.setScale(2, RoundingMode.DOWN);
                    provisionGLAmount = provisionGLAmountBigDec.doubleValue();
                    //to used npcapital in log summary.
                    npCapital = remainingCapitalBigDec.setScale(2, RoundingMode.DOWN).doubleValue();
                    riskCalculationDao.updateProvisionInDELINQUENTACCOUNT(calculateProvisionBigDec, delinquentAccountBean.getAccNo());
                    if (provisionGLAmount > 0) {
                        riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                Configurations.PROVISION_KNOCK_OFF_GL, provisionGLAmount, Configurations.DEBIT, null);
                        logInfo.info("Inserted GL entries for provision after payment knock off for accNo : " + accNo);
                    }
                    isTrue = true;
                } else {
                    BigDecimal calculateProvisionBigDec = new BigDecimal("0.0");
                    calculateProvision = 0.0;

                    BigDecimal provisionGLAmountBigDec = new BigDecimal("0.0");
                    provisionGLAmountBigDec = oldProvisionAmountBigDec.subtract(calculateProvisionBigDec);
                    provisionGLAmountBigDec = provisionGLAmountBigDec.setScale(2, RoundingMode.DOWN);
                    provisionGLAmount = provisionGLAmountBigDec.doubleValue();
                    //to used npcapital in log summary.
                    npCapital = remainingCapitalBigDec.setScale(2, RoundingMode.DOWN).doubleValue();
                    riskCalculationDao.updateProvisionInDELINQUENTACCOUNT(calculateProvisionBigDec, delinquentAccountBean.getAccNo());
                    if (provisionGLAmount > 0) {
                        riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                                Configurations.PROVISION_KNOCK_OFF_GL, provisionGLAmount, Configurations.DEBIT, null);
                        logInfo.info(("Inserted GL entries for provision after payment knock off for accNo : " + accNo));
                    }
                    isTrue = true;
                }

                details3.put("Account No", accNo);
                details3.put("Old Provision Amount", oldProvisionAmount);
                details3.put("Provision GL Amount", provisionGLAmount);
                details3.put("New Provision Amoun", calculateProvision);
                details3.put("Knock Off Status for Provision", "Passed");
                logInfo.info("Successfully updated remaining balance of Provision Amount after payment of " + paymentAmount + " for accNo : " + accNo);
                logInfo.info(logManager.logDetails(details3));
                details3.clear();
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    private ArrayList<String> updateDelinquentOnPayments(boolean isManualNp, boolean resolveStatus, double totalPayments, DelinquentAccountBean delinquentAccountBean, boolean minimumPaymentClearStatus) throws Exception {
        String remark = null;
        ArrayList<String> logDetails = new ArrayList<String>();
        //minimumPaymentClearStatus -> minimum payment dues ara all settled
        if (resolveStatus || minimumPaymentClearStatus) {
            if (delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_ZERO()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_ONE()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_TWO()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_THREE())) {
                //Send to delinquent status to resolve
                logDetails.add(delinquentAccountBean.getRiskClass());
                String oldRiskClass = delinquentAccountBean.getRiskClass();
                String newRiskClass = statusVarList.getRISK_CLASS_ZERO();
                //risk class eka zero karanna
                remark = "Risk class change from " + oldRiskClass + " to " + newRiskClass;
                if (!isManualNp) {
                    this.changeAccountStatus(delinquentAccountBean.getAccNo(), statusVarList.getACCOUNT_ACTIVE_STATUS(), statusVarList.getONLINE_CARD_ACTIVE_STATUS());
                    //change the NPSTATUS to Non NP (0)
                    riskCalculationDao.updateNpStatusCardAccount(delinquentAccountBean.getAccNo(), 0);
                    delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_ACTIVE_STATUS());
                    delinquentAccountBean.setDelinqstatus(statusVarList.getTO_RESOLVE_STATUS());
                } else {
                    delinquentAccountBean.setDelinqstatus(statusVarList.getONLY_MANUAL_NP_STATUS());
                }
                delinquentAccountBean.setRiskClass(newRiskClass);
                delinquentAccountBean.setNDIA(0);
                delinquentAccountBean.setMIA(0);
                delinquentAccountBean.setDueAmount("0.00");
                delinquentAccountBean.setRemainDue(0);
                delinquentAccountBean.setAssignStatus(statusVarList.getSTATUS_NO());
                delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                delinquentAccountBean.setAssignee(Configurations.EOD_USER);
                logDetails.add(delinquentAccountBean.getRiskClass());
                logDetails.add(delinquentAccountBean.getDelinqstatus());
                logDetails.add(Integer.toString(delinquentAccountBean.getNDIA()));
                logDetails.add(delinquentAccountBean.getDueAmount());
                logDetails.add(delinquentAccountBean.getAccStatus());
                riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);
                commonRepo.insertIntoDelinquentHistory(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), remark);

            } else if (delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_FOUR()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_FIVE()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_SIX())
                    || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_SEVEN()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_EIGHT()) || delinquentAccountBean.getRiskClass().equals(statusVarList.getRISK_CLASS_NINE())) {
                //add risk 9th (V1.09)
                //Send to delinquent status to resolve
                logDetails.add(delinquentAccountBean.getRiskClass());
                String oldRiskClass = delinquentAccountBean.getRiskClass();
                String newRiskClass = statusVarList.getRISK_CLASS_ZERO();
                remark = "Risk class change from " + oldRiskClass + " to " + newRiskClass;
                if (!isManualNp) {
                    delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_ACTIVE_STATUS());
                    this.changeAccountStatus(delinquentAccountBean.getAccNo(), statusVarList.getACCOUNT_ACTIVE_STATUS(), statusVarList.getONLINE_CARD_ACTIVE_STATUS());
                    //change the NPSTATUS to Non NP (0)
                    riskCalculationDao.updateNpStatusCardAccount(delinquentAccountBean.getAccNo(), 0);
                    delinquentAccountBean.setDelinqstatus(statusVarList.getTO_RESOLVE_STATUS());
                } else {
                    delinquentAccountBean.setDelinqstatus(statusVarList.getONLY_MANUAL_NP_STATUS());
                }
                delinquentAccountBean.setRiskClass(statusVarList.getRISK_CLASS_ZERO());
                delinquentAccountBean.setNDIA(0);
                delinquentAccountBean.setMIA(0);
                delinquentAccountBean.setDueAmount("0.00");
                delinquentAccountBean.setRemainDue(0);
                delinquentAccountBean.setAssignStatus(statusVarList.getSTATUS_NO());
                delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                delinquentAccountBean.setAssignee(Configurations.EOD_USER);
                logDetails.add(delinquentAccountBean.getRiskClass());
                logDetails.add(delinquentAccountBean.getDelinqstatus());
                logDetails.add(Integer.toString(delinquentAccountBean.getNDIA()));
                logDetails.add(delinquentAccountBean.getDueAmount());
                logDetails.add(delinquentAccountBean.getAccStatus());
                riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);
                commonRepo.insertIntoDelinquentHistory(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), remark);
            }

        } else {
            int monthNo;
            int dueCount;
            double newDueAmount = 0;
            String[] newriskClass = new String[3];
            String newDelinquentStaus;
            boolean isSameRiskClass;
            ArrayList<Object> lastStmtDetails;

            lastStmtDetails = this.getLastStmtdate(delinquentAccountBean.getCardNumber());
            logDetails.add(delinquentAccountBean.getRiskClass());

            //deduct the all the min payment by the total payments
            dueCount = checkPaymentStatus(totalPayments, delinquentAccountBean.getCardNumber());


            //need to change as not define bucket's ndia should come with 9th bucket(V1.09)
            newriskClass = this.calculateRiskClass(1, delinquentAccountBean.getCardNumber());
            newDelinquentStaus = this.getNEwDelinquentStatus(newriskClass[1], delinquentAccountBean.getRiskClass());

            isSameRiskClass = this.compareRiskClass(newriskClass[1], delinquentAccountBean.getRiskClass());
            remark = "Risk class change from " + delinquentAccountBean.getRiskClass() + " to " + newriskClass[1];

            //If Due date process need to change dueDate on delinquent also
            if (delinquentAccountBean.getIsdueDate() == 1) {
                delinquentAccountBean.setDueDate((Date) lastStmtDetails.get(2));

                // in the due date, if the customer paid the total over due without this month due amount,
                // account should remove automatic np and going to calculate ndia from 0 as usual account
                if (delinquentAccountBean.getAccStatus().equalsIgnoreCase(statusVarList.getACCOUNT_NON_PERFORMING_STATUS())
                        && newriskClass[0].equals("0") && !isManualNp) {
                    delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_ACTIVE_STATUS());
                    this.changeAccountStatus(delinquentAccountBean.getAccNo(), statusVarList.getACCOUNT_ACTIVE_STATUS(), statusVarList.getONLINE_CARD_ACTIVE_STATUS());
                    //change the NPSTATUS to Non NP (0)
                    riskCalculationDao.updateNpStatusCardAccount(delinquentAccountBean.getAccNo(), 0);
                }
            }

            //(V1.09)
            if (!newriskClass[1].equals(statusVarList.getRISK_CLASS_NINE())) {
                String npRiskClass = riskCalculationDao.getNPRiskClass();
                String[] bucketId = riskCalculationDao.getNDIAOnRiskClass(npRiskClass);
                //changed by the CR. account should be update NP when passed the ndia 90.
                if (newriskClass[0] == bucketId[1] && !isManualNp) {
                    riskCalculationDao.getNPDetailsFromLastBillingStatement(delinquentAccountBean, false);
                    delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_NON_PERFORMING_STATUS());
                    //insert GL when account got NP(NpP Interest , Capital & interest(Outstanding))
                    riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                            Configurations.INTEREST_ON_THE_NP_GL, delinquentAccountBean.getNpInterest(), Configurations.DEBIT, null);
                    riskCalculationDao.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, delinquentAccountBean.getCardNumber(),
                            Configurations.OUTSTANDING_ON_THE_NP_GL, delinquentAccountBean.getNpOutstanding(), Configurations.DEBIT, null);
                    //deacteve the account status online and backend
                    this.changeAccountStatus(delinquentAccountBean.getAccNo(), statusVarList.getACCOUNT_NON_PERFORMING_STATUS(), statusVarList.getONLINE_DEACTIVE_STATUS());
                    //change the NPSTATUS to automatic NP (1)
                    riskCalculationDao.updateNpStatusCardAccount(delinquentAccountBean.getAccNo(), 1);
                }
            }
            if (isSameRiskClass) {
                delinquentAccountBean.setDelinqstatus(newDelinquentStaus);
                delinquentAccountBean.setRiskClass(newriskClass[1]);
                delinquentAccountBean.setNDIA(Integer.parseInt(newriskClass[0]));
                delinquentAccountBean.setMIA(Integer.parseInt(newriskClass[1]));
                logDetails.add(delinquentAccountBean.getRiskClass());
                logDetails.add(delinquentAccountBean.getDelinqstatus());
                logDetails.add(Integer.toString(delinquentAccountBean.getNDIA()));
                logDetails.add(delinquentAccountBean.getDueAmount());
                logDetails.add(delinquentAccountBean.getAccStatus());

                delinquentAccountBean.setDueAmount(lastStmtDetails.get(1).toString());
                newDueAmount = Double.parseDouble(delinquentAccountBean.getDueAmount()) - totalPayments;
                delinquentAccountBean.setRemainDue(newDueAmount);

                //update min amounts of minpayment according to payments
                riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);

            } else {
                String delinq = delinquentAccountBean.getDelinqstatus();
                if (!delinq.equalsIgnoreCase(statusVarList.getTO_PERFORMING_TO_NON_PERFORMING_STATUS())) {
                    delinquentAccountBean.setDelinqstatus(newDelinquentStaus);
                }
                delinquentAccountBean.setRiskClass(newriskClass[1]);
                delinquentAccountBean.setNDIA(Integer.parseInt(newriskClass[0]));
                delinquentAccountBean.setMIA(Integer.parseInt(newriskClass[1]));
                delinquentAccountBean.setAssignStatus(statusVarList.getSTATUS_NO());
                delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                delinquentAccountBean.setAssignee(Configurations.EOD_USER);
                logDetails.add(delinquentAccountBean.getRiskClass());
                logDetails.add(delinquentAccountBean.getDelinqstatus());
                logDetails.add(Integer.toString(delinquentAccountBean.getNDIA()));
                logDetails.add(delinquentAccountBean.getDueAmount());
                logDetails.add(delinquentAccountBean.getAccStatus());

                delinquentAccountBean.setDueAmount(lastStmtDetails.get(1).toString());

                totalPayments = riskCalculationDao.getTotalPaymentSinceLastDue(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE, (Date) lastStmtDetails.get(0));
                newDueAmount = Double.parseDouble(delinquentAccountBean.getDueAmount()) - totalPayments;
                delinquentAccountBean.setRemainDue(newDueAmount);
                riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);
                commonRepo.insertIntoDelinquentHistory(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), remark);
            }
        }
        return logDetails;
    }

    private String[] calculateRiskClass(int monthNo, StringBuffer cardNo) throws Exception {
        Date dueDate;
        int noOfDates;
        String[] newriskClass = new String[3];
        dueDate = riskCalculationDao.getDueDateOnRiskClass(monthNo, cardNo);
        noOfDates = CommonMethods.getNoOfDaysDifference(dueDate, Configurations.EOD_DATE);
        //need to change as not define ndia should come with 9th bucket
        newriskClass = riskCalculationDao.getRiskclassOnNdia(noOfDates);
        return newriskClass;
    }

    private boolean compareRiskClass(String newriskClass, String riskClass) {
        boolean isSameRiskClass = newriskClass.equals(riskClass);
        return isSameRiskClass;
    }

    private int checkPaymentStatus(double totalPayments, StringBuffer cardNo) throws Exception {
        HashMap<String, Double> dueAmountList = null;
        HashMap<String, java.util.Date> dueDateList = null;
        HashMap<String, Double> newDueAmountList = new HashMap<String, Double>();
        HashMap<String, java.util.Date> newDueDateList = new HashMap<String, java.util.Date>();
        double dueAmount = 0;
        double remainAmounts = 0;
        double zeroAmount = 0.0;
        int monthNO = 0;
        int dueCount = 0;
        boolean isEmpty = true;

        try {
            //get DueAmounts & DueDates seprately
            dueAmountList = commonRepo.getDueAmountList(cardNo);
            dueDateList = riskCalculationDao.getDueDateList(cardNo);

            if (dueAmountList != null && dueAmountList.size() > 0) {
                for (int i = 12; i > 0; i--) {
                    dueAmount = dueAmountList.get("M" + i);
                    if (dueAmount > 0 && dueAmount <= totalPayments) {
                        monthNO = i;
                        break;
                    }
                }
            }

            if (totalPayments > 0) {
                //deduct the all the min payment by the total payments
                for (int i = 12; i > 0; i--) {
                    dueAmount = dueAmountList.get("M" + i);
                    remainAmounts = dueAmount - totalPayments;

                    if (remainAmounts >= 0) {
                        dueAmountList.put("M" + i, remainAmounts);
                    } else {
                        dueAmountList.put("M" + i, 0.0);
                    }
                }

                int newMonthNo = monthNO + 1;
                //swap the 0 balance dues from the min payment
                for (int i = 1; i < 13; i++) {
                    if (isEmpty) {
                        newDueAmountList.put("M" + i, dueAmountList.get("M" + newMonthNo));
                        newDueDateList.put("M" + i, dueDateList.get("M" + newMonthNo));
                        if (newMonthNo == 13) {
                            isEmpty = false;
                        }
                        if (isEmpty) {
                            if (dueAmountList.get("M" + newMonthNo) > 0) {
                                dueCount++;
                            }
                        } else {
                            newDueAmountList.put("M" + i, zeroAmount);
                            newDueDateList.put("M" + i, null);
                        }
                        newMonthNo++;
                    } else {
                        newDueAmountList.put("M" + i, zeroAmount);
                        newDueDateList.put("M" + i, null);
                    }
                }
                int count = riskCalculationDao.updateMinimumPayment(cardNo, newDueAmountList, newDueDateList, dueCount);
            }
        } catch (Exception ex) {
            throw ex;
        }
        return dueCount;
    }

    private int changeAccountStatus(String accNo, String backendStatus, int onlineStatus) throws Exception {
        int flag = 0;
        flag = riskCalculationDao.updateAccountStatus(accNo, backendStatus);
        flag = riskCalculationDao.updateOnlineAccountStatus(accNo, onlineStatus);
        return flag;
    }

    private String getNEwDelinquentStatus(String newriskClass, String currentRiskClass) throws Exception {
        String newDelinquentStaus = statusVarList.getTO_ACTIVE_STATUS();
        if (currentRiskClass.equals(statusVarList.getRISK_CLASS_FOUR()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_FIVE()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_SIX()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_SEVEN()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_EIGHT()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_NINE())) {
            if (newriskClass.equals(statusVarList.getRISK_CLASS_FOUR()) || newriskClass.equals(statusVarList.getRISK_CLASS_FIVE()) || newriskClass.equals(statusVarList.getRISK_CLASS_SIX()) || newriskClass.equals(statusVarList.getRISK_CLASS_SEVEN()) || newriskClass.equals(statusVarList.getRISK_CLASS_EIGHT()) || newriskClass.equals(statusVarList.getRISK_CLASS_NINE())) {
                newDelinquentStaus = statusVarList.getTO_NON_PERFORMING_TO_NON_PERFORMING_STATUS();
            } else if (newriskClass.equals(statusVarList.getRISK_CLASS_ONE()) || newriskClass.equals(statusVarList.getRISK_CLASS_TWO()) || newriskClass.equals(statusVarList.getRISK_CLASS_THREE())) {
                newDelinquentStaus = statusVarList.getTO_NON_PERFORMING_TO_PERFORMING_STATUS();
            }
        } else if (currentRiskClass.equals(statusVarList.getRISK_CLASS_ONE()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_TWO()) || currentRiskClass.equals(statusVarList.getRISK_CLASS_THREE())) {
            if (newriskClass.equals(statusVarList.getRISK_CLASS_FOUR()) || newriskClass.equals(statusVarList.getRISK_CLASS_FIVE()) || newriskClass.equals(statusVarList.getRISK_CLASS_SIX()) || newriskClass.equals(statusVarList.getRISK_CLASS_SEVEN()) || newriskClass.equals(statusVarList.getRISK_CLASS_EIGHT()) || newriskClass.equals(statusVarList.getRISK_CLASS_NINE())) {
                newDelinquentStaus = statusVarList.getTO_PERFORMING_TO_NON_PERFORMING_STATUS();
            } else if (newriskClass.equals(statusVarList.getRISK_CLASS_ONE()) || newriskClass.equals(statusVarList.getRISK_CLASS_TWO()) || newriskClass.equals(statusVarList.getRISK_CLASS_THREE())) {
                newDelinquentStaus = statusVarList.getTO_PERFORMING_TO_PERFORMING_STATUS();
            }
        }
        return newDelinquentStaus;
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void freshCardToTable(RiskCalculationBean riskCalculationBean, ProcessBean processBean,AtomicInteger faileCardCount) {
        if (!Configurations.isInterrupted) {
            String[] newriskClass;
            String maskedCardNumber = CommonMethods.cardNumberMask(riskCalculationBean.getCardNo());
            noOfNewCards++;
            DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
            LinkedHashMap details = new LinkedHashMap();
            int count = 0;


            try {
                String remark = null;
                double totalPayments;
                double minPayments;
                delinquentAccountBean = riskCalculationDao.setDelinquentAccountDetails(riskCalculationBean.getCardNo());
                boolean isManualNP = riskCalculationDao.isManualNp(delinquentAccountBean.getAccNo());
                totalPayments = riskCalculationDao.getTotalPaymentSinceLastDue(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE, delinquentAccountBean.getLastStatementDate());
                minPayments = riskCalculationDao.getMinPaymentFromBilling(delinquentAccountBean.getAccNo());
                if (minPayments > totalPayments) {
                    delinquentAccountBean.setNDIA(CommonMethods.getNoOfDaysDifference(riskCalculationBean.getDueDate(), Configurations.EOD_DATE));
                    newriskClass = riskCalculationDao.getRiskclassOnNdia(delinquentAccountBean.getNDIA());
                    delinquentAccountBean.setRiskClass(newriskClass[1]);
                    delinquentAccountBean.setMIA(Integer.parseInt(newriskClass[1]));
                    remark = "A fresh account for delinquent table";
                    delinquentAccountBean.setCardNumber(riskCalculationBean.getCardNo());
                    delinquentAccountBean.setDueAmount(Double.toString(riskCalculationBean.getDueAmount()));
                    delinquentAccountBean.setRemainDue(riskCalculationBean.getDueAmount() - totalPayments);
                    delinquentAccountBean.setAssignStatus(statusVarList.getSTATUS_NO());
                    delinquentAccountBean.setSupervisor(Configurations.EOD_USER);
                    delinquentAccountBean.setAssignee(Configurations.EOD_USER);
                    delinquentAccountBean.setDueDate(riskCalculationBean.getDueDate());
                    if (isManualNP) {
                        delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_NON_PERFORMING_STATUS());
                        delinquentAccountBean.setDelinqstatus(statusVarList.getTO_PERFORMING_TO_NON_PERFORMING_STATUS());
                    } else {
                        delinquentAccountBean.setDelinqstatus(statusVarList.getTO_ACTIVE_STATUS());
                    }
                    details.put("Account Number", delinquentAccountBean.getAccNo());
                    details.put("Card Number", delinquentAccountBean.getCardNumber());
                    details.put("New Risk Class", maskedCardNumber);
                    details.put("New Delinquent Status", delinquentAccountBean.getDelinqstatus());
                    details.put("Number of days in areas", delinquentAccountBean.getNDIA());
                    details.put("Due Amount", delinquentAccountBean.getDueAmount());
                    details.put("Account Status", delinquentAccountBean.getAccStatus());
                    details.put("Process Status", "Passed");
                    if (isManualNP) {
                        delinquentAccountBean.setAccStatus(statusVarList.getACCOUNT_NON_PERFORMING_STATUS());
                        delinquentAccountBean.setDelinqstatus(statusVarList.getTO_PERFORMING_TO_NON_PERFORMING_STATUS());
                    }
                    count = riskCalculationDao.addDetailsToDelinquentAccountTable(delinquentAccountBean);
                    commonRepo.insertIntoDelinquentHistory(delinquentAccountBean.getCardNumber(), delinquentAccountBean.getAccNo(), remark);
                }
                //Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                //Configurations.PROCESS_FAILD_COUNT++;
                faileCardCount.addAndGet(1);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(riskCalculationBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                details.put("Process Status", "Failed");
                logError.error("RISK_CALCULATION_PROCESS Process for new cards failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
