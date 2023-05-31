/**
 * Author :
 * Date : 2/2/2023
 * Time : 4:15 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.PaymentFileClearingDao;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.RecPaymentFileIptRowDataBean;
import com.epic.cms.model.rowmapper.RecPaymentFileIptRowDataRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import static com.epic.cms.util.LogManager.*;

@Repository
public class PaymentFileClearingRepo implements PaymentFileClearingDao {
    @Autowired
    private StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;
    @Autowired
    LogManager logManager;

    @Override
    public FileBean getPaymentFileInfo(String fileId) throws Exception {
        FileBean fileBean = new FileBean();
        try {
            String query = "SELECT FILEID,FILENAME FROM EODPAYMENTFILE WHERE FILEID=? ";
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
            logManager.logError(ex.getMessage(),errorLoggerEFPE);
        } catch (Exception ex) {
            throw ex;
        }
        return fileBean;
    }

    @Override
    public void updatePaymentFileStatus(String status, String fileId) throws Exception {
        try {
            String query = "UPDATE EODPAYMENTFILE SET STATUS=?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";
            backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    fileId
            );
        } catch (Exception e) {
            throw e;
        }
    }

    //validation
    @Override
    public Hashtable<String, String[]> getPaymentFieldsValidation() throws Exception {
        Hashtable<String, String[]> paymentFieldValidationsTable = new Hashtable<String, String[]>();

        try {
            String query = "SELECT FIELDID, VALIDATIONID FROM RECPAYMENTFIELD";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            paymentFieldValidationsTable.put(rs.getString("FIELDID"),
                                    rs.getString("VALIDATIONID").split("\\|"));
                        }
                        return paymentFieldValidationsTable;
                    });

        } catch (Exception e) {
            logManager.logError(String.valueOf(e),errorLoggerEFPE);
            throw e;
        }

        return paymentFieldValidationsTable;
    }

    @Override
    public ArrayList<RecPaymentFileIptRowDataBean> getPaymentFileContents(String fileId) throws Exception {
        ArrayList<RecPaymentFileIptRowDataBean> fileContentList = new ArrayList<RecPaymentFileIptRowDataBean>();

        try {
            String query = "SELECT PD.FILEID, PD.LINENUMBER, PD.RECORDCONTENT "
                    + "FROM RECPAYMENTINPUTROWDATA PD "
                    + " WHERE PD.FILEID=?"
                    + " AND PD.EODSTATUS = ?";

            fileContentList = (ArrayList<RecPaymentFileIptRowDataBean>) backendJdbcTemplate.query(query,
                    new RecPaymentFileIptRowDataRowMapper(),
                    fileId,
                    Configurations.EOD_PENDING_STATUS);

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
    public String getFieldDesc(String fieldId) throws Exception {
        String fieldDesc = null;
        String query = "SELECT FIELDCODE FROM RECPAYMENTFIELD WHERE FIELDID = ?";

        try {
            fieldDesc = String.valueOf(backendJdbcTemplate.update(query, fieldId));
        } catch (Exception e) {
            throw e;
        }
        return fieldDesc;
    }

    @Override
    public int insertToRECPAYMENTFILEINVALID(String fileId, BigDecimal lineNumber, String errorMsg) throws Exception {
        int count = 0;
        String query = "INSERT INTO RECPAYMENTFILEINVALID "
                + "(FILEID,EODID,LINENUMBER,ERRORDESC,"
                + "CREATEDTIME,LASTUPDATEDUSER,LASTUPDATEDDATE) "
                + "VALUES (?,?,?,?,SYSDATE,?,SYSDATE)";

        try {
            count = backendJdbcTemplate.update(query,
                    fileId, Configurations.EOD_ID,
                    lineNumber, errorMsg, Configurations.EOD_USER);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public boolean checkForValidCard(StringBuffer cardNumber) throws Exception {
        boolean status = false;
        String query = "SELECT CARDNUMBER FROM CARD WHERE CARDNUMBER=? ";

        try {
            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(query, countCallback, cardNumber.toString());
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                status = true;
            }

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    public int insertToPAYMENT(String[] paymentFields, String paymentType) throws Exception {
        int count = 0;
        String query = "INSERT INTO PAYMENT "
                + "(EODID,SEQUENCENUMBER,TRANSACTIONDATE,POSTINGDATE,"
                + "SOURCETYPE,TRANSACTIONTYPE,CRDRMAINTIND,"
                + "TRANSACTIONAMOUNT,REFERENCE,BANK,REFERENCEBRANCH,"
                + "TRACEID,TRANSACTIONDESC,TRANSACTIONDATETIME,CARDNUMBER,AUTOSETTLEMENT,"
                + "LASTUPDATEDUSER,LASTUPDATETIME,CREATEDTIME,STATUS,INTERNAL_KEY,CHEQUE_RET_CODE,USERID,SOURCEMODULE,PAYMENTTYPE) "
                + "VALUES (?,?,TO_DATE(?,'mm/dd/yyyy'),TO_DATE(?,'mm/dd/yyyy'),?,?,?,?,?,?,?,?,?,TO_DATE(?,'mm/dd/yyyy HH24:MI'),?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?) ";

        try {
            count = backendJdbcTemplate.update(query,
                    Configurations.EOD_ID,
                    paymentFields[0].trim(),
                    paymentFields[2].trim(),
                    paymentFields[9].trim(),
                    paymentFields[3].trim(),
                    paymentFields[7].trim(),
                    paymentFields[19].trim(),
                    Double.parseDouble(paymentFields[12].trim()),
                    paymentFields[13].trim(),
                    paymentFields[5].trim(),
                    paymentFields[20].trim(),
                    paymentFields[18].trim(),
                    paymentFields[21].trim(),
                    paymentFields[22].trim(),
                    paymentFields[25].trim(),
                    paymentFields[32].trim(),
                    Configurations.EOD_USER,
                    Configurations.INITIAL_STATUS,
                    paymentFields[1],
                    paymentFields[33],
                    paymentFields[6],
                    paymentFields[24],
                    paymentType
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertExceptionalTransactionData(String fileId, String txnId, String TC, String TCTCQ, String cardNumber, String authCode, String MID,
                                                String sourceAmount, String sourceCurrencyCode, String txnDate, String txnTime, String processingDate, String lstUpdateUser,
                                                Date lstUpdateDate, String destinationAmount, String destinationCurrencyCode, String financialStatus, String merchantName,
                                                String merchantCity, String merchantCountryCode, String MCC, String merchantZipCode, String merchantState,
                                                String rrn, String tid, String posEntryMode, String fileType, String description) throws Exception {
        int count = 0;
        String query = "INSERT INTO EODEXCEPTIONALTRANSACTION (FILEID,TXNID,TC,CARDNUMBER," +
                "AUTHCODE,MID,SOURCEAMOUNT,SOURCECURRENCYCODE,TXNDATE,TXNTIME,PROCESSINGDATE," +
                "LASTUPDATEDUSER,LASTUPDATEDDATE,DESTINATIONAMOUNT," +
                "DESTINATIONCURRENCYCODE,FINANCIALSTATUS,MERCHANTNAME,MERCHANTCITY,MERCHANTCOUNTRYCODE," +
                "MCC,MERCHANTZIPCODE,MERCHANTSTATE,RRN,TID,POSENTRYMODE,FILETYPE,EODID,DESCRIPTION,TCTCQ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            count = backendJdbcTemplate.update(query,
                    fileId, txnId, TC, cardNumber, authCode, MID, sourceAmount, sourceCurrencyCode,
                    txnDate, txnTime, processingDate, lstUpdateUser, lstUpdateDate, destinationAmount,
                    destinationCurrencyCode, financialStatus, merchantName, merchantCity, merchantCountryCode,
                    MCC, merchantZipCode, merchantState, rrn, tid, posEntryMode, fileType, Configurations.EOD_ID, description, TCTCQ);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateRecPaymentRaw(String fileId, BigDecimal lineNumber) throws Exception {
        int count = 0;
        String query = "UPDATE RECPAYMENTINPUTROWDATA SET EODSTATUS=? WHERE FILEID=? AND LINENUMBER = ?";

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
    public StringBuffer getCardNumberFromMainCardNIC(String nicWithLast4DigitCard) throws Exception {
        StringBuffer cardNumber = null;
        String query = "SELECT CARDNUMBER FROM CARD  C WHERE CONCAT((SELECT IDNUMBER FROM CARD C1 WHERE C1.CARDNUMBER = C.MAINCARDNO),SUBSTR(CARDNUMBER,-4))=?";

        try {
            cardNumber = backendJdbcTemplate.queryForObject(query, StringBuffer.class, nicWithLast4DigitCard);
        } catch (EmptyResultDataAccessException ex) {
            infoLoggerEFPE.info(ex.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return cardNumber;
    }

    @Override
    public StringBuffer getCardNumberFromNIC(String nicWithLast4DigitCard) throws Exception {
        StringBuffer cardNumber = null;
        String query = "SELECT CARDNUMBER FROM CARD WHERE CONCAT(idnumber,SUBSTR(cardnumber,-4))  = ?";

        try {
            cardNumber = backendJdbcTemplate.queryForObject(query, StringBuffer.class, nicWithLast4DigitCard);
        } catch (Exception e) {
            throw e;
        }
        return cardNumber;
    }
}
