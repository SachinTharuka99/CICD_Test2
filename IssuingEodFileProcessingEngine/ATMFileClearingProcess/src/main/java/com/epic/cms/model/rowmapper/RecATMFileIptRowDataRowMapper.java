/**
 * Author :
 * Date : 2/2/2023
 * Time : 11:30 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.RecATMFileIptRowDataBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class RecATMFileIptRowDataRowMapper implements RowMapper<RecATMFileIptRowDataBean> {
    @Override
    public RecATMFileIptRowDataBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        RecATMFileIptRowDataBean bean = new RecATMFileIptRowDataBean();
        bean.setFileid(rs.getString("FILEID"));
        bean.setLinenumber(rs.getBigDecimal("LINENUMBER"));
        bean.setLinecontent(rs.getString("RECORDCONTENT"));

        return bean;
    }
}
