package com.epic.cms.repository;

import com.epic.cms.dao.CardReplaceDao;
import com.epic.cms.model.bean.CardReplaceBean;
import com.epic.cms.model.rowmapper.CardReplaceRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CardReplaceRepo implements CardReplaceDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    public CardReplaceRepo(JdbcTemplate backendJdbcTemplate, JdbcTemplate onlineJdbcTemplate, QueryParametersList queryParametersList, StatusVarList status) {
        this.backendJdbcTemplate = backendJdbcTemplate;
        this.onlineJdbcTemplate = onlineJdbcTemplate;
        this.queryParametersList = queryParametersList;
        this.status = status;
    }


    @Override
    public List<CardReplaceBean> getCardListToReplace() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        List<CardReplaceBean> cardListToReplace = new ArrayList<CardReplaceBean>();
        try{
            cardListToReplace = backendJdbcTemplate.query(queryParametersList.getCardReplace_getCardListToReplace(), new CardReplaceRowMapper() ,status.getCARD_INIT(),Configurations.CARD_REPLACE_ACCEPT,sdf.format(Configurations.EOD_DATE));
        }catch (Exception e){
            throw e;
        }
        return cardListToReplace;
    }

    @Override
    public void updateBackendOldCardFromNewCard(CardReplaceBean cardReplaceBean)  throws Exception{
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("REPLACECARDBACKENDSERVER");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("oldCardNo", cardReplaceBean.getOldCardNo().toString())
                    .addValue("newCardNo", cardReplaceBean.getOldCardNo().toString())
                    .addValue("status", Configurations.EOD_DONE_STATUS);
            simpleJdbcCall.execute(in);

        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void updateOnlineOldCardFromNewCard(CardReplaceBean cardReplaceBean) throws Exception {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(onlineJdbcTemplate)
                    .withProcedureName("REPLACECARDONLINESERVER");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("oldCardNo", cardReplaceBean.getOldCardNo().toString())
                    .addValue("newCardNo", cardReplaceBean.getOldCardNo().toString());
            simpleJdbcCall.execute(in);

        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void updateCardReplaceStatus(StringBuffer newCardNo) throws Exception {
        try{
            backendJdbcTemplate.update(queryParametersList.getCardReplace_updateCardReplaceStatus(),
                    Configurations.YES_STATUS,Configurations.YES_STATUS,Configurations.EOD_DONE_STATUS,newCardNo.toString());
        }catch (Exception e){
            throw e;
        }
    }

}
