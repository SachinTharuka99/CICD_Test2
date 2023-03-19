/**
 * Created By Lahiru Sandaruwan
 * Date : 10/18/2022
 * Time : 2:35 PM
 * Project Name : ecms_eod_engine
 * Topic : balanceTransfer
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.*;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InstallmentPaymentRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class BalanceTransferService {

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    InstallmentPaymentRepo installmentPaymentRepo;

    @Autowired
    LogManager logManager;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void accelerateBalanceTransferRequestForNpAccount() throws Exception {
        try {
            /**
             * manual NP accounts
             */
            //infoLogger.info(logManager.processStartEndStyle("Balance Transfer Process Manual NP Acceleration Started"));
            logManager.logStartEnd("Balance Transfer Process Manual NP Acceleration Started", infoLogger);
            List<ManualNpRequestBean> manualNpList = installmentPaymentRepo.getManualNpRequestDetails(statusList.getYES_STATUS_1(), statusList.getCOMMON_REQUEST_ACCEPTED());
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += manualNpList.size();

            for (ManualNpRequestBean manualNpRequestBean : manualNpList) {
                String accNo = manualNpRequestBean.getAccNumber();
                try {
                    //update Balance Transfer requests for corresponding accno to Accelerate status.
                    installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "BALANCETRASFERREQUEST");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    //infoLogger.info("Balance Transfer process success for accNo " + accNo + " when Accelerate Balance Transfer for manual NP. ");
                    logManager.logInfo("Balance Transfer process success for accNo " + accNo + " when Accelerate Balance Transfer for manual NP. ", infoLogger);
                } catch (Exception ex) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    //infoLogger.info("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for manual NP. ");
                    logManager.logInfo("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for manual NP. ", infoLogger);
                    //errorLogger.error("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for manual NP. ", ex);
                    logManager.logError("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for manual NP. ", ex, errorLogger);
                }
            }
            //infoLogger.info(logManager.processStartEndStyle("Balance Transfer Process Manual NP Acceleration Finished"));
            logManager.logInfo("Balance Transfer Process Manual NP Acceleration Finished", infoLogger);
            /**
             * automatic NP accounts
             */
            //infoLogger.info(logManager.processStartEndStyle("Balance Transfer Process Automatic NP Acceleration Started"));
            logManager.logInfo("Balance Transfer Process Automatic NP Acceleration Started", infoLogger);
            List<DelinquentAccountBean> delinquentAccountList = installmentPaymentRepo.getDelinquentAccounts();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += delinquentAccountList.size();
            for (DelinquentAccountBean delinquentAccountBean : delinquentAccountList) {
                String accNo = delinquentAccountBean.getAccNo();
                boolean isPaymentOnCurrentDay = false;
                String[] newRiskClass;
                try {
                    double payment = installmentPaymentRepo.checkForPayment(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
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
                                //update Balance Transfer requests for corresponding accno to Accelerate status.
                                installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "BALANCETRASFERREQUEST");
                                Configurations.PROCESS_SUCCESS_COUNT++;
                                //infoLogger.info("Balance Transfer process success for accNo " + accNo + " when Accelerate Balance Transfer for Automatic NP. ");
                                logManager.logInfo("Balance Transfer process success for accNo " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", infoLogger);
                            } catch (Exception ex) {
                                Configurations.PROCESS_FAILD_COUNT++;
                                //infoLogger.info("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ");
                                logManager.logInfo("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", infoLogger);
                                //errorLogger.error("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", ex);
                                logManager.logError("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", ex, errorLogger);
                            }
                        }
                    } else {
                        boolean enoughPaymentForKnockOff = checkPaymentEnoughForKnockOff(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                        //if payment is not enough to knock off, check whether it's get NP Account
                        if (!enoughPaymentForKnockOff) {
                            if (delinquentAccountBean.getNDIA() == Integer.parseInt(bucketId[1])) {
                                try {
                                    //update Balance Transfer requests for corresponding accno to Accelerate status.
                                    installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "BALANCETRASFERREQUEST");
                                    Configurations.PROCESS_SUCCESS_COUNT++;
                                    //infoLogger.info("Balance Transfer process success for accNo " + accNo + " when Accelerate Balance Transfer for Automatic NP. ");
                                    logManager.logInfo("Balance Transfer process success for accNo " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", infoLogger);
                                } catch (Exception ex) {
                                    Configurations.PROCESS_FAILD_COUNT++;
                                    //infoLogger.info("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ");
                                    logManager.logInfo("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", infoLogger);
                                    //errorLogger.error("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP. ", ex);
                                    logManager.logError("Balance Transfer process failed for accno " + accNo + " when Accelerate Balance Transfer for Automatic NP.", ex, errorLogger);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    //infoLogger.info("Balance Transfer Process failed for accno " + accNo + " when Accelerate Loan On Card for Automatic NP. ");
                    logManager.logInfo("Balance Transfer Process failed for accno " + accNo + " when Accelerate Loan On Card for Automatic NP. ", infoLogger);
                    //errorLogger.error("Balance Transfer Process failed for accno " + accNo + " when Accelerate Loan On Card for Automatic NP. ", e);
                    logManager.logError("Balance Transfer Process failed for accno " + accNo + " when Accelerate Loan On Card for Automatic NP. ", e, errorLogger);
                }
            }
            //infoLogger.info(logManager.processStartEndStyle("Balance Transfer Process Automatic NP Acceleration Finished"));
            logManager.logInfo("Balance Transfer Process Automatic NP Acceleration Finished", infoLogger);
        } catch (Exception e) {
            //errorLogger.error("Exception in Balance Transfer NP Accounts", e);
            logManager.logError("Exception in Balance Transfer NP Accounts", e, errorLogger);
        }
    }

    private synchronized boolean checkPaymentEnoughForKnockOff(String accNO, Date eodDate) throws Exception {
        boolean isPaymentOnCurrentDay = false;
        double payment, m1Amount;

        try {
            payment = installmentPaymentRepo.checkForPayment(accNO, eodDate);
            m1Amount = installmentPaymentRepo.checkLeastMinimumPayment(accNO);

            if (payment > m1Amount) {
                isPaymentOnCurrentDay = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return isPaymentOnCurrentDay;
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startBalanceTransferProcess(InstallmentBean installmentBean, ProcessBean processBean) throws Exception {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                System.out.println("Current Installment = " + installmentBean.getCurrentCount());
                System.out.println("Installment month = " + installmentBean.getNxtTxnDate());

                String cardAssociation = commonRepo.getCardAssociationFromCardBin(installmentBean.getCardNumber().substring(0, 6));
                if (cardAssociation == null) {
                    cardAssociation = commonRepo.getCardAssociationFromCardBin(installmentBean.getCardNumber().substring(0, 8)); //check 8 digit bin available
                }
                String maskedCardNumber = CommonMethods.cardNumberMask(installmentBean.getCardNumber());
                try {
                    Configurations.NO_OF_BALANCE_TRANSFERS++;

                    if (installmentBean.getStatus().equals("RQAC") && installmentBean.getRunningStatus() != 1) {
                        installmentBean.setTxnID(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());//In here No txn and reversal
                        //In here real txn and reversal
                        installmentPaymentRepo.insertInToEODTransactionOnlyVisaFalse(installmentBean.getCardNumber(), installmentBean.getAccNo(), Double.parseDouble(installmentBean.getTxnAmount()),
                                installmentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_SALE, installmentBean.getTxnID(), "Balance Transfer Transaction", Configurations.DEBIT, 1, cardAssociation);

                        commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(), installmentBean.getTxnAmount(),
                                installmentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_REVERSAL_INSTALLMENT, installmentBean.getTxnID(), "Balance Transfer Transaction-Reversal", Configurations.CREDIT, null, cardAssociation);

                        String[] firstInstallment;
                        int remainingCount = installmentBean.getRemainingCount() - 1;
                        installmentBean.setCurrentCount(1);
                        installmentBean.setRemainingCount(remainingCount);

                        installmentBean.setTxnDescription("Balance Transfer");

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
                            txnDes = "Accelerated Installment Amount of Balance Transfer - " + installmentBean.getTxnDescription();
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
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, installmentBean.getTxnID(), "Balance Transfer Processing Fee - " + installmentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE, Double.parseDouble(firstInstallment[0]), Configurations.DEBIT, null);

                        } else {
                            //Debit 05112-126     account
                            double upfrontFalsefee = this.calculateUpfrontfalseFeePortion(Double.parseDouble(installmentBean.getInterestRate()), installmentBean.getDuration(), installmentBean.getRunningStatus());

                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), Double.parseDouble(firstInstallment[0]),
                                    installmentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, installmentBean.getTxnID(), "Balance Transfer Processing Fee - " + installmentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT, upfrontFalsefee, Configurations.DEBIT, null);

                        }
                        //update balance transfer table
                        installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(installmentBean, "BALANCETRASFERREQUEST");

                            /*update transaction table BT txn to 'EDON'. this txt initiated from web to online side when accepting the BT.
                             generated trace number for the txn stored in balancetransferrequest table for mapping.*/
                        installmentPaymentRepo.updateFeeToEDONInTransactionTable(installmentBean.getCardNumber(), installmentBean.getTraceNumber(), Configurations.TXN_TYPE_BALANCE_TRANSFER);

                    } else if (installmentBean.getStatus().equals("RQAC") && installmentBean.getRunningStatus() == 1) {

                        int lastPayno = installmentBean.getCurrentCount();
                        int isPaymentNo = installmentBean.getDuration() - installmentBean.getRemainingCount() + 1;
                        installmentBean.setCurrentCount(isPaymentNo);
                        String installment = installmentBean.getInstalmentAmount();// - installmentBean.getInterestRate();

                        BigDecimal fee = new BigDecimal("0.0");
                        if (installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) {
                            if (installmentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                                fee = new BigDecimal(installmentBean.getInterestRate()).multiply(new BigDecimal(installmentBean.getDuration())
                                        .subtract(BigDecimal.valueOf(lastPayno))).setScale(2, RoundingMode.FLOOR);
                            } else {
                                fee = new BigDecimal(installmentBean.getInterestRate()).setScale(2, RoundingMode.FLOOR);
                            }
                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), fee.doubleValue(),
                                    installmentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, installmentBean.getTxnID(), "Balance Transfer Processing Fee - " + installmentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE, fee.doubleValue(), Configurations.DEBIT, null);

                            installment = new BigDecimal(installmentBean.getInstalmentAmount()).subtract(new BigDecimal(installmentBean.getInterestRate())).setScale(2, RoundingMode.FLOOR).toPlainString();

                        }
                        if (installmentBean.getFeeApplyFirstMonth().equalsIgnoreCase("YES")) {
                            double upfrontFalsefee = this.calculateUpfrontfalseFeePortion(Double.parseDouble(installmentBean.getInterestRate()), installmentBean.getDuration(), installmentBean.getRunningStatus());
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, installmentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT, upfrontFalsefee, Configurations.DEBIT, null);

                        }
                        String description = "Installment " + installmentBean.getCurrentCount() + "/" + installmentBean.getDuration() + " of " + installmentBean.getTxnDescription();

                        //Insert fee portion to EOD txn table
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
                        installmentPaymentRepo.updateEasyPaymentTable(installmentBean, "BALANCETRASFERREQUEST");

                    }
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    details.put("Process Status", "Passed");

                } catch (Exception e) {
                    Configurations.FAILED_BALANCE_TRANSFERS++;
                    Configurations.PROCESS_FAILD_COUNT++;
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(installmentBean.getCardNumber()), e.getMessage(), Configurations.PROCESS_ID_BALANCE_TRANSFER, "Balance Transfer Process", 0, CardAccount.CARD));

                    //infoLogger.info("Balance Transfer process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean));
                    logManager.logInfo("Balance Transfer process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), infoLogger);
                    //errorLogger.error("Balance Transfer process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                    logManager.logError("Balance Transfer process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e, errorLogger);

                    details.put("Process Status", "Failed");

                }
                //infoLogger.info(logManager.processDetailsStyles(details));
            } catch (Exception e) {
                //errorLogger.error("Balance Transfer process failed ", e);
                logManager.logError("Balance Transfer process failed ", e, errorLogger);
            } finally {
                logManager.logDetails(details, infoLogger);
            }
        }
    }

    public synchronized String[] calculateFirstInstallment(InstallmentBean easyPaymentBean) {
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

    public synchronized double calculateUpfrontfalseFeePortion(double interestRate, int duration, int runningStatus) throws Exception {
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
