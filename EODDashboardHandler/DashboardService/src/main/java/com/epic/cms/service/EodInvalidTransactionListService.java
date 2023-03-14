/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 1:26 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import java.util.List;

public interface EodInvalidTransactionListService {
    List<Object> getEodInvalidTransactionList(Long eodId) throws Exception;
}
