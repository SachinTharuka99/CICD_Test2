/**
 * Author : sharuka_j
 * Date : 2/2/2023
 * Time : 9:33 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.model.bean.MerchantPayBean;
import com.epic.cms.model.bean.MerchantPaymentCycleBean;
import com.epic.cms.model.model.EodOuputFileBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public interface MerchantPaymentFileDao {

    HashMap<String, String> getCurrencyList() throws Exception;

    HashMap<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> getMerchantsForPayment() throws Exception;

    void updateMerchantLocationNextPaymentDate(ArrayList<String> merchantList) throws SQLException, Exception;

    void updateMerchantCustomerNextPaymentDate(ArrayList<String> merchantList) throws SQLException, Exception;

    MerchantCustomerBean getMerchantCustomerDetails(String CusId) throws Exception;

    ArrayList<MerchantPayBean> getPaymentsForCustomerFromEodMerchantpayment(String key) throws Exception;

    int updatePaymentFileStatus(ArrayList<String> paymentIdList) throws Exception, SQLException;

    java.util.Date getNextWorkingDay(java.util.Date DueDate) throws Exception;

    ArrayList<MerchantPayBean> getPaymentsFromEodMerchantpayment(String key) throws Exception;

    int InsertMerchantPaymentFilesIntoDownloadTable(String fileId, String fileType) throws Exception;

    int insertOutputFiles(EodOuputFileBean outputfilebean, String fileType) throws Exception;

    void InsertMerchantFilesIntoDownloadTable(String fileId, String fileType) throws Exception;
}
