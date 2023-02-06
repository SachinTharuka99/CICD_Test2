package com.epic.cms.dao;

import com.epic.cms.model.bean.CashBackAlertBean;

import java.util.ArrayList;
import java.util.HashMap;

public interface CashBackAlertDao {
    HashMap<String, ArrayList<CashBackAlertBean>> getConfirmedAccountToAlert() throws Exception;

    void updateCashBackAlertGenStatus(int reqId) throws Exception;

    void updateBillingStatementAlertGenStatus(String statementId) throws Exception;
}
