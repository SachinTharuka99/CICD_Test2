package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CollectionAndRecoveryBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CollectionAndRecoveryRowMapper implements RowMapper<CollectionAndRecoveryBean> {

    @Override
    public CollectionAndRecoveryBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer(rs.getString("CARDNO")));
        collectionAndRecoveryBean.setDueDate(rs.getDate("DUEDATE").toString());
        collectionAndRecoveryBean.setDueAmount(rs.getDouble("MINAMOUNT"));

        return collectionAndRecoveryBean;
    }
}
