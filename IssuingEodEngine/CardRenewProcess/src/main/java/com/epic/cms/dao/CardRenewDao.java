/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:11 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.CardRenewBean;
import com.epic.cms.model.bean.ErrorCardBean;

import java.util.ArrayList;
import java.util.List;

public interface CardRenewDao {

    int getCardValidityPeriod(StringBuffer cardNumber) throws Exception;

    void updateCardTable(StringBuffer cardNumber, String newExpireDate, String isProductChange) throws Exception;

    void updateCardRenewTable(StringBuffer cardNumber) throws Exception;

    void updateOnlineCardTable(StringBuffer cardNumber, String newExpireDate) throws Exception;

    ArrayList<CardRenewBean> getApprovedCardList(String curDate) throws Exception;

    boolean isProcessCompletlyFail(int ProcessID) throws Exception;

    public List<ErrorCardBean> getEligibleCardList(String curDate, int hasErrorEODandProcess) throws Exception;

}
