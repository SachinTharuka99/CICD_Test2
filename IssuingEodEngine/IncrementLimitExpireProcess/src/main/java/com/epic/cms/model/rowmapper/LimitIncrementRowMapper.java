package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.LimitIncrementBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LimitIncrementRowMapper implements RowMapper<LimitIncrementBean> {

    @Override
    public LimitIncrementBean mapRow(ResultSet result, int rowNum) throws SQLException {
        LimitIncrementBean bean = new LimitIncrementBean();
        bean.setCardNumber(new StringBuffer(result.getString("CARDNO")));
        bean.setIncrementAmount(result.getString("AMOUNT"));
        bean.setIncrementType(result.getString("INCREMENTTYPE"));
        bean.setIncordec(result.getString("INCORDEC"));
        bean.setRequestid(result.getString("REQUESTID"));
        bean.setCardcategorycode(result.getString("CARDCATEGORYCODE"));
        bean.setAccountnumber(result.getString("ACCOUNTNO"));
        bean.setCustomerid(result.getString("CUSTOMERID"));

        return bean;
    }
}
