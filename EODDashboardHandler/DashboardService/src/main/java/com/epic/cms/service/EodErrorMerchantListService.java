/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:31 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.EodErrorMerchantBean;

import java.util.List;

public interface EodErrorMerchantListService {
    List<EodErrorMerchantBean> getEodErrorMerchantList(Long eodId) throws Exception;
}
