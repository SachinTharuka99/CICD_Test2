package com.epic.cms.model.rowmapper;

import com.epic.cms.model.bean.ProcessBean;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProcessBeanRowMapper implements RowMapper<ProcessBean> {

    @Override
    public ProcessBean mapRow(ResultSet result, int rowNum) throws SQLException {
        ProcessBean processDetails = new ProcessBean();
        processDetails.setProcessId(result.getInt("PROCESSID"));
        processDetails.setProcessDes(result.getString("DESCRIPTION"));
        processDetails.setCriticalStatus(result.getInt("CRITICALSTATUS"));
        processDetails.setRollBackStatus(result.getInt("ROLLBACKSTATUS"));
        processDetails.setSheduleDate(result.getTimestamp("SHEDULEDATETIME"));
        processDetails.setSheduleTime(result.getString("SHEDULETIME"));
        processDetails.setFrequencyType(result.getInt("FREQUENCYTYPE"));
        processDetails.setContinuousFrequencyType(result.getInt("CONTINUESFREQUENCYTYPE"));
        processDetails.setContinuousFrequency(result.getInt("CONTINUESFREQUENCY"));
        processDetails.setMultiCycleStatus(result.getInt("MULTIPLECYCLESTATUS"));
        processDetails.setProcessCategoryId(result.getInt("PROCESSCATEGORYID"));
        processDetails.setDependancyStatus(result.getInt("DEPENDANCYSTATUS"));
        processDetails.setRunningOnMain(result.getInt("RUNNINGONMAIN"));
        processDetails.setRunningOnSub(result.getInt("RUNNINGONSUB"));
        processDetails.setProcessType(result.getInt("PROCESSTYPE"));
        processDetails.setStatus(result.getString("STATUS"));
        processDetails.setHolidayAction(result.getInt("HOLIDAYACTION"));
        processDetails.setKafkaTopic(result.getString("KAFKATOPICNAME"));
        processDetails.setKafkaGroupId(result.getString("KAFKAGROUPID"));
        processDetails.setEodmodule(result.getString("EODMODULE"));

        return processDetails;
    }
}
