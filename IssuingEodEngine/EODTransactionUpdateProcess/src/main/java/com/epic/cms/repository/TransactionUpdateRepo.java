package com.epic.cms.repository;

import com.epic.cms.dao.TransactionUpdateDao;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;


@Repository
public class TransactionUpdateRepo implements TransactionUpdateDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList status;


    @Override
    public int[] callStoredProcedureForVisaTxnUpdate() throws SQLException {
        int output;
        int txnCounts[] = new int[4];
        int failCount = -1;
        int totalCount = -1;
        int isProcessError = -1;
        try {
            output = -1;
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("VISATXNUPDATEPROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EOD_ID", Configurations.EOD_ID)
                    .addValue("ERROREODID", Configurations.ERROR_EOD_ID)
                    .addValue("EODDATE", CommonMethods.getSqldate(Configurations.EOD_DATE))
                    .addValue("EODUSER", Configurations.EOD_USER)
                    .addValue("STARTINGEODSTATUS", Configurations.STARTING_EOD_STATUS);
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
            throw e;
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int[] callStoredProcedureForMasterTxnUpdate() throws SQLException {
        int output;
        int txnCounts[] = new int[4];
        int failCount = -1;
        int totalCount = -1;
        int isProcessError = -1;
        try {
            output = -1;
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("MASTERTXNUPDATEPROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EOD_ID", Configurations.EOD_ID)
                    .addValue("ERROREODID", Configurations.ERROR_EOD_ID)
                    .addValue("EODDATE", CommonMethods.getSqldate(Configurations.EOD_DATE))
                    .addValue("EODUSER", Configurations.EOD_USER)
                    .addValue("STARTINGEODSTATUS", Configurations.STARTING_EOD_STATUS);
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
            throw e;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
