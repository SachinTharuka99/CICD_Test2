/**
 * Author : rasintha_j
 * Date : 1/24/2023
 * Time : 12:53 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.model.bean.MerchantFeeBean;
import com.epic.cms.model.bean.ProcessBean;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

public interface MerchantFeeDao {
    List<MerchantFeeBean> getMerchantFeeCountList() throws Exception;
    void insertToEODMerchantFee(MerchantFeeBean merchantFeeBean, double amount, Date effectDate) throws Exception;
    void updateMerchantFeecount(MerchantFeeBean merchantFeeBean) throws Exception;
    int insertErrorEODMerchant(ErrorMerchantBean eBean) throws Exception;
}
