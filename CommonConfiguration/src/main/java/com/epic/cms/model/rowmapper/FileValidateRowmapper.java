package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.PaymentFileDataBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FileValidateRowmapper implements RowMapper<PaymentFileDataBean> {
    @Override
    public PaymentFileDataBean mapRow(ResultSet result, int rowNum) throws SQLException {
        PaymentFileDataBean bean = new PaymentFileDataBean();
        bean.setFileid(result.getString("FILEID"));
        bean.setLinenumber(result.getBigDecimal("LINENUMBER"));
        bean.setLinecontent(result.getString("RECORDCONTENT"));


        return bean;
    }
}
