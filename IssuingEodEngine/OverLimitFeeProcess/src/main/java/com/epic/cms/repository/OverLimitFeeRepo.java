package com.epic.cms.repository;

import com.epic.cms.dao.OverLimitFeeDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

@Repository
public class OverLimitFeeRepo implements OverLimitFeeDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Override
    @Transactional("backendDb")
    public HashMap<String, StringBuffer> getOverLimitAcc() throws Exception {
        HashMap<String, StringBuffer> accountMap = new HashMap<>();


        try {
           // String query = "select CA.ACCOUNTNO,CA.OTBCREDIT,CA.OPENINGOTBCREDIT from CARDACCOUNT CA where CA.OPENINGOTBCREDIT >=0 and CA.OTBCREDIT < 0 and CA.ACCOUNTNO not in (select accountno from eodcardfee where accountno = CA.ACCOUNTNO and feetype = ? and eodid like ?)";
            String query = queryParametersList.getOverLimitFee_getOverLimitAcc();

            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                query += " and CA.ACCOUNTNO not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status='" + statusList.getEOD_PENDING_STATUS() + "')";
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query += " and CA.ACCOUNTNO in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status='" + statusList.getEOD_PENDING_STATUS() + "' and EODID < " + Configurations.ERROR_EOD_ID + " and PROCESSSTEPID <=" + Configurations.PROCESS_STEP_ID + ")";
            }

            String temp = String.valueOf(Configurations.EOD_ID).substring(0, 6).concat("%");

            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        while (result.next()) {
                            String accountNo = result.getString("ACCOUNTNO");

                            ArrayList<java.io.Serializable> MainCardNumberAndBal;
                            MainCardNumberAndBal = getMainCardOpeningBalance(accountNo);
                            accountMap.put(accountNo, new StringBuffer(MainCardNumberAndBal.get(1).toString()));
                        }
                        return accountMap;
                    }
                    , Configurations.OVER_LIMIT_FEE, temp
            );

        } catch (Exception e) {
            throw e;
        }

        return accountMap;
    }
    @Transactional("backendDb")
    ArrayList<java.io.Serializable> getMainCardOpeningBalance(String accountNo) {
        ArrayList<java.io.Serializable> cardNumberAndOpeningBal = new ArrayList<>();
        try {

        //String sql = "SELECT ECB.EODSTARTINGBAL, ECB.CARDNUMBER FROM EODCARDBALANCE ECB, CARDACCOUNTCUSTOMER CAC WHERE ECB.CARDNUMBER = CAC.CARDNUMBER AND CAC.ISPRIMARY ='YES' AND CAC.ACCOUNTNO = ? AND ECB.CARDNUMBER NOT IN (select OLDCARDNUMBER from cardreplace)";
        backendJdbcTemplate.query(queryParametersList.getOverLimitFee_getMainCardOpeningBalance(),
                (ResultSet rs) -> {
                    while (rs.next()) {
                        cardNumberAndOpeningBal.add(0, rs.getDouble("EODSTARTINGBAL"));
                        cardNumberAndOpeningBal.add(1, rs.getString("CARDNUMBER"));
                    }
                    return cardNumberAndOpeningBal;
                },
                accountNo
                );
        }catch (Exception e){
            throw e;
        }
        return cardNumberAndOpeningBal;
    }
}
