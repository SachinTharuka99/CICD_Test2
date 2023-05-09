package com.epic.cms.repository;

import com.epic.cms.dao.AtmCashAdvanceUpdateDao;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import static com.epic.cms.util.LogManager.errorLogger;
@Repository
public class AtmCashAdvanceUpdateRepo implements AtmCashAdvanceUpdateDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    LogManager logManager;

    @Override
    public int[] callStoredProcedureForCashAdvUpdate() throws SQLException {
        int output;

        int txnCounts[] = new int[4];
        int failCount = -1;
        int totalCount = -1;
        int isProcessError = -1;

        try {
            output = -1;
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("CASHADVUPDATEPROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EODID", Configurations.EOD_ID)
                    .addValue("ERROREODID", Configurations.ERROR_EOD_ID)
                    .addValue("EODDATE", DateUtil.getSqldate(Configurations.EOD_DATE))
                    .addValue("EODUSER", Configurations.EOD_USER)
                    .addValue("STARTINGEODSTATUS", Configurations.STARTING_EOD_STATUS)
                    .addValue("OUTPUTDATA", output)
                    .addValue("NUMBEROFTXN", totalCount)
                    .addValue("FAILTXNCOUNT", failCount)
                    .addValue("ISPROCESSERROR", isProcessError);
            Map<String, Object> out =  simpleJdbcCall.execute(in);

            BigDecimal out1 = (BigDecimal) out.get("OUTPUTDATA");
            BigDecimal out2 = (BigDecimal) out.get("NUMBEROFTXN");
            BigDecimal out3 = (BigDecimal) out.get("FAILTXNCOUNT");
            BigDecimal out4 = (BigDecimal) out.get("ISPROCESSERROR");

            output = out1.intValue();
            totalCount = out2.intValue();
            failCount = out3.intValue();
            isProcessError = out4.intValue();//0:process success, 1: process error
            Configurations.IS_PROCESS_ERROR = ((isProcessError == 0) ? false : true);

            txnCounts[0] = (totalCount - failCount);
            txnCounts[1] = failCount;
            txnCounts[2] = totalCount;

            switch (output) {
                case 1:
                    return txnCounts;
                case -1:
                    throw new SQLException();
                default:
                    throw new SQLException();
            }

        }catch (SQLException e){
            throw e;
        } catch (Exception ex) {
            logManager.logError("Call StoredProcedure For CashAdvUpdate Error", errorLogger);
            throw ex;
        }
    }
}
