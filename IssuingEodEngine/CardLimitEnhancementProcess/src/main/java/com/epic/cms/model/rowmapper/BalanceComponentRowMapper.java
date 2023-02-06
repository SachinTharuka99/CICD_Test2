package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.BalanceComponentBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BalanceComponentRowMapper implements RowMapper<BalanceComponentBean> {

    @Override
    public BalanceComponentBean mapRow(ResultSet result, int rowNum) throws SQLException {
        BalanceComponentBean bean = new BalanceComponentBean();

        bean.setCardNumber(new StringBuffer(result.getString("CARDNO")));
        bean.setIncrementAmount(result.getDouble("AMOUNT"));
        bean.setIncrementType(result.getString("INCREMENTTYPE"));
        bean.setIncOrDec(result.getString("INCORDEC"));
        bean.setRequestId(result.getString("REQUESTID"));
        bean.setCardCategory(result.getString("CARDCATEGORYCODE"));
        bean.setStartDate(result.getDate("STARTDATE").toString());
        bean.setEndDate(result.getDate("ENDDATE").toString());

        return bean;
    }
}
