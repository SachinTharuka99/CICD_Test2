package com.epic.cms.dao;

import com.epic.cms.model.bean.PaymentBean;

import java.util.List;

public interface PaymentReversalDao {
    List<PaymentBean> getPaymentReversals() throws Exception;

    int updatePaymentsForCashReversals(StringBuffer cardNumber, String traceNo) throws Exception;
}
