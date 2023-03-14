/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:28 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.StatementGenSummeryBean;

import java.util.List;

public interface StatementGenSummeryListService {
    List<StatementGenSummeryBean> getStatementGenSummeryList(Long eodId) throws Exception;
}
