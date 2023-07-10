package com.epic.cms.repository;

import com.epic.cms.dao.SnapShotDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;


@Repository
public class SnapShotRepo implements SnapShotDao {

    @Autowired
    StatusVarList statusList;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;
    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

//    @Autowired
//    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public int checkEodComplete() throws Exception {
        int count = 0;
        try {
           // String query = "select count(*) as COUNT from eodprocesssummery where status  in (?) and eodid=?";

            count = backendJdbcTemplate.queryForObject(queryParametersList.getSnapShot_checkEodComplete(), Integer.class, statusList.getERROR_STATUS(), Configurations.ERROR_EOD_ID);

        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void updateSnapShotTableOfCards() throws Exception {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("DAILY_SNAPSHOT_OF_CARD");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EODID", Configurations.EOD_ID);

            simpleJdbcCall.execute(in);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateSnapShotTableOfAccounts() throws Exception {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("DAILY_SNAPSHOT_OF_ACCOUNT");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EODID", Configurations.EOD_ID);

            simpleJdbcCall.execute(in);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineSnapShotTableOfCards() throws Exception {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(onlineJdbcTemplate)
                    .withProcedureName("DAILY_SNAPSHOT_ONLINE_CARD");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EODID", Configurations.EOD_ID);

            simpleJdbcCall.execute(in);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineSnapShotTableOfAccounts() throws Exception {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(onlineJdbcTemplate)
                    .withProcedureName("DAILY_SNAPSHOT_ONLINE_ACC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EODID", Configurations.EOD_ID);

            simpleJdbcCall.execute(in);

        } catch (Exception e) {
            throw e;
        }
    }
}
