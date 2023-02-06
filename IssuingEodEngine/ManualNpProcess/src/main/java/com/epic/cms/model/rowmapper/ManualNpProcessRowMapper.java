package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.DelinquentAccountBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ManualNpProcessRowMapper implements RowMapper<DelinquentAccountBean> {
    @Override
    public DelinquentAccountBean mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();

        delinquentAccountBean.setCardCategory(resultSet.getString("CARDCATEGORYCODE"));
        delinquentAccountBean.setAccNo(resultSet.getString("ACCOUNTNO"));
        delinquentAccountBean.setCif(resultSet.getString("CUSTOMERID"));
        delinquentAccountBean.setLastStatementDate(resultSet.getDate("STATEMENTENDDATE"));
        delinquentAccountBean.setDueDate(resultSet.getDate("DUEDATE"));
        delinquentAccountBean.setDueAmount(resultSet.getString("MINAMOUNT"));
        delinquentAccountBean.setNameOnCard(resultSet.getString("NAMEONCARD"));
        delinquentAccountBean.setIdNumber(resultSet.getString("IDNUMBER"));
        delinquentAccountBean.setIdType(resultSet.getString("IDTYPE"));
        delinquentAccountBean.setCif(resultSet.getString("CUSTOMERID"));
        delinquentAccountBean.setAccStatus(resultSet.getString("STATUS"));
        delinquentAccountBean.setNDIA(resultSet.getInt("NDIA"));

        return delinquentAccountBean;

    }
}
