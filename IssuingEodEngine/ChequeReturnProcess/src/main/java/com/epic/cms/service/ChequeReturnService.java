package com.epic.cms.service;

import com.epic.cms.Exception.FailedCardException;
import com.epic.cms.dao.ChequeReturnDao;
import com.epic.cms.dao.CommonDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class ChequeReturnService {

    @Autowired
    public LogManager logManager;

    @Autowired
    public ChequeReturnDao chequeReturnDao;

    @Autowired
    public CommonDao commonDao;

    @Autowired
    public StatusVarList status;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateChequeReturns() throws Exception {
        List<ReturnChequePaymentDetailsBean> chqList = null;
        try {
            chqList = chequeReturnDao.getChequeReturns();
            for (ReturnChequePaymentDetailsBean bean : chqList) {
                //Update chequepayment table from EDON to CQRT.
                int count = chequeReturnDao.updateChequeReturns(bean.getCardnumber(), bean.getTraceid(), bean.getChqRtnDate());
                String seqNoFromTrace = bean.getTraceid();
                /* For eodpayments having extra payment done for sup card and after EOM swap. */
                //chqReturnDbCon.updateChequeReturnsForEODPayment(bean.getMaincardno(), seqNoFromTrace);
                /*Main card*/
                chequeReturnDao.updateChequeReturnsForEODPayment(bean.getCardnumber(), seqNoFromTrace);
                if (count == 1) {
                    System.out.println("Updated:" + CommonMethods.cardNumberMask(bean.getCardnumber()));
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            /* PADSS Change -
               variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
            for (ReturnChequePaymentDetailsBean returnChequePaymentDetailBean : chqList) {
                CommonMethods.cardNumberMask(returnChequePaymentDetailBean.getCardnumber());
            }
            chqList = null;
        }
    }

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void proceedChequeReturn(Map.Entry pair) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            List<ReturnChequePaymentDetailsBean> chequeReturnList = new ArrayList<>();
            ReturnChequePaymentDetailsBean chqBean = new ReturnChequePaymentDetailsBean();
            StringBuffer cardNo = null;
            Date paidDate = null, dueDate = null, statementEndDate = null, chequeReturnDate = null;
            int statementEndEODID = 0, statementStartEODID = 0, ndia = 0;
            String returnFeeCode = "", oldCardStatus = "", riskclass = "";
            double totalChequeReturns = 0, calculatedInterests = 0;
            boolean datesNotNull = false;

            try {
                cardNo = new StringBuffer(pair.getKey().toString());
                //get the chequeReturnList of cheques returned for a card.
                chequeReturnList = (List<ReturnChequePaymentDetailsBean>) pair.getValue();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += chequeReturnList.size();
                //get the first card in the lists, since the statement details are same.
                chqBean = chequeReturnList.get(0);
                statementStartEODID = chqBean.getStatementstarteodid();
                statementEndEODID = chqBean.getStatementendeodid();
                statementEndDate = CommonMethods.getDateFromEODID(statementEndEODID);
                double minAmount = chqBean.getMinamount();
                oldCardStatus = chqBean.getCardstatus();
                dueDate = chqBean.getDuedate();
                /**
                 * Here the risk class and ndia are taken for the first
                 * cheque return in the series.
                 */
                riskclass = chqBean.getDelinquentclass();
                ndia = chqBean.getNdia();
                totalChequeReturns = 0;
                /*
                 * Iterate over the chequeReturnList to get cheque details
                 */
                for (ReturnChequePaymentDetailsBean bean : chequeReturnList) {
                    totalChequeReturns += bean.getAmount();
                    returnFeeCode = bean.getCHEQUE_RET_CODE();
                    chequeReturnDate = bean.getChqRtnDate();
                    paidDate = bean.getChequedate();

                    if (paidDate != null && dueDate != null && statementEndDate != null) {
                        datesNotNull = true;
                    }
                    if (datesNotNull) {
                        //Payment Knock on process.
                        //get all txn knockoffs from eodpayment table.
                        ReturnChequePaymentDetailsBean returnBean = chequeReturnDao.getChequeKnockOffBean(cardNo);
                        //EOM balance
                        if (returnBean == null) {
                            Configurations.PROCESS_FAILD_COUNT++;
                            logManager.logError("null point for eod card:" + CommonMethods.cardNumberMask(cardNo),errorLogger);
                            throw new NullPointerException("EOD Payment knockoff data is null");
                        }
                        double eomOtbCash = returnBean.getMainCashAdvanceKnockoff();
                        double eomOtbCredit = returnBean.getMainTransactionKnockoff() + returnBean.getMainFinChargeKnockoff() + returnBean.getMainCashAdvanceKnockoff();
                        //EOD balance
                        double eodOtbCash = returnBean.getSupCashAdvanceKnockoff();
                        double eodOtbCredit = returnBean.getSupTransactionKnockoff() + returnBean.getSupFinChargeKnockoff() + returnBean.getSupCashAdvanceKnockoff();

                        //Set EOM OTB
                        OtbBean eomOtbBean = new OtbBean();
                        eomOtbBean.setCardnumber(returnBean.getCardnumber());
                        eomOtbBean.setAccountnumber(returnBean.getAccountNo());
                        eomOtbBean.setCustomerid(returnBean.getCustomerid());
                        eomOtbBean.setCumpayment(returnBean.getAmount());
                        eomOtbBean.setCumcashadvance(returnBean.getMainCashAdvanceKnockoff());
                        eomOtbBean.setCumtransactions(returnBean.getMainTransactionKnockoff());
                        eomOtbBean.setFinacialcharges(returnBean.getMainFinChargeKnockoff());
                        eomOtbBean.setOtbcash(eomOtbCash);
                        eomOtbBean.setOtbcredit(returnBean.getAmount());
                        eomOtbBean.setTmpcredit(returnBean.getAmount());
                        eomOtbBean.setTmpcash(eomOtbCash);
                        //Set EOD OTB
                        OtbBean eodOtbBean = new OtbBean();
                        eodOtbBean = eomOtbBean;
                        eodOtbBean.setCumpayment(returnBean.getAmount());
                        eodOtbBean.setCumcashadvance(returnBean.getSupCashAdvanceKnockoff());
                        eodOtbBean.setCumtransactions(returnBean.getSupTransactionKnockoff());
                        eodOtbBean.setFinacialcharges(returnBean.getSupFinChargeKnockoff());
                        eodOtbBean.setOtbcash(eodOtbCash + eomOtbCash);
                        eodOtbBean.setOtbcredit(returnBean.getAmount());
                        eodOtbBean.setTmpcredit(returnBean.getAmount());
                        eodOtbBean.setTmpcash(eodOtbCash);

                        if (chequeReturnDate.after(dueDate) && chequeReturnDate.before(statementEndDate)) {
                            /**
                             *                                      *
                             * Cheque Returned after duedate and before
                             * statement date 1. change forward interest
                             * to actual interest and add to the
                             * eodcardbalance * *
                             *
                             */

                            /**
                             * 1. First, Knock on the Main Card in the
                             * EOM card balance from the eodpayment
                             * table Main charges with status EPEN. 1.a
                             * If EOM card has EPEN status, then knock
                             * on that card and EODCardbalance, card,
                             * acc, cus and Main card EOD only 2 If EOM
                             * card balance doesn't have any values,
                             * affect the supplementary card of EOD card
                             * balance, card,acc, cus. * 3. Update
                             * chequepayment as returned .
                             *
                             */
                            /*Check EOM Card Balance table for Main/Sup card entry.*/
                            OtbBean eomOtb = chequeReturnDao.getEOMPendingKnockOffList(cardNo);
                            if (eomOtb != null) {
                                /**
                                 * An entry is there in the
                                 * supplementary table for last month
                                 * which is not paid.
                                 */
                                if (eomOtb.getIsPrimary().equalsIgnoreCase(status.getSTATUS_NO())) {
                                    /**
                                     * Supplementary card has not been
                                     * paid enough for last month.
                                     */
                                    OtbBean otbEomMain = eodOtbBean;
                                    otbEomMain.setCardnumber(eomOtbBean.getMaincardno());
                                    /**
                                     * Knock on values eod cardbalance,
                                     * Main Card, card, acc, cus.
                                     */
                                    chequeReturnDao.updateEOMCARDBalanceKnockOn(eomOtbBean);
                                    /* Update the main card with the supplementary
                                     * knock on values.
                                     */
                                    //  chqReturnDbCon.updateEODCARDBalanceKnockOn(otbEomMain);
                                    updateCardCustomerAccountBalances(otbEomMain);
                                } else {
                                    /**
                                     * Card is primary and EOM is in
                                     * EPEN state. - Last month not
                                     * paid.
                                     */
                                    chequeReturnDao.updateEOMCARDBalanceKnockOn(eomOtbBean);
                                    /**
                                     * Update eodcardbalance table with
                                     * the knockon.
                                     */
                                    //   chqReturnDbCon.updateEODCARDBalanceKnockOn(eomOtbBean);
                                    updateCardCustomerAccountBalances(eomOtbBean);
                                }
                            } else {
                                /*
                                 * There is neither a remaining EOM entry for sup card nor
                                 * an extra payment has been made.
                                 */
                                //chqReturnDbCon.updateEODCARDBalanceKnockOn(eodOtbBean);
                                /*
                                 * Update the EODCardbalance table.
                                 */
                                //chqReturnDbCon.updateEODCARDBalanceKnockOn(eomOtbBean);
                                updateCardBalanceByCardCategory(returnBean, eodOtbBean);
                            }
                            int diffInDays = (int) (chequeReturnDate.getTime() - paidDate.getTime()) / (1000 * 60 * 60 * 24);

                            double period = chequeReturnDao.getIntProf(bean.getAccountNo()).getInterestperiod();
                            calculatedInterests += bean.getInterestrate() * diffInDays * bean.getAmount() / period / 100;
                            if (calculatedInterests < 0) {
                                calculatedInterests = 0;
                            }
                            calculatedInterests = Double.parseDouble(CommonMethods.ValuesRoundup(calculatedInterests));
                        } else if (chequeReturnDate.after(statementEndDate)) {
                            //Cheque returned after statement date
                            /**
                             * This goes as adjustments. TODO are the
                             * online card/customer/accounts updated?
                             */
                        } else if (chequeReturnDate.before(dueDate)) {
                            // If check returns after or on a statement day and before due date
                            //Update Backend Customer,Account and Card
                            updateCardBalanceByCardCategory(returnBean, eodOtbBean);

                        } else {
                            details.put("other", CommonMethods.cardNumberMask(cardNo));
                        }
                        //add cheque return fee
                        /**
                         * TODO check if card is supplementary?
                         */
                    }
                    /**
                     * Insert to EOD Transaction table the cheque
                     * returns.
                     */
                    int updated = 0;
                    String accNo = chequeReturnDao.getCardAccountCustomer(bean.getCardnumber()).getAccountNumber();
                    PaymentBean payBean = new PaymentBean(cardNo, bean.getEodid(), bean.getTraceid(), status.getCHEQUE_PAYMENT(), bean.getSeqNo(), accNo);
                    String txnId = chequeReturnDao.getTxnIdForLastChequeByAccount(payBean);
                    StringBuffer currentCard = bean.getCardnumber();
                    if (txnId == null) {
                        /**
                         * If the txnId is null, then the eodtransaction
                         * table doesn't have the original check entry.
                         * Therefore, check if eodtransaction table has
                         * an entry for the old card.
                         */
                        currentCard = bean.getOldcardnumber();
                        txnId = chequeReturnDao.getTxnIdForLastCheque(payBean);
                        if (txnId != null) {
                            /**
                             * If the old card has an entry, then set
                             * the entry way for the old cardnumber to
                             * get updated/inserted.
                             */
                            currentCard = bean.getOldcardnumber();
                            payBean.setCardnumber(currentCard);
                        }
                    }
                    String cardAssociation = commonDao.getCardAssociationFromCardBin(bean.getCardnumber().substring(0, 6));
                    if (cardAssociation == null) {
                        cardAssociation = commonDao.getCardAssociationFromCardBin(bean.getCardnumber().substring(0, 8)); //check 8 digit bin available
                    }
                    if (txnId != null) {//fix-me
                        if (!chequeReturnDao.checkDuplicateChequeReturnEntry(currentCard, bean.getAmount(), txnId, bean.getTraceid(), bean.getCqrtseqNo())) {
                            chequeReturnDao.insertReturnChequeToEODTransaction(currentCard, accNo, bean.getAmount(), txnId, bean.getSeqNo() + "", bean.getCqrtseqNo(), cardAssociation);
                            if (returnFeeCode != null) {
                                if (returnFeeCode.equals(Configurations.CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE)) {
                                    updated = chequeReturnDao.addCardFeeCount(cardNo, Configurations.CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE, totalChequeReturns);
                                } else if (returnFeeCode.equals(Configurations.CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE)) {
                                    updated = chequeReturnDao.addCardFeeCount(cardNo, Configurations.CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE, totalChequeReturns);
                                } else if (returnFeeCode.equals(Configurations.CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE)) {
                                    updated = chequeReturnDao.addCardFeeCount(cardNo, Configurations.CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE, totalChequeReturns);
                                }
                            } else {
                                updated = chequeReturnDao.addCardFeeCount(cardNo, Configurations.CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE, totalChequeReturns);
                            }
                        }
                    } else {
                        Configurations.PROCESS_FAILD_COUNT++;
                        throw new FailedCardException("Txn id not found for card:");
                    }
                    /**
                     * update the status of chequepayment for the card.
                     */
                    PaymentBean paymentUpdateBean = new PaymentBean(currentCard, bean.getEodid(), bean.getTraceid(), status.getCHEQUE_PAYMENT(), bean.getSeqNo());
                    //TODO update the oldcardnumber
                    chequeReturnDao.updatePaymentStatus(bean.getOldcardnumber(), status.getEOD_DONE_STATUS(), bean.getCqrtseqNo());
                    chequeReturnDao.updateTransactionEODStatus(cardNo, bean.getOldcardnumber(), status.getEOD_DONE_STATUS(), bean.getCqrtseqNo());
                    int updatedCount = chequeReturnDao.updateChequeStatusForEODTxn(paymentUpdateBean, accNo);
                    if (updatedCount == 1) {
                        chequeReturnDao.updateChequePaymentStatus(bean.getId(), status.getEOD_DONE_STATUS());
                    }
                    Configurations.PROCESS_SUCCESS_COUNT = (Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - Configurations.PROCESS_FAILD_COUNT);
                }
                //end of for loop for one card and bean chequeReturnList
                /**
                 * if cheque return day > duedate and < statementend date
                 * before billing cycle. if cheque return day >
                 * statementend date after billing cycle *
                 *
                 */
                /**
                 * update interest,fee, temp block status for current card
                 * still in the chequeReturnList iteration
                 */
                String accNo = chequeReturnDao.getAccountNoOnCard(cardNo);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
                String dueDateString = sdf.format(dueDate);
                double payments = chequeReturnDao.getPaymentAmountBetweenDueDate(accNo, statementStartEODID, status.getEOD_DONE_STATUS(), dueDateString);
                //Seperate out cheques and cash payments.
                if (payments < minAmount) {
                    if (datesNotNull) {
                        if (chequeReturnDate.after(dueDate) && chequeReturnDate.before(statementEndDate)) {
                            //summery.put("card no", common.cardNumberMask(cardNo));
                            details.put("payment insufficient for card", CommonMethods.cardNumberMask(cardNo));
                            int updated = 0;
                            EodInterestBean intBean = chequeReturnDao.getEodInterestForCard(cardNo);
                            if (intBean != null) {
                                //add the calculated Interest for payment from the payment date and sum up with the actual interest.
                                /**
                                 * TODO what if the cheque paid is the
                                 * full amount and the eodinterest is
                                 * wiped from the table? What happens to
                                 * the day by day calculations from the
                                 * cheque point onwards?
                                 */
                                updated = chequeReturnDao.updateEodInterestForCard(cardNo, calculatedInterests);
                                details.put("interests updated", updated + "");
                                CommonMethods.clearStringBuffer(intBean.getCardNumber());
                            }
                            /**
                             * NDIA and Delinquent class The total
                             * cheque returns are deducted from the cash
                             * payments. The first cheque return that
                             * has the ndia is taken in this case to
                             * restore.
                             */
                            //Update fee count for late payment
                            Boolean isThereExist = chequeReturnDao.getFeeCodeIfThereExists(cardNo, Configurations.LATE_PAYMENT_FEE);
                            if (!isThereExist) { // If already posted a late payment fee for the customer, avoid re posting the late payment.
                                updated = chequeReturnDao.addCardFeeCount(cardNo, Configurations.LATE_PAYMENT_FEE, payments - totalChequeReturns);
                                details.put("Add Late payment fee:", updated == 1 ? "Success" : "Already added");
                            }
                            /**
                             * TODO need to insert the minimum amount to
                             * minimumpayment table
                             */
                            /**
                             * Restore the minimumpayment details from
                             * backupminimumpayment table by checking if
                             * cheque+cash are less than min payment
                             */
                            boolean flag = chequeReturnDao.restoreMinimumPayment(cardNo);
                            if (flag) {
                                details.put("updated Minimumpayment table to previous", "SUCCESS");
                            } else {
                                addToMinPayment(chqBean, details);
                                details.put("Inserted to minimumPayment risk profile", "SUCCESS");
                            }

                        } else if (chequeReturnDate.after(statementEndDate)) {
                            /**
                             * is interest added as adjustment or
                             * calculated from closing balance?
                             */
                        } else {
                            /**
                             * Here, the knock on process is already
                             * done.
                             */
                            details.put("other", CommonMethods.cardNumberMask(cardNo));
                        }
                        /**
                         * get total payment and see if all the previous
                         * payments are sufficient to release from the
                         * temporary block status.
                         */
                        BlockCardBean blockBean = chequeReturnDao.getCardBlockOldCardStatus(cardNo);
                        if (blockBean != null) {
                            /* Check the current status of the card.
                             * if the card is active then move him to blocked status.
                             * else continue with the current status
                             */
                            if (blockBean.getNewStatus().equals(status.getCARD_EXPIRED_STATUS())) {
                                //If the card is in expired state and the status is temporary blocked, change it to active status.
                                blockBean.setOldStatus(status.getACTIVE_STATUS());
                            }
                            int count = chequeReturnDao.updateCardStatus(cardNo, blockBean.getOldStatus());
                            if (count == 1)//Clear temporary block status.
                            {
                                count = chequeReturnDao.updateCardStatus(cardNo, status.getCARD_TEMPORARY_BLOCK_Status());
                                details.put("deactivated temporary block status for card", CommonMethods.cardNumberMask(cardNo));
                            }
                            CommonMethods.clearStringBuffer(blockBean.getCardNo());
                            blockBean = null;
                        }
                        /**
                         * Update risk status to previous if available.
                         */
                        int diffInDays = (int) (chequeReturnDate.getTime() - paidDate.getTime()) / (1000 * 60 * 60 * 24);
                        int riskUpdate = 0;
                        if (ndia != 0) {
                            ndia += diffInDays;
                            riskclass = chequeReturnDao.getRiskClassOnNdia(ndia)[1];
                            riskUpdate = chequeReturnDao.updateDelinquencyStatus(cardNo, riskclass, ndia);
                        }

                        if (riskUpdate != 0) {
                            /**
                             * TODO what about the NDIA?
                             */
                            details.put("updated Delinquency status:", chqBean.getDelinquentclass());
                            details.put("updated Delinquency NDIA:", ndia);

                        }
                    }//if dates not null.
                } else {//if for minamount greater than cash payment
                    //cash payments are enough to cover the min amount
                }
                calculatedInterests = 0;
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception ex) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(chqBean.getCardnumber()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.PROCESS_FAILD_COUNT++;
                logManager.logError("proceedChequeReturn Process Error", ex, errorLogger);
            } finally {
                logManager.logDetails(details, infoLogger);
            /* PADSS Change -
               variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                for (ReturnChequePaymentDetailsBean returnChequePaymentDetailBean : chequeReturnList) {
                    CommonMethods.clearStringBuffer(returnChequePaymentDetailBean.getCardnumber());
                }
                chequeReturnList = null;
            }
        }
    }

    private synchronized void updateCardCustomerAccountBalances(OtbBean otbBean) throws Exception {
        try {
            chequeReturnDao.updateCustomerOtb(otbBean);
            chequeReturnDao.updateAccountOtb(otbBean);
            chequeReturnDao.updateCardOtb(otbBean);
            /*
             Update Online Customer,Account and Card For cash only
             */
            otbBean.setOtbcredit(0);
            //otbBean.setTmpcredit(0);
            chequeReturnDao.updateOnlineCustomerOtb(otbBean);
            chequeReturnDao.updateOnlineAccountOtb(otbBean);
            chequeReturnDao.updateOnlineCardOtb(otbBean);
        } catch (Exception e) {
            logManager.logError("Update Card Customer Account Balances Error", e, errorLogger);
            throw e;
        }
    }

    private synchronized void updateCardBalanceByCardCategory(ReturnChequePaymentDetailsBean returnBean, OtbBean otbBean) throws Exception {
        try {
            chequeReturnDao.updateEODCARDBalanceKnockOn(otbBean);
            updateCardCustomerAccountBalances(otbBean);
        } catch (Exception e) {
            logManager.logError("Update Card Balance By Card Category Error", e, errorLogger);
            throw e;
        }
    }

    private synchronized void addToMinPayment(ReturnChequePaymentDetailsBean bean, LinkedHashMap details) throws Exception {
        try {
            double totalTransactions = bean.getClosingbalance();
            double minAmount = bean.getMinamount();
            chequeReturnDao.insertToMinPayTableOld(bean.getCardnumber(), minAmount, totalTransactions, CommonMethods.getSqldate(bean.getDuedate()), 0);
            details.put("card added to min pay table", CommonMethods.cardNumberMask(bean.getCardnumber()));
            details.put("min payment", minAmount);
            Statusts.SUMMARY_FOR_MINPAYMENT_RISK_ADDED++;
        } catch (Exception e) {
            logManager.logError("Add To Min Payment Error", e, errorLogger);
            throw e;
        }
    }
}
