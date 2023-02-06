package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CardBillingInfoBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardBillingInfoRowMapper implements RowMapper<CardBillingInfoBean> {

    @Override
    public CardBillingInfoBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardBillingInfoBean cardInfoBilling = new CardBillingInfoBean();

        cardInfoBilling.setDueDate(rs.getDate("DUEDATE"));
        cardInfoBilling.setEndEodId(rs.getInt("ENDEODID"));
        cardInfoBilling.setStartEodId(rs.getInt("STARTEODID"));
        cardInfoBilling.setStatementStartDate(rs.getDate("STATEMENTSTARTDATE"));
        cardInfoBilling.setStatementEndDate(rs.getDate("STATEMENTENDDATE"));
        cardInfoBilling.setThisBillingClosingBalance(rs.getDouble("THISBILLCLOSINGBALANCE"));
        cardInfoBilling.setThisBillingOpeningBalance(rs.getDouble("THISBILLOPERNINGBALANCE"));
        cardInfoBilling.setMinPayDue(rs.getDouble("MINPAYMENTDUE"));

        return cardInfoBilling;
    }
}
