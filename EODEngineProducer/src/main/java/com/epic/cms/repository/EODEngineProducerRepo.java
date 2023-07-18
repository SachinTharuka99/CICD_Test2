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
import com.epic.cms.util.CommonBackendDbVarList;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.EODEngineStartFailException;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EODEngineProducerRepo implements EODEngineProducerDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CommonBackendDbVarList commonBackendDbVarList;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public List<String> getEODStatusFromEODID(String eodID) {
        List<String> eodIdList = new ArrayList<String>();
        try {
           // String query = "SELECT STATUS FROM EOD WHERE EODID = ?";
            eodIdList = backendJdbcTemplate.queryForList(queryParametersList.getEODEngineProducer_getEODStatusFromEODID(), String.class, eodID);
        } catch (Exception ex) {
            throw ex;
        }
        return eodIdList;
    }

    @Override
    public boolean checkUploadedFileStatus() {
        boolean flag = true;
        try {
            //String query = "select fileid from EODATMFILE where status not in (?, ?) union select fileid from EODDCFFILE where status not in (?, ?) union select fileid from EODMASTERFILE where status not in (?, ?) union select fileid from EODPAYMENTFILE where status not in (?, ?) union select fileid from EODVISAFILE where status not in (?, ?)";

            List<String> fileIdList = backendJdbcTemplate.queryForList(queryParametersList.getEODEngineProducer_checkUploadedFileStatus(), String.class, Configurations.COMPLETE_STATUS, Configurations.FAIL_STATUS,
                    Configurations.COMPLETE_STATUS, Configurations.FAIL_STATUS,
                    Configurations.STATUS_FILE_COMP, Configurations.STATUS_FILE_REJECT,
                    Configurations.COMPLETE_STATUS, Configurations.FAIL_STATUS,
                    Configurations.STATUS_FILE_COMP, Configurations.STATUS_FILE_REJECT);
            Iterator<String> fileIdListIterator = fileIdList.iterator();
            if (fileIdListIterator.hasNext()) {
                flag = false;
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
            //String query = "SELECT EP.PROCESSID,EP.DESCRIPTION,EP.CRITICALSTATUS,EP.ROLLBACKSTATUS,EP.SHEDULEDATE,EP.SHEDULETIME,EP.FREQUENCYTYPE,EP.CONTINUESFREQUENCYTYPE,EP.CONTINUESFREQUENCY, EP.MULTIPLECYCLESTATUS,EF.PROCESSCATEGORYID,EP.DEPENDANCYSTATUS,EP.RUNNINGONMAIN,EP.RUNNINGONSUB, EP.PROCESSTYPE,EP.STATUS,EP.SHEDULEDATETIME, EP.HOLIDAYACTION, EP.KAFKATOPICNAME, EP.KAFKAGROUPID, EP.EODMODULE FROM EODPROCESSFLOW EF, EODPROCESS EP WHERE EF.PROCESSCATEGORYID = ? AND EF.PROCESSID=EP.PROCESSID ORDER BY STEPID ASC";

            processList = backendJdbcTemplate.query(queryParametersList.getEODEngineProducer_getProcessListByCategoryId(), new ProcessBeanRowMapper(), categoryId);
        } catch (Exception ex) {
            throw ex;
        }
        return processList;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void insertToEODProcessCount(String uniqueId, int size, String includedProcess) {
        try {
            //String sql = "INSERT INTO EODPROCESSCOUNT (THREADID,PROCESSCOUNT,COMPLETEDCOUNT,INCLUDED_PROCESS) VALUES (?,?,?,?)";

            backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_insertToEODProcessCount(), uniqueId, size, 0, includedProcess);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int getCompletedProcessCount(String uniqueId) throws SQLException {
        int count = 0;
        //String sql = "SELECT COMPLETEDCOUNT FROM EODPROCESSCOUNT WHERE THREADID = ?";

        try {
            count = backendJdbcTemplate.queryForObject(queryParametersList.getEODEngineProducer_getCompletedProcessCount(), Integer.class, uniqueId);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public void clearEodProcessCountTable() throws Exception {
        try {
            //String sql = "DELETE FROM EODPROCESSCOUNT";

            backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_clearEodProcessCountTable());
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
            //String query = "update EODERRORCARDS set STATUS = ? where EODID < ?";

            result = backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updatePreviousEODErrorCardDetails(), statusList.getEOD_DONE_STATUS(), eodID);

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
            //String query = "update EODERRORMERCHANT set STATUS = ? where EODID < ?";

            result = backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updatePreviousEODErrorMerchantDetails(), statusList.getEOD_DONE_STATUS(), eodID);

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updateEodProcessProgress() throws Exception {
        int count = 0;
        //String Query = "UPDATE EODPROCESSSUMMERY SET SUCCESSCOUNT = ? , FAILEDCOUNT = ? ,PROCESSPROGRESS = ? ,LASTUPDATEDUSER = ?,LASTUPDATEDTIME=SYSDATE WHERE EODID = ? AND PROCESSID = ?";

        try {
            count = backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updateEodProcessProgress(),
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
            //String query = "SELECT PROCESSID FROM EODPROCESSSUMMERY WHERE STATUS='EROR' AND SUCCESSCOUNT=0 AND FAILEDCOUNT=0 AND EODID=?";

            processIdList = backendJdbcTemplate.queryForList(queryParametersList.getEODEngineProducer_getErrorProcessIdList(), String.class, Configurations.ERROR_EOD_ID);
        } catch (Exception e) {
            throw e;
        }
        return processIdList;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateProcessProgressForErrorProcess(String processId) throws Exception {
        try {
            //String query = "UPDATE EODPROCESSSUMMERY SET PROCESSPROGRESS='0%' WHERE EODID=? AND PROCESSID=?";
            backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updateProcessProgressForErrorProcess(), Configurations.ERROR_EOD_ID, processId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updateEodProcessStateCount() throws Exception {
        int count = 0;
        //String query = "UPDATE EOD SET NOOFSUCCESSPROCESS=(SELECT COUNT(*) FROM EODPROCESSSUMMERY WHERE EODID=? AND STATUS=?), NOOFERRORPAROCESS=(SELECT COUNT(*) FROM EODPROCESSSUMMERY WHERE EODID=? AND STATUS=?), LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE EODID=?";
        try {
            count = backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updateEodProcessStateCount(), Configurations.ERROR_EOD_ID,
                    Configurations.COMPLETE_STATUS,
                    Configurations.ERROR_EOD_ID,
                    "EROR",
                    Configurations.EOD_USER,
                    Configurations.ERROR_EOD_ID);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodStatus(int errorEodId, String status) throws Exception {
        try {
            //String query = "UPDATE EOD SET STATUS = ?,STEPID=0,STARTTIME = SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";
            backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updateEodStatus(), status, Configurations.EOD_USER, errorEodId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean hasErrorforLastEOD() throws Exception {
        //String query = null;
        boolean flag = false;
        try {
            //query = "SELECT EPS.STATUS FROM EODPROCESSSUMMERY EPS INNER JOIN EODPROCESS EP ON EP.PROCESSID=EPS.PROCESSID WHERE EPS.STATUS=? AND EPS.EODID=? and EPS.CREATEDTIME >=TO_DATE(SYSDATE,'DD-MON-YY') and EP.EODMODULE = ?";

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(queryParametersList.getEODEngineProducer_hasErrorforLastEOD(), countCallback, statusList.getERROR_STATUS(), Configurations.EOD_ID, Configurations.EOD_ENGINE);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                flag = true;
            }

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodEndStatus(int errorEodId, String status) throws Exception {
        try {
            //String query = "UPDATE EOD SET STATUS =?,ENDTIME =SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";

            backendJdbcTemplate.update(queryParametersList.getEODEngineProducer_updateEodEndStatus(), status, Configurations.EOD_USER, errorEodId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodStatus(int eodId, String status, int stepId) {
        try {
            String query = "UPDATE EOD SET STATUS =?,STEPID=?,ENDTIME =SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";

            backendJdbcTemplate.update(query, status, stepId, Configurations.EOD_USER, eodId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int getNextRunningEodId() {
        int eodId = 0;
        try {
            //String sql = "SELECT EODID FROM EOD WHERE STATUS IN (?,?,?)";
            eodId = backendJdbcTemplate.queryForObject(queryParametersList.getEODEngineProducer_getNextRunningEodId(), Integer.class, "INIT", "HOLD", "EROR");
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return eodId;
    }

    @Override
    public Map<String, String> getNextRunningEodInfo() throws Exception {
        Map<String, String> nextRunningEodInfo = new HashMap<>();
        try {
            String query = "SELECT EODID,STATUS FROM EOD ORDER BY EODID DESC LIMIT 1";
            nextRunningEodInfo = backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        Map<String, String> nextRunningEodInfoTemp = new HashMap<>();
                        while (rs.next()) {
                            nextRunningEodInfoTemp.put(rs.getString("EODID"), rs.getString("STATUS"));
                        }
                        return nextRunningEodInfoTemp;
                    });
        } catch (Exception ex) {
            throw ex;
        }
        return nextRunningEodInfo;
    }

    @Override
    public List<ProcessBean> getProcessListByModule(String module) throws Exception {
        List<ProcessBean> processList = new ArrayList<>();
        try {
            String query = "SELECT EP.PROCESSID,EP.DESCRIPTION,EP.CRITICALSTATUS,EP.ROLLBACKSTATUS," +
                    "EP.SHEDULEDATE,EP.SHEDULETIME,EP.FREQUENCYTYPE,EP.CONTINUESFREQUENCYTYPE,EP.CONTINUESFREQUENCY," +
                    "EP.MULTIPLECYCLESTATUS,EF.PROCESSCATEGORYID,EP.DEPENDANCYSTATUS,EP.RUNNINGONMAIN,EP.RUNNINGONSUB," +
                    "EP.PROCESSTYPE,EP.STATUS,EP.SHEDULEDATETIME, EP.HOLIDAYACTION, EP.KAFKATOPICNAME, EP.KAFKAGROUPID, EP.EODMODULE,EF.STEPID " +
                    "FROM EODPROCESSFLOW EF " +
                    "LEFT JOIN EODPROCESS EP " +
                    "ON EF.PROCESSID=EP.PROCESSID WHERE " +
                    "EP.EODMODULE = ? ORDER BY EF.STEPID ASC";
            processList = backendJdbcTemplate.query(query, new ProcessBeanRowMapper(), module);

            if (List.of("HOLD", "FAIL").contains(Configurations.STARTING_EOD_STATUS)) {
                try {
                    String query2 = "SELECT STEPID FROM EOD WHERE EODID=?";
                    final int nextStepId = backendJdbcTemplate.queryForObject(query2, Integer.class, Configurations.EOD_ID);
                    processList = processList
                            .stream()
                            .filter(c -> c.getStepId() >= nextStepId)
                            .collect(Collectors.toList());
                } catch (Exception ex) {
                    throw new EODEngineStartFailException("Unable to start EOD Engine. Error occurred when fetching the next step ID");
                }

            }
        } catch (Exception ex) {
            throw ex;
        }
        return processList;
    }

}
