/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 10:55 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import com.epic.cms.model.bean.ProcessBean;

import java.sql.SQLException;

public interface MerchantPaymentDao {
    int[] callStoredProcedureForEodMerchantPayment() throws SQLException;
}
