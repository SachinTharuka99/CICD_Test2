package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.MinimumPaymentBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MinimumPaymentRowMapper implements RowMapper<MinimumPaymentBean> {
    @Override
    public MinimumPaymentBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        MinimumPaymentBean minimumPaymentBean = new MinimumPaymentBean();
        minimumPaymentBean.setM1(rs.getString("m1"));
        minimumPaymentBean.setM2(rs.getString("m2"));
        minimumPaymentBean.setM3(rs.getString("m3"));
        minimumPaymentBean.setM4(rs.getString("m4"));
        minimumPaymentBean.setM5(rs.getString("m5"));
        minimumPaymentBean.setM6(rs.getString("m6"));
        minimumPaymentBean.setM7(rs.getString("m7"));
        minimumPaymentBean.setM8(rs.getString("m8"));
        minimumPaymentBean.setM9(rs.getString("m9"));
        minimumPaymentBean.setM10(rs.getString("m10"));
        minimumPaymentBean.setM11(rs.getString("m11"));
        minimumPaymentBean.setM12(rs.getString("m12"));
        return minimumPaymentBean;
    }
}
