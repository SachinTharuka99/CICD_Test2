/**
 * Author : lahiru_p
 * Date : 4/10/2023
 * Time : 1:14 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CreateEodIdDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;


@Repository
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class CreateEodIdRepo implements CreateEodIdDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Override
    public boolean isStatusComp() throws Exception {
        boolean status = true;

        String query = "SELECT E.STATUS FROM EOD E WHERE E.STATUS  NOT IN (?,?)";
        try {
            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(query, countCallback, statusList.getSUCCES_STATUS(), statusList.getERROR_INPR_STATUS());
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                status = false;
            }

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    public String getEodIdByLastCompletedEODID() throws Exception {
        String EodId = "";
        String query = "select max(e.eodid) as eodid from eod e where e.status=? ";
        try {
            EodId = backendJdbcTemplate.queryForObject(query, String.class, statusList.getSUCCES_STATUS());
        } catch (Exception e) {
            logError.error(String.valueOf(e));
        }
        return EodId;
    }

    @Override
    public boolean isHoliday(Date today) throws Exception {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM HOLIDAY WHERE YEAR = ? AND MONTH=? AND DAY=?";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, String.valueOf(today.getYear() + 1900), String.valueOf(today.getMonth() + 1), String.valueOf(today.getDate()));

            if (count > 0) {
                return true;
            } else {
                return false;
            }
        } catch (EmptyResultDataAccessException ex) {
            return false;
        } catch (Exception e) {
            logError.error(String.valueOf(e));
            return false;
        }
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public boolean insertValuesToEODTable(String EodId, Timestamp nextMainEODSchDate, String mainEODStatus, String fileGenStatus, String fileProStatus) throws Exception {
        boolean status = false;
        String query = null;
        int count = 0;
        try {
            query = "INSERT INTO EOD (EODID,STATUS,NEXTEODSTARTTIME,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,NOOFSUCCESSPROCESS,NOOFERRORPAROCESS,FILEGENSTATUS) VALUES (?,?,?,SYSDATE,SYSDATE,?,0,0,?)";

            count = backendJdbcTemplate.update(query, EodId, mainEODStatus, nextMainEODSchDate, Configurations.EOD_USER, fileGenStatus);

            if (count == 1) {
                status = true;
            }

        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    public boolean isNextEodIdExistsInEodRunningParameterTable(String eodId) throws Exception {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM EODMGTDASHBOARDPARAMETERS WHERE EODID=?";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, eodId);
        }catch (EmptyResultDataAccessException e){
            return false;
        }catch (Exception ex){
            logError.error(String.valueOf(ex));
        }
        return false;
    }

    @Override
    public void updateNextEodRunningParameterTable(String eodId) throws Exception {

    }

    @Override
    public void insertValuesToEodRunningParameterTable(String eodId) throws Exception {

    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodEndStatus(int errorEodId, String status) throws Exception {
        String Query = "UPDATE EOD SET STATUS =?,ENDTIME =SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";
        try {
            backendJdbcTemplate.update(Query, status, Configurations.EOD_USER, errorEodId);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public String getEodStatusByEodID(int errorEodId) throws Exception {
        String EodStatus = null;
        String query = null;
        try {
            query = "Select STATUS FROM EOD WHERE EODID = ?";
            EodStatus = backendJdbcTemplate.queryForObject(query, String.class, errorEodId);
        }catch (Exception e){
            logError.error(String.valueOf(e));
        }
        return EodStatus;
    }

    @Override
    public int getCurrentEodId(String status, String errorStatus) throws Exception {
        int eodId = 0;

        String query = "SELECT EODID FROM EOD WHERE STATUS = ? OR STATUS = ?";

        try {
            eodId = backendJdbcTemplate.queryForObject(query, Integer.class, status, errorStatus);

        }catch (EmptyResultDataAccessException ex){
            return 0;
        }catch (Exception e){
            logError.error(String.valueOf(e));
        }
        return eodId;
    }
}
