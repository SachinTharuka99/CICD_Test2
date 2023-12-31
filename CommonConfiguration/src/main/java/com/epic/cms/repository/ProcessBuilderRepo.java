/**
 * Author : rasintha_j
 * Date : 3/16/2023
 * Time : 9:30 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.dao.ProcessBuilderDao;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class ProcessBuilderRepo implements ProcessBuilderDao {

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ProcessBean getProcessDetails(int processId) throws Exception {
        ProcessBean processDetails = new ProcessBean();
        try {
            processDetails = backendJdbcTemplate.queryForObject(queryParametersList.getCommonSelectGetProcessDetails(), new ProcessBeanRowMapper(), processId);
        }catch (EmptyResultDataAccessException ex){
            return null;
        } catch (Exception e) {
            throw e;
        }
        return processDetails;
    }

    @Override
    public boolean isErrorProcess(int processId) {
        boolean isErrorProcess = false;
        int count = 0;
        try {

            String query = "SELECT COUNT(T.STEPID)AS COUNT FROM (SELECT EPF.STEPID,EPF.PROCESSID,EEC.CARDNO,EEC.STATUS FROM EODPROCESSFLOW EPF LEFT JOIN EODERRORCARDS EEC ON EPF.PROCESSID = EEC.ERRORPROCESSID ORDER BY STEPID)T WHERE T.STEPID <= (SELECT max(STEPID) as STEPID FROM EODPROCESSFLOW WHERE PROCESSID = ?) AND T.STATUS = ?";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, processId, Configurations.EOD_PENDING_STATUS);
            if (count > 0) {
                isErrorProcess = true;
            }

        } catch (Exception e) {
            throw e;
        }

        return isErrorProcess;
    }

    @Override
    public int getRunningEODId(String inprogress_status, String error_inpr_status) {
        int eodId = 0;
        String query ="select max(eodid) from eod where status in (?, ?) order by eodid desc";
        try {
            eodId = backendJdbcTemplate.queryForObject(query, Integer.class, inprogress_status, error_inpr_status);

        }catch (EmptyResultDataAccessException ex){
            return 0;
        }catch (Exception e){
            throw e;
        }
        return eodId;
    }
}
