/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:09 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.EodBean;
import com.epic.cms.model.bean.NextRunningEodBean;

public interface EodIdInfoService {
    EodBean getEodInfoList(Long eodId) throws Exception;
    NextRunningEodBean getNextRunningEodId() throws Exception;
}
