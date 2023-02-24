/**
 * Author :
 * Date : 2/3/2023
 * Time : 3:48 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.VisaBaseIIFileClearingDao;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.VisaTC56ComposingDataBean;
import com.epic.cms.model.bean.VisaTC56CurrencyEntryBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DatabaseStatus;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epic.cms.util.LogManager.*;

@Repository
public class VisaBaseIIFileClearingRepo implements VisaBaseIIFileClearingDao {
    @Autowired
    QueryParametersList queryParametersList;
    @Autowired
    StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;
    @Autowired
    CommonRepo commonRepo;

    @Override
    public FileBean getVisaFileInfo(String fileId) throws Exception {
        FileBean fileDataBean = new FileBean();
        try {
            String query = "SELECT FILEID,FILENAME,STATUS FROM EODVISAFILE WHERE FILEID=? AND STATUS IN(?,?)";
            fileDataBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                        @Override
                        public FileBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            FileBean bean = new FileBean();
                            bean.setFileId(rs.getString("FILEID"));
                            bean.setFileName(rs.getString("FILENAME"));
                            bean.setFileStatus(rs.getString("STATUS"));
                            return bean;
                        }
                    },
                    fileId,
                    DatabaseStatus.STATUS_FILE_INIT,
                    DatabaseStatus.STATUS_FILE_REPT
            );

        } catch (EmptyResultDataAccessException ex) {
            infoLoggerEFPE.info(ex.getMessage());
        } catch (Exception ex) {
            throw ex;
        }
        return fileDataBean;
    }

    @Override
    public int updateEODVISAFILE(String fileid) throws Exception {
        int count = 0;

        try {

            String query = "UPDATE EODVISAFILE SET EODID = ?,STATUS = ?, STARTTIME = SYSDATE, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,
                    Configurations.EOD_ID,
                    DatabaseStatus.STATUS_FILE_READ,
                    Configurations.EOD_USER,
                    fileid
            );

        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateEODVISAILE(String fileid, String status) throws Exception {
        int count = 0;

        try {

            String query = "UPDATE EODVISAFILE "
                    + "SET ENDTIME = SYSDATE, STATUS =?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE "
                    + "WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    fileid
            );

        } catch (Exception ex) {
            throw ex;
        }

        return count;
    }

    @Override
    public void updateRecVisaFileStatus(String fileId, String status) throws Exception {
        String query = "UPDATE EODVISAFILE SET STATUS = ?, EODID=? WHERE FILEID = ?";
        try {
            backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_ID,
                    fileId);
        } catch (Exception ex) {
            errorLoggerEFPE.error("VisaBaseIIClearing", ex);
        }
    }

    @Override
    public void updateVisaProcessingStartTime(String fileId) throws Exception {
        String query = "UPDATE EODVISAFILE SET STARTTIME=SYSDATE WHERE FILEID=?";
        try {
            backendJdbcTemplate.update(query,
                    fileId);
        } catch (Exception ex) {
            errorLoggerEFPE.error("VisaBaseIIClearing", ex);
        }
    }

    @Override
    public void updateVisaFileLineNumbers(int noOfRecords, String fileID) throws Exception {
        String query = "UPDATE EODVISAFILE SET NOOFRECORDS=? WHERE FILEID=?";
        try {
            backendJdbcTemplate.update(query,
                    noOfRecords,
                    fileID);
        } catch (Exception ex) {
            errorLoggerEFPE.error("VisaBaseIIClearing", ex);
        }
    }

    @Override
    public int visaFileValidate(String fileId, String fileStatus, String sessionId) throws Exception {
        int procedureOutput = 1;
        try {
            if (fileStatus.equals(DatabaseStatus.STATUS_FILE_REPT)) {
                //delete records from RECVISAREJECT table
                try {
                    String query = "DELETE FROM RECVISAREJECT WHERE FILEID=?";
                    backendJdbcTemplate.update(query,
                            fileId);
                } catch (Exception ex) {
                    throw ex;
                }
            }
            //call to the procedure for file validation process
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("VISABASE2FILEVALIDATEPROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EOD_ID", Configurations.EOD_ID)
                    .addValue("FILE_ID", fileId)
                    .addValue("FILE_STATUS", fileStatus)
                    .addValue("SESSION_ID", sessionId);
            Map<String, Object> out = simpleJdbcCall.execute(in);
            //procedure output
            //procedureOutput = (int) out.get("OUTPUTDATA");
        } catch (Exception ex) {
            throw ex;
        }
        return procedureOutput;
    }

    @Override
    public int composeVisaFileTransactions(String fileId, String sessionId) throws Exception {
        int procedureOutput = 0;
        try {
            //call to the procedure for file validation process
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("VISABASE2TXNCOMPOSEPROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EOD_ID", Configurations.EOD_ID)
                    .addValue("FILE_ID", fileId)
                    .addValue("SESSION_ID", sessionId);
            Map<String, Object> out = simpleJdbcCall.execute(in);
            //procedure output
            //procedureOutput = (int) out.get("OUTPUTDATA");
        } catch (Exception ex) {
            throw ex;
        }
        return procedureOutput;
    }

    @Override
    public ArrayList<String> getVisaTxnIDListForTC56(String fileID) throws Exception {
        ArrayList<String> visaTxnIDList = new ArrayList<>();
        String query = "SELECT DISTINCT TXNID FROM RECVISAFIELDIDENTITY T1 WHERE T1.FILEID=? AND T1.TC='56' AND T1.STATUS=0 ORDER BY TXNID";
        try {
            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            visaTxnIDList.add(rs.getString("TXNID"));
                        }
                        return visaTxnIDList;
                    }, fileID
            );
        } catch (Exception ex) {
            throw ex;
        }
        return visaTxnIDList;
    }

    @Override
    public VisaTC56ComposingDataBean getVisaComposingDataForTC56(String txnID, String tcr, String fileID) throws Exception {
        VisaTC56ComposingDataBean visaTC56ComposingDataBean = new VisaTC56ComposingDataBean();
        String query = "SELECT FIELD04,FIELD05,FIELD06,FIELD07,FIELD08,FIELD09,FIELD10 FROM RECVISAFIELDIDENTITY WHERE TXNID=? "
                + "AND TCR=? AND FILEID=?";
        try {
            visaTC56ComposingDataBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                        @Override
                        public VisaTC56ComposingDataBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            VisaTC56ComposingDataBean visaTC56ComposingDataBean = new VisaTC56ComposingDataBean();
                            visaTC56ComposingDataBean.setField4(rs.getString("FIELD04"));
                            visaTC56ComposingDataBean.setField5(rs.getString("FIELD05"));
                            visaTC56ComposingDataBean.setField6(rs.getString("FIELD06"));
                            visaTC56ComposingDataBean.setField7(rs.getString("FIELD07"));
                            visaTC56ComposingDataBean.setField8(rs.getString("FIELD08"));
                            visaTC56ComposingDataBean.setField9(rs.getString("FIELD09"));
                            visaTC56ComposingDataBean.setField10(rs.getString("FIELD10"));
                            return visaTC56ComposingDataBean;
                        }
                    },
                    txnID,
                    tcr,
                    fileID
            );
        } catch (EmptyResultDataAccessException ex) {
            infoLoggerEFPE.info(ex.getMessage());
        } catch (Exception ex) {
            throw ex;
        }
        return visaTC56ComposingDataBean;
    }

    @Override
    public void insertVisaTC56ComposedData(List<VisaTC56CurrencyEntryBean> currencyList, String fileBaseCurrencyCode, BigDecimal eodBaseCurrencyBuyingRate, BigDecimal eodBaseCurrencySellingRate) throws Exception {
        String query = "";
        try {
            for (VisaTC56CurrencyEntryBean visaTC56CurrencyEntryBean : currencyList) {
                try {
                    String calculatedBuyingRate;
                    String calculatedSellingRate;
                    String effectiveDate = "";

                    //Configurations.BASE_CURRENCY = commonRepo.getBaseCurrency();

                    if (!fileBaseCurrencyCode.equals(Configurations.BASE_CURRENCY)) { //if file base currency not equal to eod base currency (eg: file base currency:USD, eod base currency:LKR)
                        calculatedBuyingRate = visaTC56CurrencyEntryBean.getBuyRate().divide(eodBaseCurrencyBuyingRate, MathContext.DECIMAL32).setScale(12, RoundingMode.DOWN).toString();
                        calculatedSellingRate = visaTC56CurrencyEntryBean.getSellRate().divide(eodBaseCurrencySellingRate, MathContext.DECIMAL32).setScale(12, RoundingMode.DOWN).toString();
                        effectiveDate = visaTC56CurrencyEntryBean.getEffectiveDate();
                    } else {
                        calculatedBuyingRate = visaTC56CurrencyEntryBean.getBuyRate().setScale(12, RoundingMode.DOWN).toString();
                        calculatedSellingRate = visaTC56CurrencyEntryBean.getSellRate().setScale(12, RoundingMode.DOWN).toString();
                        effectiveDate = visaTC56CurrencyEntryBean.getEffectiveDate();
                    }
                    //update in backend CURRENCYEXCHANGERATE table
                    try {
                        query = "UPDATE CURRENCYEXCHANGERATE SET BUYINGRATE=?,SELLINGRATE=?,EFFECTIVEDATE =?,"
                                + "LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE CURRENCYCODE=?";
                        int updateRecordCount = backendJdbcTemplate.update(query,
                                calculatedBuyingRate,
                                calculatedSellingRate,
                                effectiveDate,
                                Configurations.EOD_USER,
                                visaTC56CurrencyEntryBean.getCounterCurrencyCode());

                        if (updateRecordCount == 0) { //if currency code not in CURRENCYEXCHANGERATE table, insert a new record
                            try {
                                query = "INSERT INTO CURRENCYEXCHANGERATE(CURRENCYCODE,BUYINGRATE,SELLINGRATE,EFFECTIVEDATE,LASTUPDATEDUSER) VALUES(?,?,?,?,?) ";
                                int insertRecordCount = backendJdbcTemplate.update(query,
                                        visaTC56CurrencyEntryBean.getCounterCurrencyCode(),
                                        calculatedBuyingRate,
                                        calculatedSellingRate,
                                        effectiveDate,
                                        Configurations.EOD_USER);
                            } catch (Exception ee) {
                                errorLoggerEFPE.error("Currency not defined in CURRENCY table: " + visaTC56CurrencyEntryBean.getCounterCurrencyCode(), ee);
                            }
                        }
                    } catch (Exception ee) {
                        errorLoggerEFPE.error("Unable to update CURRENCYEXCHANGERATE table for currency: " + visaTC56CurrencyEntryBean.getCounterCurrencyCode(), ee);
                    }
                    //update in wallet schema CURRENCY table
                    try {
                        query = "UPDATE " + Configurations.WALLET_SCHEMA_NAME + ".WALLET_CURRENCY SET BUYING_RATE = ?,SELLING_RATE = ?,"
                                + "   LASTUPDATEDTIME=SYSDATE,LASTUPDATEDUSER=? WHERE CODE = ?";
                        int insertRecordCount = backendJdbcTemplate.update(query,
                                calculatedBuyingRate,
                                calculatedSellingRate,
                                Configurations.EOD_USER,
                                visaTC56CurrencyEntryBean.getCounterCurrencyCode());
                    } catch (Exception ee) {
                        errorLoggerEFPE.error("Unable to update WALLET_CURRENCY table in  " + Configurations.WALLET_SCHEMA_NAME + " for currency: " + visaTC56CurrencyEntryBean.getCounterCurrencyCode(), ee);
                    }
                    //add to online side ECMS_ONLINE_EXCHANGE_RATE table
                    try {
                        query = "UPDATE ECMS_ONLINE_EXCHANGE_RATE@" + Configurations.ONLINE_DB_VIEW_NAME + " SET FROMRATE=?,TORATE=? "
                                + "  WHERE CURRENCYNOCODE=?";
                        int insertRecordCount = backendJdbcTemplate.update(query,
                                calculatedSellingRate,
                                calculatedBuyingRate,
                                visaTC56CurrencyEntryBean.getCounterCurrencyCode());

                        if (insertRecordCount == 0) { //if currency code not in ECMS_ONLINE_EXCHANGE_RATE table, insert a new record
                            try {
                                query = "INSERT INTO ECMS_ONLINE_EXCHANGE_RATE@" + Configurations.ONLINE_DB_VIEW_NAME + " (CURRENCYNOCODE,FROMRATE,TORATE) "
                                        + "  VALUES(?,?,?) ";
                                insertRecordCount = backendJdbcTemplate.update(query,
                                        visaTC56CurrencyEntryBean.getCounterCurrencyCode(),
                                        calculatedSellingRate,
                                        calculatedBuyingRate);
                            } catch (Exception ee) {
                                errorLoggerEFPE.error("Unable to insert ECMS_ONLINE_EXCHANGE_RATE table in  " + Configurations.ONLINE_DB_VIEW_NAME + " for currency: " + visaTC56CurrencyEntryBean.getCounterCurrencyCode(), ee);
                            }
                        }
                    } catch (Exception ee) {
                        errorLoggerEFPE.error("Unable to update ECMS_ONLINE_EXCHANGE_RATE table in  " + Configurations.ONLINE_DB_VIEW_NAME + " for currency: " + visaTC56CurrencyEntryBean.getCounterCurrencyCode(), ee);
                    }
                } catch (Exception ex) {
                    throw ex;
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int updateTC56RecordsAsComposed(String fileId) throws Exception {
        int count = 0;
        String sql = "UPDATE RECVISAFIELDIDENTITY SET STATUS=1 WHERE FILEID=? AND TC=56 AND STATUS=0";
        try {
            count = backendJdbcTemplate.update(sql,
                    fileId);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public void updateVisaProcessingStopTime(String fileID) {
        String query = "UPDATE EODVISAFILE SET ENDTIME=SYSDATE WHERE FILEID=?";
        try {
            backendJdbcTemplate.update(query,
                    fileID);
        } catch (Exception ex) {
            errorLoggerEFPE.error("VisaBaseIIClearing", ex);
        }
    }

    @Override
    public void updateVisaFileStatus(String status, String fileId) throws Exception {
        try {
            String query = "UPDATE EODVISAFILE SET STATUS=?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";
            backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    fileId
            );
        } catch (Exception e) {
            throw e;
        }
    }
}
