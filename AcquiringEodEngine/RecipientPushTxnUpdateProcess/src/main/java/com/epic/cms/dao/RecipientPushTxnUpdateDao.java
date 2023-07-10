package com.epic.cms.dao;

import com.epic.cms.model.bean.EodTransactionBean;

import java.util.ArrayList;
import java.util.HashMap;

public interface RecipientPushTxnUpdateDao {
    ArrayList<EodTransactionBean> getAllRecipientPushTxn() throws Exception;

    HashMap<String, String> getFinancialStatus() throws Exception;

    String getCardProduct(String bin) throws Exception;

    int insertIntoEodMerchantTransaction(EodTransactionBean eodTransactionBean, String status) throws Exception;

    int updateTransactionToEDON(String txnId, StringBuffer cardNo) throws Exception;

    int updateEodProcessSummery(int eodId, String status, int processId, int successCount, int failedCount, String progress)
            throws Exception;

    String getAccountNoOnCard(StringBuffer cardNo) throws Exception;
}
