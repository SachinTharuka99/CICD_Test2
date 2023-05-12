package com.epic.cms.dao;

import java.sql.Timestamp;
import java.util.Date;

public interface CreateEodIdDao {
    boolean isStatusComp() throws Exception;

    String getEodIdByLastCompletedEODID() throws Exception;

    boolean isHoliday(Date toDate) throws Exception;

    boolean insertValuesToEODTable(String EodId, Timestamp nextMainEODSchDate, String mainEODStatus, String fileGenStatus, String fileProStatus) throws Exception;

    boolean isNextEodIdExistsInEodRunningParameterTable(String eodId) throws Exception;

    void updateNextEodRunningParameterTable(String eodId) throws Exception;

    void insertValuesToEodRunningParameterTable(String eodId) throws Exception;

    void updateEodEndStatus(int errorEodId, String status) throws Exception;

    String getEodStatusByEodID(int errorEodId) throws Exception;

    int getCurrentEodId(String status, String errorStatus) throws Exception;
}
