package com.epic.cms.dao;

import com.epic.cms.model.bean.CashBackBean;
import com.epic.cms.model.bean.ProcessBean;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface CashBackDao {

    ProcessBean getProcessDetails(int processId) throws Exception;

    void loadInitialConfigurationsForCashback() throws Exception;

    List<CashBackBean> getEligibleAccountsForCashback() throws Exception;

    BigDecimal getCashbackAmount(CashBackBean cashbackBean);

    BigDecimal getCashbackAdjustmentAmount(CashBackBean cashbackBean);

    int addNewCashBack(CashBackBean cashbackBean, BigDecimal cashbackAmount, BigDecimal cashbackAdjustmentAmount, String txnVolume);

    int updateCashbackAdjustmentStatus(String accountNumber, String billing_done_status);

    int updateCashbackStartDate(String accountNumber, Date nextCashbackStartDate);

    BigDecimal getRedeemRequestAmount(String accountNumber);

    int redeemCashbacks(CashBackBean cashbackBean, BigDecimal cashbackAmountToBeRedeem);

    int updateEodStatusInCashbackRequest(String accountNumber);

    BigDecimal getRedeemableAmount(CashBackBean cashbackBean);

    int updateNextCBRedeemDate(String accountNumber, String creditOption);

    BigDecimal getCashbackAmountToBeExpireForAccount(String accountNumber);

    int expireNonPerformingCashbacks(CashBackBean cashbackBean, BigDecimal expireCashbackAmount);

    int expireCardCloseCashbacks(CashBackBean cashbackBean, BigDecimal expireCashbackAmount);

    int expireCashbacks(CashBackBean cashbackBean);

    int updateTotalCBAmount(String accountNumber);

}
