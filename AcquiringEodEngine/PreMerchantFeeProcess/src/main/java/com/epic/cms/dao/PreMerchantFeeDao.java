/**
 * Author : sharuka_j
 * Date : 1/26/2023
 * Time : 12:52 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.MerchantBeanForFee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface PreMerchantFeeDao {
    HashMap<String, List<String>> getFeeCodeListForFeeProfile() throws Exception;

    ArrayList<MerchantBeanForFee> getMerchantListForFeeProcess() throws Exception;

    int updateAllMerchantRecurringDates() throws Exception;

    int updateAllTerminalRecurringDates() throws Exception;

    int addMerchantFeeCount(String merchantId, String feeCode) throws Exception;
}
