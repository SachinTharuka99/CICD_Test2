package com.epic.cms.service;

import com.epic.cms.model.bean.CashBackBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.repository.CashBackRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CashBackService {

    @Autowired
    LogManager logManager;

    @Autowired
    CashBackRepo cashBackRepo;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void cashBack(CashBackBean cashbackBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                try {
                    details.put("Account Number ", cashbackBean.getAccountNumber());
                    // *** calculate cashback amount if statementdate is eoddate or less ***
                    Date statementDate = cashbackBean.getStatementDate();

                    if (statementDate != null && (statementDate.toString().equals(dateformat.format(Configurations.EOD_DATE)) || statementDate.before(Configurations.EOD_DATE))) {
                        if (!(cashbackBean.getAccountStatus().equals(status.DEACTIVE_STATUS) || cashbackBean.getAccountStatus().equals(status.ACCOUNT_NON_PERFORMING_STATUS))
                                && !cashbackBean.getMainCardStatus().equals(status.CARD_CLOSED_STATUS)
                                && !cashbackBean.getMainCardStatus().equals(status.CARD_EXPIRED_STATUS)
                                && !cashbackBean.getMainCardStatus().equals(status.CARD_REPLACED_STATUS)
                                && !cashbackBean.getMainCardStatus().equals(status.CARD_PRODUCT_CHANGE_STATUS)) { //Account need not to deactive and main card need not to CACL,CAEX
                            //get calculated cashback amount for this statement month
                            BigDecimal cashbackAmount = cashBackRepo.getCashbackAmount(cashbackBean);

                            //calculated transaction volume for calculated cashback amount
                            String txnVolume = cashbackAmount.multiply(BigDecimal.valueOf(100).
                                    divide(BigDecimal.valueOf(cashbackBean.getCashbackRate()), 2, RoundingMode.FLOOR)).setScale(2, RoundingMode.FLOOR).toString();

                            // get approved cashback adjustments from adjustment table with  eod status='EPEN' (if any)
                            BigDecimal cashbackAdjustmentAmount = cashBackRepo.getCashbackAdjustmentAmount(cashbackBean);
                            cashbackAmount = cashbackAmount.add(cashbackAdjustmentAmount);

                            //add new record to CASHBACK table and update Available cashback amount,last cashback date in CARDACCOUNT table
                            cashBackRepo.addNewCashBack(cashbackBean, cashbackAmount, cashbackAdjustmentAmount, txnVolume);

                            //update adjustement table eod status to 'BCCP'
                            cashBackRepo.updateCashbackAdjustmentStatus(cashbackBean.getAccountNumber(), status.getBILLING_DONE_STATUS());
                        }
                    }
                    // update CASHBACKSTARTDATE in CARDACCOUNT table if it complete a year today from that date.
                    Date nextCashbackStartDate = cashbackBean.getNextCashbackStartDate();
                    if (nextCashbackStartDate != null && (nextCashbackStartDate.toString().equals(dateformat.format(Configurations.EOD_DATE)) || nextCashbackStartDate.before(Configurations.EOD_DATE))) {
                        //set eoddate as cashbackstartdate. this date will consider for maximum redeem limit for year. (from that date onward)
                        cashBackRepo.updateCashbackStartDate(cashbackBean.getAccountNumber(), DateUtil.getSqldate(nextCashbackStartDate));
                    }

                    // *** REDEEM (from web request or monthly/quartally/anually) ***
                    if (!(cashbackBean.getAccountStatus().equals(status.getDEACTIVE_STATUS()) || cashbackBean.getAccountStatus().equals(status.getACCOUNT_NON_PERFORMING_STATUS()))
                            && !cashbackBean.getMainCardStatus().equals(status.getCARD_CLOSED_STATUS())
                            && !cashbackBean.getMainCardStatus().equals(status.getCARD_EXPIRED_STATUS())
                            && !cashbackBean.getMainCardStatus().equals(status.getCARD_REPLACED_STATUS())
                            && !cashbackBean.getMainCardStatus().equals(status.getCARD_PRODUCT_CHANGE_STATUS())) { //Account need not to deactive and main card need not to CACL,CAEX

                        //consider redeem  requests from web
                        BigDecimal cashbackAmountToBeRedeem = cashBackRepo.getRedeemRequestAmount(cashbackBean.getAccountNumber()); //get requested total redeem amount if any
                        if (cashbackAmountToBeRedeem.signum() != 0) { //pending redeem request available
                            cashBackRepo.redeemCashbacks(cashbackBean, cashbackAmountToBeRedeem);
                            cashBackRepo.updateEodStatusInCashbackRequest(cashbackBean.getAccountNumber());
                        }
                        //consider account credit option (monthly/quartally/anually)
                        Date nextCBRedeemDate = cashbackBean.getNextCBRedeemDate(); //this will be laststatementdate+25 days
                        if (nextCBRedeemDate != null && (nextCBRedeemDate.toString().equals(dateformat.format(Configurations.EOD_DATE)) || nextCBRedeemDate.before(Configurations.EOD_DATE))) {
                            //logic to calculate actual redeemabal amount
                            BigDecimal cashbackAmountToAutoRedeem = cashBackRepo.getRedeemableAmount(cashbackBean);
                            if (cashbackAmountToAutoRedeem.signum() != 0) { //there exist a redeemable amount can redeem
                                cashBackRepo.redeemCashbacks(cashbackBean, cashbackAmountToAutoRedeem);
                            }
                            //add next cb redeem date
                            cashBackRepo.updateNextCBRedeemDate(cashbackBean.getAccountNumber(), cashbackBean.getCreditOption());
                        }
                    }

                    // *** EXPIRE (when expire the valid months or account goes to non performing)  ***
                    if (cashbackBean.getAccountStatus().equals(status.getDEACTIVE_STATUS()) || cashbackBean.getAccountStatus().equals(status.getACCOUNT_NON_PERFORMING_STATUS())) { //account is non performing.need to expire all cashback amounts
                        BigDecimal cashbackAmountToBeExpire = cashBackRepo.getCashbackAmountToBeExpireForAccount(cashbackBean.getAccountNumber());
                        if (cashbackAmountToBeExpire.signum() != 0) {
                            cashBackRepo.expireNonPerformingCashbacks(cashbackBean, cashbackAmountToBeExpire);
                        }
                    } else if (cashbackBean.getMainCardStatus().equals(status.getCARD_CLOSED_STATUS())) { //main card is close,need to expire all cashback amounts
                        BigDecimal cashbackAmountToBeExpire = cashBackRepo.getCashbackAmountToBeExpireForAccount(cashbackBean.getAccountNumber());
                        if (cashbackAmountToBeExpire.signum() != 0) {
                            cashBackRepo.expireCardCloseCashbacks(cashbackBean, cashbackAmountToBeExpire);
                        }
                    } else {  // for other account, check if any cashback exists for expire; if any the expire it
                        cashBackRepo.expireCashbacks(cashbackBean);
                    }
                    //update TOTALCBAMOUNT column in cashback table that containing final cashback amount for this statement cycle. for reporting purpose
                    cashBackRepo.updateTotalCBAmount(cashbackBean.getAccountNumber());
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    details.put("Process Status", "Passed");

                } catch (Exception e) {
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cashbackBean.getAccountNumber()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                    logManager.logInfo("Cashback process failed for accountnumber " + cashbackBean.getAccountNumber(), infoLogger);
                    logManager.logError("Cashback process failed for accountnumber " + cashbackBean.getAccountNumber(), e, errorLogger);
                    details.put("Process Status", "Failed");
                    Configurations.PROCESS_FAILD_COUNT++;
                }
            } catch (Exception e) {
                logManager.logError("Error Occured while getting db connections ", e, errorLogger);
            } finally {
                logManager.logDetails(details, infoLogger);
            }
        }
    }
}
