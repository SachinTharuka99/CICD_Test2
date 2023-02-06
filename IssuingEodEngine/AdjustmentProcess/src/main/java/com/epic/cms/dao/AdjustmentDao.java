package com.epic.cms.dao;

import com.epic.cms.model.bean.AdjustmentBean;
import com.epic.cms.model.bean.PaymentBean;

import java.util.List;

public interface AdjustmentDao {

    List<AdjustmentBean> getAdjustmentList() throws Exception;

    void insertToEODPayments(PaymentBean paymentBean) throws Exception;

    String getCardAssociationFromCardBin(String cardBin) throws Exception;

    void insertInToEODTransaction(AdjustmentBean adjustmentBean,String cardAssociation) throws Exception;

    int updateAdjustmentStatus(String id) throws Exception;

    int updateTransactionToEDON(String txnId) throws Exception;
}
