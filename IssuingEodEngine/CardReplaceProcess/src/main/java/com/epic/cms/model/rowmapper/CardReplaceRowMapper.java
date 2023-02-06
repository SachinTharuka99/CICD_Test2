package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.CardReplaceBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardReplaceRowMapper implements RowMapper<CardReplaceBean> {
    @Override
    public CardReplaceBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        CardReplaceBean CardReplaceBean = new CardReplaceBean();
        CardReplaceBean.setOldCardNo(new StringBuffer(rs.getString("NEWCARDNUMBER")));
        CardReplaceBean.setNewCardNo(new StringBuffer(rs.getString("OLDCARDNUMBER")));
        return CardReplaceBean;
    }
}
