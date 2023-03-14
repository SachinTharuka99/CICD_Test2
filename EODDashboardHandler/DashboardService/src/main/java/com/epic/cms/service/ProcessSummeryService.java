/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:04 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessSummeryBean;

import java.util.List;

public interface ProcessSummeryService {
    List<ProcessSummeryBean> getEodProcessSummeryList(Long eodID) throws Exception;
}
