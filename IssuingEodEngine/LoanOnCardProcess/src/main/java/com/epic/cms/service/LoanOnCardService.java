package com.epic.cms.service;

import com.epic.cms.model.bean.*;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InstallmentPaymentRepo;
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
import java.math.RoundingMode;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoanOnCardService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    public LogManager logManager;
    @Autowired
    public InstallmentPaymentRepo installmentPaymentRepo;
    @Autowired
    public CommonRepo commonRepo;
    @Autowired
    public StatusVarList status;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void accelerateLOCRequestForNpAccount() throws Exception {
        try {
            /**
             * manual NP accounts
             */
            logInfo.info(logManager.logStartEnd("Loan On Card Process Manual NP Acceleration Started"));
            List<ManualNpRequestBean> manualNpList = installmentPaymentRepo.getManualNpRequestDetails(status.getYES_STATUS_1(), status.getCOMMON_REQUEST_ACCEPTED());
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += manualNpList.size();
            for (ManualNpRequestBean manualNpRequestBean : manualNpList) {
                String accNo = manualNpRequestBean.getAccNumber();
                try {
                    //update Loan On Card requests for corresponding accNo to Accelerate status.
                    installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "LOANONCARDREQUEST");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    logInfo.info("Loan On Card process success for accNo " + accNo + " when Accelerate Loan On Card for manual NP. ");
                } catch (Exception e) {
                    //con.rollback();
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("Loan On Card process failed for accNo " + accNo + " when Accelerate Loan On Card for manual NP. ", e);
                }
            }
            /* PADSS Change -
               variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
            manualNpList.clear();

            logInfo.info(logManager.logStartEnd("Loan On Card Process Manual NP Acceleration Finished"));
            /**
             * automatic NP accounts
             */
            logInfo.info(logManager.logStartEnd("Loan On Card Process Automatic NP Acceleration Started"));
            List<DelinquentAccountBean> delinquentAccountList = installmentPaymentRepo.getDelinquentAccounts();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += delinquentAccountList.size();
            for (DelinquentAccountBean delinquentAccountBean : delinquentAccountList) {
                String accNo = delinquentAccountBean.getAccNo();
                boolean isPaymentOnCurrentDay = false;
                String[] newRiskClass;
                try {
                    //check for payment on current date
                    double payment = installmentPaymentRepo.checkForPayment(accNo, Configurations.EOD_DATE);
                    if (payment > 0) {
                        isPaymentOnCurrentDay = true;
                    }
                    delinquentAccountBean.setNDIA(delinquentAccountBean.getNDIA() + 1);
                    newRiskClass = installmentPaymentRepo.getRiskClassOnNdia(delinquentAccountBean.getNDIA());
                    delinquentAccountBean.setRiskClass(newRiskClass[1]);
                    String npRiskClass = installmentPaymentRepo.getNPRiskClass();
                    String[] bucketId = installmentPaymentRepo.getNDIAOnRiskClass(npRiskClass);
                    if (!isPaymentOnCurrentDay) {
                        //added from new CR 2019/09/16
                        //changed by the CR. account should be update NP when passed the ndia 90.
                        if (delinquentAccountBean.getNDIA() == Integer.parseInt(bucketId[1])) {
                            try {
                                //update Loan On Card requests for corresponding accno to Accelerate status.
                                installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "LOANONCARDREQUEST");
                                Configurations.PROCESS_SUCCESS_COUNT++;
                                logInfo.info("Loan On Card process success for accNo " + accNo + " when Accelerate Loan On Card for Automatic NP. ");
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    } else {
                        boolean enoughPaymentForKnockOff = checkPaymentEnoughForKnockOff(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                        //if payment is not enough to knock off, check whether it's get NP Account
                        if (!enoughPaymentForKnockOff) {
                            if (delinquentAccountBean.getNDIA() == Integer.parseInt(bucketId[1])) {
                                try {
                                    //update Loan On Card requests for corresponding accno to Accelerate status.
                                    installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "LOANONCARDREQUEST");
                                    Configurations.PROCESS_SUCCESS_COUNT++;
                                    logInfo.info("Loan On Card process success for accNo " + accNo + " when Accelerate Loan On Card for Automatic NP. ");
                                } catch (Exception e) {
                                    throw e;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("Loan On Card process failed for accNo " + accNo + " when Accelerate Loan On Card for Automatic NP. ", e);
                }
            }
            logInfo.info(logManager.logStartEnd("Loan On Card Process Automatic NP Acceleration Finished"));
        } catch (Exception e) {
            logError.error("Exception in Loan on NP Accounts", e);
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startLOCProcess(InstallmentBean installmentBean, ProcessBean processBean, AtomicInteger faileCardCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                String cardAssociation = commonRepo.getCardAssociationFromCardBin((installmentBean.getCardNumber().substring(0, 6)));
                if (cardAssociation == null) {
                    cardAssociation = commonRepo.getCardAssociationFromCardBin(installmentBean.getCardNumber().substring(0, 8)); //check 8 digit bin available
                }
                String maskedCardNumber = CommonMethods.cardNumberMask(installmentBean.getCardNumber());
                try {
                    Configurations.NO_OF_LOAN_ON_CARDS++;

                    if (installmentBean.getStatus().equals("RQAC") && installmentBean.getRunningStatus() != 1) {
                        installmentBean.setTxnID(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
                        installmentPaymentRepo.insertInToEODTransactionOnlyVisaFalse(installmentBean.getCardNumber(), installmentBean.getAccNo(), Double.parseDouble(installmentBean.getTxnAmount()),
                                installmentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_SALE, installmentBean.getTxnID(), "Loan On Card Transaction", Configurations.DEBIT, 1, cardAssociation);

                        commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(), installmentBean.getTxnAmount(),
                                installmentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_REVERSAL_INSTALLMENT, installmentBean.getTxnID(), "Loan On Card Transaction-Reversal", Configurations.CREDIT, null, cardAssociation);

                        String[] firstInstallment;
                        int remainingCount = installmentBean.getRemainingCount() - 1;
                        installmentBean.setCurrentCount(1);
                        installmentBean.setRemainingCount(remainingCount);
                        installmentBean.setTxnDescription("Loan On Card");

                        //insert the fee or interest amount to EOD gl table as unearned income(only use for GL account process)
                        if ((installmentBean.getFeeType().equalsIgnoreCase("FEE") && installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) || installmentBean.getFeeType().equalsIgnoreCase("INT")) {
                            //Debit 05112-127     account
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE, Double.parseDouble(installmentBean.getTotalFEeAmount()), Configurations.CREDIT, null);
                        } else {
                            //Debit 05112-126     account
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_UNEARNED_INCOME, Double.parseDouble(installmentBean.getTotalFEeAmount()), Configurations.CREDIT, null);
                        }
                        String txnDes = "Installment 1/" + installmentBean.getDuration() + " of " + installmentBean.getTxnDescription();
                        //Calulate 1st Installment
                        firstInstallment = this.calculateFirstInstallment(installmentBean);
                        if (installmentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                            details.put("Accelerated Status", "YES");
                            firstInstallment[0] = installmentBean.getTotalFEeAmount();
                            firstInstallment[1] = installmentBean.getTxnAmount();
                            txnDes = "Accelerated Installment Amount of Loan On  Card - " + installmentBean.getTxnDescription();
                        }
                        details.put("Description", txnDes);
                        details.put("Duration of installment plan", Integer.toString(installmentBean.getDuration()));
                        details.put("Remaining Count", Integer.toString(remainingCount));
                        installmentBean.setInstalmentAmount(new BigDecimal(firstInstallment[1]).add(new BigDecimal(firstInstallment[0])).toPlainString());//InstallmentAmount + fee
                        details.put("First Installment Amount", installmentBean.getInstalmentAmount());

                        //insert first installment amount without fee to the EODTXN table
                        commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(), firstInstallment[1],
                                installmentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_INSTALLMENT, installmentBean.getTxnID(), txnDes, Configurations.DEBIT, null, cardAssociation);

                        //insert fee portion

                        //txn type must be fee type of installment fee
                        if ((installmentBean.getFeeType().equalsIgnoreCase("FEE") && installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) || installmentBean.getFeeType().equalsIgnoreCase("INT")) {
                            //Debit 05112-127     account
                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), Double.parseDouble(firstInstallment[0]),
                                    installmentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, installmentBean.getTxnID(), "Loan On Card Processing Fee - " + installmentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE, Double.parseDouble(firstInstallment[0]), Configurations.DEBIT, null);
                        } else {
                            //Debit 05112-126     account
                            double upfrontFalsefee = this.calculateUpfrontfalseFeePortion(Double.parseDouble(installmentBean.getInterestRate()), installmentBean.getDuration(), installmentBean.getRunningStatus());
                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), Double.parseDouble(firstInstallment[0]),
                                    installmentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, installmentBean.getTxnID(), "Loan On Card Processing Fee - " + installmentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT, upfrontFalsefee, Configurations.DEBIT, null);
                        }
                        //update balance transfer table
                        installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(installmentBean, "LOANONCARDREQUEST");
                    /*update transaction table loan on card txn to 'EDON'. this txt initiated from web to online side when accepting the LOC.
                             generated trace number for the txn stored in loanoncardrequest table for mapping.*/
                        installmentPaymentRepo.updateFeeToEDONInTransactionTable(installmentBean.getCardNumber(), installmentBean.getTraceNumber(), Configurations.TXN_TYPE_LOAN_ON_CARD);
                    } else if (installmentBean.getStatus().equals("RQAC") && installmentBean.getRunningStatus() == 1) {
                        int lastPayno = installmentBean.getCurrentCount();
                        int isPaymentNo = installmentBean.getDuration() - installmentBean.getRemainingCount() + 1;
                        installmentBean.setCurrentCount(isPaymentNo);
                        String installment = installmentBean.getInstalmentAmount();//- installmentBean.getInterestRate();

                        BigDecimal fee = new BigDecimal("0.0");
                        //Insert fee portion to EOD txn table
                        if (installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) {
                            if (installmentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                                fee = new BigDecimal(installmentBean.getInterestRate()).multiply(new BigDecimal(installmentBean.getDuration())
                                        .subtract(BigDecimal.valueOf(lastPayno))).setScale(2, RoundingMode.FLOOR);
                            } else {
                                fee = new BigDecimal(installmentBean.getInterestRate()).setScale(2, RoundingMode.FLOOR);
                            }
                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), fee.doubleValue(),
                                    installmentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, installmentBean.getTxnID(), "Loan On Card Processing Fee - " + installmentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE, fee.doubleValue(), Configurations.DEBIT, null);
                            installment = new BigDecimal(installmentBean.getInstalmentAmount()).subtract(new BigDecimal(installmentBean.getInterestRate())).setScale(2, RoundingMode.FLOOR).toPlainString();
                        }
                        if (installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("YES")) {
                            double upfrontFalsefee = this.calculateUpfrontfalseFeePortion(Double.parseDouble(installmentBean.getInterestRate()), installmentBean.getDuration(), installmentBean.getRunningStatus());
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT, upfrontFalsefee, Configurations.DEBIT, null);

                        }
                        String description = "Installment " + installmentBean.getCurrentCount() + "/" + installmentBean.getDuration() + " of " + installmentBean.getTxnDescription();

                        if (installmentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                            details.put("Accelerated Status", "YES");
                            if (installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("yes")) {
                                installment = new BigDecimal(installmentBean.getInstalmentAmount()).multiply(new BigDecimal(installmentBean.getRemainingCount())).setScale(2, RoundingMode.FLOOR).toPlainString();
                            } else {
                                BigDecimal installmentWithFee = new BigDecimal(installmentBean.getInstalmentAmount()).multiply(new BigDecimal(installmentBean.getRemainingCount())).setScale(2, RoundingMode.FLOOR);
                                installment = installmentWithFee.subtract(fee).setScale(2, RoundingMode.FLOOR).toPlainString();
//                                    installmentBean.setInterestRate(installmentBean.getInterestRate() * (installmentBean.getDuration() - lastPayno));
//                                    installment = installmentBean.getInstalmentAmount() - installmentBean.getInterestRate();
                            }
                            description = "Installment " + installmentBean.getDuration() + "/" + installmentBean.getDuration() + " of " + installmentBean.getTxnDescription();
                        }
                        details.put("Description", description);
                        details.put("Duration of easy payment plan", Integer.toString(installmentBean.getDuration()));
                        details.put("Installment Amount without fee", installment);

                        //insert installment portion to EOD txn table
                        commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(), installment,
                                installmentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_INSTALLMENT, installmentBean.getTxnID(), description, Configurations.DEBIT, null, cardAssociation);

                        //update blance transfer table
                        int remainingCount = installmentBean.getRemainingCount() - 1;
                        installmentBean.setRemainingCount(remainingCount);
                        installmentPaymentRepo.updateEasyPaymentTable(installmentBean, "LOANONCARDREQUEST");
                    }
                    details.put("Process Status", "Passed");
                   // Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception e) {
                    //Configurations.FAILED_LOAN_ON_CARDS++;
                    faileCardCount.addAndGet(1);
                    Configurations.PROCESS_FAILD_COUNT++;
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                    logError.error("Loan On Card process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                    details.put("Process Status", "Failed");
                }
            } catch (Exception e) {
                logError.error("Loan on Card process failed ", e);
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    private synchronized boolean checkPaymentEnoughForKnockOff(String accNO, Date EOD_DATE) throws Exception {
        boolean isPaymentOnCurrentDay = false;
        double payment, m1Amount;

        try {
            payment = installmentPaymentRepo.checkForPayment(accNO, EOD_DATE);
            m1Amount = installmentPaymentRepo.checkLeastMinimumPayment(accNO);

            if (payment > m1Amount) {
                isPaymentOnCurrentDay = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return isPaymentOnCurrentDay;

    }

    private synchronized String[] calculateFirstInstallment(InstallmentBean easyPaymentBean) throws Exception {

        String[] firstInstallment = new String[2];
        String totalfeeAmount = new BigDecimal(easyPaymentBean.getInterestRate()).multiply(new BigDecimal(easyPaymentBean.getDuration())).setScale(2, RoundingMode.FLOOR).toPlainString();
        //0 fee,1 ins amount
        if (easyPaymentBean.getFeeType().equalsIgnoreCase("FEE")) {
            if (easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) {

                if (totalfeeAmount.equals(easyPaymentBean.getTotalFEeAmount())) {
                    firstInstallment[0] = easyPaymentBean.getInterestRate();
                } else {
                    firstInstallment[0] = new BigDecimal(easyPaymentBean.getTotalFEeAmount()).
                            subtract(new BigDecimal(easyPaymentBean.getInterestRate()).
                                    multiply(new BigDecimal(easyPaymentBean.getDuration()).
                                            subtract(new BigDecimal("1.00")))).setScale(2, RoundingMode.FLOOR).toPlainString();
                }
                BigDecimal installmentAmountWithoutFee = new BigDecimal(easyPaymentBean.getInstalmentAmount()).subtract(new BigDecimal(easyPaymentBean.getInterestRate()));
                BigDecimal totalInstallmentAmountWithoutFee = installmentAmountWithoutFee.multiply(new BigDecimal(easyPaymentBean.getDuration())).setScale(2, RoundingMode.FLOOR);

                if (totalInstallmentAmountWithoutFee.toPlainString().equals(easyPaymentBean.getTxnAmount())) {
                    firstInstallment[1] = installmentAmountWithoutFee.toPlainString();
                } else {
                    firstInstallment[1] = new BigDecimal(easyPaymentBean.getTxnAmount()).subtract(installmentAmountWithoutFee.multiply(new BigDecimal(easyPaymentBean.getDuration()).subtract(BigDecimal.valueOf(1)))).setScale(2, RoundingMode.FLOOR).toPlainString();
                }
            }
            if (easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("YES")) {
                firstInstallment[0] = easyPaymentBean.getInterestRate();

                BigDecimal totalpaymentAmount = new BigDecimal(easyPaymentBean.getInstalmentAmount()).multiply(new BigDecimal(easyPaymentBean.getDuration()));
                if (totalpaymentAmount.toPlainString().equals(easyPaymentBean.getTxnAmount())) {
                    firstInstallment[1] = easyPaymentBean.getInstalmentAmount();
                } else {
                    firstInstallment[1] = new BigDecimal(easyPaymentBean.getTxnAmount())
                            .subtract(new BigDecimal(easyPaymentBean.getInstalmentAmount())
                                    .multiply(new BigDecimal(easyPaymentBean.getDuration()).
                                            subtract(BigDecimal.valueOf(1)))).setScale(2, RoundingMode.FLOOR).toPlainString();
                }

            }
        } else if (easyPaymentBean.getFeeType().equalsIgnoreCase("INT")) {
            if (totalfeeAmount.equals(easyPaymentBean.getTotalFEeAmount())) {
                firstInstallment[0] = easyPaymentBean.getInterestRate();
            } else {
                firstInstallment[0] = new BigDecimal(easyPaymentBean.getTotalFEeAmount()).subtract(new BigDecimal(easyPaymentBean.getInterestRate()).multiply(new BigDecimal(easyPaymentBean.getDuration()).subtract(BigDecimal.valueOf(1)))).setScale(2, RoundingMode.FLOOR).toPlainString();
            }
            BigDecimal installmentAmountWithoutFee = new BigDecimal(easyPaymentBean.getInstalmentAmount()).subtract(new BigDecimal(easyPaymentBean.getInterestRate())).setScale(2, RoundingMode.FLOOR);
            BigDecimal totalInstallmentAmountWithoutFee = installmentAmountWithoutFee.multiply(new BigDecimal(easyPaymentBean.getDuration())).setScale(2, RoundingMode.FLOOR);

            if (totalInstallmentAmountWithoutFee.toPlainString().equals(easyPaymentBean.getTxnAmount())) {
                firstInstallment[1] = installmentAmountWithoutFee.toPlainString();
            } else {
                firstInstallment[1] = new BigDecimal(easyPaymentBean.getTxnAmount()).subtract(installmentAmountWithoutFee.
                        multiply(new BigDecimal(easyPaymentBean.getDuration()).subtract(BigDecimal.valueOf(1)))).setScale(2, RoundingMode.FLOOR).toPlainString();
            }
        }
        return firstInstallment;

    }

    private synchronized double calculateUpfrontfalseFeePortion(double interestRate, int duration, int runningStatus) throws Exception {
        double feeAmount = 0, totalFee;

        feeAmount = interestRate / duration;
        double roundOff = Math.floor(feeAmount * 100) / 100;
        totalFee = roundOff * duration;

        if (runningStatus == 0) {
            if (interestRate == totalFee) {
                return roundOff;
            } else {
                BigDecimal feeAmount1 = new BigDecimal(interestRate).subtract(new BigDecimal(roundOff).multiply(new BigDecimal(duration - 1)));
                feeAmount = Math.floor(Double.parseDouble(feeAmount1.toString()) * 100) / 100;

                return feeAmount;

            }
        } else {
            return roundOff;
        }
    }
}
