package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.TransactionTypeBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EodTransactionRowMapper implements RowMapper<EodTransactionBean> {

    @Override
    public EodTransactionBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        EodTransactionBean eodTransactionBean = new EodTransactionBean();

        return eodTransactionBean;
    }
}
