package com.epic.cms.dao;

import com.epic.cms.model.bean.MerchantLocationBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MerchantStatementDao {

    public HashMap<String, MerchantLocationBean> getMerchantlocationstobill() throws Exception;

    public MerchantLocationBean insertMerchantStatement(MerchantLocationBean bean) throws Exception;

    public MerchantLocationBean getMerchantPayments(MerchantLocationBean bean) throws Exception;

    public List<Object[]> getMerchantStatementTxnList(String mID) throws Exception;

    public List<Object[]> getMerchantStatementAdjustmentList(String mID) throws Exception;

    public List<Object[]> getMerchantStatementFeesList(String mID) throws Exception;

    public int insertMerchantEodStatus(String type, String status) throws Exception;

    public int insertAuditMerchantEodStatus(String type, String status) throws Exception;

    public void callMerchantStatementProcedure() throws Exception;

    public void callAuditMerchantStatementProcedure() throws Exception;

    public MerchantLocationBean getLastmerchantStatementDetails(MerchantLocationBean bean) throws Exception;

    public double getPreviuosBalance(String mId, int startEodId) throws Exception;

    public boolean insertInToMerchantStatementTable(MerchantLocationBean bean) throws Exception;

    public boolean updateMerchantBillingDate(MerchantLocationBean bean) throws Exception;

    public int updateMerchantPayment(ArrayList<String> paymentList, String type) throws Exception;





}
