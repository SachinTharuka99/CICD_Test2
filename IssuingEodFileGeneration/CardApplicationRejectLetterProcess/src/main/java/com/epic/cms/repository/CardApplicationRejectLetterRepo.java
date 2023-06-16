/**
 * Author : yasiru_l
 * Date : 12/5/2022
 * Time : 11:37 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CardApplicationRejectLetterDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;


@Repository
public class CardApplicationRejectLetterRepo implements CardApplicationRejectLetterDao {

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<String> getRejectApplictionIDsToGenerateLetters(String StartEodStatus, boolean isErrorProcessInLastEod, boolean isProcessCompletlyFail) throws Exception {

        ArrayList<String> cardNoList = new ArrayList<String>();
        String query = "";
        try {

            if (StartEodStatus.equals(statusVarList.getERROR_STATUS()) && isErrorProcessInLastEod) {
                query = "SELECT C.APPLICATIONID FROM (SELECT EPF.STEPID,EPF.PROCESSID,EEC.CARDNO,EEC.STATUS FROM EODPROCESSFLOW EPF FULL OUTER JOIN EODERRORCARDS EEC ON EPF.PROCESSID = EEC.ERRORPROCESSID ORDER BY STEPID)T JOIN cardapplication c ON t.cardno = c.cardnumber WHERE T.STEPID <= (SELECT STEPID FROM EODPROCESSFLOW WHERE PROCESSID = ?) AND T.STATUS = ? AND T.CARDNO NOT IN (SELECT CARDNO FROM EODERRORCARDS WHERE EODID =? AND STATUS = ?)";
                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                cardNoList.add(rs.getString("APPLICATIONID"));
                            }
                            return cardNoList;
                        },
                        Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT,
                        statusVarList.getEOD_PENDING_STATUS(),
                        Configurations.ERROR_EOD_ID,
                        statusVarList.getEOD_PENDING_STATUS()
                );

            } else {
                query = "SELECT CA.APPLICATIONID FROM CARDAPPLICATION CA WHERE CA.LETTERGENSTATUS <> ? AND CA.APPLICATIONID NOT IN (SELECT CARDNO FROM EODERRORCARDS WHERE EODID < ? AND STATUS = ?)";
                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                cardNoList.add(rs.getString("APPLICATIONID"));
                            }
                            return cardNoList;
                        },
                        "NO",
                        Configurations.ERROR_EOD_ID,
                        statusVarList.getEOD_PENDING_STATUS()
                );
            }

        } catch (Exception e) {
            throw e;
        }
        return cardNoList;
    }

    @Override
    public int updateLettergenStatus(StringBuffer cardNo, String yes) throws Exception {

        int count = 0;
        try {

            String updatePay = "update cardapplication set lettergenstatus=? where cardnumber=?";//TODO NEED SOME MORE PARAMETERS.
            count = backendJdbcTemplate.update(updatePay,
                    yes,
                    cardNo.toString()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public StringBuffer getCardNo(String applicationI) throws Exception {

        StringBuffer cardNo = null;
        try {
            String query = "SELECT ca.maincardno FROM card ca LEFT JOIN cardaccount acc ON ca.maincardno = acc.cardnumber INNER JOIN cardapplication cap ON ca.maincardno = cap.cardnumber WHERE cap.applicationid =?";
            cardNo = backendJdbcTemplate.queryForObject(query,
                    StringBuffer.class, applicationI
                    );
        }catch (Exception e){
            throw e;
        }
        return cardNo;
    }
}
