package com.epic.cms.model.rowmapper;


import com.epic.cms.model.bean.OtbBean;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component("CustAccRowMapper")
public class CustAccRowMapper implements OtbRowMapper {

    @Override
    public OtbBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        OtbBean otbBean = new OtbBean();
        otbBean.setCustomerid(rs.getString("CUSTOMERID"));
        otbBean.setAccountnumber(rs.getString("ACCOUNTNO"));
        return otbBean;
    }
}
