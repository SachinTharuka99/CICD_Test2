package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CardFeeBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardFeeRowMapper implements RowMapper<CardFeeBean> {
    @Override
    public CardFeeBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardFeeBean cardFeeBean=new CardFeeBean();
        cardFeeBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
        cardFeeBean.setAccNumber(rs.getString("ACCOUNTNO"));
        cardFeeBean.setAccStatus(rs.getString("STATUS"));
        cardFeeBean.setFeeCode(rs.getString("FEECODE"));
        cardFeeBean.setFeeCount(rs.getInt("FEECOUNT"));
        cardFeeBean.setCurrCode(rs.getInt("CURRENCYCODE"));
        cardFeeBean.setCrOrDr(rs.getString("CRORDR"));
        cardFeeBean.setFlatFee(rs.getDouble("FLATFEE"));
        cardFeeBean.setMinAmount(rs.getDouble("MINIMUMAMOUNT"));
        cardFeeBean.setMaxAmount(rs.getDouble("MAXIMUMAMOUNT"));
        cardFeeBean.setPercentageAmount(rs.getDouble("PERSENTAGE"));
        cardFeeBean.setCombination(rs.getString("COMBINATION"));
        cardFeeBean.setNextAnniversaryDate(rs.getDate("NEXTANNIVERSARYDATE").toString());
        cardFeeBean.setOtbCredit(rs.getDouble("OTBCREDIT"));
        cardFeeBean.setCreditLimit(rs.getDouble("CREDITLIMIT"));
        cardFeeBean.setCashAmount(rs.getDouble("CASHAMOUNT"));
        return cardFeeBean;
    }
}
