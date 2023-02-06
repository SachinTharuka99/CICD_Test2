package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.OtbBean;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component("FeeAmountRowMapper")
public class FeeAmountRowMapper implements OtbRowMapper {

    @Override
    public OtbBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        OtbBean otbBean = new OtbBean();
        otbBean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
        otbBean.setOtbcredit(rs.getDouble("FINANCIALCHARGES"));
        otbBean.setTmpcredit(rs.getDouble("FINANCIALCHARGES"));
        return otbBean;
    }
}
