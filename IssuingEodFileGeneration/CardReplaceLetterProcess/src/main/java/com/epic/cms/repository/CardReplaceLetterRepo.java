/**
 * Author : yasiru_l
 * Date : 11/22/2022
 * Time : 4:39 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CardReplaceLetterDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CardReplaceLetterRepo implements CardReplaceLetterDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public ArrayList<StringBuffer> getReplacedToGenerateLetters() throws SQLException {
        ArrayList<StringBuffer> replaceCardList = new ArrayList<StringBuffer>();
        String query = null;
        try {
            //query = " select cr.NEWCARDNUMBER from cardreplace cr left join card ca on cr.NEWCARDNUMBER = ca.cardnumber where ca.EMBOSSSTATUS=? and ca.PINSTATUS=? AND cr.LETTERGENERATIONSTATUS=?";
            query = queryParametersList.getCardReplaceLetter_getReplacedToGenerateLetters();
            if (!Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query = query + " and ca.cardnumber not in (select cardno from eoderrorcards where status = ?) ";
                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                replaceCardList.add(new StringBuffer(rs.getString("NEWCARDNUMBER")));
                            }
                            return replaceCardList;
                        },
                        Configurations.YES_STATUS,
                        Configurations.YES_STATUS,
                        Configurations.NO_STATUS,
                        Configurations.EOD_PENDING_STATUS
                        );
            }else {
                query = query + " and ca.cardnumber in (SELECT cardno FROM eoderrorcards where processstepid <= (select processstepid from eoderrorcards where errorprocessid = ?) and status = ?) ";
                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                replaceCardList.add(new StringBuffer(rs.getString("NEWCARDNUMBER")));
                            }
                            return replaceCardList;
                        },
                        Configurations.YES_STATUS,
                        Configurations.YES_STATUS,
                        Configurations.NO_STATUS,
                        Configurations.PROCESS_ID_CARDREPLACE_LETTER,
                        Configurations.EOD_PENDING_STATUS
                        );
            }
        }catch (Exception e){
            throw e;
        }
        return replaceCardList;
    }
    @Override
    public int updateLettergenStatusInCardReplace(StringBuffer cardNo, String yes) throws Exception {
        int count = 0;
        try{
            //String updatePay = "update cardreplace set LETTERGENERATIONSTATUS=? where NEWCARDNUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getCardReplaceLetter_updateLettergenStatusInCardReplace(),
                    yes,
                    cardNo.toString()
                    );
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<StringBuffer> getProductChangedCardsToGenerateLetters() throws SQLException {
        ArrayList<StringBuffer> productChangeCards = new ArrayList<StringBuffer>();
        String query = null;
        try{
            //query = "select cr.NEWCARDNUMBER from CARDPRODUCTCHANGESTAGE cr left join card ca on cr.NEWCARDNUMBER = ca.cardnumber where ca.EMBOSSSTATUS=? and ca.PINSTATUS=? AND cr.LETTERGENSTATUS=?";
            query = queryParametersList.getCardReplaceLetter_getProductChangedCardsToGenerateLetters();

            if (!Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query = query + " and ca.cardnumber not in (select cardno from eoderrorcards where status = ?) ";
               backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                productChangeCards.add(new StringBuffer(rs.getString("NEWCARDNUMBER")));
                            }
                            return productChangeCards;
                            },
                        Configurations.YES_STATUS,
                        Configurations.YES_STATUS,
                        Configurations.NO_STATUS,
                        Configurations.EOD_PENDING_STATUS
                        );
            }else {
                query = query + " and ca.cardnumber in (SELECT cardno FROM eoderrorcards where processstepid <= (select processstepid from eoderrorcards where errorprocessid = ?) and status = ?) ";
               backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                productChangeCards.add(new StringBuffer(rs.getString("NEWCARDNUMBER")));
                            }
                            return productChangeCards;
                        },
                        Configurations.YES_STATUS,
                        Configurations.YES_STATUS,
                        Configurations.NO_STATUS,
                        Configurations.PROCESS_ID_CARDREPLACE_LETTER,
                        Configurations.EOD_PENDING_STATUS
                        );
            }
        }catch (Exception e){
            throw e;
        }
        return productChangeCards;
    }

    @Override
    public List<String> getCardProductCardTypeForProductChangeCards(StringBuffer cardNo) {
        List<String> cardDetails = new ArrayList<String>();
        String sql = null;
        try{
            //sql = "SELECT C.CARDTYPE,C.CARDPRODUCT,CAC.ACCOUNTNO FROM CARD C LEFT JOIN CARDACCOUNTCUSTOMER CAC ON C.CARDNUMBER=CAC.CARDNUMBER WHERE C.CARDNUMBER= ?";
            sql = queryParametersList.getCardReplaceLetter_getCardProductCardTypeForProductChangeCards();

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            cardDetails.add(0, rs.getString("CARDTYPE"));
                            cardDetails.add(1, rs.getString("CARDPRODUCT"));
                            cardDetails.add(2, rs.getString("ACCOUNTNO"));
                        }
                        return cardDetails;
                    },
                    cardNo.toString()
            );
        }catch (Exception e){
            throw e;
        }
        return cardDetails;
    }

    @Override
    public int updateLettergenStatusInProductChange(StringBuffer cardNo, String yes) throws Exception {
        int count = 0;
        try{
            //String updatePay = "update CARDPRODUCTCHANGESTAGE set LETTERGENSTATUS=? where NEWCARDNUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getCardReplaceLetter_updateLettergenStatusInProductChange(),
                    yes,
                    cardNo.toString()
                    );
        }catch (Exception e){
            throw e;
        }
        return count;
    }
}
