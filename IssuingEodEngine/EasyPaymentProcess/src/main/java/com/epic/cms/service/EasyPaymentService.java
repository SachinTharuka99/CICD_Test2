/**
 * Created By Lahiru Sandaruwan
 * Date : 10/18/2022
 * Time : 2:57 PM
 * Project Name : ecms_eod_engine
 * Topic : easyPayment
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class EasyPaymentService {

    @Autowired
    LogManager logManager;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    @Autowired
    InstallmentPaymentRepo installmentPaymentRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void accelerateEasyPaymentRequestForNpAccount() throws Exception {
        try {
            /**
             * manual NP accounts
             */
            logManager.logStartEnd("Easy Payment process Manual NP Acceleration Started", infoLogger);
            List<ManualNpRequestBean> manualNpList = installmentPaymentRepo.getManualNpRequestDetails(statusList.getYES_STATUS_1(), statusList.getCOMMON_REQUEST_ACCEPTED());
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += manualNpList.size();

            for (ManualNpRequestBean manualNpRequestBean : manualNpList) {
                String accNo = manualNpRequestBean.getAccNumber();
                try {
                    //update easy payement requests for corresponding accno to Accelerate status.
                    installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "EASYPAYMENTREQUEST");
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    logManager.logInfo("Easy Payment process success for accNo " + accNo + " when Accelerate easy payment for manual NP. ", infoLogger);
                } catch (Exception ex) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logManager.logInfo("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for manual NP. ", infoLogger);
                    logManager.logError("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for manual NP. ", ex, errorLogger);
                }
            }
            logManager.logStartEnd("Easy Payment process Manual NP Acceleration Finished", infoLogger);
            /**
             * automatic NP accounts
             */
            logManager.logStartEnd("Easy Payment process Automatic NP Acceleration Started", infoLogger);
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
                                installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "EASYPAYMENTREQUEST");
                                Configurations.PROCESS_SUCCESS_COUNT++;
                                logManager.logInfo("Easy Payment process success for accNo " + accNo + " when Accelerate easy payment for Automatic NP. ", infoLogger);
                            } catch (Exception ex) {
                                Configurations.PROCESS_FAILD_COUNT++;
                                logManager.logInfo("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for Automatic NP. ", infoLogger);
                                logManager.logError("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for Automatic NP. ", ex, errorLogger);
                            }
                        }
                    } else {
                        boolean enoughPaymentForKnockOff = checkPaymentEnoughForKnockOff(delinquentAccountBean.getAccNo(), Configurations.EOD_DATE);
                        //if payment is not enough to knock off, check whether it's get NP Account
                        if (!enoughPaymentForKnockOff) {
                            if (delinquentAccountBean.getNDIA() == Integer.parseInt(bucketId[1])) {
                                try {
                                    //update Balance Transfer requests for corresponding accno to Accelerate status.
                                    installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(accNo, "EASYPAYMENTREQUEST");
                                    Configurations.PROCESS_SUCCESS_COUNT++;
                                    logManager.logInfo("Easy Payment process success for accNo " + accNo + " when Accelerate easy payment for Automatic NP. ", infoLogger);
                                } catch (Exception ex) {
                                    Configurations.PROCESS_FAILD_COUNT++;
                                    logManager.logInfo("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for Automatic NP. ", infoLogger);
                                    logManager.logError("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for Automatic NP. ", ex, errorLogger);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logManager.logInfo("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for Automatic NP. ", infoLogger);
                    logManager.logError("Easy Payment process failed for accno " + accNo + " when Accelerate easy payment for Automatic NP. ", e, errorLogger);
                }
            }
            logManager.logStartEnd("Easy Payment process Automatic NP Acceleration Finished", infoLogger);

        } catch (Exception e) {
            logManager.logError("Easy Payment process failed for accelerate EasyPayment Request For NpAccount", e, errorLogger);
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

    public synchronized String[] calculateFirstInstallmentAmountAndFee(InstallmentBean easyPaymentBean) {
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

    //@Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startEasyPaymentProcess(InstallmentBean easyPaymentBean, ProcessBean processBean) throws Exception{
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                System.out.println("Current Installment = " + easyPaymentBean.getCurrentCount());
                System.out.println("Installment month = " + easyPaymentBean.getNxtTxnDate());

                String cardAssociation = commonRepo.getCardAssociationFromCardBin(easyPaymentBean.getCardNumber().substring(0, 6));
                if (cardAssociation == null) {
                    cardAssociation = commonRepo.getCardAssociationFromCardBin(easyPaymentBean.getCardNumber().substring(0, 8)); //check 8 digit bin available
                }
                String maskedCardNumber = CommonMethods.cardNumberMask(easyPaymentBean.getCardNumber());
                details.put("Card Number", maskedCardNumber);

                try {
                    Configurations.NO_OF_EASY_PAYMENTS++;
                    ;
                    if (easyPaymentBean.getStatus().equals("RQAC") && easyPaymentBean.getRunningStatus() != 1) {
                        String txtDescription = installmentPaymentRepo.getEodtxnDescription(easyPaymentBean.getTxnID());

                        //Reversal is done by web when accepting the request
                        /**
                         * count =
                         * dbCon.insertInToEODTransaction(easyPaymentBean.getCardNumber(),
                         * easyPaymentBean.getAccNo(),
                         * easyPaymentBean.getTxnAmount(),
                         * easyPaymentBean.getCurruncyCode(), "TEST",
                         * "TEST",
                         * Configurations.TXN_TYPE_REVERSAL_INSTALLMENT,
                         * easyPaymentBean.getTxnID(), txtDescription +
                         * "-Reverse",Configurations.CREDIT);*
                         */
                        //insert the fee or interest amount to EOD gl table as unearned income(only use for GL account process)
                        if ((easyPaymentBean.getFeeType().equalsIgnoreCase("FEE") && easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) || easyPaymentBean.getFeeType().equalsIgnoreCase("INT")) {
                            //Debit 05112-127     account
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, easyPaymentBean.getCardNumber(), Configurations.TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE, Double.parseDouble(easyPaymentBean.getTotalFEeAmount()), Configurations.CREDIT, null);
                        } else {
                            //Debit 05112-126     account
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, easyPaymentBean.getCardNumber(), Configurations.TXN_TYPE_UNEARNED_INCOME, Double.parseDouble(easyPaymentBean.getTotalFEeAmount()), Configurations.CREDIT, null);
                        }
                        easyPaymentBean.setTxnDescription(txtDescription);
                        int remainingCount = easyPaymentBean.getRemainingCount() - 1;
                        easyPaymentBean.setCurrentCount(1);
                        easyPaymentBean.setRemainingCount(remainingCount);
                        String description = "Easy payment No 1" + "/" + easyPaymentBean.getDuration() + " of " + easyPaymentBean.getTxnDescription();

                        String[] firstInstallmentAmount = this.calculateFirstInstallmentAmountAndFee(easyPaymentBean);
                        if (easyPaymentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                            details.put("Accelerated Status", "YES");
                            firstInstallmentAmount[0] = easyPaymentBean.getTotalFEeAmount();
                            firstInstallmentAmount[1] = easyPaymentBean.getTxnAmount();
                            description = "Accelerated Installment Amount of Easy Payment - " + easyPaymentBean.getTxnDescription();
                        }
                        details.put("Description", description);

                        details.put("Duration of easy payment plan", Integer.toString(easyPaymentBean.getDuration()));
                        details.put("Remaining Count", Integer.toString(remainingCount));
                        easyPaymentBean.setInstalmentAmount(new BigDecimal(firstInstallmentAmount[1]).add(new BigDecimal(firstInstallmentAmount[0])).toPlainString());//InstallmentAmount + fee
                        details.put("First Installment Amount", easyPaymentBean.getInstalmentAmount());

                        commonRepo.insertInToEODTransaction(easyPaymentBean.getCardNumber(), easyPaymentBean.getAccNo(), firstInstallmentAmount[1],
                                easyPaymentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_INSTALLMENT, easyPaymentBean.getTxnID(), description, Configurations.DEBIT, null, cardAssociation);
                        //insert fee portion
                        //txn type must be fee type of installment fee

                        if ((easyPaymentBean.getFeeType().equalsIgnoreCase("FEE") && easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) || easyPaymentBean.getFeeType().equalsIgnoreCase("INT")) {
                            //Debit 05112-127     account
                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(easyPaymentBean.getCardNumber(), easyPaymentBean.getAccNo(), Double.parseDouble(firstInstallmentAmount[0]),
                                    easyPaymentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, easyPaymentBean.getTxnID(), "Easy Payment Processing fee - " + easyPaymentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, easyPaymentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE, Double.parseDouble(firstInstallmentAmount[0]), Configurations.DEBIT, null);

                        } else {
                            //Debit 05112-126     account
                            double upfrontFalsefee = this.calculateUpfrontfalseFeePortion(Double.parseDouble(easyPaymentBean.getInterestRate()), easyPaymentBean.getDuration(), easyPaymentBean.getRunningStatus());

                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(easyPaymentBean.getCardNumber(), easyPaymentBean.getAccNo(), Double.parseDouble(firstInstallmentAmount[0]),
                                    easyPaymentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, easyPaymentBean.getTxnID(), "Easy Payment Processing fee - " + easyPaymentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, easyPaymentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT, upfrontFalsefee, Configurations.DEBIT, null);

                        }

                        //update EASYPAYMENTREQUEST  table
                        installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(easyPaymentBean, "EASYPAYMENTREQUEST");

                            /*update transaction table easy payment fee to 'EDON'. this fee initiated from web to online side when accepting the easypaymentrequest.
                             generated trace number for the fee stored in easypaymentrequest table for mapping.*/
                        installmentPaymentRepo.updateFeeToEDONInTransactionTable(easyPaymentBean.getCardNumber(), easyPaymentBean.getTraceNumber(), Configurations.TXN_TYPE_INSTALLMENT);
                    } else if (easyPaymentBean.getStatus().equals("RQAC") && easyPaymentBean.getRunningStatus() == 1) {
                        int lastPayno = easyPaymentBean.getCurrentCount();
                        int isPaymentNo = easyPaymentBean.getDuration() - easyPaymentBean.getRemainingCount() + 1;
                        easyPaymentBean.setCurrentCount(isPaymentNo);
                        String installment = easyPaymentBean.getInstalmentAmount();// - easyPaymentBean.getInterestRate();

                        BigDecimal fee = new BigDecimal("0.0");
                        //Insert fee portion to EOD txn table
                        if (easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("NO")) {
                            if (easyPaymentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                                fee = new BigDecimal(easyPaymentBean.getInterestRate()).multiply(new BigDecimal(easyPaymentBean.getDuration())
                                        .subtract(BigDecimal.valueOf(lastPayno))).setScale(2, RoundingMode.FLOOR);
                            } else {
                                fee = new BigDecimal(easyPaymentBean.getInterestRate()).setScale(2, RoundingMode.FLOOR);
                            }
                            installmentPaymentRepo.insertInToEODTransactionWithoutGL(easyPaymentBean.getCardNumber(), easyPaymentBean.getAccNo(), fee.doubleValue(),
                                    easyPaymentBean.getCurruncyCode(), "TEST", "TEST",
                                    Configurations.TXN_TYPE_FEE_INSTALLMENT, easyPaymentBean.getTxnID(), "Easy payment Processing fee - " + easyPaymentBean.getTxnDescription(), Configurations.DEBIT, 1, cardAssociation);
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, easyPaymentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE, fee.doubleValue(), Configurations.DEBIT, null);

                            installment = new BigDecimal(easyPaymentBean.getInstalmentAmount()).subtract(new BigDecimal(easyPaymentBean.getInterestRate())).setScale(2, RoundingMode.FLOOR).toPlainString();

                        }
                        if (easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("YES")) {
                            double upfrontFalsefee = this.calculateUpfrontfalseFeePortion(Double.parseDouble(easyPaymentBean.getInterestRate()), easyPaymentBean.getDuration(), easyPaymentBean.getRunningStatus());
                            commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, easyPaymentBean.getCardNumber(), Configurations.TXN_TYPE_FEE_INSTALLMENT, upfrontFalsefee, Configurations.DEBIT, null);

                        }
                        String description = "Installment " + easyPaymentBean.getCurrentCount() + "/" + easyPaymentBean.getDuration() + " of " + easyPaymentBean.getTxnDescription();
                        if (easyPaymentBean.getAccelarateStatus().equalsIgnoreCase("YES")) {
                            details.put("Accelerated Status", "YES");
                            if (easyPaymentBean.getFeeApplyFirstMonth().equalsIgnoreCase("yes")) {
                                installment = new BigDecimal(easyPaymentBean.getInstalmentAmount()).multiply(new BigDecimal(easyPaymentBean.getRemainingCount())).setScale(2, RoundingMode.FLOOR).toPlainString();
                            } else {
                                BigDecimal installmentWithFee = new BigDecimal(easyPaymentBean.getInstalmentAmount()).multiply(new BigDecimal(easyPaymentBean.getRemainingCount())).setScale(2, RoundingMode.FLOOR);
                                installment = installmentWithFee.subtract(fee).setScale(2, RoundingMode.FLOOR).toPlainString();
//                                    easyPaymentBean.setInterestRate(easyPaymentBean.getInterestRate() * (easyPaymentBean.getDuration() - lastPayno));
//                                    installment = easyPaymentBean.getInstalmentAmount() - easyPaymentBean.getInterestRate();
                            }

                            description = "Accelerated Installment Amount of Easy Payment - " + easyPaymentBean.getTxnDescription();
                        }
                        details.put("Description", description);
                        details.put("Duration of easy payment plan", Integer.toString(easyPaymentBean.getDuration()));
                        details.put("Installment Amount without fee", installment);

                        //insert installment portion to EOD txn table
                        commonRepo.insertInToEODTransaction(easyPaymentBean.getCardNumber(), easyPaymentBean.getAccNo(), installment,
                                easyPaymentBean.getCurruncyCode(), "TEST", "TEST",
                                Configurations.TXN_TYPE_INSTALLMENT, easyPaymentBean.getTxnID(), description, Configurations.DEBIT, null, cardAssociation);

                        //update easy payment table
                        int remainingCount = easyPaymentBean.getRemainingCount() - 1;
                        details.put("Remaining Count", Integer.toString(remainingCount));
                        easyPaymentBean.setRemainingCount(remainingCount);

                        installmentPaymentRepo.updateEasyPaymentTable(easyPaymentBean, "EASYPAYMENTREQUEST");
                    }
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    details.put("Process Status", "Passed");
                } catch (Exception e) {
                    Configurations.FAILED_EASY_PAYMENTS++;
                    Configurations.PROCESS_FAILD_COUNT++;
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(easyPaymentBean.getCardNumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));

                    logManager.logInfo("Easy Payment process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), infoLogger);
                    logManager.logError("Easy Payment  process failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e, errorLogger);
                    details.put("Process Status", "Failed");
                }

            } catch (Exception e) {
                logManager.logError("Easy Payment process failed ", e, errorLogger);
            } finally {
                logManager.logDetails(details,infoLogger);
            }
        }
    }
}
