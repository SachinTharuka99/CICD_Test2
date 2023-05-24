/**
 * Author : lahiru_p
 * Date : 1/23/2023
 * Time : 5:36 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.EODFileGenEngineProducerDao;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EODFileGenEngineProducerRepo implements EODFileGenEngineProducerDao {

    @Autowired
    JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Override
    public List<ProcessBean> getProcessListByFileGenCategoryId(int categoryId, int issuingOrAcquiring) throws Exception {
        List<ProcessBean> processList = new ArrayList<ProcessBean>();
        try {
            //ISSUING - ISS
            //Acquiring - ACQ
            /*String query = "SELECT EP.PROCESSID,DESCRIPTION,CRITICALSTATUS,ROLLBACKSTATUS,"
                    + "SHEDULEDATE,SHEDULETIME,FREQUENCYTYPE,CONTINUESFREQUENCYTYPE,CONTINUESFREQUENCY,"
                    + "MULTIPLECYCLESTATUS,EF.PROCESSCATEGORYID,DEPENDANCYSTATUS,RUNNINGONMAIN,RUNNINGONSUB,"
                    + "PROCESSTYPE,STATUS,SHEDULEDATETIME FROM EODPROCESSFLOW EF, EODPROCESS EP WHERE"
                    + " EF.PROCESSCATEGORYID = ? AND EP.ISSUINGORACQUIRING = ?  AND EF.PROCESSID=EP.PROCESSID";*/
            String query = "SELECT EP.PROCESSID,EP.DESCRIPTION,EP.CRITICALSTATUS,EP.ROLLBACKSTATUS,EP.SHEDULEDATE,EP.SHEDULETIME,EP.FREQUENCYTYPE,EP.CONTINUESFREQUENCYTYPE,EP.CONTINUESFREQUENCY,EP.MULTIPLECYCLESTATUS,EF.PROCESSCATEGORYID,EP.DEPENDANCYSTATUS,EP.RUNNINGONMAIN,EP.RUNNINGONSUB,EP.PROCESSTYPE,EP.STATUS,EP.SHEDULEDATETIME, EP.HOLIDAYACTION, EP.KAFKATOPICNAME, EP.KAFKAGROUPID, EP.EODMODULE FROM EODPROCESSFLOW EF, EODPROCESS EP WHERE EP.ISSUINGORACQUIRING = ? AND EF.PROCESSCATEGORYID = ? AND EF.PROCESSID=EP.PROCESSID";

            processList = backendJdbcTemplate.query(query, new ProcessBeanRowMapper(), issuingOrAcquiring, categoryId);


        }catch (Exception e){
            throw e;
        }
        return processList;
    }

    @Override
    public int getCurrentEodId(String initial_status, String error_status) throws Exception {
        int eodId = 0;
        try{
            String query = "SELECT EODID FROM EOD WHERE STATUS = ? OR STATUS = ?";

            eodId = backendJdbcTemplate.queryForObject(query, Integer.class, initial_status, error_status);
        }catch (EmptyResultDataAccessException ex){
           return eodId;
        } catch (Exception e){
            throw e;
        }
        return eodId;
    }

    @Override
    public String getEodStatusByEodID(int eodId) throws Exception {
        String EodStatus = null;
        String query = null;
        try {
            query = "Select STATUS FROM EOD WHERE EODID = ?";

            EodStatus = backendJdbcTemplate.queryForObject(query, String.class,eodId);

        }catch (Exception e){
            throw e;
        }
        return EodStatus;
    }

    @Override
    public String getProcessIdByUniqueId(String uniqueId) throws Exception {
        String processList = "";
        String sql = "SELECT INCLUDED_PROCESS FROM EODPROCESSCOUNT WHERE THREADID = ?";
        try {
            processList = backendJdbcTemplate.queryForObject(sql, String.class, uniqueId);
        }catch (Exception e){
            throw e;
        }
        return processList;
    }

    @Override
    public void insertToEODProcessCount(String uniqueId, int size, String includedProcess) throws Exception {
        try {
            String sql = "INSERT INTO EODPROCESSCOUNT (THREADID,PROCESSCOUNT,COMPLETEDCOUNT,INCLUDED_PROCESS)" +
                    " VALUES (?,?,?,?)";

            backendJdbcTemplate.update(sql, uniqueId, size, 0, includedProcess);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int updateEodProcessProgress(int successCount, int failedCount, String progress, int processId) throws Exception {
        int count = 0;
        String Query = "UPDATE EODPROCESSSUMMERY SET SUCCESSCOUNT = ? , FAILEDCOUNT = ? ,PROCESSPROGRESS = ? ,LASTUPDATEDUSER = ?,LASTUPDATEDTIME=SYSDATE WHERE EODID = ? AND PROCESSID = ?";

        try {
            count = backendJdbcTemplate.update(Query,
                    successCount,
                    failedCount,
                    progress,
                    Configurations.EOD_USER,
                    Configurations.ERROR_EOD_ID,
                    processId);
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
    public void updateProcessProgressForErrorProcess(String processId) throws Exception {
        try {
            String query = "UPDATE EODPROCESSSUMMERY SET PROCESSPROGRESS='0%' WHERE EODID=? AND PROCESSID=? ";
            backendJdbcTemplate.update(query, Configurations.ERROR_EOD_ID, processId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
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
    public int getCompletedProcessCount(String uniqueId) throws Exception {
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
    public void updateEodFileGenStatus(int eodId, String status) throws Exception {
        try {
            String query = "UPDATE EOD SET FILEGENSTATUS =?,ENDTIME =SYSDATE,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ?";

            backendJdbcTemplate.update(query, status, Configurations.EOD_USER, eodId);
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
            backendJdbcTemplate.query(query, countCallback, statusList.getERROR_STATUS(), Configurations.EOD_ID, Configurations.EOD_FILE_GENERATION);
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
    public void clearEodProcessCountTable() throws Exception {
        try {
            String sql = "DELETE FROM EODPROCESSCOUNT";

            backendJdbcTemplate.update(sql);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
