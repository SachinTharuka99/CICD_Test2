package com.epic.cms.repository;

import com.epic.cms.dao.CollectionAndRecoveryAlertDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.HashMap;

@Repository
public class CollectionAndRecoveryAlertRepo implements CollectionAndRecoveryAlertDao {

    @Autowired
    StatusVarList statusList;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public HashMap<StringBuffer, String> getConfirmedCardToAlert() throws Exception {
        HashMap<StringBuffer, String> confirmCardList = new HashMap<>();
        String query = null;
        try {
            query = "SELECT CARDNO, LASTTRIGGERPOINT FROM TRIGGERCARDS WHERE NOTIFICATIONFLAG = ? ";
            if (!Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query = query + "AND CARDNO NOT IN (SELECT CARDNO FROM EODERRORCARDS WHERE STATUS = ?) ";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                    while (result.next()) {
                        confirmCardList.put(new StringBuffer(result.getString("CARDNO")), result.getString("LASTTRIGGERPOINT"));
                    }
                    return confirmCardList;
                }, 0, Configurations.EOD_PENDING_STATUS);
            } else {
                query = query + "AND CARDNO IN (SELECT CARDNO FROM EODERRORCARDS WHERE PROCESSSTEPID <= (SELECT PROCESSSTEPID FROM EODERRORCARDS WHERE ERRORPROCESSID = ?) AND STATUS = ?) ";
                backendJdbcTemplate.query(query, (ResultSet result) -> {
                    while (result.next()) {
                        confirmCardList.put(new StringBuffer(result.getString("CARDNO")), result.getString("LASTTRIGGERPOINT"));
                    }
                    return confirmCardList;
                }, 0, Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE, Configurations.EOD_PENDING_STATUS);
            }
        } catch (Exception e) {
            throw e;
        }
        return confirmCardList;
    }

    @Override
    public void updateAlertGenStatus(StringBuffer cardNumber, String trigger) throws Exception {
        try {
            String updatePay = "UPDATE TRIGGERCARDS SET NOTIFICATIONFLAG = ? WHERE CARDNO = ? AND LASTTRIGGERPOINT = ? ";

            backendJdbcTemplate.update(updatePay, 1, cardNumber.toString(), trigger);
        } catch (Exception e) {
            throw e;
        }
    }
}
