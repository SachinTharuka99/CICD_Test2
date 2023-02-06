/**
 * Author :
 * Date : 2/3/2023
 * Time : 10:33 AM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CommonFileReadDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DatabaseStatus;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CommonFileReadRepo implements CommonFileReadDao {
    @Autowired
    private QueryParametersList queryParametersList;
    @Autowired
    private StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    @Transactional(value = "backendDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public void updateFileReadStartTime(String tableName, String fileId) throws Exception {
        try {
            String sql = "UPDATE " + tableName + " SET EODID = ?, STARTTIME = SYSDATE, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=?";
            backendJdbcTemplate.update(sql,
                    Configurations.EOD_ID,
                    Configurations.EOD_USER,
                    fileId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    @Transactional(value = "backendDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public void updateFileReadSummery(String tableName, int recordCount, String fileId) throws Exception {
        try {
            String sql = "UPDATE " + tableName + " SET ENDTIME = SYSDATE, STATUS =?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE, NOOFRECORDS =? WHERE FILEID=?";
            backendJdbcTemplate.update(sql,
                    DatabaseStatus.STATUS_FILE_READ,
                    Configurations.EOD_USER,
                    recordCount,
                    fileId);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
