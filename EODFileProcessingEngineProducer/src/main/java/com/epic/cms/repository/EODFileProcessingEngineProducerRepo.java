/**
 * Author :
 * Date : 4/7/2023
 * Time : 5:39 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.dao.EODFileProcessingEngineProducerDao;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EODFileProcessingEngineProducerRepo implements EODFileProcessingEngineProducerDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;
    @Autowired
    StatusVarList status;

    @Override
    public int getCurrentEODId(String status) throws Exception {
        return 0;
    }

    @Override
    public String getFileStatus(String query, String fileId) throws Exception {
        String fileStatus = null;
        try {
            fileStatus = backendJdbcTemplate.queryForObject(query, String.class, fileId);
        } catch (EmptyResultDataAccessException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            throw ex;
        }
        return fileStatus;
    }

    @Override
    public void updateFileStatus(String query, String fileId, String status) throws Exception {
        try {
            backendJdbcTemplate.update(query, status, fileId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public HashMap<String, List<String>> getAllProcessingPendingFiles() throws Exception {
        HashMap<String, List<String>> fileMap = new HashMap<>();
        try {
            String sql = "SELECT FILEID,'VISA' AS FILETYPE FROM EODVISAFILE WHERE STATUS=? UNION ALL SELECT FILEID,'MASTER' AS FILETYP FROM EODMASTERFILE WHERE STATUS=? UNION ALL SELECT FILEID,'ATM' AS FILETYPE FROM EODATMFILE WHERE STATUS=? UNION ALL SELECT FILEID,'PAYMENT' AS FILETYPE FROM EODPAYMENTFILE WHERE STATUS=?";
            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            if (fileMap.size() == 0) {
                                fileMap.put(rs.getString("FILETYPE"), Arrays.asList(rs.getString("FILEID")));
                            } else {
                                for (Map.Entry<String, List<String>> entry : fileMap.entrySet()) {
                                    if (entry.getKey().equals(rs.getString("FILETYPE"))) {
                                        List<String> tempList = entry.getValue();
                                        tempList.add(rs.getString("FILEID"));
                                        fileMap.put(entry.getKey(), tempList);
                                        break;
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        }
                    },
                    status.getINITIAL_STATUS(),
                    status.getINITIAL_STATUS(),
                    status.getINITIAL_STATUS(),
                    status.getINITIAL_STATUS());
        } catch (Exception ex) {

        }
        return fileMap;
    }

    @Override
    public String getProcessIdByUniqueId(String uniqueId) throws Exception {
        return null;
    }

    @Override
    public List<String> getErrorProcessIdList() throws Exception {
        return null;
    }

    @Override
    public void updateProcessProgressForErrorProcess(String processId) throws Exception {

    }

    @Override
    public void updateEodProcessStateCount() throws Exception {

    }

    @Override
    public void updateEodProcessProgress(int successCount, int failedCount, String progress, int processId) throws Exception {

    }
}
