package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CardBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardRowMapper implements RowMapper<CardBean> {
    @Override
    public CardBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardBean cardBean = new CardBean();
        cardBean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
        cardBean.setMainCardNo(new StringBuffer(rs.getString("MAINCARDNO")));
        cardBean.setCardStatus(rs.getString("CARDSTATUS"));
        cardBean.setAccStatus(rs.getString("ACCOUNTSTATUS"));
        cardBean.setCreditLimit(rs.getDouble("CREDITLIMIT"));
        cardBean.setCashLimit(rs.getDouble("CASHLIMIT"));
        cardBean.setOtbCredit(rs.getDouble("OTBCREDIT"));
        cardBean.setOtbCash(rs.getDouble("OTBCASH"));
        cardBean.setPriorityLevel(rs.getString("PRIORITYLEVEL"));
        cardBean.setNextAnniversaryDate(rs.getDate("NEXTANNIVERSARYDATE"));
        cardBean.setActivateDate(rs.getDate("ACTIVATIONDATE"));
        cardBean.setCardCategory(rs.getString("CARDCATEGORYCODE"));
        return cardBean;
    }
}
