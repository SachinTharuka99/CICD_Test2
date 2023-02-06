package com.epic.cms.dao;

import com.epic.cms.model.bean.CommissionProfileBean;
import com.epic.cms.model.bean.CommissionTxnBean;
import com.epic.cms.model.bean.MerchantLocationBean;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public interface MerchantCommissionCalculationDao {
    List<MerchantLocationBean> getAllMerchants() throws Exception;

    Boolean getCustomerCommStatus(String merchantCustomerNo) throws Exception;

    String getCommissionProfile(String merchantCustomerNo) throws Exception;

    String getCalMethod(String commissionProfile) throws Exception;

    Queue<CommissionProfileBean> getAllCommCombination(String comisionProfile, String COMMISSION_TABLE, String COMMISSION_SEGMENT, String COMMISSION_DEFAULT_KEY) throws Exception;

    ArrayList<CommissionTxnBean> getTransactionForCommission(String merchantId, String binType, String cardProduct, String segment, String calMethod, String segmentColumnName, String COMMISSION_DEFAULT_KEY) throws Exception;

    void getMerchantDetails(CommissionTxnBean commissionTxnBean) throws Exception;

    int insertToEodMerchantComission(String merchantCusNo,
                                     String merhantCusAcountNo, String mId, String merchantAccNo, String tId,
                                     String txnAmount, Double merchantComission, String currency,
                                     String crDr, Date txnDate, String txnTypeCode, String batchNo,
                                     String txnId, String binStatus, String calMethod, String cardAssociation,
                                     String cardProduct, String segment, String originCardProduct, String calculatedMdrPercentage, String calculatedMdrFlatAmount) throws Exception;

    int updateEodMerchantTxnEdon(String transactionid, String eod_done_status) throws Exception;

    Queue<CommissionProfileBean> getAllCommCombinationForVolume(String commissionProfile, String commissionVolumeTable, String commissionSegmentVolume, String commissionDefaultVolume) throws Exception;

    String getVolumeId(double totalTxnAmount) throws Exception;

    CommissionProfileBean getCommissionProfile(String commissionProfile, String binType, String productCode, String volumeId) throws Exception;

    ArrayList<CommissionTxnBean> getTransactionForCommissionVolumeWise(String merchantId, String binType, String cardProduct, String calMethod, ArrayList<CommissionTxnBean> commissionTxnList) throws Exception;
}
