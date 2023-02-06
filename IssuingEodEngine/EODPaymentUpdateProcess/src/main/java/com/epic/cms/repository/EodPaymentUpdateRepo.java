package com.epic.cms.repository;

import com.epic.cms.dao.EodPaymentUpdateDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
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
public class EodPaymentUpdateRepo implements EodPaymentUpdateDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Override
    public int[] callStoredProcedureForEodPaymentUpdate() throws SQLException {
        int output;

        int txnCounts[] = new int[4];
        int failCount = -1;
        int totalCount = -1;
        int isProcessError = -1;

        try {
            output = -1;

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("PAYMENTTXNUPDATEPROC");
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
            Map<String, Object> out = simpleJdbcCall.execute(in);

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
        } catch (SQLException e){
            errorLogger.error(String.valueOf(e));
            throw e;
        } catch (Exception ex) {
            errorLogger.error("Call StoredProcedure For EodPaymentUpdate Error", ex);
            throw ex;
        }
    }
}
