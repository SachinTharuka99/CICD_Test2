package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.InstallmentBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InstallmentRowMapper implements RowMapper<InstallmentBean> {
    @Override
    public InstallmentBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        InstallmentBean installmentBean = new InstallmentBean();
        installmentBean.setRequestID(rs.getString("REQUESTID"));
        installmentBean.setCurrentCount(rs.getInt("CURRINSTALLMENT"));
        installmentBean.setTotalFEeAmount(rs.getString("INTERESTORFEETOTALAMOUNT"));
        installmentBean.setAccelarateStatus(rs.getString("ACCELERATEDSTATUS"));
        installmentBean.setFeeType(rs.getString("PROCESSINGFEETYPE"));
        installmentBean.setIncludeFirstMonth(rs.getString("FIRSTMONTHINCLUDE"));
        installmentBean.setFeeApplyFirstMonth(rs.getString("FEEAPPLYINFIRSTMONTH"));
        installmentBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
        installmentBean.setTxnAmount(rs.getString("TOTALAMOUNT"));
        installmentBean.setStatus(rs.getString("STATUS"));
        installmentBean.setTxnID(rs.getString("TXNID"));
        installmentBean.setInstalmentAmount(rs.getString("INSTALLMENTAMOUNT"));
        installmentBean.setInterestRate(rs.getString("INTERESTORFEEAMOUNT"));
        installmentBean.setAccNo(rs.getString("ACCOUNTNO"));
        installmentBean.setRemainingCount(rs.getInt("REMAININGCOUNT"));
        installmentBean.setDuration(Integer.parseInt(rs.getString("DURATION")));
        installmentBean.setRunningStatus(rs.getInt("RUNNINGSTATUS"));
        installmentBean.setEffectivedate(rs.getDate("EFECTIVEDATE").toString());
        installmentBean.setNxtTxnDate(rs.getDate("NEXTTXNDATE").toString());
        installmentBean.setTxnDescription(rs.getString("TXNDESCRIPTION"));
        installmentBean.setCurruncyCode(rs.getString("CURRENCYNUMCODE"));
        installmentBean.setTraceNumber(rs.getString("TRACENO"));
        return installmentBean;
    }
}
