/**
 * Author : rasintha_j
 * Date : 7/13/2023
 * Time : 9:59 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.model.bean.PaymentFileDataBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import com.epic.cms.dao.DCFFileClearingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;

@Repository
public class DCFFileClearingRepo implements DCFFileClearingDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Override
    public ArrayList<String> getNameFields(String fileType) throws Exception {
        ArrayList<String> nameFieldList = new ArrayList<String>();

        try {
            String query = "SELECT FILENAMEPRIFIX, FILENAMEPOSTFIX, FILEEXTENTION FROM EODFILETYPE WHERE FILETYPECODE=? ";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            nameFieldList.add(rs.getString("FILENAMEPRIFIX"));
                            nameFieldList.add(rs.getString("FILENAMEPOSTFIX"));
                            nameFieldList.add(rs.getString("FILEEXTENTION"));
                        }
                        return nameFieldList;
                    }, fileType
            );
        } catch (Exception e) {
            throw e;
        }
        return nameFieldList;
    }

    @Override
    public ArrayList<PaymentFileDataBean> getDCFFileList() throws Exception {
        ArrayList<PaymentFileDataBean> dcfFileList = new ArrayList<PaymentFileDataBean>();

        try {
            String query = "SELECT FILEID,FILENAME FROM EODDCFFILE WHERE STATUS=? AND UPLOADTIME <= SYSDATE";

            dcfFileList = (ArrayList<PaymentFileDataBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        PaymentFileDataBean bean = new PaymentFileDataBean();
                        bean.setFileid(result.getString("FILEID"));
                        bean.setFilename(result.getString("FILENAME"));
                        return bean;
                    }),
                    Configurations.INITIAL_STATUS
            );
        } catch (Exception e) {
           throw e;
        }
        return dcfFileList;
    }

    @Override
    public int insertToRECDCFINPUTROWDATA(String fileid, int noofrecords, String recordContent, int batchNo, String fieldType) throws Exception {
        int count = 0;
        String eodStatus = statusList.getEOD_PENDING_STATUS();
        try {
            String query = "INSERT INTO RECDCFINPUTROWDATA (FILEID,LINENUMBER,BATCHNUMBER,FIELDTYPE,RECORDCONTENT, EODSTATUS) VALUES (?,?,?,?,?,?)";

            if (fieldType.equals("6200") || fieldType.equals("6240")) {
                eodStatus = statusList.getEOD_DONE_STATUS();
            }

            count = backendJdbcTemplate.update(query, fileid, noofrecords, batchNo, fieldType, recordContent, eodStatus);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void dcfFileSplitter() throws Exception {

        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("DCFFILESPLIT");
            simpleJdbcCall.execute();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateEODDCFFILE(int noofrecords, String status, String fileid) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODDCFFILE SET ENDTIME = SYSDATE, STATUS =?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE, NOOFRECORDS =? WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,status,Configurations.EOD_USER,String.valueOf(noofrecords),fileid);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int updateEODDCFFILE(String fileid) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODDCFFILE SET EODID = ?, STARTTIME = SYSDATE, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,Configurations.EOD_ID,Configurations.EOD_USER,fileid);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
