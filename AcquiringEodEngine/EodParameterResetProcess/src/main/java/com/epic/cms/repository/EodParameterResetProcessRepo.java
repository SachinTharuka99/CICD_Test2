package com.epic.cms.repository;

import com.epic.cms.dao.EodParameterResetProcessDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EodParameterResetProcessRepo implements EodParameterResetProcessDao {

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Override
    public int resetTerminalParameters() throws Exception {
        int count = 0;
        try {
            count = onlineJdbcTemplate.update(queryParametersList.getEodParamResetUpdateResetTerminalParameters(), 0, 0, 0, 0, Configurations.EOD_USER);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int resetMerchantParameters() throws Exception {
        int count = 0;
        try {
            count = onlineJdbcTemplate.update(queryParametersList.getEodParamResetUpdateResetMerchantParameters(), 0, 0, 0, 0, Configurations.EOD_USER);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
