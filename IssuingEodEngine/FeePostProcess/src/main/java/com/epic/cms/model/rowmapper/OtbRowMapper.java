package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.OtbBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface OtbRowMapper extends RowMapper<OtbBean> {
    @Override
    default OtbBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        OtbBean otbBean = new OtbBean();
        otbBean.setCustomerid(rs.getString("CUSTOMERID"));
        otbBean.setAccountnumber(rs.getString("ACCOUNTNO"));
        otbBean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
        otbBean.setOtbcredit(rs.getDouble("FINANCIALCHARGES"));
        otbBean.setTmpcredit(rs.getDouble("FINANCIALCHARGES"));
        return otbBean;
    }
}
