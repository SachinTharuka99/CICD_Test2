package com.ecms.web.api.tokenservice.repository.impl;

import com.ecms.web.api.tokenservice.repository.CommonRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Repository
public class CommonRepositoryImpl implements CommonRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final String SQL_SYSTEM_TIME = "SELECT TO_CHAR(SYSDATE,'YYYY-MM-DD HH24:MI:SS') AS NOW FROM DUAL";

    @Override
    public Date getSysDate() throws ParseException {
        Date sysDateTime = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Query query = entityManager.createNativeQuery(SQL_SYSTEM_TIME);
        String stime = (String) query.getSingleResult();
        if (stime != null && !stime.isEmpty()) {
            sysDateTime = dateFormat.parse(stime);
        } else {
            sysDateTime = new Date();
        }
        return sysDateTime;
    }

}
