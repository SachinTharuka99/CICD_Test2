/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:44 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.DelinquentAccountBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DelinquentAccountRowMapper implements RowMapper<DelinquentAccountBean> {
    @Override
    public DelinquentAccountBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        delinquentAccountBean.setAccNo(rs.getString("ACCOUNTNO"));
        delinquentAccountBean.setAccStatus(rs.getString("ACCSTATUS"));
        delinquentAccountBean.setSupervisor(rs.getString("SUPERVISOR"));
        delinquentAccountBean.setAssignStatus(rs.getString("ASSIGNSTATUS"));
        delinquentAccountBean.setAssignee(rs.getString("ASSIGNEE"));
        delinquentAccountBean.setCardCategory(rs.getString("CARDCATEGORYCODE"));
        delinquentAccountBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
        delinquentAccountBean.setCif(rs.getString("CIF"));
        delinquentAccountBean.setLastStatementDate(rs.getDate("LASTSTATEMENTDATE"));
        delinquentAccountBean.setDelinqstatus(rs.getString("DELINQSTATUS"));
        delinquentAccountBean.setDueDate(rs.getDate("DUEDATE"));
        delinquentAccountBean.setMIA(rs.getInt("MIA"));
        delinquentAccountBean.setNDIA(rs.getInt("NDIA"));
        delinquentAccountBean.setIdNumber(rs.getString("IDNUMBER"));
        delinquentAccountBean.setIdType(rs.getString("IDTYPE"));
        delinquentAccountBean.setContactNo(rs.getString("CONTACTNO"));
        delinquentAccountBean.setRiskClass(rs.getString("RISKCLASS"));
        delinquentAccountBean.setDueAmount(Double.toString(rs.getDouble("DUEAMOUNT")));
        delinquentAccountBean.setNpDate(rs.getDate("NPDATE"));
        delinquentAccountBean.setNpInterest(rs.getDouble("NPINTEREST"));
        delinquentAccountBean.setNpOutstanding(rs.getDouble("NPOUTSTANDING"));
        delinquentAccountBean.setAccruedInterest(rs.getDouble("NPACCRUEDINTEREST"));
        delinquentAccountBean.setAccruedFees(rs.getDouble("NPACCRUEDFEES"));
        delinquentAccountBean.setAccruedOverLimit(rs.getDouble("NPACCRUEDOVERLIMITFEES"));
        delinquentAccountBean.setAccruedlatePay(rs.getDouble("NPACCRUEDLATEPAYFEES"));
        delinquentAccountBean.setProvisionAmount(rs.getDouble("NPPROVISIONAMOUNT"));
        delinquentAccountBean.setRemainDue(rs.getDouble("REMAINDUE"));

        return delinquentAccountBean;
    }
}
