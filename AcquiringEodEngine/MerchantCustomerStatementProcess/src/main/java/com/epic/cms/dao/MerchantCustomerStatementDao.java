package com.epic.cms.dao;

import com.epic.cms.model.bean.MerchantCustomerBean;

import java.util.HashMap;

public interface MerchantCustomerStatementDao {
    HashMap<String, MerchantCustomerBean> getMerchantCustomersToBill() throws Exception;

    void insertMerchantCustomerStatement(MerchantCustomerBean merchantBean) throws Exception;

    int insertMerchantEodStatus(String type, String status) throws Exception;

    int insertAuditMerchantEodStatus(String type, String status) throws Exception;

    void callMerchantCustomerStatementProcedure() throws Exception;

    void callAuditMerchantCustomerStatementProcedure() throws Exception;

    MerchantCustomerBean getMerchantCustomerPayments(MerchantCustomerBean bean) throws Exception;

    MerchantCustomerBean getLastmerchantCustomerStatementDetails(MerchantCustomerBean bean) throws Exception;

    boolean insertInToMerchantCustomerStatementTable(MerchantCustomerBean bean) throws Exception;

    boolean updateMerchantCustomerBillingDate(MerchantCustomerBean bean) throws Exception;
}
