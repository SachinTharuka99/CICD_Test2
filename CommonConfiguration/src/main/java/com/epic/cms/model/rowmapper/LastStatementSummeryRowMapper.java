package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.LastStatementSummeryBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LastStatementSummeryRowMapper implements RowMapper<LastStatementSummeryBean> {

    @Override
    public LastStatementSummeryBean mapRow(ResultSet result, int rowNum) throws SQLException {
        LastStatementSummeryBean bean = new LastStatementSummeryBean();
        bean.setAccNo(result.getString("ACCOUNTNO"));
        bean.setCardno(new StringBuffer(result.getString("CARDNO")));
        bean.setOpaningBalance(result.getDouble("OPENINGBALANCE"));
        bean.setClosingBalance(result.getDouble("CLOSINGBALANCE"));
        bean.setMinAmount(result.getDouble("MINAMOUNT"));
        bean.setDueDate(result.getDate("DUEDATE"));
        bean.setStatementStartDate(result.getDate("STATEMENTSTARTDATE"));
        bean.setStatementEndDate(result.getDate("STATEMENTENDDATE"));
        bean.setClosingloyaltypoint(result.getLong("CLOSINGLOYALTYPOINT"));

        return bean;
    }
}
