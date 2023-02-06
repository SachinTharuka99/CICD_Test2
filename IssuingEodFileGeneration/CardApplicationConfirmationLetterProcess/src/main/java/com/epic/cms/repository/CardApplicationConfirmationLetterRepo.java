/**
 * Author : yasiru_l
 * Date : 11/21/2022
 * Time : 10:09 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CardApplicationConfirmationLetterDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class CardApplicationConfirmationLetterRepo implements CardApplicationConfirmationLetterDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private StatusVarList statusVarList;

    @Override
    public ArrayList<StringBuffer> getConfirmedCardToGenerateLetters() throws SQLException {
        ArrayList<StringBuffer> confirmCardList = new ArrayList<StringBuffer>();
        String query = null;
        try {
            query = "select ca.cardnumber from card CA left join cardapplication capp on capp.CARDNUMBER = ca.cardnumber where ca.EMBOSSSTATUS=? and PINSTATUS=? AND LETTERGENSTATUS=? ";
            if (!Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query = query + "and ca.cardnumber not in (select cardno from eoderrorcards where status = ?) ";
                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                confirmCardList.add(new StringBuffer(rs.getString("cardnumber")));
                            }
                            return confirmCardList;
                        },
                        Configurations.YES_STATUS,
                        Configurations.YES_STATUS,
                        Configurations.NO_STATUS,
                        Configurations.EOD_PENDING_STATUS
                );
            }else {
                query = query + "and ca.cardnumber in (SELECT cardno FROM eoderrorcards where processstepid <= (select processstepid from eoderrorcards where errorprocessid = ?) and status = ?) ";
               backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                confirmCardList.add(new StringBuffer(rs.getString("cardnumber")));
                            }
                            return confirmCardList;
                        },
                        Configurations.YES_STATUS,
                        Configurations.YES_STATUS,
                        Configurations.NO_STATUS,
                        Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE,
                        Configurations.EOD_PENDING_STATUS
                        );
            }
        }catch (Exception e){
            errorLogger.error("Error in get Confirm card to Generate Letter ", e);
            throw e;
        }
        return confirmCardList;
    }

    @Override
    public int updateLettergenStatus(StringBuffer cardNo, String yes) throws Exception {
        int count = 0;
        try {
            String updatePay = "update cardapplication set lettergenstatus=? where cardnumber=?";
            count = backendJdbcTemplate.update(updatePay,
                    yes,
                    cardNo.toString()
                    );
        }catch (Exception e){
            errorLogger.error("Error in Update Letter generate status ", e);
            throw e;
        }
        return count;
    }
}
