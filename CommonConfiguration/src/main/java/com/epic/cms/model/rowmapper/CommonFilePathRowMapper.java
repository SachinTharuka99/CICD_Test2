package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CommonFilePathBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommonFilePathRowMapper implements RowMapper<CommonFilePathBean> {
    /**
     * @param rs
     * @param rowNum
     * @return
     * @throws SQLException
     */
    @Override
    public CommonFilePathBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CommonFilePathBean commonFilePathBean = new CommonFilePathBean();
        commonFilePathBean.setStatement(rs.getString("STATEMENTFILE"));
        commonFilePathBean.setAutoSettlement(rs.getString("AUTOSETTLEMENT"));
        commonFilePathBean.setLetters(rs.getString("LETTERS"));
        commonFilePathBean.setGlFile(rs.getString("GLFILE"));
        commonFilePathBean.setMerchantGlFile(rs.getString("MERCHANTGLFILE"));
        commonFilePathBean.setEodFile(rs.getString("EODFILE"));
        commonFilePathBean.setExposureFile(rs.getString("EXPOSUREFILE"));
        commonFilePathBean.setRb36(rs.getString("RB36"));
        commonFilePathBean.setMerchantStatementFile(rs.getString("MERCHANTSTATEMENTFILE"));
        commonFilePathBean.setMerchantCustomerStatementFile(rs.getString("MERCHANTCUSTOMERSTATEMENTFILE"));
        commonFilePathBean.setMerchantStatementSummeryFile(rs.getString("MERCHANTSTATEMENTSUMMARYFILE"));
        commonFilePathBean.setOutgoingFile(rs.getString("OUTGOINGFILE"));
        commonFilePathBean.setCashBack(rs.getString("CASHBACK"));
        commonFilePathBean.setMerchantPaymentFile(rs.getString("MERCHANTPAYMENTFILE"));
        commonFilePathBean.setBulkApplication(rs.getString("BULKAPPLICATION"));
        commonFilePathBean.setPrintedStatementReport(rs.getString("PRINTEDSTATEMENTREPORT"));
        commonFilePathBean.setOutgoingIpmFile(rs.getString("OUTGOINGIPMFILE"));
        commonFilePathBean.setMasterCardAbuFile(rs.getString("MASTERCARDABUFILE"));
        commonFilePathBean.setEodDashboardConsoleLog(rs.getString("EODDASHBOARDCONSOLELOG"));
        commonFilePathBean.setOutgoingCupStatementFile(rs.getString("OUTGOINGUPISTATEMENTFILE"));
        return commonFilePathBean;
    }
}
