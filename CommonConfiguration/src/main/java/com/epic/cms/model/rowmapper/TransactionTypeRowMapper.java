package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.TransactionTypeBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionTypeRowMapper implements RowMapper<TransactionTypeBean> {

    @Override
    public TransactionTypeBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        TransactionTypeBean transactionTypeBean = new TransactionTypeBean();
        transactionTypeBean.setOnlineTxnType(rs.getString("ONLINETXNTYPE"));
        transactionTypeBean.setBackendTxnType(rs.getString("TRANSACTIONCODE"));
        return transactionTypeBean;
    }
}
