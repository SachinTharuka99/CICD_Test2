/**
 * Author : rasintha_j
 * Date : 7/10/2023
 * Time : 1:23 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MasterCardT67FileReadDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Repository
public class MasterCardT67FileReadRepo implements MasterCardT67FileReadDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public FilePathBean loadFilePaths() throws Exception {
        FilePathBean filePathBean = null;

        try {
            String query = "SELECT FILETYPE,FILEPATHWINDOWS,FILEPATHLINUX,BACKUPPATHWINDOWS,BACKUPPATHLINUX FROM EODFILEINFO WHERE FILETYPE = ?";

            filePathBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                @Override
                public FilePathBean mapRow(ResultSet result, int rowNum) throws SQLException {
                    FilePathBean filePathBean = new FilePathBean();

                    filePathBean.setPath_master_file_windows(result.getString("FILEPATHWINDOWS"));
                    filePathBean.setPath_master_file_linux(result.getString("FILEPATHLINUX"));
                    filePathBean.setPath_backup_windows(result.getString("BACKUPPATHWINDOWS"));
                    filePathBean.setPath_backup_linux(result.getString("BACKUPPATHLINUX"));
                    return filePathBean;
                }
            }, Configurations.FILE_CODE_MASTERCARD_T67);

        } catch (Exception e) {
            throw e;
        }
        return filePathBean;
    }

    @Override
    public boolean isFilesAvailable(String status) throws Exception {
        int count = 0;
        try {
            String query = "SELECT COUNT(NVL(STATUS, '0')) AS TOTAL FROM EODMASTERT67FILE WHERE STATUS = ?";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, status);

            return count > 0;

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ArrayList<FileDetailsBean> getFileDetails(String status) throws Exception {
        ArrayList<FileDetailsBean> fileNames = new ArrayList<>();

        try {
            String query = "SELECT FILENAME,FILEID FROM EODMASTERT67FILE WHERE STATUS = ?";

            fileNames = (ArrayList<FileDetailsBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        FileDetailsBean fileDetails = new FileDetailsBean();

                        fileDetails.setFileName(result.getString("FILENAME"));
                        fileDetails.setFileId(result.getString("FILEID"));
                        return fileDetails;
                    }),
                    status
            );
        } catch (Exception e) {
            throw e;
        }
        return fileNames;
    }

    @Override
    public void updateFileStartTime(String fileId) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODMASTERT67FILE SET STARTTIME = SYSDATE WHERE FILEID = ?";

            count = backendJdbcTemplate.update(query, fileId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateFileStatus(String fileId, String status) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODMASTERT67FILE SET STATUS=?,EODID=? WHERE FILEID = ?";

            count = backendJdbcTemplate.update(query, status, Configurations.EOD_ID, fileId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateFileStatistics(String fileId, String status, String transactionCount) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODMASTERT67FILE SET STATUS=?,NOOFTRANSACTION = ?,NOOFRECORDS = ?, ENDTIME = SYSDATE WHERE FILEID = ?";

            count = backendJdbcTemplate.update(query, status, transactionCount, transactionCount, fileId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int truncateEodMasterIP0075T1Data() throws Exception {
        int count = 0;
        try {
            String query = "TRUNCATE TABLE EODMASTERIP0075T1DATA";

            count = backendJdbcTemplate.update(query);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int truncateEodMasterIP0040T1Data() throws Exception {
        int count = 0;
        try {
            String query = "TRUNCATE TABLE EODMASTERIP0075T1DATA";

            count = backendJdbcTemplate.update(query);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public boolean isInputFileExists(String fileName) throws Exception {
        boolean isFileAvailable = false;
        int count = 0;
        try {
            String query = "SELECT COUNT(NVL(CHECKSUM, 0)) AS FILECOUNT FROM EODMASTERT67FILE WHERE FILENAME=?";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, fileName);

            if (count > 0) {
                isFileAvailable = true;
            }

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            throw e;
        }
        return isFileAvailable;
    }

    @Override
    public int insertRecordToEODMASTERT67FILE(EODInputFileDetailBean bean) throws Exception {
        String query;
        int count = 0;
        try {
            query = "INSERT INTO EODMASTERT67FILE (FILEID,EODID,FILENAME,STATUS,LASTUPDATEDUSER,LASTUPDATEDDATE,CREATETIME,UPLOADTIME,CHECKSUM) VALUES (?,?,?,?,?,SYSDATE,SYSTIMESTAMP,SYSTIMESTAMP,?)";

            count = backendJdbcTemplate.update(query,
                    bean.getFileId(),
                    Configurations.EOD_ID,
                    bean.getFileName(),
                    Configurations.INITIAL_STATUS,
                    Configurations.EOD_USER,
                    bean.getCheckSum()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }


    @Override
    public synchronized void updateOrInsertMasterIP0040T1Data(IP0040T1Bean ip0040t1Bean, String fileId) throws Exception {
        int count, count_insert = 0;
        try {
            String updateQuery = "UPDATE EODMASTERIP0040T1DATA SET EFFECTIVETIMESTAMP =? ,ACTIVEINACTIVECODE =? ,TABLEID =? ,LOWACCOUNTRANGE =? , GCMSPRODUCTID =? ,HIGHACCOUNTRANGE =? ,CARDPROGRAMIDENTIFIER =? ,ICPIPRIORITYCODE  =? ,MEMBERID =? ,PRODUCTTYPEID =? ,ENDPOINT =? , COUNTRYCODEALPHA =? ,COUNTRYCODENUMERIC =? ,REGION =? ,PRODUCTCLASS =? ,TXNROUTINGINDICATOR =? ,LICENSEDPRODUCTID =? , MAPPINGSERVICEINDICATOR =? ,BILLINGCURRENCYDEFAULT =? ,BILLINGEXPONENTDEFAULT =? ,BILLINGPRIMARYCURRENCY =? ,CONTACLESSENABLEIND =? ,CURRENCYINDICATOR =? ,LASTUPDATEDDATE =SYSDATE ,FILEID =? WHERE KEY = ?";

            count = backendJdbcTemplate.update(updateQuery,
                    ip0040t1Bean.getEffectiveTimeStamp(),
                    ip0040t1Bean.getActiveInactiveCode(),
                    ip0040t1Bean.getTableID(),
                    ip0040t1Bean.getLowAccountRange(),
                    ip0040t1Bean.getGCMSProductID(),
                    ip0040t1Bean.getHighAccountRange(),
                    ip0040t1Bean.getCardProgramIdentifier(),
                    ip0040t1Bean.getICPIPriorityCode(),
                    ip0040t1Bean.getMemberId(),
                    ip0040t1Bean.getProductTypeId(),
                    ip0040t1Bean.getEndpoint(),
                    ip0040t1Bean.getCountryCodeAlpha(),
                    ip0040t1Bean.getCountryCodeNumeric(),
                    ip0040t1Bean.getRegion(),
                    ip0040t1Bean.getProductClass(),
                    ip0040t1Bean.getTxnRoutingIndicator(),
                    ip0040t1Bean.getLicensedProductId(),
                    ip0040t1Bean.getMappingServiceIndicator(),
                    ip0040t1Bean.getBillingCurrencyDefault(),
                    ip0040t1Bean.getBillingExponentDefault(),
                    ip0040t1Bean.getBillingPrimaryCurrency(),
                    ip0040t1Bean.getContaclessEnableInd(),
                    ip0040t1Bean.getCurrencyIndicator(),
                    fileId,
                    ip0040t1Bean.getKey()
            );

            //if (count == 0 && ip0040t1Bean.getActiveInactiveCode().equalsIgnoreCase("A")) { // need to insert only active records if it is not in the database already
            if (count == 0) {
                String query = "INSERT INTO EODMASTERIP0040T1DATA(KEY,EFFECTIVETIMESTAMP,ACTIVEINACTIVECODE,TABLEID,LOWACCOUNTRANGE, GCMSPRODUCTID,HIGHACCOUNTRANGE,CARDPROGRAMIDENTIFIER,ICPIPRIORITYCODE ,MEMBERID,PRODUCTTYPEID,ENDPOINT, COUNTRYCODEALPHA,COUNTRYCODENUMERIC,REGION,PRODUCTCLASS,TXNROUTINGINDICATOR,LICENSEDPRODUCTID, MAPPINGSERVICEINDICATOR,BILLINGCURRENCYDEFAULT,BILLINGEXPONENTDEFAULT,BILLINGPRIMARYCURRENCY,CONTACLESSENABLEIND,CURRENCYINDICATOR,LASTUPDATEDDATE,FILEID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,?)";

                count_insert = backendJdbcTemplate.update(query,
                        ip0040t1Bean.getKey(),
                        ip0040t1Bean.getEffectiveTimeStamp(),
                        ip0040t1Bean.getActiveInactiveCode(),
                        ip0040t1Bean.getTableID(),
                        ip0040t1Bean.getLowAccountRange(),
                        ip0040t1Bean.getGCMSProductID(),
                        ip0040t1Bean.getHighAccountRange(),
                        ip0040t1Bean.getCardProgramIdentifier(),
                        ip0040t1Bean.getICPIPriorityCode(),
                        ip0040t1Bean.getMemberId(),
                        ip0040t1Bean.getProductTypeId(),
                        ip0040t1Bean.getEndpoint(),
                        ip0040t1Bean.getCountryCodeAlpha(),
                        ip0040t1Bean.getCountryCodeNumeric(),
                        ip0040t1Bean.getRegion(),
                        ip0040t1Bean.getProductClass(),
                        ip0040t1Bean.getTxnRoutingIndicator(),
                        ip0040t1Bean.getLicensedProductId(),
                        ip0040t1Bean.getMappingServiceIndicator(),
                        ip0040t1Bean.getBillingCurrencyDefault(),
                        ip0040t1Bean.getBillingExponentDefault(),
                        ip0040t1Bean.getBillingPrimaryCurrency(),
                        ip0040t1Bean.getContaclessEnableInd(),
                        ip0040t1Bean.getCurrencyIndicator(),
                        fileId
                );
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public synchronized void updateOrInsertMasterIP0075T1Data(IP0075T1Bean ip0075t1Bean, String fileId) throws Exception {
        int count, count_insert = 0;
        try {
            String updateQuery = "UPDATE EODMASTERIP0075T1DATA SET EFFECTIVETIMESTAMP =? ,ACTIVEINACTIVECODE =? ,TABLEID =? ,MCC =? , CAB =? ,CABPROLIFECYCLEIND =? ,CABTYPE =? ,CABLIFECYCLEIND  =? ,LASTUPDATEDDATE =SYSDATE ,FILEID =? WHERE KEY = ?";

            count = backendJdbcTemplate.update(updateQuery,
                    ip0075t1Bean.getEffectiveTimeStamp(),
                    ip0075t1Bean.getActiveInactiveCode(),
                    ip0075t1Bean.getTableID(),
                    ip0075t1Bean.getMCC(),
                    ip0075t1Bean.getCAB(),
                    ip0075t1Bean.getCABProgramLifecycleIndicator(),
                    ip0075t1Bean.getCABType(),
                    ip0075t1Bean.getCABLifeCycleIndicator(),
                    fileId,
                    ip0075t1Bean.getKey()
            );

            //if (count == 0 && ip0075t1Bean.getActiveInactiveCode().equalsIgnoreCase("A")) { // need to insert only active records if it is not in the database already
            if (count == 0) {
                String query = "INSERT INTO EODMASTERIP0075T1DATA(KEY,EFFECTIVETIMESTAMP,ACTIVEINACTIVECODE,TABLEID,MCC, CAB,CABPROLIFECYCLEIND,CABTYPE,CABLIFECYCLEIND,LASTUPDATEDDATE,FILEID) VALUES (?,?,?,?,?,?,?,?,?,SYSDATE,?)";

                count_insert = backendJdbcTemplate.update(query,
                        ip0075t1Bean.getKey(),
                        ip0075t1Bean.getEffectiveTimeStamp(),
                        ip0075t1Bean.getActiveInactiveCode(),
                        ip0075t1Bean.getTableID(),
                        ip0075t1Bean.getMCC(),
                        ip0075t1Bean.getCAB(),
                        ip0075t1Bean.getCABProgramLifecycleIndicator(),
                        ip0075t1Bean.getCABType(),
                        ip0075t1Bean.getCABLifeCycleIndicator(),
                        fileId
                );
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
