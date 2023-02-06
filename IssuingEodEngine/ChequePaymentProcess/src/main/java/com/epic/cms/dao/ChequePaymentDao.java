package com.epic.cms.dao;

import com.epic.cms.model.bean.ReturnChequePaymentDetailBean;

import java.util.List;

public interface ChequePaymentDao {

    List<ReturnChequePaymentDetailBean> getChequePaymentsBackup() throws Exception;

    int insertChequePayments(ReturnChequePaymentDetailBean bean) throws Exception;

    int updateChequePayment(ReturnChequePaymentDetailBean bean) throws Exception;
}
