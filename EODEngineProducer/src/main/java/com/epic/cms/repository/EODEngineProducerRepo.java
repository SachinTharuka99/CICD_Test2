/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:38 PM
 * Project Name : eod-engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.EODEngineProducerDao;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class EODEngineProducerRepo implements EODEngineProducerDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public List<String> getEODStatusFromEODID(String eodID) {
        List<String> eodIdList = new ArrayList<String>();
        try {
            String query = "SELECT STATUS FROM EOD WHERE EODID = ?";
            eodIdList = backendJdbcTemplate.queryForList(query, String.class, eodID);
        } catch (Exception ex) {
            throw ex;
        }
        return eodIdList;
    }

    @Override
    public boolean checkUploadedFileStatus() {
        boolean flag = false;
        try {
            String query = "select fileid from EODATMFILE where status not in (?, ?) " +
                    "union " +
                    "select fileid from EODDCFFILE where status not in (?, ?) " +
                    "union " +
                    "select fileid from EODMASTERFILE where status not in (?, ?) " +
                    "union " +
                    "select fileid from EODPAYMENTFILE where status not in (?, ?) " +
                    "union " +
                    "select fileid from EODVISAFILE where status not in (?, ?)";

            List<String> fileIdList = backendJdbcTemplate.queryForList(query, String.class, Configurations.COMPLETE_STATUS, Configurations.FAIL_STATUS,
                    Configurations.COMPLETE_STATUS, Configurations.FAIL_STATUS,
                    Configurations.STATUS_FILE_COMP, Configurations.STATUS_FILE_REJECT,
                    Configurations.COMPLETE_STATUS, Configurations.FAIL_STATUS,
                    Configurations.STATUS_FILE_COMP, Configurations.STATUS_FILE_REJECT);
            Iterator<String> fileIdListIterator = fileIdList.iterator();
            if (fileIdListIterator.hasNext()) {
                flag = true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return flag;
    }

    @Override
    public List<ProcessBean> getProcessListByCategoryId(int categoryId) {
        List<ProcessBean> processList = new ArrayList<ProcessBean>();
        try {
            String query = "SELECT EP.PROCESSID,EP.DESCRIPTION,EP.CRITICALSTATUS,EP.ROLLBACKSTATUS,"
                    + "EP.SHEDULEDATE,EP.SHEDULETIME,EP.FREQUENCYTYPE,EP.CONTINUESFREQUENCYTYPE,EP.CONTINUESFREQUENCY,"
                    + "EP.MULTIPLECYCLESTATUS,EF.PROCESSCATEGORYID,EP.DEPENDANCYSTATUS,EP.RUNNINGONMAIN,EP.RUNNINGONSUB,"
                    + "EP.PROCESSTYPE,EP.STATUS,EP.SHEDULEDATETIME, EP.HOLIDAYACTION, EP.KAFKATOPICNAME, EP.KAFKAGROUPID"
                    + " FROM EODPROCESSFLOW EF, EODPROCESS EP WHERE"
                    + " EF.PROCESSCATEGORYID = ? AND EF.PROCESSID=EP.PROCESSID";

            processList = backendJdbcTemplate.query(query, new ProcessBeanRowMapper(), categoryId);
        } catch (Exception ex) {
            throw ex;
        }
        return processList;
    }

    @Override
    public void insertToEODProcessCount(String uniqueId, int size, String includedProcess) {
        try {
            String sql = "INSERT INTO EODPROCESSCOUNT (THREADID,PROCESSCOUNT,COMPLETEDCOUNT,INCLUDED_PROCESS)" +
                    " VALUES (?,?,?,?)";

            backendJdbcTemplate.update(sql, uniqueId, size, 0, includedProcess);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int getCompletedProcessCount(String uniqueId) throws SQLException {
        String sql = "SELECT COMPLETEDCOUNT FROM EODPROCESSCOUNT WHERE THREADID = ?";
        return backendJdbcTemplate.queryForObject(sql, Integer.class, uniqueId);
    }

    @Override
    public void clearEodProcessCountTable() throws Exception {
        try {
            String sql = "DELETE FROM EODPROCESSCOUNT";

            backendJdbcTemplate.update(sql);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
