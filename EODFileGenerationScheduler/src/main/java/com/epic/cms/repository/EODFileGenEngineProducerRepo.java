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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EODFileGenEngineProducerRepo implements EODFileGenEngineProducerDao {

    @Autowired
    JdbcTemplate backendJdbcTemplate;

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
}
