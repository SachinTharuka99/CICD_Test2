/**
 * Author : lahiru_p
 * Date : 11/22/2022
 * Time : 10:32 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CollectionAndRecoveryLetterDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;

@Repository
public class CollectionAndRecoveryLetterRepo implements CollectionAndRecoveryLetterDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<StringBuffer> getFirstReminderEligibleCards() throws Exception {
        ArrayList<StringBuffer> cardList = new ArrayList<>();
        try {
            String query = "SELECT CARDNO FROM TRIGGERCARDS  WHERE LETTERGENSTATUS = ? AND LASTTRIGGERPOINT = ? ";

            if (!Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query = query + "and CARDNO not in (select cardno from eoderrorcards where status = ?) ";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                    while (result.next()) {
                        cardList.add(new StringBuffer(result.getString("CARDNO")));
                    }
                    return cardList;
                }, Configurations.NO_STATUS, Configurations.TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE, Configurations.EOD_PENDING_STATUS);

            } else {
                query = query + "and CARDNO in (SELECT cardno FROM eoderrorcards where processstepid <= (select processstepid from eoderrorcards where errorprocessid = ?) and status = ?) ";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                    while (result.next()) {
                        cardList.add(new StringBuffer(result.getString("CARDNO")));
                    }
                    return cardList;
                }, Configurations.NO_STATUS, Configurations.TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE, Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS, Configurations.EOD_PENDING_STATUS);

            }
        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public ArrayList<StringBuffer> getSecondReminderEligibleCards() throws Exception {
        ArrayList<StringBuffer> cardList = new ArrayList<>();
        try {
            String query = "SELECT CARDNO FROM TRIGGERCARDS  WHERE LETTERGENSTATUS = ? and LASTTRIGGERPOINT = ? ";
            if (!Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query = query + "and CARDNO not in (select cardno from eoderrorcards where status = ?) ";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                    while (result.next()) {
                        cardList.add(new StringBuffer(result.getString("CARDNO")));
                    }
                    return cardList;
                }, Configurations.NO_STATUS, Configurations.TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE, Configurations.EOD_PENDING_STATUS);

            } else {
                query = query + "and CARDNO in (SELECT cardno FROM eoderrorcards where processstepid <= (select processstepid from eoderrorcards where errorprocessid = ?) and status = ?) ";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                    while (result.next()) {
                        cardList.add(new StringBuffer(result.getString("CARDNO")));
                    }
                    return cardList;
                }, Configurations.NO_STATUS, Configurations.TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE, Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS, Configurations.EOD_PENDING_STATUS);

            }
        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public boolean getTriggerEligibleStatus(String triggerPoint, String smsOrEmail) throws Exception {
        boolean status = false;
        try {
            String sql = "SELECT B.BUCKETID "
                    + "FROM ALLOCATIONRULEACTION ARA INNER JOIN BUCKETACTION B "
                    + "ON ARA.RULECODE=B.ACTIONRULE "
                    + "WHERE ARA.ACTIONMESSAGECODE "
                    + "IN(SELECT MESSAGECODE FROM ALLOCATIONMESSAGE "
                    + "WHERE TRIGGERPOINT=? "
                    + "AND MESSAGETYPE =?) ";

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(sql, countCallback, triggerPoint, smsOrEmail);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                status = true;
            }

        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    public int updateTriggerCards(StringBuffer cardNumber) throws Exception {
        int count = 0;
        try {
            String sql = "UPDATE TRIGGERCARDS SET LASTUPDATEDTIME = TO_DATE(SYSDATE, 'DD-MM-YY')  ,LASTUPDATEDUSER = ?,LETTERGENSTATUS =? WHERE CARDNO = ?";
            count = backendJdbcTemplate.update(sql, Configurations.EOD_USER, Configurations.YES_STATUS, cardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNumber) throws Exception {
        String accNo = null;
        try {
            String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";
            accNo = backendJdbcTemplate.queryForObject(query, String.class, cardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
        return accNo;
    }

    @Override
    public int insertIntoDelinquentHistory(StringBuffer cardNumber, String accountNo, String remark) throws Exception {
        int count = 0;
        try {
            String sql = "INSERT INTO DELINQUENTHISTORY (CARDNUMBER,  ACCOUNTNO,REMARK,  LASTUPDATEDUSER,"
                    + "   LASTUPDATEDTIME,   CREATEDTIME ) VALUES (?,?,?,?,TO_DATE(SYSDATE, 'DD-MM-YY') ,TO_DATE(SYSDATE, 'DD-MM-YY'))";

            count = backendJdbcTemplate.update(sql, cardNumber.toString(), accountNo, remark, Configurations.EOD_USER);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
