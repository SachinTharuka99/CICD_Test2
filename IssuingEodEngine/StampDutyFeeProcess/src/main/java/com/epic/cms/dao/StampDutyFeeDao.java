package com.epic.cms.dao;

import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.StampDutyBean;

import java.util.ArrayList;
import java.util.Date;

public interface StampDutyFeeDao {

    ArrayList<StampDutyBean> getInitStatementAccountList() throws Exception;

    ArrayList<StampDutyBean> getErrorStatementAccountList() throws Exception;

    ArrayList<StringBuffer> getOldCardNumbers(StringBuffer newcardnumber) throws Exception;

    double getTotalForeignTxns(String inClauseString, int startEodID) throws Exception;

    void insertToEODcardFee(CardFeeBean cardFeeBean, double amount, Date effectDate, String txnType) throws Exception;

    int getStartEodId(String accountNo) throws Exception;

    ArrayList<StampDutyBean> getStatementCardList(String accountNumber) throws Exception;

    boolean checkFeeExixtForCard(StringBuffer cardNumber, String feeCode) throws Exception;
}
