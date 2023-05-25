package com.epic.cms.repository;

import com.epic.cms.dao.ClearMinAmountAndTempBlockDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class ClearMinAmountAndTempBlockRepo implements ClearMinAmountAndTempBlockDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Override
    public ArrayList<StringBuffer[]> getAllCards(StringBuffer cardNo) throws Exception {
        ArrayList<StringBuffer[]> cardList = new ArrayList<>();

        try {
            String sql = "SELECT C.CARDNUMBER, C.CARDCATEGORYCODE FROM CARD C WHERE C.MAINCARDNO = ? AND C.CARDSTATUS NOT IN(?,?,?)";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            StringBuffer[] card = new StringBuffer[2];
                            card[0] = new StringBuffer(rs.getString("CARDNUMBER"));
                            card[1] = new StringBuffer(rs.getString("CARDCATEGORYCODE"));
                            cardList.add(card);
                        }
                        return cardList;

                    }, cardNo.toString()
                    , statusList.getCARD_CLOSED_STATUS()
                    , statusList.getCARD_REPLACED_STATUS()
                    , statusList.getCARD_PRODUCT_CHANGE_STATUS()
            );
        } catch (Exception e) {
            logManager.logError("Get All Cards Error", errorLogger);
            throw e;
        }
        return cardList;
    }

    @Override
    public void removeFromMinPayTable(StringBuffer cardNo, double payment) throws Exception {
        Boolean isAmountFullyPaid = true;

        try {
            String allMinPayments = "select * from minimumpayment where cardno=? and count>0";
            String insertToBackuptable = "insert into backupminimumpayment (select m.* from minimumpayment m where cardno=? and count>0)";
            String removeFromTriggerCard = "delete from triggercards where cardno=?";

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(allMinPayments, countCallback, cardNo);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                String monthCount = "";
                int month = 12;
                int updated = 0;

                //Update the minpaytable with the counts and cards.
                if (isAmountFullyPaid) {
                    //First backup the current table to the backup_minpayment table. This is for payment reversals.
                    updated = backendJdbcTemplate.update(insertToBackuptable, cardNo.toString());
                    if (updated == 1) {
                        //Find the M columns for the values to get updated and set it to 0.
                        while (month >= 1) {
                            monthCount = monthCount + "M" + month + "=0,M" + month + "DATE=null,";
                            month--;
                        }

                        String updateQuery = "UPDATE MINIMUMPAYMENT SET " + monthCount + " STATUS=? ,COUNT =?, LASTUPDATEDTIME=sysdate WHERE CARDNO=?";
                        updated = backendJdbcTemplate.update(updateQuery,
                                Configurations.EOD_PENDING_STATUS,
                                0,
                                cardNo.toString());

                        //remove from trigger cards.
                        if (updated == 1) {
                            backendJdbcTemplate.update(removeFromTriggerCard, cardNo.toString());
                        } else {
                            try {
                                throw new Exception("Clearing minimumpayment table failed");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        try {
                            throw new Exception("Insertion to backup min table failed");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logManager.logError("Remove From MinPay Table Error", errorLogger);
            throw e;
        }
    }

    @Override
    public int updateCardBlock(StringBuffer cardNo, String oldStatus, String newStatus) throws Exception {
        int count = 0;
        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

        String sql = "UPDATE CARDBLOCK SET OLDSTATUS=? ,NEWSTATUS=?,LASTEODUPDATEDDATE=? WHERE STATUS=? AND CARDNUMBER=?";
        try {
            count = backendJdbcTemplate.update(sql, oldStatus, newStatus, eodDate, statusList.getACTIVE_STATUS(), cardNo.toString());

        } catch (Exception e) {
            logManager.logError("Update Card Block Error", errorLogger);
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<Object> getMinimumPaymentExistStatementDate(StringBuffer cardNo, int monthNo) throws Exception {
        ArrayList<Object> lastStmtDetails = new ArrayList<>();

        String sql = "SELECT BS.STATEMENTENDDATE, BS.TOTALMINPAYMENT, BS.DUEDATE FROM MINIMUMPAYMENT MP INNER JOIN BILLINGSTATEMENT BS ON MP.CARDNO = BS.MAINCARDNO WHERE BS.MAINCARDNO = ? AND TRUNC(MP.M" + monthNo + "DATE) = TRUNC(BS.DUEDATE)";
        try {
            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            lastStmtDetails.add(rs.getDate("STATEMENTENDDATE"));
                            lastStmtDetails.add(rs.getDouble("TOTALMINPAYMENT"));
                            lastStmtDetails.add(rs.getDate("DUEDATE"));
                        }
                        return lastStmtDetails;
                    }, cardNo.toString()
            );
        } catch (Exception e) {
            logManager.logError("Get Minimum Payment Exist Statement Date Error", errorLogger);
            throw e;
        }
        return lastStmtDetails;
    }
}
