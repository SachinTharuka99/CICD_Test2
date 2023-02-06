package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CardBalanceBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardBalanceBeanRowMapper implements RowMapper<CardBalanceBean> {
    @Override
    public CardBalanceBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardBalanceBean cardBalanceBean = new CardBalanceBean();
        cardBalanceBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
        cardBalanceBean.setCumFinanceCharge(rs.getDouble("FINANCIALCHARGES"));
        cardBalanceBean.setPayment(rs.getDouble("PAYMENTS"));
        cardBalanceBean.setCumTxn(rs.getDouble("CUMTRANSACTIONS"));
        return cardBalanceBean;
    }
}
