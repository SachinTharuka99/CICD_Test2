/**
 * Author :
 * Date : 2/2/2023
 * Time : 2:12 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.ATMFileClearingDao;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.bean.RecATMFileIptRowDataBean;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.model.rowmapper.RecATMFileIptRowDataRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.epic.cms.util.LogManager.*;

@Repository
public class ATMFileClearingRepo implements ATMFileClearingDao {
    @Autowired
    QueryParametersList queryParametersList;
    @Autowired
    StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public FileBean getATMFileInfo(String fileId) throws Exception {
        FileBean fileBean = new FileBean();
        try {
            String query = "SELECT FILEID,FILENAME FROM EODATMFILE WHERE FILEID=? ";
            fileBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                        @Override
                        public FileBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            FileBean bean = new FileBean();
                            bean.setFileId(rs.getString("FILEID"));
                            bean.setFileName(rs.getString("FILENAME"));
                            return bean;
                        }
                    },
                    fileId
            );

        } catch (EmptyResultDataAccessException ex) {
        } catch (Exception ex) {
            throw ex;
        }
        return fileBean;
    }

    @Override
    public void updateATMFileStatus(String status, String fileId) throws Exception {
        try {
            String query = "UPDATE EODATMFILE SET STATUS=?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";
            backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    fileId
            );
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ProcessBean getProcessDetails(int processId) throws Exception {
        SimpleDateFormat sdf = null;
        String DATE_FORMAT = null;
        Calendar cal = null;
        ProcessBean processDetails = null;

        cal = Calendar.getInstance(TimeZone.getDefault());

        DATE_FORMAT = "MM/dd/yyyy hh:mm:ss aaa";

        sdf = new SimpleDateFormat(DATE_FORMAT);

        sdf.setTimeZone(TimeZone.getDefault());

        try {

            String query = "SELECT PROCESSID,DESCRIPTION,CRITICALSTATUS,ROLLBACKSTATUS,"
                    + "SHEDULEDATE,SHEDULETIME,FREQUENCYTYPE,CONTINUESFREQUENCYTYPE,CONTINUESFREQUENCY,"
                    + "MULTIPLECYCLESTATUS,PROCESSCATEGORYID,DEPENDANCYSTATUS,RUNNINGONMAIN,RUNNINGONSUB,"
                    + "PROCESSTYPE,STATUS,SHEDULEDATETIME,HOLIDAYACTION FROM EODPROCESS WHERE PROCESSID = ? ";//AND SHEDULEDATETIME <= to_date(?,'MM/dd/YYYY HH:mi:ss AM') ";

            backendJdbcTemplate.query(query,
                    new ProcessBeanRowMapper(),
                    processId
            );

        } catch (Exception e) {
            throw e;
        }
        return processDetails;
    }

    @Override
    public Hashtable<String, String[]> getATMFieldsValidation() throws Exception {
        Hashtable<String, String[]> paymentFieldValidationsTable = new Hashtable<String, String[]>();

        try {

            String query = "SELECT FIELDID,VALIDATIONID FROM RECATMFIELD";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            paymentFieldValidationsTable.put(rs.getString("FIELDID"), rs.getString("VALIDATIONID").split("\\|"));
                        }
                        return paymentFieldValidationsTable;
                    });

        } catch (Exception e) {
            throw e;
        }
        return paymentFieldValidationsTable;
    }

    @Override
    public ArrayList<RecATMFileIptRowDataBean> getAtmFileContents(String fileid) throws Exception {
        ArrayList<RecATMFileIptRowDataBean> fileContentList = new ArrayList<RecATMFileIptRowDataBean>();

        try {

            String query = "SELECT PD.FILEID, PD.LINENUMBER, PD.RECORDCONTENT "
                    + "FROM RECATMINPUTROWDATA PD "
                    + "WHERE PD.FILEID=? "
                    + " AND EODSTATUS=?";


            fileContentList = (ArrayList<RecATMFileIptRowDataBean>) backendJdbcTemplate.query(query,
                    new RecATMFileIptRowDataRowMapper(),
                    fileid,
                    Configurations.EOD_PENDING_STATUS
            );

        } catch (Exception e) {
            throw e;
        }

        return fileContentList;
    }

    @Override
    public String getErrorDesc(String validaionId) throws Exception {
        String validationDesc = null;
        String query = "SELECT VALIDATIONDESC FROM RECPAYMENTFIELDVALIDATION WHERE VALIDATIONID = ?";

        try {
            validationDesc = backendJdbcTemplate.queryForObject(query, String.class, validaionId.trim());
        } catch (Exception e) {
            throw e;
        }
        return validationDesc;
    }

    @Override
    public String getATMFieldDesc(String fieldId) throws Exception {
        String fieldDesc = "";
        String query = "SELECT FIELDCODE FROM RECATMFIELD WHERE FIELDID = ?";

        try {
            fieldDesc = backendJdbcTemplate.queryForObject(query, String.class, fieldId);
        } catch (Exception e) {
            throw e;
        }
        return fieldDesc;
    }

    @Override
    public boolean checkForValidCard(StringBuffer cardNumber) throws Exception {
        boolean status = false;
        int recordCount = 0;
        String query = "SELECT COUNT(CARDNUMBER) AS RECORDCOUNT FROM CARD WHERE CARDNUMBER=? ";

        try {
            recordCount = backendJdbcTemplate.queryForObject(query, Integer.class, cardNumber.toString());

            if (recordCount > 0) {
                status = true;
            }

        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    public int insertToATMTRANSACTION(String fileId, String txnId, String[] paymentFields) throws Exception {
        int count = 0;

        String query = "INSERT INTO RECATMTRANSACTION "
                + "(FILEID,EODID,SESSIONID,PRI_TRAN_CODE,SEC_TRAN_CODE,AUTHORIZER,VOID_CODE,TP_DATETIME,"
                + "SYS_SEQ_NBR,RTRVL_REF_NBR,BIN,CARDHOLDER,ACCT_TYPE_1,AMOUNT_AUTH,TRN_CURR_CODE,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?)";

        try {
            count = backendJdbcTemplate.update(query,
                    fileId,
                    Configurations.EOD_ID,
                    txnId,
                    paymentFields[0],
                    paymentFields[1],
                    paymentFields[2],
                    paymentFields[3],
                    paymentFields[4],
                    paymentFields[5],
                    paymentFields[6],
                    paymentFields[7],
                    paymentFields[8],
                    Integer.parseInt(paymentFields[9]),
                    Double.parseDouble(paymentFields[10]),
                    (paymentFields.length != 11) ? Integer.parseInt(paymentFields[11]) : 0,
                    Configurations.EOD_USER
            );
        } catch (Exception e) {
            throw e;
        }

        return count;
    }

    @Override
    public int insertExceptionalTransactionData(String fileId, String txnId, String TC, StringBuffer cardNumber, String authCode, String MID, String sourceAmount, String sourceCurrencyCode, String txnDate, String txnTime, String processingDate, String lstUpdateUser, Date lstUpdateDate, String destinationAmount, String destinationCurrencyCode, String financialStatus, String merchantName, String merchantCity, String merchantCountryCode, String MCC, String merchantZipCode, String merchantState, String rrn, String tid, String posEntryMode, String fileType, String description) throws Exception {
        int count = 0;

        String query = "INSERT INTO EODEXCEPTIONALTRANSACTION (FILEID,TXNID,TC,CARDNUMBER,"
                + "AUTHCODE,MID,SOURCEAMOUNT,SOURCECURRENCYCODE,TXNDATE,TXNTIME,PROCESSINGDATE,"
                + "LASTUPDATEDUSER,LASTUPDATEDDATE,DESTINATIONAMOUNT,"
                + "DESTINATIONCURRENCYCODE,FINANCIALSTATUS,MERCHANTNAME,MERCHANTCITY,MERCHANTCOUNTRYCODE,"
                + "MCC,MERCHANTZIPCODE,MERCHANTSTATE,RRN,TID,POSENTRYMODE,FILETYPE,EODID,DESCRIPTION) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


        try {
            count = backendJdbcTemplate.update(query,
                    fileId, txnId, TC, cardNumber.toString().trim(), authCode, MID, sourceAmount,
                    sourceCurrencyCode, txnDate, txnTime, processingDate, lstUpdateUser, lstUpdateDate,
                    destinationAmount, destinationCurrencyCode, financialStatus, merchantName,
                    merchantCity, merchantCountryCode, MCC, merchantZipCode, merchantState, rrn, tid,
                    posEntryMode, fileType, Configurations.EOD_ID, description
            );

        } catch (Exception e) {
            throw e;
        }

        return count;
    }

    @Override
    public int updateRawAtm(String fileId, BigDecimal lineNumber) throws Exception {
        int count = 0;

        String query = "UPDATE RECATMINPUTROWDATA SET EODSTATUS=? WHERE FILEID=? AND LINENUMBER = ?";

        try {
            count = backendJdbcTemplate.update(query,
                    Configurations.EOD_DONE_STATUS,
                    fileId,
                    lineNumber
            );
        } catch (Exception e) {
            throw e;
        }

        return count;
    }

    @Override
    public void markAtmReversal(String fileId) throws Exception {
        List<String> lst = new ArrayList<>();

        //get all transaction with reversal
        String query1 = "SELECT RTRVL_REF_NBR,CARDHOLDER,AMOUNT_AUTH,FILEID FROM RECATMTRANSACTION R WHERE FILEID=? GROUP BY RTRVL_REF_NBR,CARDHOLDER,AMOUNT_AUTH,FILEID "
                + " HAVING COUNT(RTRVL_REF_NBR)>=2 AND COUNT(CARDHOLDER)>=2 AND COUNT(AMOUNT_AUTH)>=2 AND COUNT(FILEID)>=2";

        try {
            backendJdbcTemplate.query(query1,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            lst.add(rs.getString("RTRVL_REF_NBR") + "|" + rs.getString("CARDHOLDER") + "|" + rs.getString("AMOUNT_AUTH"));
                        }
                    },
                    fileId
            );

            String query2 = "UPDATE RECATMTRANSACTION SET STATUS=? WHERE RTRVL_REF_NBR=? AND CARDHOLDER=? AND AMOUNT_AUTH=?";


            for (String str : lst) {
                String[] arr = str.split("\\|");
                backendJdbcTemplate.update(query2, Configurations.EOD_DONE_STATUS, arr[0], arr[1], arr[2]);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int insertToRECATMFILEINVALID(String fileId, BigDecimal linenumber, String errorMsg) throws Exception {
        int count = 0;

        try {
            String query = "INSERT INTO RECATMFILEINVALID (FILEID,EODID,LINENUMBER,ERRORDESC,CREATEDTIME,LASTUPDATEDUSER,LASTUPDATEDDATE) VALUES (?,?,?,?,SYSDATE,?,SYSDATE)";

            count = backendJdbcTemplate.update(query,
                    fileId,
                    Configurations.EOD_ID,
                    linenumber,
                    errorMsg,
                    Configurations.EOD_USER
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
