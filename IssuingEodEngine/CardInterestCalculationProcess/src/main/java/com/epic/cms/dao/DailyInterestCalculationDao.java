package com.epic.cms.dao;

import com.epic.cms.model.bean.DailyInterestBean;
import com.epic.cms.model.bean.InterestDetailBean;
import com.epic.cms.model.bean.StatementBean;

import java.util.ArrayList;
import java.util.Date;

public interface DailyInterestCalculationDao {
    ArrayList<StatementBean> getLatestStatementAccountList() throws Exception;

    InterestDetailBean getIntProf(String accountNo) throws Exception;

    ArrayList<DailyInterestBean> getTxnOrPaymentDetailByAccount(String accountNumber, int startEodId, int endEodId, Date endDate, Double lastBillOpenningBalance, Date lastBillStartDate, Date lastBillEndDate, int lastBillEndEodId) throws Exception;

    int updateEodInterest(StatementBean bean, double txnInterest, double interestRate) throws Exception;
}
