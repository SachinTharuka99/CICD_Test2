package com.epic.cms.service;

import com.epic.cms.dao.CommonDao;
import com.epic.cms.dao.RunnableFeeDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class RunnableFeeService {
    @Autowired
    public LogManager logManager;

    @Autowired
    public RunnableFeeDao runnableFeeDao;

    @Autowired
    public CommonDao commonDao;

    @Autowired
    public StatusVarList status;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addRunnableFees(CardBean cardBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            try {
                //System.out.println("Checking EOD fee for card" + CommonMethods.cardNumberMask(cardBean.getCardnumber()));
                //details.put("Checking EOD fee for card", CommonMethods.cardNumberMask(cardBean.getCardnumber()));
                addAnniversaryFee(cardBean, format, details);
                addCashAdvanceFee(cardBean, details);
                addLatePaymentFee(cardBean, format, details);
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception ex) {
                Configurations.PROCESS_FAILD_COUNT++;
            } finally {
                logManager.logDetails(details, infoLogger);
            }
        }
    }

    /**
     * check anniversary date from the card and add an annual fee code to the
     * card table. Next change the status as taken for eod. Finally update the
     * next anniversary date in the card table.
     *
     * @param cardBean
     * @param format   * throws Exception
     * @param details
     */
    private void addAnniversaryFee(CardBean cardBean, SimpleDateFormat format, LinkedHashMap details) throws Exception {
        Date ann_date = null;

        try {
            int updated = 0;
            ann_date = (cardBean.getNextAnniversaryDate());
            if (ann_date != null && (ann_date.toString().equals(format.format(Configurations.EOD_DATE)) || ann_date.before(Configurations.EOD_DATE))) {
                if (!cardBean.getCardCategory().equalsIgnoreCase(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                    Configurations.SUMMARY_FOR_FEE_ANNIVERSARY++;

                    //Calculate the annual fee for this on the current day, or if anniv. date already passed.
                    details.put("Currently adding annual fee for: ", CommonMethods.cardNumberMask(cardBean.getCardnumber()));
                    details.put("Adding anniversary fee for card", "SUCCESS");

                    if (!(cardBean.getCardStatus().equalsIgnoreCase(status.getCARD_REPLACED_STATUS()) || cardBean.getCardStatus().equalsIgnoreCase(status.getCARD_PRODUCT_CHANGE_STATUS()))) {
                        //check whether annual fee wave of for np accounts
                        if (Configurations.ANNUAL_FEE_FOR_NP_ACCOUNTS == status.getNO_STATUS_0()) {
                            //if not annual fee for np accounts, check whether acc status is deactive.then not calculate annual fee
                            if (!(cardBean.getAccStatus().equalsIgnoreCase(status.getDEACTIVE_STATUS())
                                    || cardBean.getAccStatus().equalsIgnoreCase(status.getACCOUNT_NON_PERFORMING_STATUS()))) {
                                boolean feeExist = runnableFeeDao.checkFeeExistForCard(cardBean.getCardnumber(), Configurations.ANNUAL_FEE);
                                logManager.logStartEnd("Annual Fee Exist :" + feeExist, infoLogger);

                                details.put("Adding anniversary fee for " + Configurations.ANNUAL_FEE_FOR_NP_ACCOUNTS + " : " + status.getNO_STATUS_0(), "SUCCESS");

                                updated = runnableFeeDao.addCardFeeCount(cardBean.getCardnumber(), Configurations.ANNUAL_FEE, 0);
                            }
                        } else if (Configurations.ANNUAL_FEE_FOR_NP_ACCOUNTS == status.getYES_STATUS_1()) {
                            boolean feeExist = runnableFeeDao.checkFeeExistForCard(cardBean.getCardnumber(), Configurations.ANNUAL_FEE);
                            logManager.logStartEnd("Annual Fee Exist :" + feeExist, infoLogger);

                            details.put("Adding anniversary fee for " + Configurations.ANNUAL_FEE_FOR_NP_ACCOUNTS + " : " + status.getYES_STATUS_1(), "SUCCESS");

                            updated = runnableFeeDao.addCardFeeCount(cardBean.getCardnumber(), Configurations.ANNUAL_FEE, 0);
                        }

                        //Change the anniversary date to next year.
                        runnableFeeDao.updateNextAnniversaryDate(cardBean.getCardnumber());
                        details.put("updated the next anniversary fee for card", "SUCCESS");
                        details.put("updated the next anniversary date for card number:", CommonMethods.cardNumberMask(cardBean.getCardnumber()));
                        Configurations.SUMMARY_FOR_FEE_ANNIVERSARY_PROCESSED++;
                    }
                }
            }
        } catch (NullPointerException ex) {
            details.put("anniversary date is not found for: ", CommonMethods.cardNumberMask(cardBean.getCardnumber()));
        } catch (Exception ex) {
            logManager.logError("exception in anniversary date for card: " + CommonMethods.cardNumberMask(cardBean.getCardnumber()), ex, errorLogger);
            details.put("exception in anniversary date for card:", CommonMethods.cardNumberMask(cardBean.getCardnumber()));
            Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardBean.getCardnumber(), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            Configurations.FAILED_CARDS++;
            throw ex;
        }
    }

    /**
     * Cash advance fee needs to be captured here. First, check the transaction
     * table, get the cash advance codes, then increment the card feecount
     * table.
     *
     * @param cardBean
     * @param details
     * @throws Exception
     */
    private void addCashAdvanceFee(CardBean cardBean, LinkedHashMap details) throws Exception {
        List<CashAdvanceBean> cashAdvances = null;

        try {
            cashAdvances = runnableFeeDao.findCashAdvances(cardBean.getCardnumber());
            if (cashAdvances != null && !cashAdvances.isEmpty()) {
                details.put("Cash Advances found for card: " + CommonMethods.cardNumberMask(cardBean.getCardnumber()) + "", cashAdvances.size() + "");

                double cashAmount = 0;
                for (CashAdvanceBean bean : cashAdvances) {
                    try {
                        cashAmount = bean.getTotalCashAdvanceAmount();
                        details.put("Cash advance taken:", cashAmount);
                        //TODO add straight away to eodcardfee table.

                        insertToEODCARDFee(cardBean.getCardnumber(), bean.getTxnid(), bean.getAccountNo(), Configurations.CASH_ADVANCE_FEE, cashAmount, details);

                        details.put("Added Cash Advance Fee", "SUCCESS");
                        Configurations.SUMMARY_FOR_FEE_CASHADVANCES++;
                    } catch (Exception e) {
                        logManager.logError("--error--" + e, errorLogger);
                        Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardBean.getCardnumber(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                        Configurations.FAILED_CARDS++;
                    }
                }
            }
        } catch (NullPointerException ex) {
            logManager.logError("cash advances not found for: " + CommonMethods.cardNumberMask(cardBean.getCardnumber()), ex, errorLogger);//WebComHandler.showOnWeb(CommonMethods.eodDashboardProcessInfoStyle("cash advances not found for: " + common.cardNumberMask(cardBean.getCardnumber())));
            logManager.logError("Null for cash advances", ex, errorLogger);
        } catch (Exception ex) {
            logManager.logError("SQL error for cash advances", ex, errorLogger);
            Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardBean.getCardnumber(), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            throw ex;
        } finally {
            for (CashAdvanceBean bean : cashAdvances) {
                CommonMethods.clearStringBuffer(bean.getCardNumber());
            }
            cashAdvances = null;
        }
    }

    /**
     * On due date, check if the payments received from last billing cycle to
     * today is less than the min amount then add the late payment fee. Late
     * payment fee is added on next billing date while the "count" is increased
     * on the due date.
     *
     * @param cardBean
     * @param format
     * @param details
     */
    private void addLatePaymentFee(CardBean cardBean, SimpleDateFormat format, LinkedHashMap details) throws Exception {

        try {
            LastStmtSummeryBean lastStatement = runnableFeeDao.getLastStatementSummaryInfor(cardBean.getCardnumber());
            if (lastStatement != null) {
                java.sql.Date nextBillingDate = runnableFeeDao.getNextBillingDateForCard(cardBean.getCardnumber());
                java.sql.Date effectDate = nextBillingDate;
                if (effectDate.toString().equals(format.format(Configurations.EOD_DATE))) {
                    addMinPaymentFee(cardBean.getCardnumber(), lastStatement, details);
                }
            } else {
                System.out.println("--no laststatement found--");
            }
        } catch (Exception ex) {
            logManager.logError("SQL error for late payment fee", ex, errorLogger);
            throw ex;
        }
    }

    /**
     * Insert into EODCard Fee table the amount with respect to the MIN or MAX
     * of percentage or fees.
     *
     * @param cardNumber
     * @param accountNo
     * @param feeCode
     * @param cashAmount
     * @throws Exception
     * @author Bilal_a
     */
    public synchronized void insertToEODCARDFee(StringBuffer cardNumber, String txnId, String accountNo, String feeCode, double cashAmount, LinkedHashMap details) throws Exception {
        CardFeeBean cardFeeBean = null;
        try {
            cardFeeBean = runnableFeeDao.getCardFeeProfileForCard(cardNumber, feeCode);
            cardFeeBean.setTxnId(txnId);
            cardFeeBean.setAccNumber(accountNo);
            double perc_amount = cardFeeBean.getPercentageAmount() * cashAmount / 100;
            //Add effective date.
            double fee = cardFeeBean.getFlatFee();
            String combination = cardFeeBean.getCombination();
            double amount = CommonMethods.getAmountfromCombination(perc_amount, fee, combination);
            if (amount >= cardFeeBean.getMaxAmount()) {
                amount = cardFeeBean.getMaxAmount();
            } else if (amount <= cardFeeBean.getMinAmount()) {
                amount = cardFeeBean.getMinAmount();
            }
            details.put("card", CommonMethods.cardNumberMask(cardNumber));
            details.put("fee type", feeCode);
            details.put("flat fee", fee);
            details.put("fee MIN/MAX/CMB", combination);
            details.put("final amount", amount);

            java.sql.Date effectDate = CommonMethods.getSqldate(Configurations.EOD_DATE);
            if (feeCode.equals(Configurations.LATE_PAYMENT_FEE)) {
                java.sql.Date nextBillingDate = runnableFeeDao.getNextBillingDateForCard(cardNumber);
                effectDate = nextBillingDate;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                if (effectDate.toString().equals(format.format(Configurations.EOD_DATE))) {
                    runnableFeeDao.insertToEODcardFee(cardFeeBean, amount, effectDate);
                    runnableFeeDao.updateCardFeeCount(cardFeeBean);
                    Configurations.SUMMARY_FOR_FEE_UPDATE++;
                }
            } else {//Can add else if statements here if needed to insert data on effective date only!
                if (!runnableFeeDao.checkDuplicateCashAdvances(cardNumber, txnId, feeCode)) {
                    runnableFeeDao.insertToEODcardFee(cardFeeBean, amount, effectDate);
                    Configurations.SUMMARY_FOR_FEE_UPDATE++;
                }
            }

        } catch (Exception e) {
            logManager.logError("exceptions occurred for:" + CommonMethods.cardNumberMask(cardNumber), e, errorLogger);
            Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardFeeBean.getCardNumber(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            //FAILED_CARD_FEE++;
        } finally {
            CommonMethods.clearStringBuffer(cardFeeBean.getCardNumber());
            cardFeeBean = null;
            CommonMethods.clearStringBuffer(cardNumber);
        }
    }

    /**
     * Add the minimum payment fee if a payment has not been made at the due date.
     *
     * @param cardNo
     * @param lastStatement
     * @throws SQLException
     * @throws IOException
     */
    private synchronized void addMinPaymentFee(StringBuffer cardNo, LastStmtSummeryBean lastStatement, LinkedHashMap details) throws SQLException, IOException {
        //Check if the current EOD date is due date for the current EOD date.
        try {
            //Check the minamount from the billingstatement table
            String accNo;
            double payments, minAmount = 0;
            int statementDayEODID, dueDateEodid = 0;
            Date statementEnd = lastStatement.getStatementEndDate();
            Date dueDate = lastStatement.getDueDate();
            statementDayEODID = Integer.parseInt(CommonMethods.getDate(statementEnd) + "00");
            dueDateEodid = Integer.parseInt(CommonMethods.getDate(dueDate) + "00");
            minAmount = lastStatement.getMinAmount();

            accNo = runnableFeeDao.getAccountNoOnCard(cardNo);
            payments = runnableFeeDao.getTotalPayment(accNo, statementDayEODID, dueDateEodid);

            /**
             * Check the payment from the payment table and if the total payment
             * done after statement date and the due date is equal or greater
             * than minumum amount if the payment is less than the minimum
             * amount, increment the minimum fee count. *
             */
            if (payments < minAmount) {
                runnableFeeDao.addCardFeeCount(cardNo, Configurations.LATE_PAYMENT_FEE, 0);
                details.put("Added min payment fee for card", "SUCCESS");
                Configurations.SUMMARY_FOR_FEE_LATEPAYMENTS++;
            }

        } catch (Exception ex) {
            logManager.logError("Exception in getting due date: ", ex, errorLogger);
            Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, cardNo, ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
            Configurations.FAILED_CARDS++;
        }
    }
}
