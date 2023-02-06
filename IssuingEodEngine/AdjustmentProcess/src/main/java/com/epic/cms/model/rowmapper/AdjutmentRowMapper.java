package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.AdjustmentBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdjutmentRowMapper implements RowMapper<AdjustmentBean> {
    @Override
    public AdjustmentBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        AdjustmentBean adjustmentBean = new AdjustmentBean();
        adjustmentBean.setId(rs.getString("ID"));
        adjustmentBean.setAdjustAmount(rs.getDouble("AMOUNT"));
        adjustmentBean.setAdjustDate(rs.getDate("ADJUSTDATE"));
        adjustmentBean.setAdjustDes(rs.getString("REMARK"));
        adjustmentBean.setTxnType(rs.getString("TRANSACTIONTYPE"));
        adjustmentBean.setCrDr(rs.getString("CRDR"));
        adjustmentBean.setCardNumber(new StringBuffer(rs.getString("UNIQUEID")));
        adjustmentBean.setAccNo(rs.getString("ACCOUNTNO"));
        adjustmentBean.setAdjustType(rs.getString("ADJUSTMENTTYPE"));
        adjustmentBean.setCurruncyType(rs.getString("CURRENCYTYPE"));
        adjustmentBean.setTxnId(rs.getString("TXNID"));
        adjustmentBean.setTraceNo(rs.getString("TRACENO"));
        return adjustmentBean;
    }
}
