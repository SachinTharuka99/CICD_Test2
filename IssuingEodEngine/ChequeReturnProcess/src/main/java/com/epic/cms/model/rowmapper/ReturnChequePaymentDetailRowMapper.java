/**
 * Author : rasintha_j
 * Date : 12/29/2022
 * Time : 8:56 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.ReturnChequePaymentDetailsBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnChequePaymentDetailRowMapper implements RowMapper<ReturnChequePaymentDetailsBean> {
    @Override
    public ReturnChequePaymentDetailsBean mapRow(ResultSet result, int rowNum) throws SQLException {
        ReturnChequePaymentDetailsBean bean = new ReturnChequePaymentDetailsBean();
        bean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
        bean.setEodid(result.getInt("EODID"));
        bean.setAmount(result.getDouble("TRANSACTIONAMOUNT"));
        bean.setChequenumber(result.getString("CHEQUENUMBER"));
        bean.setTraceid(result.getString("TRACEID"));
        bean.setChqRtnDate(result.getDate("POSTINGDATE"));
        return bean;
    }
}
