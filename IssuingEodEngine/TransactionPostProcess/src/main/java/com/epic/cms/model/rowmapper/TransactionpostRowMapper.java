/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:53 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.OtbBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionpostRowMapper implements RowMapper<OtbBean>  {

    @Override
    public OtbBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        OtbBean otbBean = new OtbBean();
        otbBean.setCustomerid(rs.getString("CUSTOMERID"));
        otbBean.setAccountnumber(rs.getString("ACCOUNTNO"));

        otbBean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
        otbBean.setPayment(rs.getDouble("PAYMENT"));
        otbBean.setSale(rs.getDouble("SALE"));
        otbBean.setCashadavance(rs.getDouble("CASHADVANCE"));
        otbBean.setEasypayrev(rs.getDouble("EASYPAYREV"));
        otbBean.setEasypay(rs.getDouble("EASYPAY"));
        otbBean.setEasypayfee(rs.getDouble("EASYPAYFEE"));
        otbBean.setMvisaRefund(rs.getDouble("MVISAREFUND"));
        otbBean.setRefund(rs.getDouble("REFUND"));
        otbBean.setReversal(rs.getDouble("REVERSAL"));

        return otbBean;
    }
}
