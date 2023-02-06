/**
 * Author : lahiru_p
 * Date : 11/22/2022
 * Time : 12:11 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CardRenewLetterDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;

@Repository
public class CardRenewLetterRepo implements CardRenewLetterDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;


    @Override
    public ArrayList<StringBuffer> getRenewalCardsToGenerateLetters() throws Exception {
        ArrayList<StringBuffer> renewalCardList = new ArrayList<>();
        try {
            String sql = "SELECT cr.cardnumber FROM cardrenew cr WHERE cr.status = ? and LETTERGENERATIONSTATUS <> ? ";

            if (!Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                sql = sql + "and cr.cardnumber not in (select cardno from eoderrorcards where status = ? ) ";

                backendJdbcTemplate.query(sql, (ResultSet result) -> {
                    while (result.next()) {
                        renewalCardList.add(new StringBuffer(result.getString("CARDNUMBER")));
                    }
                    return renewalCardList;
                }, statusList.getCARD_RENEWAL_COMPLETE(), Configurations.YES_STATUS, Configurations.EOD_PENDING_STATUS);

            } else {
                sql = sql + "and cr.cardnumber in (SELECT cardno FROM eoderrorcards where processstepid <= (select processstepid from eoderrorcards where errorprocessid = ?) and status = ?) ";

                backendJdbcTemplate.query(sql, (ResultSet result) -> {
                    while (result.next()) {
                        renewalCardList.add(new StringBuffer(result.getString("CARDNUMBER")));
                    }
                    return renewalCardList;
                }, statusList.getCARD_RENEWAL_COMPLETE(), Configurations.YES_STATUS, Configurations.PROCESS_ID_CARDRENEW_LETTER, Configurations.EOD_PENDING_STATUS);

            }
        } catch (Exception e) {
            throw e;
        }
        return renewalCardList;
    }

    @Override
    public int updateLettergenStatusInCardRenew(StringBuffer cardNo, String letterGenStatus) throws Exception {
        int count = 0;
        try {
            String updatePay = "update cardrenew set LETTERGENERATIONSTATUS=? where cardnumber=?";
            count = backendJdbcTemplate.update(updatePay, letterGenStatus, cardNo.toString());

        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
