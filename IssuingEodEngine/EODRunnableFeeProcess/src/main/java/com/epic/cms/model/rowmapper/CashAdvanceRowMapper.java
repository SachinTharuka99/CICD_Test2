package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CashAdvanceBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CashAdvanceRowMapper implements RowMapper<CashAdvanceBean> {
    @Override
    public CashAdvanceBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CashAdvanceBean cashAdvanceBean = new CashAdvanceBean();
        cashAdvanceBean.setTotalCashAdvanceAmount(rs.getDouble("TRANSACTIONAMOUNT"));
        cashAdvanceBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
        cashAdvanceBean.setAccountNo(rs.getString("ACCOUNTNO"));
        cashAdvanceBean.setTransactionDate(rs.getDate("TRANSACTIONDATE"));
        cashAdvanceBean.setTxnid(rs.getString("TRANSACTIONID"));
        return cashAdvanceBean;
    }
}
