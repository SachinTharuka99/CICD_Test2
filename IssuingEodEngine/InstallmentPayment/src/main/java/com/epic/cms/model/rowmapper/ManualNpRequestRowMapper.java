package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.ManualNpRequestBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ManualNpRequestRowMapper implements RowMapper<ManualNpRequestBean> {

    @Override
    public ManualNpRequestBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        ManualNpRequestBean manualNpRequestBean = new ManualNpRequestBean();

        manualNpRequestBean.setAccNumber(rs.getString("ACCOUNTNO"));
//        manualNpRequestBean.setRequestId(rs.getInt("REQUESTID"));
//        manualNpRequestBean.setCardNumber(new StringBuffer(rs.getString("MAINCARDNUMBER")));
//        manualNpRequestBean.setAccStatus(rs.getString("ACCSTATUS"));
//        manualNpRequestBean.setNdia(rs.getInt("NDIA"));

        return manualNpRequestBean;

    }
}
