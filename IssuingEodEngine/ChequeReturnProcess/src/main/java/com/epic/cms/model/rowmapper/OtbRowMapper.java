package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.OtbBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OtbRowMapper implements RowMapper<OtbBean> {

    @Override
    public OtbBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        OtbBean eomOtbBean = new OtbBean();
        eomOtbBean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
        eomOtbBean.setMaincardno(new StringBuffer(rs.getString("MAINCARDNUMBER")));
        eomOtbBean.setAccountnumber(rs.getString("ACCOUNTNO"));
        eomOtbBean.setCustomerid(rs.getString("CUSTOMERID"));
        eomOtbBean.setIsPrimary(rs.getString("ISPRIMARY"));
        eomOtbBean.setFinacialcharges(rs.getDouble("CUMFINANCIALCHARGE"));
        eomOtbBean.setCumcashadvance(rs.getDouble("CUMCASHADVANCE"));
        eomOtbBean.setCumtransactions(rs.getDouble("CUMTRANSACTION"));
        return eomOtbBean;
    }
}
