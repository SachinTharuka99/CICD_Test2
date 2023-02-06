/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 1:45 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.MerchantEasyPaymentRequestBean;

import java.util.ArrayList;

public interface MerchantEasyPaymentRequestDao {
    ArrayList<MerchantEasyPaymentRequestBean> getAllEasypaymentTransactions() throws Exception;
    int insertEasyPaymentRequest(MerchantEasyPaymentRequestBean bean) throws Exception;
    int updateEodTransactionForEasyPaymentStatus(String txnid) throws Exception;
    int updateEodMerchantTransactionForEasyPaymentStatus(String txnid) throws Exception;
    int insertEasyPaymentRejectRequest(MerchantEasyPaymentRequestBean bean) throws Exception;
}
