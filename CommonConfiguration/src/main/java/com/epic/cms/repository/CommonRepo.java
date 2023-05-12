package com.epic.cms.repository;

import com.epic.cms.dao.CommonDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.*;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class CommonRepo implements CommonDao {

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Override
    public ProcessBean getProcessDetails(int processId) throws Exception {
        ProcessBean processDetails = new ProcessBean();
        try {
            processDetails = backendJdbcTemplate.queryForObject(queryParametersList.getCommonSelectGetProcessDetails(), new ProcessBeanRowMapper(), processId);
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
        return processDetails;
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void insertToEodProcessSumery(int processId, String eodmodule) throws Exception {
        try {
            backendJdbcTemplate.update(queryParametersList.getCommonInsertToEodProcessSumery(), Configurations.ERROR_EOD_ID, processId, statusList.getINITIAL_STATUS(), Configurations.EOD_USER, eodmodule);
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateEodProcessSummery(int eodId, String status, int processId, int successCount, int failedCount, String progress) throws Exception {
        try {
            backendJdbcTemplate.update(queryParametersList.getCommonUpdateEodProcessSummery(), status, Configurations.EOD_USER, successCount, failedCount, progress, eodId, processId);
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
    }

    @Override
    public int updateEodProcessSummery(int eodId, String status, int processId) throws Exception {
        int count = 0;

        try {
            String query = "UPDATE EODPROCESSSUMMERY SET ENDTIME = SYSDATE , STATUS = ?,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ? AND PROCESSID = ?";

            count = backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    eodId,
                    processId
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public StringBuffer getMainCardNumber(StringBuffer cardNo) throws Exception {
        StringBuffer mainCardNo = null;
        try {
            mainCardNo = backendJdbcTemplate.queryForObject(queryParametersList.getCommon_getMainCardNumber(), StringBuffer.class, new Object[]{cardNo});
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
        return mainCardNo;
    }

    @Override
    public HashMap<Integer, ArrayList<EodTransactionBean>> getAllSettledTxnFromTxn() throws Exception {
        HashMap<Integer, ArrayList<EodTransactionBean>> txnMap = new HashMap<>();
        ArrayList<EodTransactionBean> txnList;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            txnList = (ArrayList<EodTransactionBean>) backendJdbcTemplate.query(queryParametersList.getAcqTxnUpdate_getAllSettledTxnFromTxn(),
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        int onOffStatus = result.getInt("ONOFFSTSTUS");
                        EodTransactionBean eodTransactionBean = new EodTransactionBean();
                        //Set values to eodTransactionBean
                        eodTransactionBean.setAccountNo(this.getAccountNoOnCard(new StringBuffer(result.getString("CARDNO"))));
                        eodTransactionBean.setAuthCode(result.getString("AUTHCODE"));
                        eodTransactionBean.setBatchNo(result.getString("BATCHNO"));
                        eodTransactionBean.setCardNo(new StringBuffer(result.getString("CARDNO")));
                        eodTransactionBean.setCountryNumCode(result.getString("COUNTRYCODE"));
                        eodTransactionBean.setCurrencyType(result.getString("TXNCURRENCY"));
                        eodTransactionBean.setMid(result.getString("MID"));
                        eodTransactionBean.setOnOffStatus(onOffStatus);
                        eodTransactionBean.setPosEntryMode(result.getString("POSENTRYMODE"));
                        eodTransactionBean.setRrn(result.getString("RRN"));
                        eodTransactionBean.setSequenceNumber(result.getString("CB_SEQ_NO"));
                        eodTransactionBean.setSettlementDate(result.getDate("SETTLEMENTDATE"));
                        eodTransactionBean.setTid(result.getString("TID"));
                        eodTransactionBean.setToAccNo(result.getString("TOACCOUNT"));
                        eodTransactionBean.setTraceId(result.getString("TRACENO"));
                        eodTransactionBean.setTxnAmount(result.getString("TRANSACTIONAMOUNT"));
                        eodTransactionBean.setTxnDate(result.getDate("CREATETIME"));
                        eodTransactionBean.setTxnDescription(result.getString("CAIC"));
                        eodTransactionBean.setTxnId(result.getString("TXNID"));
                        eodTransactionBean.setTxnType(result.getString("BACKENDTXNTYPE"));
                        eodTransactionBean.setMcc(result.getString("MCC"));
                        eodTransactionBean.setBillingAmount(result.getString("BILLINGAMOUNT"));
                        eodTransactionBean.setRequestFrom(result.getString("REQUESTFROM"));
                        eodTransactionBean.setSecondPartyPan(result.getString("SECOND_PARTY_PAN"));
                        eodTransactionBean.setChannelType(result.getInt("CHANNELTYPE"));
                        eodTransactionBean.setListenerType(result.getString("LISTENERTYPE"));
                        return eodTransactionBean;
                    }),
                    Configurations.EOD_ACQUIRING_STATUS,
                    statusList.getRESPONSE_CODE_00(),
                    Configurations.EOD_PENDING_STATUS,
                    statusList.getTXN_SETTLLED_STATUS(),
                    statusList.getTXN_COMPLETE_STATUS(),
                    sdf.format(Configurations.EOD_DATE),
                    Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT,
                    Configurations.TXN_TYPE_MVISA_ORIGINATOR,
                    Configurations.EOD_CONSIDER_STATUS);

            for (EodTransactionBean eodTransactionBean : txnList) {
                int onOffStatus = 0;
                onOffStatus = eodTransactionBean.getOnOffStatus();
                if (txnMap.containsKey(onOffStatus)) {
                    txnList = txnMap.get(onOffStatus);
                } else {
                    txnList = new ArrayList<>();
                }
                txnList.add(eodTransactionBean);
                txnMap.put(onOffStatus, txnList);
            }

        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
        return txnMap;
    }

    public String getAccountNoOnCard(StringBuffer cardNo) {
        String accNo = null;
        String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";
        try {
            accNo = backendJdbcTemplate.queryForObject(query, String.class, cardNo.toString());
        } catch (Exception e) {
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return accNo;
    }

    @Override
    public int insertToEODTransaction(StringBuffer cardNumber, String accountNo, String mId, String tId, String txnAmount, int currencyType, String crDr, Date settlementDate, Date txnDate, String txnType, String batchNo, String txnId, String toAccNo, Double loyaltyPoint, String Description, String countryCode, int onOffStatus, String poStringsEntryMode, String traceId, String authCode, int adjustmentFlag, String requestFrom, String secondPartyPan, String fualSurchargeAmount, String mcc, String cardAssociation) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            count = backendJdbcTemplate.update(queryParametersList.getCommon_insertToEODTransaction(),
                    Configurations.EOD_ID,
                    cardNumber.toString(),
                    accountNo,
                    mId,
                    tId,
                    txnAmount,
                    currencyType,
                    crDr,
                    sdf.format(settlementDate),
                    sdf.format(txnDate),
                    txnType,
                    batchNo,
                    txnId,
                    Configurations.EOD_USER,
                    toAccNo,
                    statusList.getINITIAL_STATUS(),
                    Description,
                    countryCode,
                    onOffStatus,
                    poStringsEntryMode,
                    onOffStatus,
                    authCode,
                    adjustmentFlag,
                    requestFrom,
                    secondPartyPan,
                    fualSurchargeAmount,
                    mcc,
                    txnType,
                    cardAssociation
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoEodMerchantTransaction(EodTransactionBean eodTransactionBean, String status) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            int adjustmentFlag = 0;
            if (!(eodTransactionBean.getAdjustmentFlag() == null)) {
                adjustmentFlag = Integer.parseInt(eodTransactionBean.getAdjustmentFlag());
            } else {
                adjustmentFlag = 0;
            }

            count = backendJdbcTemplate.update(queryParametersList.getCommon_insertIntoEodMerchantTransaction(),
                    eodTransactionBean.getAuthCode(),//Base Currency
                    eodTransactionBean.getBatchNo(),//ONUS ACQ Status
                    eodTransactionBean.getCountryNumCode(),//Response Code
                    eodTransactionBean.getCrDr(),//EPEN Status
                    eodTransactionBean.getCurrencyType(),//Settle Txn Status
                    Configurations.EOD_ID,//Base Currency
                    eodTransactionBean.getForexMarkupAmount(),//ONUS ACQ Status
                    0,//Response Code
                    Configurations.EOD_USER,//EPEN Status
                    eodTransactionBean.getMid(),//Settle Txn Status
                    eodTransactionBean.getOnOffStatus(),//Base Currency
                    eodTransactionBean.getPaymentType(),//ONUS ACQ Status
                    eodTransactionBean.getPosEntryMode(),//Response Code
                    eodTransactionBean.getRrn(),//EPEN Status
                    eodTransactionBean.getSequenceNumber(),//Settle Txn Status
                    sdf.format(eodTransactionBean.getSettlementDate()),//Base Currency
                    status,//ONUS ACQ Status
                    eodTransactionBean.getTid(),//Response Code
                    eodTransactionBean.getToAccNo(),//EPEN Status
                    eodTransactionBean.getTraceId(),//Settle Txn Status
                    eodTransactionBean.getTxnAmount(),//Base Currency
                    sdf.format(eodTransactionBean.getTxnDate()),//ONUS ACQ Status
                    eodTransactionBean.getTxnDescription(),//Response Code
                    eodTransactionBean.getTxnId(),//EPEN Status
                    eodTransactionBean.getTxnType(),
                    eodTransactionBean.getMcc(),//Settle Txn Status
                    eodTransactionBean.getBin(),
                    eodTransactionBean.getCardNo().toString(),
                    adjustmentFlag,
                    "2", //acq transaction
                    eodTransactionBean.getFuelSurchargeAmount(), //fuel surcharge amount
                    eodTransactionBean.getCardAssociation(), //VISA OR MASTER
                    eodTransactionBean.getCardProduct()
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateTransactionToEDON(String txnId, StringBuffer cardNo) throws Exception {
        int count = 0;
        try {
            count = backendJdbcTemplate.update(queryParametersList.getCommon_updateTransactionToEDON(),
                    statusList.getEOD_DONE_STATUS(), txnId);
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
        return count;
    }

    @Override
    public StringBuffer getNewCardNumber(StringBuffer oldCardNumber) throws Exception {
        StringBuffer cardNumber = oldCardNumber;

        try {
            cardNumber = backendJdbcTemplate.queryForObject(queryParametersList.getCommon_getNewCardNumber(),
                    StringBuffer.class,
                    statusList.getCARD_REPLACED_STATUS(),
                    statusList.getCARD_PRODUCT_CHANGE_STATUS(),
                    oldCardNumber.toString());

        } catch (EmptyResultDataAccessException e) {
            cardNumber = oldCardNumber;

        } catch (Exception e) {
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return cardNumber;
    }

    @Override
    public void updateCardOtb(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            count = onlineJdbcTemplate.update(queryParametersList.getCommon_updateCardOtb(),
                    cardBean.getOtbcredit(),
                    cardBean.getOtbcash(),
                    cardBean.getTmpcredit(),
                    cardBean.getTmpcash(),
                    Configurations.EOD_USER,
                    cardBean.getCardnumber().toString()
            );

            // if (Configurations.ONLINE_LOG_LEVEL == 1) {
            //Only for troubleshoot
            infoLogger.info("================ updateCardOtb ===================" + Configurations.EOD_ID);
            //infoLogger.info(query);
            infoLogger.info(Double.toString(cardBean.getOtbcredit()));
            infoLogger.info(Double.toString(cardBean.getOtbcash()));
            infoLogger.info(Double.toString(cardBean.getTmpcredit()));
            infoLogger.info(Double.toString(cardBean.getTmpcash()));
            infoLogger.info(Configurations.EOD_USER);
            infoLogger.info(CommonMethods.cardNumberMask(cardBean.getCardnumber()));
            infoLogger.info("================ updateCardOtb END ===================");
            //}
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
    }

    @Override
    public void updateAccountOtb(OtbBean otbBean) throws Exception {
        int count = 0;
        try {
            count = onlineJdbcTemplate.update(queryParametersList.getCommon_updateAccountOtb(),
                    otbBean.getOtbcredit(),
                    otbBean.getOtbcash(),
                    otbBean.getAccountnumber()
            );

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateAccountOtb ===================" + Configurations.EOD_ID);
                infoLogger.info(queryParametersList.getCommon_updateAccountOtb());
                infoLogger.info(Double.toString(otbBean.getOtbcredit()));
                infoLogger.info(Double.toString(otbBean.getOtbcash()));
                infoLogger.info(otbBean.getAccountnumber());
                infoLogger.info("================ updateAccountOtb END ===================");
            }
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
    }

    @Override
    public void updateCustomerOtb(OtbBean bean) throws Exception {
        int count = 0;
        try {
            count = onlineJdbcTemplate.update(queryParametersList.getCommon_updateCustomerOtb(),
                    bean.getOtbcredit(),
                    bean.getOtbcash(),
                    bean.getCustomerid()
            );

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateCustomerOtb ===================" + Configurations.EOD_ID);
                infoLogger.info(queryParametersList.getCommon_updateCustomerOtb());
                infoLogger.info(Double.toString(bean.getOtbcredit()));
                infoLogger.info(Double.toString(bean.getOtbcash()));
                infoLogger.info(bean.getCustomerid());
                infoLogger.info("================ updateCustomerOtb END ===================");
            }
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
    }

    @Override
    public double getTotalPaymentSinceLastDue(String accNo, Date eodDate, Date dueDate) throws Exception {
        double payments = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "SELECT COALESCE(SUM(TRANSACTIONAMOUNT),0) AS TOTAL FROM EODTRANSACTION WHERE ACCOUNTNO = ? AND TRANSACTIONTYPE IN (?,?,?,?) AND TRUNC(SETTLEMENTDATE) > TO_DATE(?, 'DD-MM-YY') AND TRUNC(SETTLEMENTDATE) <= TO_DATE(?, 'DD-MM-YY') AND STATUS NOT IN (?)";

            payments = backendJdbcTemplate.queryForObject(sql, Double.class,
                    accNo,
                    Configurations.TXN_TYPE_PAYMENT,
                    Configurations.TXN_TYPE_REVERSAL,
                    Configurations.TXN_TYPE_REFUND,
                    Configurations.TXN_TYPE_MVISA_REFUND,
                    sdf.format(dueDate),
                    sdf.format(eodDate),
                    statusList.getCHEQUE_RETURN_STATUS()
            );
        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            errorLogger.error("Get Total Payment Since LastDue Error" + e);
            throw e;
        }
        return payments;
    }

    @Override
    public HashMap<String, Double> getDueAmountList(StringBuffer cardNumber) throws Exception {
        HashMap<String, Double> dueAmountList = new HashMap<String, Double>();
        String sql = "SELECT NVL(M1,0) AS M1 , NVL(M2,0) AS M2 , NVL(M3,0) AS M3 , NVL(M4,0) AS M4 , NVL(M5,0) AS M5 , NVL(M6,0) AS M6, NVL(M7,0) AS M7 , NVL(M8,0) AS M8 , NVL(M9,0) AS M9,NVL(M10,0) AS M10 , NVL(M11,0) AS M11 , NVL(M12,0) AS M12 FROM MINIMUMPAYMENT WHERE CARDNO = ?";

        try {
            backendJdbcTemplate.query(sql,
                    (ResultSet result) -> {
                        while (result.next()) {
                            for (int i = 1; i <= 12; i++) {
                                dueAmountList.put("M" + i, result.getDouble("M" + i));
                            }
                        }
                        return dueAmountList;

                    }, cardNumber.toString()
            );
        } catch (Exception e) {
            infoLogger.error("Exception occurred for cardNumber " + CommonMethods.cardNumberMask(cardNumber), e);
            throw e;
        }
        return dueAmountList;
    }

    @Override
    public DelinquentAccountBean setDelinquentAccountDetails(StringBuffer cardNo) throws Exception {
        DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        try {
            String sql = "SELECT C.CARDCATEGORYCODE, C.NAMEONCARD, C.IDTYPE, C.IDNUMBER, CAC.ACCOUNTNO, CA.STATUS, CAC.CUSTOMERID, B.STATEMENTENDDATE, B.DUEDATE,  B.MINAMOUNT, NVL(DA.NDIA,0) AS NDIA  FROM CARD C, CARDACCOUNTCUSTOMER CAC, BILLINGLASTSTATEMENTSUMMARY B, CARDACCOUNT CA LEFT JOIN DELINQUENTACCOUNT DA ON DA.ACCOUNTNO = CA.ACCOUNTNO  WHERE CAC.CARDNUMBER = C.CARDNUMBER AND CA.CARDNUMBER = C.MAINCARDNO AND C.CARDNUMBER = C.MAINCARDNO AND B.CARDNO = C.MAINCARDNO  AND C.MAINCARDNO = ? ";

            delinquentAccountBean = backendJdbcTemplate.queryForObject(sql,
                    new RowMapper<>() {
                        @Override
                        public DelinquentAccountBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            DelinquentAccountBean delinquentBean = new DelinquentAccountBean();
                            delinquentBean.setCardCategory(rs.getString("CARDCATEGORYCODE"));
                            delinquentBean.setAccNo(rs.getString("ACCOUNTNO"));
                            delinquentBean.setCif(rs.getString("CUSTOMERID"));
                            delinquentBean.setLastStatementDate(rs.getDate("STATEMENTENDDATE"));
                            delinquentBean.setDueDate(rs.getDate("DUEDATE"));
                            delinquentBean.setDueAmount(rs.getString("MINAMOUNT"));
                            delinquentBean.setNameOnCard(rs.getString("NAMEONCARD"));
                            delinquentBean.setIdNumber(rs.getString("IDNUMBER"));
                            delinquentBean.setIdType(rs.getString("IDTYPE"));
                            delinquentBean.setCif(rs.getString("CUSTOMERID"));
                            delinquentBean.setAccStatus(rs.getString("STATUS"));
                            delinquentBean.setNDIA(rs.getInt("NDIA"));
                            return delinquentBean;
                        }
                    }, cardNo.toString());

            if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_MAIN)
                    || delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY)
                    || delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {

                sql = "SELECT NAMEWITHINITIAL ,CONTACTNO ,EMAIL FROM CARDMAINCUSTOMERDETAIL WHERE CUSTOMERID = ?";

                delinquentAccountBean = backendJdbcTemplate.queryForObject(sql,
                        new RowMapper<>() {
                            @Override
                            public DelinquentAccountBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                                DelinquentAccountBean delinquentBean = new DelinquentAccountBean();
                                delinquentBean.setNameInFull(rs.getString("NAMEWITHINITIAL"));
                                delinquentBean.setContactNo(rs.getString("CONTACTNO"));
                                delinquentBean.setEmail(rs.getString("EMAIL"));
                                return delinquentBean;
                            }
                        }, delinquentAccountBean.getCif());

            } else if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD)) {
                sql = "SELECT CUSTOMERNAME ,CONTACTNO ,EMAIL FROM CARDFDCUSTOMERDETAIL WHERE CUSTOMERID = ?";

                delinquentAccountBean = (DelinquentAccountBean) backendJdbcTemplate.query(sql,
                        new RowMapper<>() {
                            @Override
                            public DelinquentAccountBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                                DelinquentAccountBean delinquentBean = new DelinquentAccountBean();
                                delinquentBean.setNameInFull(rs.getString("CUSTOMERNAME"));
                                delinquentBean.setContactNo(rs.getString("CONTACTNO"));
                                delinquentBean.setEmail(rs.getString("EMAIL"));
                                return delinquentBean;
                            }
                        }, delinquentAccountBean.getCif());

            } else if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                sql = "SELECT NAMEOFTHECOMPANY ,CONTACTNUMBERSLAND ,CONTACTEMAIL FROM CARDESTCUSTOMERDETAILS WHERE CUSTOMERID = ?";
                delinquentAccountBean = (DelinquentAccountBean) backendJdbcTemplate.query(sql,
                        new RowMapper<>() {
                            @Override
                            public DelinquentAccountBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                                DelinquentAccountBean delinquentBean = new DelinquentAccountBean();
                                delinquentBean.setNameInFull(rs.getString("NAMEOFTHECOMPANY"));
                                delinquentBean.setContactNo(rs.getString("CONTACTNUMBERSLAND"));
                                delinquentBean.setEmail(rs.getString("CONTACTEMAIL"));
                                return delinquentBean;
                            }
                        }, delinquentAccountBean.getCif());
            }

        } catch (Exception e) {
            throw e;
        }
        return delinquentAccountBean;
    }

    @Override
    public int insertToRECPAYMENTFILEINVALID(String fileid, BigDecimal linenumber, String errorMsg) {
        int count = 0;

        try {

            String query = "INSERT INTO RECPAYMENTFILEINVALID (FILEID,EODID,LINENUMBER,ERRORDESC,CREATEDTIME,LASTUPDATEDUSER,LASTUPDATEDDATE) VALUES (?,?,?,?,SYSDATE,?,SYSDATE)";

            count = backendJdbcTemplate.update(query,
                    fileid,
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

    @Override
    public int insertToRECATMFILEINVALID(String fileid, BigDecimal linenumber, String errorMsg) throws Exception {
        int count = 0;

        try {

            String query = "INSERT INTO RECATMFILEINVALID (FILEID,EODID,LINENUMBER,ERRORDESC,CREATEDTIME,LASTUPDATEDUSER,LASTUPDATEDDATE) VALUES (?,?,?,?,SYSDATE,?,SYSDATE)";

            count = backendJdbcTemplate.update(query,
                    fileid,
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

    @Override
    public int updateEODPAYMENTFILE(String fileid) throws Exception {
        int count = 0;

        try {

            String query = "UPDATE EODPAYMENTFILE SET EODID = ?, STARTTIME = SYSDATE, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,
                    Configurations.EOD_ID,
                    Configurations.EOD_USER,
                    fileid
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODPAYMENTFILE(int noofrecords, String status, String fileid) throws Exception {
        int count = 0;

        try {

            String query = "UPDATE EODPAYMENTFILE SET ENDTIME = SYSDATE, STATUS =?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE, NOOFRECORDS =? WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_ID,
                    String.valueOf(noofrecords),
                    fileid
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODATMFILE(String fileid) throws Exception {
        int count = 0;

        try {

            String query = "UPDATE EODATMFILE SET EODID = ?, STARTTIME = SYSDATE, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,
                    Configurations.EOD_ID,
                    Configurations.EOD_USER,
                    fileid
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODATMFILE(int noofrecords, String status, String fileid) throws Exception {
        int count = 0;

        try {

            String query = "UPDATE EODATMFILE SET ENDTIME = SYSDATE, STATUS =?, LASTUPDATEDUSER=?, LASTUPDATEDDATE=SYSDATE, NOOFRECORDS =? WHERE FILEID=? ";

            count = backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_ID,
                    String.valueOf(noofrecords),
                    fileid
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public boolean isErrorProcess(int ProcessID) throws Exception {
        boolean isErrorProcess = false;
        int count = 0;
        try {

            String query = "SELECT COUNT(T.STEPID)AS COUNT FROM (SELECT EPF.STEPID,EPF.PROCESSID,EEC.CARDNO,EEC.STATUS FROM EODPROCESSFLOW EPF LEFT JOIN EODERRORCARDS EEC ON EPF.PROCESSID = EEC.ERRORPROCESSID ORDER BY STEPID)T WHERE T.STEPID <= (SELECT max(STEPID) as STEPID FROM EODPROCESSFLOW WHERE PROCESSID = ?) AND T.STATUS = ?";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, ProcessID, Configurations.EOD_PENDING_STATUS);
            if (count > 0) {
                isErrorProcess = true;
            }

        } catch (Exception e) {
            throw e;
        }

        return isErrorProcess;
    }

    @Override
    public boolean isProcessCompletlyFail(int ProcessID) throws Exception {
        boolean isProcessCompletlyFail = false;

        try {
            String query = "SELECT ISPROCESSFAIL FROM EODERRORCARDS WHERE  ERRORPROCESSID = ? and STATUS = ? ";

            isProcessCompletlyFail = Objects.requireNonNull(backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        boolean tempIsProcessCompletlyFail = false;
                        while (rs.next()) {
                            if (rs.getInt("ISPROCESSFAIL") == 1) {
                                tempIsProcessCompletlyFail = true;
                            }
                        }
                        return tempIsProcessCompletlyFail;
                    },
                    ProcessID,
                    statusList.getEOD_PENDING_STATUS()
            ));
        } catch (Exception e) {
            throw e;
        }
        return isProcessCompletlyFail;
    }

    @Override
    public CardAccountCustomerBean getCardAccountCustomer(StringBuffer cardNo) throws Exception {
        CardAccountCustomerBean cardBean = null;

        try {

            String query = "select CC.CUSTOMERID,ca.accountno,c.cardnumber, c.maincardno from cardaccount ca,card c,CARDCUSTOMER cc,CARDACCOUNTCUSTOMER CAC where C.CARDNUMBER = CAC.CARDNUMBER and CA.ACCOUNTNO = CAC.ACCOUNTNO and  CC.CUSTOMERID = CAC.CUSTOMERID and c.cardnumber =?";

            cardBean = (CardAccountCustomerBean) backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        CardAccountCustomerBean bean = new CardAccountCustomerBean();
                        bean.setAccountNumber(rs.getString("ACCOUNTNO"));
                        bean.setCustomerId(rs.getString("CUSTOMERID"));
                        bean.setMaincardNumber(new StringBuffer(rs.getString("maincardno")));
                        return bean;
                    }),
                    cardNo.toString()
            );
        } catch (Exception e) {
            throw e;
        }
        return cardBean;
    }

    @Override
    public void insertErrorEODCard(ErrorCardBean eBean) throws Exception {
        int count = 0;
        CardAccountCustomerBean cardBean = null;
        List<StringBuffer> cardList = null;

        try {

            if (eBean.getCardAccount().equals(CardAccount.ACCOUNT)) {
                //The cardno is replaced with the account no.
                cardList = getAllTheCardsForAccount(eBean.getCardNo());
            } else {
                cardList = new ArrayList<>(Arrays.asList(eBean.getCardNo()));
            }
            cardBean = getCardAccountCustomer(cardList.get(0));

            for (int i = 0; i < cardList.size(); i++) {

                String sql = "INSERT INTO EODERRORCARDS (EODID,CARDNO,ACCOUNTNO,CUSTOMERID,ERRORPROCESSID,ERRORPROCESSNAME,ERRORREMARK,EODDATE,STATUS,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,ISPROCESSFAIL,PROCESSSTEPID) VALUES (?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?)";

                backendJdbcTemplate.update(sql,
                        Configurations.ERROR_EOD_ID,
                        cardList.get(i).toString(),
                        cardBean.getAccountNumber(),
                        cardBean.getCustomerId(),
                        eBean.getProcessId(),
                        eBean.getProcessName(),
                        eBean.getRemark(),
                        DateUtil.getSqldate(Configurations.EOD_DATE),
                        statusList.EOD_PENDING_STATUS,
                        Configurations.EOD_USER,
                        eBean.getIsProcessFails(),
                        Configurations.PROCESS_STEP_ID
                );

            }

        } catch (Exception e) {
            throw e;
        }
    }

    public synchronized List<StringBuffer> getAllTheCardsForAccount(StringBuffer accNo) throws Exception {
        ArrayList<StringBuffer> cardList = new ArrayList<>();

        try {
            String sql = "SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA,CARD CD WHERE CA.cardnumber = CD.CARDNUMBER AND CD.CARDSTATUS  NOT IN (?,?) AND CA.ACCOUNTNO  = ? ORDER BY CA.ACCOUNTNO,  CASE WHEN cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? THEN 1 WHEN cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? THEN 2 ELSE 3 END,   CD.CARDNUMBER";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            cardList.add(new StringBuffer(rs.getString("CARDNUMBER")));
                        }
                        return cardList;
                    },
                    statusList.CARD_PRODUCT_CHANGE_STATUS,
                    statusList.CARD_REPLACED_STATUS,
                    accNo.toString(),
                    Configurations.CARD_CATEGORY_MAIN,
                    Configurations.CARD_CATEGORY_ESTABLISHMENT,
                    Configurations.CARD_CATEGORY_FD,
                    Configurations.CARD_CATEGORY_AFFINITY,
                    Configurations.CARD_CATEGORY_CO_BRANDED,
                    Configurations.CARD_CATEGORY_SUPPLEMENTORY,
                    Configurations.CARD_CATEGORY_CORPORATE,
                    Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY,
                    Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY,
                    Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY
            );

        } catch (Exception e) {
            throw e;
        }

        return cardList;
    }

    @Override
    public int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception {
        int count = 0;
        boolean forward = false;
        String query;

        try {
            forward = this.checkFeeExistForCard(cardNumber, feeCode);
            if (forward) {
                boolean isFeeUpdateRequired = this.getFeeCode(cardNumber, feeCode);
                if (isFeeUpdateRequired) {
                    query = "UPDATE CARDFEECOUNT SET FEECOUNT = FEECOUNT + 1,"
                            + " CASHAMOUNT=CASHAMOUNT+?, LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE, STATUS =?"
                            + " WHERE CARDNUMBER = ? AND FEECODE = ? ";

                    count = backendJdbcTemplate.update(query, cashAmount, Configurations.EOD_USER,
                            Configurations.EOD_PENDING_STATUS,
                            cardNumber.toString(),
                            feeCode);
                } else {
                    query = "INSERT INTO CARDFEECOUNT (CARDNUMBER,FEECODE,FEECOUNT,CASHAMOUNT,STATUS,CREATEDDATE,LASTUPDATEDTIME,LASTUPDATEDUSER) VALUES (?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),SYSDATE,?)";
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

                    count = backendJdbcTemplate.update(query, cardNumber.toString(), feeCode,
                            1,
                            cashAmount,
                            Configurations.EOD_PENDING_STATUS,
                            sdf.format(Configurations.EOD_DATE),
                            Configurations.EOD_USER);
                }

            }
        } catch (Exception e) {
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }

        return count;
    }

    @Override
    public boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;
        try {
            String sql = "SELECT C.CARDNUMBER, "
                    + "  C.FEEPROFILECODE, "
                    + "  FPF.FEECODE "
                    + "FROM CARD C "
                    + "INNER JOIN FEEPROFILEFEE FPF "
                    + "ON C.FEEPROFILECODE  = FPF.FEEPROFILECODE "
                    + "WHERE C.CARDNUMBER   = ? "
                    + "AND FPF.FEECODE NOT IN "
                    + "  (SELECT PFPF.FEECODE "
                    + "  FROM CARD C "
                    + "  INNER JOIN PROMOFEEPROFILE PFP "
                    + "  ON C.PROMOFEEPROFILECODE = PFP.PROMOFEEPROFILECODE "
                    + "  INNER JOIN PROMOFEEPROFILEFEE PFPF "
                    + "  ON C.PROMOFEEPROFILECODE = PFPF.PROMOFEEPROFILECODE "
                    + "  WHERE C.CARDNUMBER       = ? "
                    + "  AND STATUS              <> ? "
                    + "  ) "
                    + "AND FPF.FEECODE = ?";

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(sql, countCallback, cardNumber.toString(), cardNumber.toString(), statusList.getFEE_PROMOTION_PROFILE_EXPIRE(), feeCode);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                forward = true;
            }

        } catch (Exception e) {
            infoLogger.error("Check Fee Exist For Card Exception ", e);
            throw e;
        }
        return forward;
    }

    @Override
    public boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;
        int count = 0;
        try {
            //String sql = "SELECT C.CARDNUMBER, C.STATUS, C.FEECOUNT FROM CARDFEECOUNT C WHERE C.CARDNUMBER = ? AND C.FEECODE = ? ";
            String sql = "SELECT  C.FEECOUNT FROM CARDFEECOUNT C WHERE C.CARDNUMBER = ? AND C.FEECODE = ? ";

            count = backendJdbcTemplate.queryForObject(sql, Integer.class, cardNumber.toString(), feeCode);


            if (count >= 0) {
                forward = true;

                //If it is not late payment fee which has already been incremented
                if (feeCode.equals(Configurations.LATE_PAYMENT_FEE) || feeCode.equals(Configurations.ANNUAL_FEE)) {
                    forward = false;
                }
            }
        } catch (EmptyResultDataAccessException e) {
            return false;

        } catch (Exception e) {
            infoLogger.error("Exception ", e);
            throw e;
        }
        return forward;
    }

    @Override
    public CardBean getCardDetails(StringBuffer cardNo) throws Exception {
        CardBean bean = null;
        StringBuffer replacedCard = null;

        String query = "SELECT ca.nextbillingdate,C.MAINCARDNO,c.createdtime as createdtime,c.activationdate,ca.accountno as ACCOUNTO,bs.duedate as duedate,ip.interestrate, EXPIERYDATE,SERVICECODE,PRODUCTIONMODE,CARDSTATUS,c.OTBCASH,"
                + "c.OTBCREDIT,c.CARDTYPE,CARDKEYID FROM CARD c left join cardaccount ca on c.maincardno=ca.cardnumber "
                + "left join interestprofile ip on ca.interestprofilecode=ip.interestprofilecode "
                + "left join billingstatement bs on bs.cardno=ca.cardnumber "
                + "WHERE c.CARDNUMBER = ?";

        try {
            replacedCard = getNewCardNumber(cardNo);
            if (replacedCard != null) {
                cardNo = replacedCard;
            }

            bean = backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        CardBean cardBean = new CardBean();
                        while (result.next()) {
                            cardBean.setServiceCode(result.getString("SERVICECODE"));
                            cardBean.setExpiryDate(result.getString("EXPIERYDATE"));
                            cardBean.setProductionMode(result.getString("PRODUCTIONMODE"));
                            cardBean.setCardStatus(result.getString("CARDSTATUS"));
                            cardBean.setOtbCash(result.getDouble("OTBCASH"));
                            cardBean.setOtbCredit(result.getDouble("OTBCREDIT"));
                            cardBean.setCardtype(result.getString("CARDTYPE"));
                            cardBean.setCardKeyId(result.getString("CARDKEYID"));
                            cardBean.setNextBillingDate(result.getDate("NEXTBILLINGDATE"));
                            cardBean.setCreatedDate(result.getDate("createdtime"));
                            cardBean.setInterestrate(result.getDouble("INTERESTRATE"));
                            cardBean.setMainCardNo(new StringBuffer(result.getString("MAINCARDNO")));
                            String accNo = result.getString("ACCOUNTO");
                            Date dueDate = result.getDate("DUEDATE");
                            cardBean.setDueDate(dueDate);
                            cardBean.setActivateDate(result.getDate("activationdate"));

                        }
                        return cardBean;
                    },
                    cardNo.toString()
            );

        } catch (Exception e) {
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return bean;
    }

    @Override
    public boolean getTriggerEligibleStatus(String triggerPoint, String smsOrEmail) throws Exception {
        boolean status = false;
        try {
            String sql = "SELECT B.BUCKETID FROM ALLOCATIONRULEACTION ARA INNER JOIN BUCKETACTION B ON ARA.RULECODE=B.ACTIONRULE WHERE ARA.ACTIONMESSAGECODE IN(SELECT MESSAGECODE FROM ALLOCATIONMESSAGE WHERE TRIGGERPOINT=? AND MESSAGETYPE =?) ";

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(sql, countCallback, triggerPoint, smsOrEmail);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                status = true;
            }

        } catch (Exception e) {
            infoLogger.error("Exception ", e);
            throw e;
        }
        return status;
    }

    @Override
    public void insertIntoDelinquentHistory(StringBuffer cardNumber, String accountNo, String remark) throws Exception {
        try {
            String sql = "INSERT INTO DELINQUENTHISTORY (CARDNUMBER,  ACCOUNTNO,REMARK,  LASTUPDATEDUSER,"
                    + "   LASTUPDATEDTIME,   CREATEDTIME ) VALUES (?,?,?,?,TO_DATE(SYSDATE, 'DD-MM-YY') ,TO_DATE(SYSDATE, 'DD-MM-YY'))";

            backendJdbcTemplate.update(sql, cardNumber.toString(), accountNo, remark, Configurations.EOD_USER);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public double getPaymentAmount(String accNo, int statementDayEODID, String initial_status) throws Exception {
        double paymentAmount = 0;
        try {
            String query = "SELECT SUM(TRANSACTIONAMOUNT) AS TOTALPAY "
                    + "FROM EODTRANSACTION "
                    + "WHERE TRANSACTIONTYPE =? "
                    + "AND EODID > ? AND EODID <= ? AND STATUS IN(?,?) "
                    + "AND  ACCOUNTNO IN (?) ";

            paymentAmount = backendJdbcTemplate.queryForObject(query, Double.class, Configurations.TXN_TYPE_PAYMENT
                    , statementDayEODID
                    , Configurations.EOD_ID
                    , initial_status
                    , Configurations.EOD_DONE_STATUS
                    , accNo);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return paymentAmount;
    }

    @Override
    public String getCardAssociationFromCardBin(String cardBin) throws Exception {
        String cardAssociation = null;
        try {
            String query = "SELECT CAC.CARDASSOCIATION AS CARDASSOCIATION FROM CARDASSOCIATIONCHANNEL CAC INNER JOIN CARDBIN CB ON CAC.CHANNELID=CB.CHANNELID WHERE CB.BIN=?";
            cardAssociation = backendJdbcTemplate.queryForObject(query, String.class, cardBin);
        } catch (EmptyResultDataAccessException e) {
            infoLogger.info("--no result found--");
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
        }
        return cardAssociation;
    }

    @Override
    public int insertInToEODTransaction(StringBuffer cardNmber, String accountNo, String txnAmount, String currencyType, String settlementDate, String txnDate, String txnType, String txnId, String description, String CrDr, String adjStatus, String cardAssociation) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        String query = null;
        Object[] param = null;
        try {
            if (adjStatus == null) {
                query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,TRANSACTIONAMOUNT,CURRENCYTYPE,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,STATUS,TRANSACTIONDESCRIPTION,CRDR,CARDASSOCIATION) VALUES (?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),TO_DATE(?, 'DD-MM-YY'),?,?,?,SYSDATE,SYSDATE,?,?,?,?)";
                param = new Object[]{Configurations.EOD_ID, cardNmber.toString(), accountNo, txnAmount, currencyType, sdf.format(Configurations.EOD_DATE), sdf.format(Configurations.EOD_DATE), txnType, txnId, Configurations.EOD_USER, statusList.getINITIAL_STATUS(), description, CrDr, cardAssociation};
            } else {
                query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,TRANSACTIONAMOUNT,CURRENCYTYPE,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,STATUS,TRANSACTIONDESCRIPTION,CRDR,CARDASSOCIATION,ADJUSTMENTSTATUS,ONLYVISAFALSE) VALUES (?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),TO_DATE(?, 'DD-MM-YY'),?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?)";
                param = new Object[]{Configurations.EOD_ID, cardNmber.toString(), accountNo, txnAmount, currencyType, sdf.format(Configurations.EOD_DATE), sdf.format(Configurations.EOD_DATE), txnType, txnId, Configurations.EOD_USER, statusList.getINITIAL_STATUS(), description, CrDr, cardAssociation, adjStatus, 1};
            }
            count = backendJdbcTemplate.update(query, param);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        try {
            String query = "INSERT INTO EODGLACCOUNT (EODID,GLDATE,CARDNO,GLTYPE,AMOUNT,CRDR,PAYMENTTYPE) VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?,?)";
            count = backendJdbcTemplate.update(query, eodID, sdf.format(glDate), cardNo.toString(), glType, String.valueOf(amount), cdStatus, payType);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public Boolean updateEODProcessCount(String uniqueId) throws Exception {
        try {
            int count = 0;
            String query = "UPDATE EODPROCESSCOUNT SET COMPLETEDCOUNT = (COMPLETEDCOUNT + 1) WHERE THREADID = ?";

            count = backendJdbcTemplate.update(query, uniqueId);

            if (count == 1) {
                return true;
            }
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    @Override
    public int insertOutputFiles(EodOuputFileBean outputFileBean, String fileType) throws Exception {
        int count = 0;
        String insertToTable;
        try {
            switch (fileType) {
                case "GL":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "GL", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "RB36":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "RB36", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "OUTCTF":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "OUTCTF", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "CUSTOMERCSV":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime,subfolder)VALUES("
                            + "?,?,?,?,sysdate,?)";

                    count = backendJdbcTemplate.update(insertToTable, "CUSTOMERCSV", outputFileBean.getFileName(),
                            Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords(), outputFileBean.getSubFolder());
                    break;

                case "OUTMASTER":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "OUTMASTER", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "MERCHANTGL":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "MERCHANTGL", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "CASHBACK":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "CASHBACK", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "AUTOSETTLEMENT":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "AUTOSETTLEMENT", outputFileBean.getFileName(), Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords());
                    break;

                case "EODLOGS":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime,subfolder)VALUES("
                            + "?,?,?,?,sysdate,?)";

                    count = backendJdbcTemplate.update(insertToTable, "EODLOGS", outputFileBean.getFileName(),
                            Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords(), outputFileBean.getSubFolder());
                    break;

                case "MERCHANTPAYMENTDIRECT":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,subfolder,createdtime)VALUES("
                            + "?,?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "MERCHANTPAYMENTDIRECT", outputFileBean.getFileName(),
                            Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords(), outputFileBean.getSubFolder());
                    break;

                case "MERCHANTPAYMENTSLIP":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,subfolder,createdtime)VALUES("
                            + "?,?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "MERCHANTPAYMENTSLIP", outputFileBean.getFileName(),
                            Configurations.ERROR_EOD_ID, outputFileBean.getNoOfRecords(), outputFileBean.getSubFolder());
                    break;
            }
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    @Transactional("backendDb")
    public boolean checkForValidCard(StringBuffer cardNumber) throws Exception {
        boolean status = false;
        int recordCount = 0;
        try {
            String query = "SELECT COUNT(CARDNUMBER) AS RECORDCOUNT FROM CARD WHERE CARDNUMBER=? ";
            recordCount = backendJdbcTemplate.queryForObject(query, Integer.class, cardNumber);
            if (recordCount > 0) {
                status = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    @Transactional("backendDb")
    public String getLinuxFilePath(String fileCode) throws Exception {
        String filePath = "";

        try {
            String query = "SELECT FILEPATHLINUX FROM EODFILEINFO WHERE FILETYPE = ?";

            filePath = backendJdbcTemplate.queryForObject(query, String.class, fileCode);

        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
            throw e;
        }
        return filePath;
    }

    @Override
    @Transactional("backendDb")
    public String getWindowsFilePath(String fileCode) throws Exception {
        String filePath = "";

        try {
            String query = "SELECT FILEPATHWINDOWS FROM EODFILEINFO WHERE FILETYPE = ?";

            filePath = backendJdbcTemplate.queryForObject(query, String.class, fileCode);

        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
            throw e;
        }
        return filePath;
    }

    @Override
    @Transactional("backendDb")
    public ArrayList<String> getNameFields(String fileType) throws Exception {
        ArrayList<String> nameFieldList = new ArrayList<String>();

        try {

            String query = "SELECT FILENAMEPRIFIX, FILENAMEPOSTFIX, FILEEXTENTION "
                    + "FROM EODFILETYPE "
                    + "WHERE FILETYPECODE=? ";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            nameFieldList.add(rs.getString("FILENAMEPRIFIX"));
                            nameFieldList.add(rs.getString("FILENAMEPOSTFIX"));
                            nameFieldList.add(rs.getString("FILEEXTENTION"));
                        }
                        return nameFieldList;
                    },
                    fileType);

        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
            throw e;
        }

        return nameFieldList;
    }

    @Override
    public void updateFileGenProcessSummery(String fileName, int eodId, String status, int processId, int processSuccessCount, int processFaildCount, String progress) {
        try {
            String query = "UPDATE EODPROCESSSUMMERY SET ENDTIME = SYSDATE , STATUS = ?,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ? AND PROCESSID = ?";

            backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    eodId,
                    processId
            );

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int getCurrentEodId(String status1, String status2) throws Exception {
        return 0;
    }

    @Override
    public String getEodStatusByEodID(int eodId) throws Exception {
        return null;
    }


}
