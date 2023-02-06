/**
 * Author :
 * Date : 2/3/2023
 * Time : 1:44 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.RecPaymentFileIptRowDataBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecPaymentFileIptRowDataRowMapper implements RowMapper<RecPaymentFileIptRowDataBean> {
    @Override
    public RecPaymentFileIptRowDataBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        RecPaymentFileIptRowDataBean bean = new RecPaymentFileIptRowDataBean();
        bean.setFileid(rs.getString("FILEID"));
        bean.setLinenumber(rs.getBigDecimal("LINENUMBER"));
        bean.setLinecontent(rs.getString("RECORDCONTENT"));
        return bean;
    }
}
