/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 6:22 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.model.EodOuputFileBean;
import com.epic.cms.model.model.GlAccountBean;
import com.epic.cms.model.model.GlBean;

import java.util.ArrayList;
import java.util.HashMap;

public interface MerchantGLSummaryFileDao {
    ArrayList<GlAccountBean> getCommissionDataToEODGL() throws Exception;

    int insertIntoEodMerchantGLAccount(int eodID, java.util.Date glDate, String merchantID, String glType, String amount, String cdStatus) throws Exception;

    int updateCommissions(String key, int i) throws Exception;

    ArrayList<GlAccountBean> getMerchantFeeDataToEODGL() throws Exception;

    int updateMerchantFeeGlStatus(String key, int i) throws Exception;

    ArrayList<GlAccountBean> getEODMerchantTxnDataToGL() throws Exception;

    int updateEODMerchantTxn(String key, int i) throws Exception;

    int updateEODMerchantPayment(String key, int i) throws Exception;

    ArrayList<GlAccountBean> getEODMerchantPaymentDataToGL() throws Exception;

    HashMap<String, ArrayList<GlAccountBean>> getDataFromEODMERCHANTGl() throws Exception;

    int updateEodMerchantGLAccount(int key) throws Exception;

    void InsertMerchantFilesIntoDownloadTable(String fileId, String fileType) throws Exception;

    int insertOutputFiles(EodOuputFileBean outputfilebean, String fileType) throws Exception;

    HashMap<String, GlBean> getGLAccData() throws Exception;

    HashMap<String, String[]> getGLTxnTypes() throws Exception;

    HashMap<String, String[]> getMerchantGLTypesData() throws Exception;

    java.util.Date getNextWorkingDay(java.util.Date DueDate) throws Exception;

    String getCRDRFromGlTxn(String key) throws Exception;
}
