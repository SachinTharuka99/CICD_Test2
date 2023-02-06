package com.epic.cms.dao;

import com.epic.cms.model.bean.CardBillingInfoBean;
import com.epic.cms.model.bean.EomCardBean;

import java.util.ArrayList;
import java.util.List;

public interface EOMInterestDao {

    ArrayList<EomCardBean> getEomCardList(int day) throws Exception;
    String CheckForCardIncrementStatus(StringBuffer cardNumber) throws Exception;
    int clearEomInterest(StringBuffer cardNo) throws Exception;
    ArrayList<java.util.Date> getLastTwoBillingDatesOnAccount(String accNo) throws Exception;
    CardBillingInfoBean getLastTwoBillingDatesAndEodIdOnAccount(String accNo) throws Exception;
    ArrayList<Double> getEOMInterest(EomCardBean eomCardBean, CardBillingInfoBean lastBillingDatesAndEodId, int noOfStatement) throws Exception;
    int insertIntoEomInterest(StringBuffer cardNo, String accNO, double FORWARDINTEREST, double INTERESTAMOUNT, int eodId, String status) throws Exception;
    int insertIntoEodGLAccount(int eodID, java.util.Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception;


}
