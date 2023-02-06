package com.epic.cms.dao;

import com.epic.cms.model.bean.DropRequestBean;

import java.util.List;

public interface TxnDropRequestDao {
    int getTransactionValidityPeriod() throws Exception;

    List<DropRequestBean> getDropTransactionList(int txnValidityPeriod) throws Exception;

    boolean getTransactionReverseStatus(String txnId) throws Exception;

    void addTxnDropRequest(String txnId, StringBuffer cardNumber) throws Exception;
}
