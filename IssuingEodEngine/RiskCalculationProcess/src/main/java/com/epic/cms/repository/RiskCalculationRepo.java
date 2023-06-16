/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:36 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.RiskCalculationDao;
import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.RiskCalculationBean;
import com.epic.cms.model.rowmapper.DelinquentAccountRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class RiskCalculationRepo implements RiskCalculationDao {
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    LogManager logManager;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;
    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public ArrayList<DelinquentAccountBean> getDelinquentAccounts() throws Exception {
        ArrayList<DelinquentAccountBean> delinquentCardList = new ArrayList<DelinquentAccountBean>();
        try {
            String query = "SELECT DL.* FROM DELINQUENTACCOUNT DL,CARD C WHERE C.CARDNUMBER=DL.CARDNUMBER AND DL.DELINQSTATUS NOT IN (?,?) AND C.CARDSTATUS <>? AND LASTUPDATEDEODID <> ?";
            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                query += " AND DL.ACCOUNTNO NOT IN (SELECT EC.ACCOUNTNO FROM EODERRORCARDS EC WHERE EC.STATUS= ? )";
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query += " AND DL.ACCOUNTNO IN (SELECT EC.ACCOUNTNO FROM EODERRORCARDS EC WHERE EC.STATUS= ? AND EODID < ? AND PROCESSSTEPID <= ? )";
            }
            Object[] param = null;
            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                param = new Object[]{statusVarList.getTO_RESOLVE_STATUS(), statusVarList.getONLY_MANUAL_NP_STATUS(), statusVarList.getCARD_CLOSED_STATUS(), Configurations.EOD_ID, statusVarList.getEOD_PENDING_STATUS()};
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                param = new Object[]{statusVarList.getTO_RESOLVE_STATUS(), statusVarList.getONLY_MANUAL_NP_STATUS(), statusVarList.getCARD_CLOSED_STATUS(), Configurations.EOD_ID, statusVarList.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID};
            }
            delinquentCardList = (ArrayList<DelinquentAccountBean>) backendJdbcTemplate.query(query, new DelinquentAccountRowMapper(), param);
        } catch (Exception e) {
            throw e;
        }
        return delinquentCardList;
    }

    @Override
    public boolean isManualNp(String accNo) throws Exception {
        boolean isManualNp = false;
        try {
            int npStatus = 0;
            String sql = "SELECT NPSTATUS FROM CARDACCOUNT WHERE ACCOUNTNO = ? ";

            npStatus = backendJdbcTemplate.queryForObject(sql, Integer.class, accNo);
            isManualNp = npStatus == 2;
        } catch (Exception e) {
            throw e;
        }
        return isManualNp;
    }

    @Override
    public double checkForPayment(String accNo, Date EOD_DATE) throws Exception {
        double payment = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String sql = "SELECT NVL(SUM(TRANSACTIONAMOUNT), 0) AS TOTAL "
                    + " FROM EODTRANSACTION "
                    + " WHERE ACCOUNTNO = ? "
                    + " AND TRANSACTIONTYPE IN (?,?,?,?) "
                    + " AND TRUNC(SETTLEMENTDATE) = TO_DATE(?, 'DD-MM-YY') "
                    + " AND STATUS NOT IN (?)";

            payment = backendJdbcTemplate.queryForObject(sql, Double.class
                    , accNo
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.TXN_TYPE_REVERSAL
                    , Configurations.TXN_TYPE_REFUND
                    , Configurations.TXN_TYPE_MVISA_REFUND
                    , sdf.format(EOD_DATE)
                    , statusVarList.getCHEQUE_RETURN_STATUS());

        } catch (EmptyResultDataAccessException ex) {
            return payment;
        } catch (Exception e) {
            throw e;
        }
        return payment;
    }

    @Override
    public String[] getRiskclassOnNdia(int noOfDates) throws Exception {
        String[] newriskClass = new String[3];

        try {

            String query = "SELECT bucketid, minndia FROM bucket WHERE"
                    + "    CASE"
                    + "        WHEN ("
                    + "            SELECT"
                    + "                MAX(maxndia)"
                    + "            FROM"
                    + "                bucket"
                    + "            WHERE"
                    + "                status = 'ACT'"
                    + "        ) < ? THEN ("
                    + "            SELECT"
                    + "                MAX(bucketid)"
                    + "            FROM"
                    + "                bucket"
                    + "            WHERE"
                    + "                status = 'ACT'"
                    + "        )"
                    + "        ELSE ("
                    + "            SELECT"
                    + "                bucketid"
                    + "            FROM"
                    + "                bucket"
                    + "            WHERE"
                    + "                status = 'ACT'"
                    + "                AND minndia <= ?"
                    + "                AND maxndia >= ?"
                    + "        )"
                    + "    END = bucketid";


            backendJdbcTemplate.query(query
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            String riskClass = rs.getString("BUCKETID");
                            String minNdia = rs.getString("MINNDIA");
                            if (riskClass.equals(statusVarList.getRISK_CLASS_NINE())) {
                                newriskClass[0] = minNdia;
                            } else {
                                newriskClass[0] = Integer.toString(noOfDates);
                            }
                            newriskClass[1] = riskClass;
                            newriskClass[2] = minNdia;
                        }
                        return newriskClass;
                    }, noOfDates, noOfDates, noOfDates
            );

        } catch (Exception e) {
            throw e;
        }
        return newriskClass;
    }

    @Override
    public String getNPRiskClass() throws Exception {
        String npRiskClass = null;

        String query = "SELECT NVL(NONPERFORMINGRISKCLASS,'4') AS NONPERFORMINGRISKCLASS FROM COMMONCARDPARAMETER";

        try {
            npRiskClass = backendJdbcTemplate.queryForObject(query, String.class);
        } catch (Exception e) {
            throw e;
        }
        return npRiskClass;
    }

    @Override
    public String[] getNDIAOnRiskClass(String riskClass) throws Exception {
        String[] bucket = new String[3];


        try {
            String query = "SELECT BUCKETID,MINNDIA,MAXNDIA,NOOFDAYSINAREERS FROM BUCKET WHERE BUCKETID =? ";

            backendJdbcTemplate.query(query
                    , (ResultSet result) -> {
                        while (result.next()) {
                            bucket[0] = result.getString("BUCKETID");
                            bucket[1] = result.getString("MINNDIA");
                            bucket[2] = result.getString("MAXNDIA");
                        }
                        return bucket;
                    }
                    , riskClass
            );

        } catch (Exception e) {
            throw e;
        }
        return bucket;
    }

    @Override
    public int getNPDetailsFromLastBillingStatement(DelinquentAccountBean delinquentAccountBean, boolean manualNp) throws Exception {
        int count = 0;
        String sql = null;

        //NPoustanding = last month outstanding + SUM(laststament date - today)txn
        if (manualNp) {
            sql = "SELECT NVL(B.INTEREST,0) AS INTEREST, ";
        } else {
            sql = "SELECT (SELECT NVL(SUM(INTEREST),0) "
                    + "FROM (SELECT ROWNUM RN, "
                    + "    A.INTEREST "
                    + "  FROM "
                    + "    (SELECT BS.CARDNO, "
                    + "      BS.INTEREST "
                    + "    FROM BILLINGSTATEMENT BS "
                    + "    WHERE BS.CARDNO = ? "
                    + "    ORDER BY BS.DUEDATE DESC "
                    + "    ) A "
                    + "  ) B "
                    + "WHERE B.RN < 4 "
                    + ") AS INTEREST, ";
        }

        String sqlMain = sql + " (NVL(B.THISBILLCLOSINGBALANCE,0) + NVL(Y.TOTALOUTSTANING,0)) AS THISBILLCLOSINGBALANCE "
                + "FROM BILLINGLASTSTATEMENTSUMMARY BS "
                + "INNER JOIN BILLINGSTATEMENT B "
                + "ON B.STATEMENTID=BS.STATEMENTID "
                + "LEFT JOIN "
                + "  (SELECT X.CARDNO, "
                + "    SUM(X.TOTALAMOUNT) AS TOTALOUTSTANING "
                + "  FROM "
                + "    (SELECT BLS.CARDNO, "
                + "      SUM( "
                + "      CASE "
                + "        WHEN ECF.CRDR = 'CR' "
                + "        THEN -1 * ECF.FEEAMOUNT "
                + "        ELSE ECF.FEEAMOUNT "
                + "      END) AS TOTALAMOUNT "
                + "    FROM BILLINGLASTSTATEMENTSUMMARY BLS "
                + "    INNER JOIN BILLINGSTATEMENT BS "
                + "    ON BLS.STATEMENTID = BS.STATEMENTID "
                + "    LEFT JOIN EODCARDFEE ECF "
                + "    ON BS.ACCOUNTNO      = ECF.ACCOUNTNO "
                + "    WHERE ECF.EFFECTDATE > BLS.STATEMENTENDDATE "
                + "    AND ECF.EFFECTDATE  <= TO_DATE(?,'DD-MM-YY')"
                + "    AND ECF.STATUS       = ? "
                + "    AND BLS.CARDNO       = ? "
                + "    GROUP BY BLS.CARDNO "
                + "    UNION ALL "
                + "    SELECT BLS.CARDNO, "
                + "      SUM( "
                + "      CASE "
                + "        WHEN E.CRDR = 'CR' "
                + "        THEN -1 * E.TRANSACTIONAMOUNT "
                + "        ELSE E.TRANSACTIONAMOUNT "
                + "      END) AS TOTALAMOUNT "
                + "    FROM BILLINGLASTSTATEMENTSUMMARY BLS "
                + "    INNER JOIN BILLINGSTATEMENT BS "
                + "    ON BLS.STATEMENTID = BS.STATEMENTID "
                + "    LEFT JOIN EODTRANSACTION E "
                + "    ON BS.ACCOUNTNO        = E.ACCOUNTNO "
                + "    WHERE E.SETTLEMENTDATE > BLS.STATEMENTENDDATE "
                + "    AND E.SETTLEMENTDATE  <= TO_DATE(?,'DD-MM-YY') "
                + "    AND E.STATUS           = ? "
                + "    AND BLS.CARDNO         = ? "
                + "    GROUP BY BLS.CARDNO "
                + "    ) X "
                + "  GROUP BY X.CARDNO "
                + "  ) Y ON B.CARDNO = Y.CARDNO "
                + "WHERE 1           =1 "
                + "AND B.CARDNO      =?";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

            count = backendJdbcTemplate.query(sqlMain,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            delinquentAccountBean.setNpInterest(rs.getDouble("INTEREST"));
                            delinquentAccountBean.setNpOutstanding(rs.getDouble("THISBILLCLOSINGBALANCE"));
                            delinquentAccountBean.setNpDate(Configurations.EOD_DATE);
//                count = 1;
                        }
                        return 1;
                    });

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String sql = "INSERT INTO EODGLACCOUNT (EODID,GLDATE,CARDNO,GLTYPE,AMOUNT,CRDR,PAYMENTTYPE) " +
                    "VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?,?)";

            count = backendJdbcTemplate.update(sql, eodID, sdf.format(glDate), cardNo.toString(), glType, String.valueOf(amount), cdStatus, payType);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateNpStatusCardAccount(String accNo, int npstatus) throws Exception {
        int flag = 0;
        try {

            String query = "UPDATE CARDACCOUNT SET NPSTATUS = ? WHERE ACCOUNTNO = ?";

            flag = backendJdbcTemplate.update(query, npstatus, accNo);

        } catch (Exception e) {
            throw e;
        }
        return flag;//If updated return number of updated rows
    }

    @Override
    public double getTotalPaymentSinceLastDue(String cardNumber, Date EOD_DATE, Date dueDate) throws Exception {
        double payment = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTAL "
                    + " FROM EODTRANSACTION "
                    + " WHERE ACCOUNTNO = ? "
                    + " AND TRANSACTIONTYPE IN (?,?,?,?) "
                    + " AND TRUNC(SETTLEMENTDATE) > TO_DATE(?, 'DD-MM-YY') "
                    + " AND TRUNC(SETTLEMENTDATE) <= TO_DATE(?, 'DD-MM-YY') "
                    + " AND STATUS NOT IN (?)";

            payment = backendJdbcTemplate.queryForObject(sql, Double.class
                    , cardNumber
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.TXN_TYPE_REVERSAL
                    , Configurations.TXN_TYPE_REFUND
                    , Configurations.TXN_TYPE_MVISA_REFUND
                    , sdf.format(dueDate)
                    , sdf.format(EOD_DATE)
                    , statusVarList.getCHEQUE_RETURN_STATUS()
            );

        } catch (EmptyResultDataAccessException ex) {
            return payment;
        } catch (Exception e) {
            throw e;
        }
        return payment;
    }

    @Override
    public int addDetailsToDelinquentAccountTable(DelinquentAccountBean delinquentAccountBean) throws Exception {
        int count = 0;
        boolean ststus = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String sql = "SELECT DELINQSTATUS FROM DELINQUENTACCOUNT WHERE CARDNUMBER = ?";

            String result = backendJdbcTemplate.queryForObject(sql, String.class,
                    delinquentAccountBean.getCardNumber().toString());
            if (result == null) {
            } else {
                ststus = true;
            }
            if (ststus) {
                sql = "UPDATE DELINQUENTACCOUNT SET NDIA = ? ,MIA = ? ,RISKCLASS = ?,"
                        + " DUEAMOUNT = ? ,ACCSTATUS = ?,CARDCATEGORYCODE = ?,"
                        + " LASTSTATEMENTDATE = TO_DATE(?, 'DD-MM-YY') ,LASTUPDATEDUSER = ?,LASTUPDATEDTIME = TO_DATE(SYSDATE, 'DD-MM-YY'),"
                        + " CONTACTNO = ?,ASSIGNEE = ?,ASSIGNSTATUS = ?,SUPERVISOR = ?,DELINQSTATUS = ?,DUEDATE =  TO_DATE(?, 'DD-MM-YY'),NPDATE= TO_DATE(?, 'DD-MM-YY'),NPINTEREST=?,NPOUTSTANDING=?,REMAINDUE=?,LASTUPDATEDEODID=?"
                        + " WHERE CARDNUMBER = ?";

                count = backendJdbcTemplate.update(sql
                        , delinquentAccountBean.getNDIA() //1
                        , delinquentAccountBean.getMIA() //2
                        , delinquentAccountBean.getRiskClass() //3
                        , Double.parseDouble(delinquentAccountBean.getDueAmount()) //4
                        , delinquentAccountBean.getAccStatus() //5
                        , delinquentAccountBean.getCardCategory() //6
                        , sdf.format(delinquentAccountBean.getLastStatementDate()) //7
                        , Configurations.EOD_USER //8
                        , delinquentAccountBean.getContactNo() //9
                        , delinquentAccountBean.getAssignee() //10
                        , delinquentAccountBean.getAssignStatus() //11
                        , delinquentAccountBean.getSupervisor() //12
                        , delinquentAccountBean.getDelinqstatus() //13
                        , sdf.format(delinquentAccountBean.getDueDate()) //14
                        , (delinquentAccountBean.getNpDate() != null) ? sdf.format(delinquentAccountBean.getNpDate()) : null  //15
                        , delinquentAccountBean.getNpInterest() //16
                        , delinquentAccountBean.getNpOutstanding() //17
                        , delinquentAccountBean.getRemainDue() //18
                        , Configurations.EOD_ID //19
                        , delinquentAccountBean.getCardNumber().toString() //20
                );

            } else {
                sql = "INSERT INTO DELINQUENTACCOUNT(CARDNUMBER,MAINCARDNO,ACCOUNTNO,CIF,"
                        + "NAMEONCARD,NAMEINFULL,IDTYPE,IDNUMBER,NDIA,MIA,RISKCLASS,"
                        + "DUEAMOUNT,ACCSTATUS,CARDCATEGORYCODE,LASTSTATEMENTDATE,"
                        + "LASTUPDATEDUSER,LASTUPDATEDTIME,CREATEDTIME,CONTACTNO,DUEDATE,DELINQSTATUS,ASSIGNEE,ASSIGNSTATUS,SUPERVISOR,REMAINDUE,LASTUPDATEDEODID) "
                        + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),?,TO_DATE(SYSDATE, 'DD-MM-YY') ,"
                        + "TO_DATE(SYSDATE, 'DD-MM-YY'),?,TO_DATE(?, 'DD-MM-YY'),?,?,?,?,?,?)";

                count = backendJdbcTemplate.update(sql
                        , delinquentAccountBean.getCardNumber().toString()  //1
                        , delinquentAccountBean.getCardNumber().toString() //2
                        , delinquentAccountBean.getAccNo() //3
                        , delinquentAccountBean.getCif() //4
                        , delinquentAccountBean.getNameOnCard() //5
                        , delinquentAccountBean.getNameInFull() //6
                        , delinquentAccountBean.getIdType() //7
                        , delinquentAccountBean.getIdNumber() //8
                        , delinquentAccountBean.getNDIA() //9
                        , delinquentAccountBean.getMIA() //10
                        , delinquentAccountBean.getRiskClass() //11
                        , Double.parseDouble(delinquentAccountBean.getDueAmount()) //12
                        , delinquentAccountBean.getAccStatus() //13
                        , delinquentAccountBean.getCardCategory() //14
                        , sdf.format(delinquentAccountBean.getLastStatementDate()) //15
                        , Configurations.EOD_USER
                        , delinquentAccountBean.getContactNo() //17
                        , sdf.format(delinquentAccountBean.getDueDate()) //18
                        , delinquentAccountBean.getDelinqstatus() //19
                        , delinquentAccountBean.getAssignee() //20
                        , delinquentAccountBean.getAssignStatus() //21
                        , delinquentAccountBean.getSupervisor() //22
                        , delinquentAccountBean.getRemainDue() //23
                        , Configurations.EOD_ID
                );
            }
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoDelinquentHistory(StringBuffer cardNumber, String accNo, String remark) throws Exception {
        int flag = 0;
        String sql = "INSERT INTO DELINQUENTHISTORY (CARDNUMBER,  ACCOUNTNO,REMARK,  LASTUPDATEDUSER,"
                + "   LASTUPDATEDTIME,   CREATEDTIME ) VALUES (?,?,?,?,TO_DATE(SYSDATE, 'DD-MM-YY') ,TO_DATE(SYSDATE, 'DD-MM-YY'))";

        try {
            flag = backendJdbcTemplate.update(sql, cardNumber.toString(), accNo, remark, Configurations.EOD_USER);
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public ArrayList<com.epic.cms.model.bean.RiskCalculationBean> getRiskCalculationCardList() throws Exception {
        ArrayList<RiskCalculationBean> cardList = new ArrayList<RiskCalculationBean>();
        String sql = "SELECT M.CARDNO,M.M1,M.M1DATE FROM MINIMUMPAYMENT M, BILLINGLASTSTATEMENTSUMMARY B WHERE M.M1 > 0 "
                + " AND (M.M2 IS NULL OR M.M2 = 0) "
                + " AND B.CARDNO = M.CARDNO "
                + " AND M.CARDNO NOT IN"
                + " (SELECT CARDNUMBER FROM DELINQUENTACCOUNT WHERE DELINQSTATUS NOT IN (?,?) )";

        sql += CommonMethods.checkForErrorCards("M.CARDNO");

        try {

            RiskCalculationBean riskCalculationBean = new RiskCalculationBean();
            backendJdbcTemplate.query(sql
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            riskCalculationBean.setCardNo(new StringBuffer(rs.getString("CARDNO")));
                            riskCalculationBean.setDueDate(rs.getDate("M1DATE"));
                            riskCalculationBean.setDueAmount(rs.getDouble("M1"));
                            cardList.add(riskCalculationBean);
                        }
                        return cardList;
                    }, statusVarList.getTO_RESOLVE_STATUS()
                    , statusVarList.getONLY_MANUAL_NP_STATUS()
            );

        } catch (NullPointerException ex) {
            throw ex;
        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public int updateProvisionInDELINQUENTACCOUNT(BigDecimal provisionAmount, String accNo) throws Exception {
        PreparedStatement stmt = null;
        int count = 0;
        try {
            String query = "UPDATE DELINQUENTACCOUNT SET NPPROVISIONAMOUNT = ? WHERE ACCOUNTNO = ? ";

            count = backendJdbcTemplate.update(query, provisionAmount.toString(), accNo);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<Object> getLastStatementDate(StringBuffer cardNumber) throws Exception {

        ArrayList<Object> lastStmtDetails = new ArrayList<>();
        String sql = "SELECT MINAMOUNT,STATEMENTENDDATE,DUEDATE FROM BILLINGLASTSTATEMENTSUMMARY WHERE CARDNO = ?";
        try {
            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            lastStmtDetails.add(rs.getDate("STATEMENTENDDATE"));
                            lastStmtDetails.add(rs.getDouble("MINAMOUNT"));
                            lastStmtDetails.add(rs.getDate("DUEDATE"));
                        }
                        return lastStmtDetails;
                    }, cardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
        return lastStmtDetails;
    }

    @Override
    public HashMap<String, Date> getDueDateList(StringBuffer cardNumber) throws Exception {
        HashMap<String, java.util.Date> dueDateList = new HashMap<String, java.util.Date>();

        String sql = "SELECT M1DATE,M2DATE,M3DATE,M4DATE,M5DATE,M6DATE,M7DATE,M8DATE,"
                + "M9DATE,M10DATE,M11DATE,M12DATE FROM MINIMUMPAYMENT WHERE CARDNO = ? ";

        try {
            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            for (int i = 1; i <= 12; i++) {
                                dueDateList.put("M" + i, rs.getDate("M" + i + "DATE"));
                            }
                        }
                        return dueDateList;
                    }, cardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
        return dueDateList;
    }

    @Override
    public ArrayList<Object> getMinimumPaymentExistStatementDate(StringBuffer cardNo, int monthNo) throws Exception {
        ArrayList<Object> lastStmtDetails = new ArrayList<>();

        String sql = "SELECT BS.STATEMENTENDDATE, "
                + "  BS.TOTALMINPAYMENT, "
                + "  BS.DUEDATE "
                + "FROM MINIMUMPAYMENT MP "
                + "INNER JOIN BILLINGSTATEMENT BS "
                + "ON MP.CARDNO         = BS.MAINCARDNO "
                + "WHERE BS.MAINCARDNO  = ? "
                + "AND TRUNC(MP.M" + monthNo + "DATE) = TRUNC(BS.DUEDATE)";
        try {
            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            lastStmtDetails.add(rs.getDate("STATEMENTENDDATE"));
                            lastStmtDetails.add(rs.getDouble("TOTALMINPAYMENT"));
                            lastStmtDetails.add(rs.getDate("DUEDATE"));
                        }
                        return lastStmtDetails;
                    }, cardNo.toString()
            );
        } catch (Exception e) {
            throw e;
        }
        return lastStmtDetails;
    }

    @Override
    public int updateOnlineAccountStatus(String accNo, int status) throws Exception {
        int flag = 0;

        try {
            String sql = "UPDATE ECMS_ONLINE_ACCOUNT SET STATUS=? WHERE ACCOUNTNUMBER=? ";

            flag = onlineJdbcTemplate.update(sql,
                    status,
                    accNo
            );
            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateOnlineAccountStatus ===================" + Configurations.EOD_ID);
                logInfo.info(sql);
                logInfo.info(Integer.toString(status));
                logInfo.info(accNo);
                logInfo.info("================ updateOnlineAccountStatus END ===================");
            }
        } catch (Exception e) {
            throw e;
        }

        return flag;
    }

    @Override
    public int updateAccountStatus(String accNo, String status) throws Exception {
        int flag = 0;

        try {
            String sql = "UPDATE CARDACCOUNT SET STATUS=?,LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE ACCOUNTNO=? ";

            flag = backendJdbcTemplate.update(sql,
                    status,
                    Configurations.EOD_USER,
                    accNo
            );
        } catch (Exception e) {
            throw e;
        }

        return flag;
    }

    @Override
    public double checkLeastMinimumPayment(String accNo) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        double payment = 0;
        try {
            String query = "SELECT M1 FROM MINIMUMPAYMENT WHERE CARDNO IN (SELECT CARDNUMBER FROM CARDACCOUNT WHERE ACCOUNTNO = ?)";
            payment = backendJdbcTemplate.queryForObject(query, Double.class, accNo);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return payment;
    }

    @Override
    public int insertIntoEodGLAccountBigDecimal(int eodID, Date glDate, StringBuffer cardNo, String glType, BigDecimal amount, String cdStatus, String payType) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String sql = "INSERT INTO EODGLACCOUNT (EODID,GLDATE,CARDNO,GLTYPE,AMOUNT,CRDR,PAYMENTTYPE) VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?,?)";

            count = backendJdbcTemplate.update(sql, Integer.class
                    , eodID, sdf.format(glDate), cardNo.toString(), glDate, amount.toString(), cdStatus, payType);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateMinimumPayment(StringBuffer cardNo, HashMap<String, Double> dueAmountList, HashMap<String, Date> dueDateList, int dueCount) throws Exception {
        int count = 0;

        try {
            for (Map.Entry<String, Double> entrySet : dueAmountList.entrySet()) {
                String key = entrySet.getKey();
                Double value = entrySet.getValue();
                String Query = "UPDATE MINIMUMPAYMENT SET " + key + "=?,  " + key + "DATE=?, COUNT=? WHERE CARDNO =?";

                count = backendJdbcTemplate.update(Query, value, dueDateList.get(key), dueCount, cardNo.toString());

            }

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public Date getDueDateOnRiskClass(int monthNo, StringBuffer cardNo) throws Exception {
        java.sql.Date dueDate = null;
        String sql = "SELECT M" + monthNo + "DATE AS MONTH FROM MINIMUMPAYMENT WHERE CARDNO = ?";

        try {
            dueDate = backendJdbcTemplate.queryForObject(sql, java.sql.Date.class, cardNo.toString());
        } catch (Exception e) {
            throw e;
        }
        return dueDate;
    }

    @Override
    public DelinquentAccountBean setDelinquentAccountDetails(StringBuffer cardNo) throws Exception {

        DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        try {
            String sql = "SELECT C.CARDCATEGORYCODE,"
                    + " C.NAMEONCARD,"
                    + " C.IDTYPE,"
                    + " C.IDNUMBER,"
                    + " CAC.ACCOUNTNO,"
                    + " CA.STATUS,"
                    + " CAC.CUSTOMERID,"
                    + " B.STATEMENTENDDATE,"
                    + " B.DUEDATE, "
                    + " B.MINAMOUNT, NVL(DA.NDIA,0) AS NDIA "
                    + " FROM CARD C,"
                    + " CARDACCOUNTCUSTOMER CAC,"
                    + " BILLINGLASTSTATEMENTSUMMARY B,"
                    + " CARDACCOUNT CA"
                    + " LEFT JOIN DELINQUENTACCOUNT DA ON DA.ACCOUNTNO = CA.ACCOUNTNO "
                    + " WHERE CAC.CARDNUMBER = C.CARDNUMBER"
                    + " AND CA.CARDNUMBER = C.MAINCARDNO"
                    + " AND C.CARDNUMBER = C.MAINCARDNO"
                    + " AND B.CARDNO = C.MAINCARDNO "
                    + " AND C.MAINCARDNO = ? ";

            backendJdbcTemplate.query(sql
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            delinquentAccountBean.setCardCategory(rs.getString("CARDCATEGORYCODE"));
                            delinquentAccountBean.setAccNo(rs.getString("ACCOUNTNO"));
                            delinquentAccountBean.setCif(rs.getString("CUSTOMERID"));
                            delinquentAccountBean.setLastStatementDate(rs.getDate("STATEMENTENDDATE"));
                            delinquentAccountBean.setDueDate(rs.getDate("DUEDATE"));
                            delinquentAccountBean.setDueAmount(rs.getString("MINAMOUNT"));
                            delinquentAccountBean.setNameOnCard(rs.getString("NAMEONCARD"));
                            delinquentAccountBean.setIdNumber(rs.getString("IDNUMBER"));
                            delinquentAccountBean.setIdType(rs.getString("IDTYPE"));
                            delinquentAccountBean.setCif(rs.getString("CUSTOMERID"));
                            delinquentAccountBean.setAccStatus(rs.getString("STATUS"));
                            delinquentAccountBean.setNDIA(rs.getInt("NDIA"));
                        }
                        return delinquentAccountBean;
                    }, cardNo.toString()
            );

            if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_MAIN)
                    || delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY)
                    || delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                sql = "SELECT NAMEWITHINITIAL ,CONTACTNO ,EMAIL FROM CARDMAINCUSTOMERDETAIL WHERE CUSTOMERID = ?";

                backendJdbcTemplate.query(sql
                        , (ResultSet rs) -> {
                            while (rs.next()) {
                                delinquentAccountBean.setNameInFull(rs.getString("NAMEWITHINITIAL"));
                                delinquentAccountBean.setContactNo(rs.getString("CONTACTNO"));
                                delinquentAccountBean.setEmail(rs.getString("EMAIL"));

                            }
                            return delinquentAccountBean;
                        }, delinquentAccountBean.getCif()
                );
            } else if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD)) {
                sql = "SELECT CUSTOMERNAME ,CONTACTNO ,EMAIL FROM CARDFDCUSTOMERDETAIL WHERE CUSTOMERID = ?";

                backendJdbcTemplate.query(sql
                        , (ResultSet rs) -> {
                            while (rs.next()) {
                                delinquentAccountBean.setNameInFull(rs.getString("CUSTOMERNAME"));
                                delinquentAccountBean.setContactNo(rs.getString("CONTACTNO"));
                                delinquentAccountBean.setEmail(rs.getString("EMAIL"));
                            }
                            return delinquentAccountBean;
                        }, delinquentAccountBean.getCif()
                );
            } else if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                sql = "SELECT NAMEOFTHECOMPANY ,CONTACTNUMBERSLAND ,CONTACTEMAIL FROM CARDESTCUSTOMERDETAILS WHERE CUSTOMERID = ?";

                backendJdbcTemplate.query(sql
                        , (ResultSet rs) -> {
                            while (rs.next()) {
                                delinquentAccountBean.setNameInFull(rs.getString("NAMEOFTHECOMPANY"));
                                delinquentAccountBean.setContactNo(rs.getString("CONTACTNUMBERSLAND"));
                                delinquentAccountBean.setEmail(rs.getString("CONTACTEMAIL"));
                            }
                            return delinquentAccountBean;
                        }, delinquentAccountBean.getCif()
                );
            }

        } catch (Exception e) {
            throw e;
        }
        return delinquentAccountBean;
    }

    @Override
    public double getMinPaymentFromBilling(String accNumber) throws Exception {
        double minPayment = 0;

        try {
            String sql = "SELECT BLS.MINAMOUNT "
                    + "FROM BILLINGLASTSTATEMENTSUMMARY BLS "
                    + "INNER JOIN BILLINGSTATEMENT BS "
                    + "ON BS.STATEMENTID = BLS.STATEMENTID "
                    + "WHERE BS.ACCOUNTNO = ? ";

            minPayment = backendJdbcTemplate.queryForObject(sql, Double.class, accNumber);

        } catch (Exception e) {
            throw e;
        }
        return minPayment;
    }

    @Override
    public List<BigDecimal> getDelinquentAccountDetailsAsList(String accNo) throws Exception {
        List<BigDecimal> delinquentList = new ArrayList<BigDecimal>();
        try {

            String sql = "SELECT NPINTEREST, NPOUTSTANDING, NPACCRUEDINTEREST, NPACCRUEDFEES, NPACCRUEDOVERLIMITFEES, "
                    + " NPACCRUEDLATEPAYFEES, NPPROVISIONAMOUNT FROM DELINQUENTACCOUNT WHERE ACCOUNTNO = ?";

            backendJdbcTemplate.query(sql
                    , (ResultSet rs) -> {
                        if (rs.next()) {
                            BigDecimal npInterest = new BigDecimal(rs.getString("NPINTEREST"));
                            BigDecimal npOutstanding = new BigDecimal(rs.getString("NPOUTSTANDING"));
                            BigDecimal accruedLatePay = new BigDecimal(rs.getString("NPACCRUEDLATEPAYFEES"));
                            BigDecimal accruedOverLimit = new BigDecimal(rs.getString("NPACCRUEDOVERLIMITFEES"));
                            BigDecimal accruedFees = new BigDecimal(rs.getString("NPACCRUEDFEES"));
                            BigDecimal accruedInterest = new BigDecimal(rs.getString("NPACCRUEDINTEREST"));

                            BigDecimal npCapital = npOutstanding.subtract(npInterest);
                            BigDecimal totalOverAndLatePay = accruedLatePay.add(accruedOverLimit);
                            BigDecimal accruedOtherFees = accruedFees.subtract(totalOverAndLatePay);

                            // payment knock-off order should be same as below
                            // 1.NPINTEREST 2.NPCAPITAL 3. ACCRUEDINTEREST 4. ACCRUEDLATEPAYMENT 5. ACCRUEDOVERLIMIT 6. ACCRUEDOTHERFEES
                            // according to this order paymentKnockOffList,balanceAfterSetOff & KnockOffList has been created.
                            // if you want to chnage the order those 3 list should be change accordingly.
                            delinquentList.add(npInterest);
                            delinquentList.add(npCapital);
                            delinquentList.add(accruedInterest);
                            delinquentList.add(accruedLatePay);
                            delinquentList.add(accruedOverLimit);
                            delinquentList.add(accruedOtherFees);
                        }
                    }, accNo
            );

        } catch (Exception e) {
            throw e;
        }
        return delinquentList;
    }

    @Override
    public int updateAllDELINQUENTACCOUNTnpdetails(double npInterest, double npOutstanding, double accruedInterest, double accruedOverLimitFees, double accruedLatePayFees, double otherFees, String accNo) throws Exception {
        int count = 0;
        double totalFees = 0;
        try {
            totalFees = otherFees + accruedLatePayFees + accruedOverLimitFees;

            String query = "UPDATE DELINQUENTACCOUNT SET NPINTEREST = ?, NPOUTSTANDING = ?, NPACCRUEDINTEREST = ?, NPACCRUEDOVERLIMITFEES = ?, "
                    + "NPACCRUEDLATEPAYFEES = ?, NPACCRUEDFEES = ? WHERE ACCOUNTNO = ? ";

            count = backendJdbcTemplate.update(query, npInterest, npOutstanding, accruedInterest, accruedOverLimitFees
                    , accruedLatePayFees, totalFees, accNo);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateAllDELINQUENTACCOUNTnpdetails(BigDecimal npInterest, BigDecimal npOutstanding, BigDecimal accruedInterest, BigDecimal accruedOverLimitFees, BigDecimal accruedLatePayFees, BigDecimal otherFees, String accNo) throws Exception {
        int count = 0;
        BigDecimal totalFees = BigDecimal.ZERO;
        try {
            totalFees = otherFees.add(accruedLatePayFees).add(accruedOverLimitFees);

            String query = "UPDATE DELINQUENTACCOUNT SET NPINTEREST = ?, NPOUTSTANDING = ?, NPACCRUEDINTEREST = ?, NPACCRUEDOVERLIMITFEES = ?, "
                    + "NPACCRUEDLATEPAYFEES = ?, NPACCRUEDFEES = ? WHERE ACCOUNTNO = ? ";

            backendJdbcTemplate.update(query, npInterest.toString()
                    , npOutstanding.toString()
                    , accruedInterest.toString()
                    , accruedOverLimitFees.toString()
                    , accruedLatePayFees.toString()
                    , totalFees.toString()
                    , accNo
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
