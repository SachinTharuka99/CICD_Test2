/**
 * Created By Lahiru Sandaruwan
 * Date : 10/18/2022
 * Time : 2:04 PM
 * Project Name : ecms_eod_engine
 * Topic :
 */

package com.epic.cms.repository;

import com.epic.cms.dao.InstallmentPaymentDao;
import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.InstallmentBean;
import com.epic.cms.model.bean.ManualNpRequestBean;
import com.epic.cms.model.rowmapper.DelinquentAccountRowMapper;
import com.epic.cms.model.rowmapper.InstallmentRowMapper;
import com.epic.cms.model.rowmapper.ManualNpRequestRowMapper;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class InstallmentPaymentRepo implements InstallmentPaymentDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Override
    public List<ManualNpRequestBean> getManualNpRequestDetails(int reqType, String status) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        List<ManualNpRequestBean> manualNpRequestList = new ArrayList<>();
        try {
            String query = "SELECT REQUESTID, ACCOUNTNO, ACCSTATUS, MAINCARDNUMBER, NDIA, REQUESTTYPE, STATUS FROM MANUALNPREQUEST WHERE REQUESTTYPE = ? AND STATUS = ? AND TRUNC(CREATEDTIME) <= TO_DATE(?,'DD-MM-YY')";
            manualNpRequestList = backendJdbcTemplate.query(query, new ManualNpRequestRowMapper(), reqType, status, sdf.format(Configurations.EOD_DATE));

        } catch (Exception e) {
            throw e;
        }
        return manualNpRequestList;
    }

    @Override
    public int updateEasyPaymentRequestToAccelerate(String accNo, String tableName) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int flag = 0;
        try {
            String query = "UPDATE " + tableName + " SET INSTALLMENTAMOUNT=REMAININGCOUNT*INSTALLMENTAMOUNT, REMAININGCOUNT=?, LASTUPDATEDTIME=SYSDATE, NEXTTXNDATE = TO_DATE(?, 'DD-MM-YY'), LASTUPDATEDUSER =?, ACCELERATEDSTATUS =? WHERE CARDNUMBER IN (SELECT CARDNUMBER FROM CARDACCOUNTCUSTOMER WHERE ACCOUNTNO = ?) AND ACCELERATEDSTATUS <> ? AND STATUS = ?";
            flag = backendJdbcTemplate.update(query, 1, sdf.format(Configurations.EOD_DATE), Configurations.EOD_USER, statusList.getSTATUS_YES(), accNo, statusList.getSTATUS_YES(), statusList.getCOMMON_REQUEST_ACCEPTED());
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public List<DelinquentAccountBean> getDelinquentAccounts() throws Exception {
        List<DelinquentAccountBean> delinquentCardList = new ArrayList<>();
        try {
            String query = "SELECT DL.* FROM DELINQUENTACCOUNT DL,CARD C WHERE C.CARDNUMBER=DL.CARDNUMBER AND DL.DELINQSTATUS NOT IN (?,?) AND C.CARDSTATUS <>? AND LASTUPDATEDEODID <> ? ";// Test Script AND DL.ACCOUNTNO = '212130002646'
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                query += " AND DL.ACCOUNTNO NOT IN (SELECT EC.ACCOUNTNO FROM EODERRORCARDS EC WHERE EC.STATUS= ?)";
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query += " AND DL.ACCOUNTNO IN (SELECT EC.ACCOUNTNO FROM EODERRORCARDS EC WHERE EC.STATUS= ? AND EODID < ? AND PROCESSSTEPID <= ?)";
            }
            Object[] param = null;
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                param = new Object[]{statusList.getTO_RESOLVE_STATUS(), statusList.getONLY_MANUAL_NP_STATUS(), statusList.getCARD_CLOSED_STATUS(), Configurations.EOD_ID, statusList.getEOD_PENDING_STATUS()};
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                param = new Object[]{statusList.getTO_RESOLVE_STATUS(), statusList.getONLY_MANUAL_NP_STATUS(), statusList.getCARD_CLOSED_STATUS(), Configurations.EOD_ID, statusList.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID};
            }
            delinquentCardList = backendJdbcTemplate.query(query, new DelinquentAccountRowMapper(), param);
        } catch (Exception e) {
            throw e;
        }
        return delinquentCardList;
    }

    @Override
    public double checkForPayment(String accNo, Date eodDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        double payment = 0;
        boolean isPaymentOnCurrentDay = false;
        try {
            String query = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTAL FROM EODTRANSACTION WHERE ACCOUNTNO = ? AND TRANSACTIONTYPE IN (?,?,?,?) AND SETTLEMENTDATE = TO_DATE(?, 'DD-MM-YY') AND STATUS NOT IN (?)";
            payment = backendJdbcTemplate.queryForObject(query, Double.class, accNo, Configurations.TXN_TYPE_PAYMENT, Configurations.TXN_TYPE_REVERSAL, Configurations.TXN_TYPE_REFUND, Configurations.TXN_TYPE_MVISA_REFUND, sdf.format(eodDate), statusList.getCHEQUE_RETURN_STATUS());
            //payment = backendJdbcTemplate.queryForObject(query, Double.class, accNo, "TTC023", "TTC003", "TTC041", "TTC048", sdf.format(eodDate), "CQRT");

        } catch (EmptyResultDataAccessException ex) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return payment;
    }

    @Override
    public String[] getRiskClassOnNdia(int noOfDates) throws Exception {
        String[] newRiskClass = new String[3];
        try {
            String query = "SELECT bucketid, minndia, maxndia FROM bucket WHERE CASE WHEN (SELECT MAX(maxndia) FROM bucket WHERE status = 'ACT') < ? THEN (SELECT MAX(bucketid) FROM bucket WHERE status = 'ACT') ELSE (SELECT bucketid FROM bucket WHERE status = 'ACT' AND minndia <= ? AND maxndia >= ?) END = bucketid";
            backendJdbcTemplate.query(query, (ResultSet result) -> {
                while (result.next()) {
                    String riskClass = result.getString("BUCKETID");
                    String minNdia = result.getString("MINNDIA");

                    if (riskClass.equals(statusList.getRISK_CLASS_NINE())) {
                        newRiskClass[0] = minNdia;
                    } else {
                        newRiskClass[0] = Integer.toString(noOfDates);
                    }

                    newRiskClass[1] = riskClass;
                    newRiskClass[2] = minNdia;
                }
                return newRiskClass;

            }, noOfDates, noOfDates, noOfDates);
        } catch (Exception e) {
            throw e;
        }
        return newRiskClass;
    }

    @Override
    public String getNPRiskClass() throws Exception {
        String npRiskClass = null;
        try {
            String query = "SELECT NVL(NONPERFORMINGRISKCLASS,'4') AS NONPERFORMINGRISKCLASS FROM COMMONCARDPARAMETER";
            npRiskClass = backendJdbcTemplate.queryForObject(query, String.class);
        } catch (Exception e) {
            throw e;
        }
        return npRiskClass;
    }

    @Override
    public String[] getNDIAOnRiskClass(String riskClass) throws Exception {
        String[] bucket = new String[3];
        String query = "SELECT BUCKETID,MINNDIA,MAXNDIA FROM BUCKET WHERE BUCKETID =?";
        try {
            backendJdbcTemplate.query(query, (ResultSet result) -> {
                        while (result.next()) {
                            bucket[0] = result.getString("BUCKETID");
                            bucket[1] = result.getString("MINNDIA");
                            bucket[2] = result.getString("MAXNDIA");
                        }
                        return bucket;
                    }, riskClass
            );
        } catch (Exception e) {
            throw e;
        }
        return bucket;
    }

    @Override
    public double checkLeastMinimumPayment(String accNo) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        double payment = 0;
        try {
            String query = "SELECT M1 FROM MINIMUMPAYMENT WHERE CARDNO IN (SELECT CARDNUMBER FROM CARDACCOUNT WHERE ACCOUNTNO = ?)";
            payment = backendJdbcTemplate.queryForObject(query, Double.class, accNo);
        } catch (EmptyResultDataAccessException e) {
            logManager.logError("--result not found--",errorLogger);
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return payment;
    }

    @Override
    public List<InstallmentBean> getBTOrLOCDetails(String tblName1, String tblName2) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        List<InstallmentBean> beanList = new ArrayList<>();
        try {
            String query = "SELECT  B.CARDNUMBER,B.FIRSTINSTALLMENTAMOUNT,B.CURRINSTALLMENT,B.INSTALLMENTAMOUNT,B.INTERESTORFEETOTALAMOUNT,B.ACCELERATEDSTATUS,B.INTERESTORFEEAMOUNT,B.NEXTTXNDATE, B.REMAININGCOUNT,B.REQUESTID,B.RUNNINGSTATUS,B.TOTALAMOUNT,B.EFECTIVEDATE,B.STATUS,B.TXNID,B.TXNDESCRIPTION,P.DURATION,P.FEEAPPLYINFIRSTMONTH, P.FIRSTMONTHINCLUDE,P.INTERESTRATEORFEE,P.PROCESSINGFEETYPE,d.ACCOUNTNO,B.CURRENCYNUMCODE,B.TRACENO FROM " + tblName1 + " B LEFT JOIN " + tblName2 + " P ON B.PAYMENTPLAN    =P.PAYMENTPLANCODE LEFT JOIN CARDACCOUNTCUSTOMER D ON B.CARDNUMBER = D.CARDNUMBER WHERE( B.STATUS    IN(?) AND B.RUNNINGSTATUS = ? AND d.accountno='212140059710' AND b.cardnumber='4890111940585813' AND B.EFECTIVEDATE <= TO_DATE(?, 'DD-MM-YY')) OR (B.STATUS       IN(?) AND B.RUNNINGSTATUS = ? AND B.NEXTTXNDATE  <= TO_DATE(?, 'DD-MM-YY') AND d.accountno='212140059710' AND b.cardnumber='4890111940585813')";
            beanList = backendJdbcTemplate.query(query, new InstallmentRowMapper(), statusList.getCOMMON_REQUEST_ACCEPTED(), 0, sdf.format(Configurations.EOD_DATE), statusList.getCOMMON_REQUEST_ACCEPTED(), 1, sdf.format(Configurations.EOD_DATE));
        } catch (Exception e) {
            throw e;
        }
        return beanList;
    }

    @Override
    public int insertInToEODTransactionOnlyVisaFalse(StringBuffer cardNumber, String accNo, double txnAmount, String curruncyCode, String test, String test0, String TXN_TYPE_SALE, String txnID, String description, String CrDr, int object, String cardAssociation) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        try {
            String query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,TRANSACTIONAMOUNT,CURRENCYTYPE,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,STATUS,TRANSACTIONDESCRIPTION,CRDR,ONLYVISAFALSE,CARDASSOCIATION) VALUES (?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),TO_DATE(?, 'DD-MM-YY'),?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)";
            count = backendJdbcTemplate.update(query, Configurations.EOD_ID, cardNumber.toString(), accNo, txnAmount, curruncyCode, sdf.format(Configurations.EOD_DATE), sdf.format(Configurations.EOD_DATE), TXN_TYPE_SALE, txnID, Configurations.EOD_USER, statusList.getINITIAL_STATUS(), description, CrDr, object, cardAssociation);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertInToEODTransactionWithoutGL(StringBuffer cardnumber, String accountNo, Double txnAmount, String currencyType, String settlementDate, String txnDate, String txnType, String txnId, String description, String CrDr, int i, String cardAssociation) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        try {
            String query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,TRANSACTIONAMOUNT,CURRENCYTYPE,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,STATUS,TRANSACTIONDESCRIPTION,CRDR,GLSTATUS,CARDASSOCIATION) VALUES (?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),TO_DATE(?, 'DD-MM-YY'),?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)";
            count = backendJdbcTemplate.update(query, Configurations.EOD_ID, cardnumber.toString(), accountNo, txnAmount, currencyType, sdf.format(Configurations.EOD_DATE), sdf.format(Configurations.EOD_DATE), txnType, txnId, Configurations.EOD_USER, statusList.getINITIAL_STATUS(), description, CrDr, i, cardAssociation);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEasyPaymentTableWithFirstInstallment(InstallmentBean easyPaymentBean, String tableName) throws Exception {
        int count = 0;
        Object[] param = null;
        String query = null;
        try {
            java.sql.Date eodDate = CommonMethods.getSqldate(Configurations.EOD_DATE);
            if (easyPaymentBean.getRemainingCount() > 0) {
                int isPaymentNo = easyPaymentBean.getDuration() - easyPaymentBean.getRemainingCount();
                query = "update " + tableName + " set NEXTTXNDATE = ADD_MONTHS(TO_DATE(?, 'YYYY-MM-DD'), ?) ,RUNNINGSTATUS = 1, REMAININGCOUNT = ?,CURRINSTALLMENT=?,TXNDESCRIPTION = ?,TXNID=?,LASTEODUPDATEDDATE=?,FIRSTINSTALLMENTAMOUNT=? where  REQUESTID=?";
                param = new Object[]{easyPaymentBean.getEffectivedate(), isPaymentNo, easyPaymentBean.getRemainingCount(), easyPaymentBean.getCurrentCount(), easyPaymentBean.getTxnDescription(), easyPaymentBean.getTxnID(), eodDate, easyPaymentBean.getInstalmentAmount(), easyPaymentBean.getRequestID()};
            } else {
                query = "update " + tableName + " set STATUS = ? , REMAININGCOUNT = REMAININGCOUNT-1,CURRINSTALLMENT=?,LASTEODUPDATEDDATE=?,FIRSTINSTALLMENTAMOUNT=? where  REQUESTID=?";
                param = new Object[]{statusList.getCOMMON_COMPLETED(), easyPaymentBean.getCurrentCount(), eodDate, easyPaymentBean.getInstalmentAmount(), easyPaymentBean.getRequestID()};
            }
            count = backendJdbcTemplate.update(query, param);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateFeeToEDONInTransactionTable(StringBuffer cardNumber, String traceNumber, String transactionType) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE TRANSACTION SET EODSTATUS=? WHERE CARDNO=? AND TRACENO=? AND BACKENDTXNTYPE=?";
            count = backendJdbcTemplate.update(query, statusList.getEOD_DONE_STATUS(), cardNumber.toString(), traceNumber, transactionType);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEasyPaymentTable(InstallmentBean easyPaymentBean, String tableName) throws Exception {
        int count = 0;
        String query = null;
        Object[] param = null;
        try {
            java.sql.Date eodDate = CommonMethods.getSqldate(Configurations.EOD_DATE);
            //int RemainingCoun = easyPaymentBean.getRemainingCount() - 1;
            if (easyPaymentBean.getRemainingCount() > 0) {
                int isPaymentNo = easyPaymentBean.getDuration() - easyPaymentBean.getRemainingCount();
                param = new Object[]{easyPaymentBean.getEffectivedate(), isPaymentNo, easyPaymentBean.getRemainingCount(), easyPaymentBean.getCurrentCount(), easyPaymentBean.getTxnDescription(), easyPaymentBean.getTxnID(), eodDate, easyPaymentBean.getRequestID()};
                query = "update " + tableName + " set NEXTTXNDATE = ADD_MONTHS(TO_DATE(?, 'YYYY-MM-DD'), ?) ,RUNNINGSTATUS = 1, REMAININGCOUNT = ?,CURRINSTALLMENT=?,TXNDESCRIPTION = ?,TXNID=?,LASTEODUPDATEDDATE=? where  REQUESTID=?";
            } else {
                param = new Object[]{statusList.getCOMMON_COMPLETED(), easyPaymentBean.getCurrentCount(), eodDate, easyPaymentBean.getRequestID()};
                query = "update " + tableName + " set STATUS = ? , REMAININGCOUNT = REMAININGCOUNT-1,CURRINSTALLMENT=?,LASTEODUPDATEDDATE=? where  REQUESTID=?";
            }
            count = backendJdbcTemplate.update(query, param);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public String getEodtxnDescription(String txnID) throws Exception {
        String sql = "select TRANSACTIONDESCRIPTION from EODTRANSACTION where TRANSACTIONID = ? AND TRANSACTIONTYPE NOT IN(?,?)";
        String txtDescription = null;

        try {
            txtDescription = backendJdbcTemplate.queryForObject(sql, String.class, txnID, Configurations.TXN_TYPE_INSTALLMENT, Configurations.TXN_TYPE_REVERSAL_INSTALLMENT);

        } catch (Exception e) {
            throw e;
        }
        return txtDescription;
    }

    @Override
    public List<InstallmentBean> getEasyPaymentDetails() throws Exception {
        ArrayList<InstallmentBean> txnList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "SELECT e.RUNNINGSTATUS,  e.LASTEODTRANSACTIONID,NVL(e.CURRINSTALLMENT,0) AS CURRINSTALLMENT, e.TXNDESCRIPTION, e.INTERESTORFEETOTALAMOUNT, "
                    + " e.ACCEDPTEDDATE, e.REQUESTID, e.EFECTIVEDATE,  NVL(e.NEXTTXNDATE,SYSDATE) AS NEXTTXNDATE,  e.ACCELERATEDSTATUS,"
                    + " d.ACCOUNTNO,  e.CARDNUMBER, e.TXNAMOUNT,e.STATUS,e.INTERESTORFEEAMOUNT, p.DURATION, p.PROCESSINGFEETYPE, p.FEEAPPLYINFIRSTMONTH,"
                    + " p.FIRSTMONTHINCLUDE, e.TXNID, e.INSTALLMENTAMOUNT,e.CURRENCYNUMCODE, e.REMAININGCOUNT, e.TRACENO "
                    + " from EASYPAYMENTREQUEST e"
                    + " LEFT JOIN paymentplan p"
                    + " ON e.paymentplan = p.PAYMENTPLANCODE"
                    + " LEFT JOIN CARDACCOUNTCUSTOMER d "
                    + " ON d.cardnumber     =e.cardnumber "
                    + " where(e.status       = ? AND e.RUNNINGSTATUS = ? "
                    + " AND e.EFECTIVEDATE <= TO_DATE(?, 'dd-MM-YY')) OR (e.RUNNINGSTATUS = ? AND e.status  = ? AND e.NEXTTXNDATE  <= TO_DATE(?, 'DD-MM-YY'))";

            sql += CommonMethods.checkForErrorCards("e.CARDNUMBER");

            txnList = (ArrayList<InstallmentBean>) backendJdbcTemplate.query(sql,new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                InstallmentBean easyPaymentBean = new InstallmentBean();
                easyPaymentBean.setTotalFEeAmount(rs.getString("INTERESTORFEETOTALAMOUNT"));
                easyPaymentBean.setCurrentCount(rs.getInt("CURRINSTALLMENT"));
                easyPaymentBean.setRequestID(rs.getString("REQUESTID"));
                easyPaymentBean.setAccelarateStatus(rs.getString("ACCELERATEDSTATUS"));
                easyPaymentBean.setFeeType(rs.getString("PROCESSINGFEETYPE"));
                easyPaymentBean.setIncludeFirstMonth(rs.getString("FIRSTMONTHINCLUDE"));
                easyPaymentBean.setFeeApplyFirstMonth(rs.getString("FEEAPPLYINFIRSTMONTH"));
                easyPaymentBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
                easyPaymentBean.setTxnAmount(rs.getString("TXNAMOUNT"));
                easyPaymentBean.setStatus(rs.getString("STATUS"));
                easyPaymentBean.setTxnID(rs.getString("TXNID"));
                easyPaymentBean.setInstalmentAmount(rs.getString("INSTALLMENTAMOUNT"));
                easyPaymentBean.setInterestRate(rs.getString("INTERESTORFEEAMOUNT"));
                easyPaymentBean.setCurruncyCode(String.valueOf(rs.getInt("CURRENCYNUMCODE")));
                easyPaymentBean.setAccNo(rs.getString("ACCOUNTNO"));
                easyPaymentBean.setRemainingCount(rs.getInt("REMAININGCOUNT"));
                easyPaymentBean.setDuration(Integer.parseInt(rs.getString("DURATION")));
                easyPaymentBean.setRunningStatus(rs.getInt("RUNNINGSTATUS"));
                easyPaymentBean.setAcptDate(rs.getDate("ACCEDPTEDDATE").toString());
                easyPaymentBean.setEffectivedate(rs.getDate("EFECTIVEDATE") + "");
                easyPaymentBean.setNxtTxnDate(rs.getDate("NEXTTXNDATE").toString());
                easyPaymentBean.setTxnDescription(rs.getString("TXNDESCRIPTION"));
                easyPaymentBean.setLastEodTxnId(rs.getInt("LASTEODTRANSACTIONID"));
                easyPaymentBean.setTraceNumber(rs.getString("TRACENO"));
                return easyPaymentBean;
            }),statusList.getCOMMON_REQUEST_ACCEPTED(), 0, sdf.format(Configurations.EOD_DATE),
                    1, statusList.getCOMMON_REQUEST_ACCEPTED(),sdf.format(Configurations.EOD_DATE));

        }catch (Exception e){
            throw e;
        }
        return txnList;
    }
}
