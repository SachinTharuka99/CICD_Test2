package com.epic.cms.repository;

import com.epic.cms.dao.CardPermanentBlockDao;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CardPermanentBlockRepo implements CardPermanentBlockDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList query;

    @Autowired
    StatusVarList statusList;

}
