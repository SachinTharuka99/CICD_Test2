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
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class EODEngineProducerRepo implements EODEngineProducerDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

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
                    + "EP.PROCESSTYPE,EP.STATUS,EP.SHEDULEDATETIME, EP.HOLIDAYACTION, EP.KAFKATOPICNAME, EP.KAFKAGROUPID, EP.EODMODULE"
                    + " FROM EODPROCESSFLOW EF, EODPROCESS EP WHERE"
                    + " EF.PROCESSCATEGORYID = ? AND EF.PROCESSID=EP.PROCESSID ORDER BY STEPID ASC";

            processList = backendJdbcTemplate.query(query, new ProcessBeanRowMapper(), categoryId);
        } catch (Exception ex) {
            throw ex;
        }
        return processList;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
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
        int count = 0;
        String sql = "SELECT COMPLETEDCOUNT FROM EODPROCESSCOUNT WHERE THREADID = ?";

        try {
            count = backendJdbcTemplate.queryForObject(sql, Integer.class, uniqueId);
        }catch (EmptyResultDataAccessException e){
            return  0;
        }catch (Exception ex){
            throw ex;
        }
        return count;
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

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updatePreviousEODErrorCardDetails(String prevEODID) throws Exception {
        int result = 0;

        int eodID = Integer.parseInt(prevEODID);
        try {
            String query = "update EODERRORCARDS set STATUS = ? where EODID < ?";

            result = backendJdbcTemplate.update(query, statusList.getEOD_DONE_STATUS(), eodID);

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updatePreviousEODErrorMerchantDetails(String prevEODID) throws Exception {
        int result = 0;

        int eodID = Integer.parseInt(prevEODID);
        try {
            String query = "update EODERRORMERCHANT set STATUS = ? where EODID < ?";

            result = backendJdbcTemplate.update(query, statusList.getEOD_DONE_STATUS(), eodID);

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updateEodProcessProgress() throws Exception {
        int count = 0;
        String Query = "UPDATE EODPROCESSSUMMERY SET SUCCESSCOUNT = ? , FAILEDCOUNT = ? ,PROCESSPROGRESS = ? ,LASTUPDATEDUSER = ?,LASTUPDATEDTIME=SYSDATE WHERE EODID = ? AND PROCESSID = ?";

        try {
            count = backendJdbcTemplate.update(Query,
                    Configurations.PROCESS_SUCCESS_COUNT,
                    Configurations.PROCESS_FAILD_COUNT,
                    Configurations.PROCESS_PROGRESS,
                    Configurations.EOD_USER,
                    Configurations.ERROR_EOD_ID,
                    Configurations.RUNNING_PROCESS_ID);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public List<String> getErrorProcessIdList() throws Exception {
        List<String> processIdList = new ArrayList<String>();
        try {
            String query = "SELECT PROCESSID FROM EODPROCESSSUMMERY WHERE STATUS='EROR' AND SUCCESSCOUNT=0 AND FAILEDCOUNT=0 AND EODID=?";

            processIdList = backendJdbcTemplate.queryForList(query, String.class, Configurations.ERROR_EOD_ID);
        } catch (Exception e) {
            throw e;
        }
        return processIdList;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateProcessProgressForErrorProcess(String processId) throws Exception {
        try {
            String query = "UPDATE EODPROCESSSUMMERY SET PROCESSPROGRESS='0%' WHERE EODID=? AND PROCESSID=? ";
            backendJdbcTemplate.update(query, Configurations.ERROR_EOD_ID, processId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updateEodProcessStateCount() throws Exception {
        int count = 0;
        String query = "UPDATE EOD SET NOOFSUCCESSPROCESS=(SELECT COUNT(*) FROM EODPROCESSSUMMERY WHERE EODID=? AND STATUS=?),"
                + "NOOFERRORPAROCESS=(SELECT COUNT(*) FROM EODPROCESSSUMMERY WHERE EODID=? AND STATUS=?),"
                + "LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE EODID=?";
        try {
            count = backendJdbcTemplate.update(query, Configurations.ERROR_EOD_ID,
                    Configurations.COMPLETE_STATUS,
                    Configurations.ERROR_EOD_ID,
                    "EROR",
                    Configurations.EOD_USER,
                    Configurations.ERROR_EOD_ID);
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodStatus(int errorEodId, String status) throws Exception {
        try {
            String query = "UPDATE EOD SET STATUS = ?,STARTTIME = SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";

            backendJdbcTemplate.update(query, status, Configurations.EOD_USER, errorEodId);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public boolean hasErrorforLastEOD() throws Exception {
        String query = null;
        boolean flag = false;
        try {
            query = "SELECT EPS.STATUS FROM EODPROCESSSUMMERY EPS INNER JOIN EODPROCESS EP ON EP.PROCESSID=EPS.PROCESSID WHERE EPS.STATUS=? AND EPS.EODID=? and EPS.CREATEDTIME >=TO_DATE(SYSDATE,'DD-MON-YY') and EP.EODMODULE = ? ";

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(query, countCallback, statusList.getERROR_STATUS(), Configurations.EOD_ID, Configurations.EOD_ENGINE);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                flag = true;
            }

        }catch (EmptyResultDataAccessException e) {
            return false;
        }catch (Exception e){
            throw e;
        }
        return flag;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodEndStatus(int errorEodId, String status) throws Exception {
        try {
            String query = "UPDATE EOD SET STATUS =?,ENDTIME =SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";

            backendJdbcTemplate.update(query, status, Configurations.EOD_USER, errorEodId);
        }catch (Exception e){
            throw e;
        }
    }
}
