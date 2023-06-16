package com.epic.cms.repository;

import com.epic.cms.dao.CRIBFileDao;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class CRIBFileRepo implements CRIBFileDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;


    @Override
    public int[] callStoredProcedureCribFileGeneration() throws Exception {
        String query;
        int output;

        int cardCount[] = new int[4];
        int mainCardCount = -1;
        int supCardCount = -1;
        int corpCardCount = -1;
        int fdCardCount = -1;
        try {
            output = -1;

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("CRIBFILEGENERATION");

            Map<String, Object> out =  simpleJdbcCall.execute();

            BigDecimal out1 = (BigDecimal) out.get("OUTPUTDATA");
            BigDecimal out2 = (BigDecimal) out.get("NUMBEROFMAINCARDCUS");
            BigDecimal out3 = (BigDecimal) out.get("NUMBEROFSUPCARDCUS");
            BigDecimal out4 = (BigDecimal) out.get("NUMBEROFCORPCARDCUS");
            BigDecimal out5 = (BigDecimal) out.get("NUMBEROFFDCARDCUS");

            output = out1.intValue();
            mainCardCount = out2.intValue();
            supCardCount = out3.intValue();
            corpCardCount = out4.intValue();
            fdCardCount = out5.intValue();

            cardCount[0] = mainCardCount;
            cardCount[1] = supCardCount;
            cardCount[2] = corpCardCount;
            cardCount[3] = fdCardCount;

            switch (output) {
                case 1:
                    return cardCount;
                default:
                    throw new SQLException();
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
