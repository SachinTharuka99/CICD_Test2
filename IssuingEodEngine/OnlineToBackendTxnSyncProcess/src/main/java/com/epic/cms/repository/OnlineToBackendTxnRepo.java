package com.epic.cms.repository;

import com.epic.cms.dao.OnlineToBackendTxnDao;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class OnlineToBackendTxnRepo implements OnlineToBackendTxnDao {
    @Autowired
    JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Override
    public int[] callStoredProcedureForTxnSync() throws SQLException {

        int output;
        int txnCounts[] = new int[4];
        int successCount = -1;
        int failCount = -1;
        int totalCount = -1;
        try {
            output = -1;
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate).withProcedureName("TRANSACTIONSYNCPROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("OUTPUTDATA", output)
                    .addValue("SUCCESSTXNCOUNT", successCount)
                    .addValue("FAILTXNCOUNT", failCount)
                    .addValue("TOTALTXNCOUNT", totalCount);

            Map<String, Object> out = simpleJdbcCall.execute(in);

            BigDecimal out1 = (BigDecimal)  out.get("OUTPUTDATA");
            BigDecimal out2 = (BigDecimal)  out.get("SUCCESSTXNCOUNT");
            BigDecimal out3 = (BigDecimal)  out.get("TOTALTXNCOUNT");
            BigDecimal out4 = (BigDecimal)  out.get("FAILTXNCOUNT");

            output = out1.intValue();
            successCount = out2.intValue();
            totalCount = out3.intValue();
            failCount = out4.intValue();

//            BigDecimal out1 = (BigDecimal) out.get("OUTPUTDATA");
//            BigDecimal out2 = (BigDecimal) out.get("SUCCESSTXNCOUNT");
//            BigDecimal out3 = (BigDecimal) out.get("TOTALTXNCOUNT");
//            BigDecimal out4 = (BigDecimal) out.get("FAILTXNCOUNT");
//
//            output = out1.intValue();
//            successCount = out2.intValue();
//            totalCount = out3.intValue();
//            failCount = out4.intValue();

            txnCounts[0] = successCount;
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
        }catch (Exception e){
            logManager.logError("Exception in Call Stored Procedure for Txn Sync " , errorLogger);
            throw e;
        }
    }
}
