package com.epic.cms.repository;

import com.epic.cms.dao.ManualNpDao;
import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;


@Repository
public class ManualNpRepo implements ManualNpDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    JdbcTemplate backendJdbcTemplate;

    @Autowired
    JdbcTemplate onlineJdbcTemplate;

    @Autowired
    LogManager logManager;


    @Override
    @Transactional(value = "backendDb")
    public ProcessBean getProcessDetails(int processId) throws Exception {
        ProcessBean processDetails = null;
        try {
            String query = "SELECT PROCESSID,DESCRIPTION,CRITICALSTATUS,ROLLBACKSTATUS,SHEDULEDATE,SHEDULETIME,FREQUENCYTYPE,CONTINUESFREQUENCYTYPE,CONTINUESFREQUENCY,MULTIPLECYCLESTATUS,PROCESSCATEGORYID,DEPENDANCYSTATUS,RUNNINGONMAIN,RUNNINGONSUB,PROCESSTYPE,STATUS,SHEDULEDATETIME,HOLIDAYACTION, HOLIDAYACTION, KAFKATOPICNAME, KAFKAGROUPID FROM EODPROCESS WHERE PROCESSID = ? ";//AND SHEDULEDATETIME <= to_date(?,'MM/dd/YYYY HH:mi:ss AM') ";
            processDetails = backendJdbcTemplate.queryForObject(query,new ProcessBeanRowMapper(),processId
            );
        } catch (Exception e) {
            throw e;
        }
        return processDetails;
    }

    @Override
    public HashMap<String, String[]> getManualNpRequestDetails(int reqType, String Status) throws Exception {

        HashMap<String, String[]> manualNpMap = new HashMap<String, String[]>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {

            String sql = "SELECT REQUESTID, ACCOUNTNO, ACCSTATUS, MAINCARDNUMBER, NDIA, REQUESTTYPE, STATUS FROM MANUALNPREQUEST WHERE REQUESTTYPE = ? AND STATUS = ? AND TRUNC(CREATEDTIME) <= TO_DATE(?,'DD-MM-YY')";
            manualNpMap = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        String[] accDetails = new String[4];
                        HashMap<String, String[]> tempManualNpMap = new HashMap<String, String[]>();
                        while (rs.next()) {
                            String accNo = rs.getString("ACCOUNTNO");
                            String accStatus = rs.getString("ACCSTATUS");
                            String cardNo = rs.getString("MAINCARDNUMBER");
                            int reqID = rs.getInt("REQUESTID");
                            int ndia = rs.getInt("NDIA");
                            accDetails[0] = cardNo;
                            accDetails[1] = accStatus;
                            accDetails[2] = String.valueOf(reqID);
                            accDetails[3] = String.valueOf(ndia);
                            tempManualNpMap.put(accNo, accDetails);
                        }
                        return tempManualNpMap;
                    },
                    reqType, Status, sdf.format(Configurations.EOD_DATE)
            ));
        } catch (Exception e) {
            logManager.logError("Exception in Get Manual Np Request Details ", errorLogger);
            throw e;
        }
        return manualNpMap;
    }

    @Override
    public int updateNpStatusCardAccount(String accNo, int npstatus) throws Exception {

        int flag = 0;
        try {
            String sql = "UPDATE CARDACCOUNT SET NPSTATUS = ? WHERE ACCOUNTNO = ?";
            flag = backendJdbcTemplate.update(sql,
                    npstatus,
                    accNo
            );
        } catch (Exception e) {
            logManager.logError("Exception in Update Np Status Card Account ", errorLogger);
            throw e;
        }
        return flag;
    }

    @Override
    public int insertIntoDelinquentHistory(StringBuffer cardNumber, String accNo, String remark) throws Exception {

        int flag = 0;
        String sql = "INSERT INTO DELINQUENTHISTORY (CARDNUMBER, ACCOUNTNO,REMARK, LASTUPDATEDUSER, LASTUPDATEDTIME, CREATEDTIME ) VALUES (?,?,?,?,TO_DATE(SYSDATE, 'DD-MM-YY') ,TO_DATE(SYSDATE, 'DD-MM-YY'))";
        try {
            flag = backendJdbcTemplate.update(sql,
                    cardNumber,
                    accNo,
                    remark,
                    Configurations.EOD_USER
            );
        } catch (Exception e) {
            logManager.logError("Exception in Insert In to Delinquent History ", errorLogger);
            throw e;
        }
        return flag;
    }

    @Override
    public int getNPDetailsFromLastBillingStatement(DelinquentAccountBean delinquentAccountBean, boolean manualNp) throws Exception {

        int count = 0;
        String sql = null;
        if (manualNp) {
            sql = "SELECT NVL(B.INTEREST,0) AS INTEREST, ";
        } else {
            sql = "SELECT (SELECT NVL(SUM(INTEREST),0) FROM (SELECT ROWNUM RN, A.INTEREST FROM (SELECT BS.CARDNO, BS.INTEREST FROM BILLINGSTATEMENT BS WHERE BS.CARDNO = ? ORDER BY BS.DUEDATE DESC ) A ) B WHERE B.RN < 4 ) AS INTEREST, ";
        }
        sql += " (NVL(B.THISBILLCLOSINGBALANCE,0) + NVL(Y.TOTALOUTSTANING,0)) AS THISBILLCLOSINGBALANCE FROM BILLINGLASTSTATEMENTSUMMARY BS INNER JOIN BILLINGSTATEMENT B ON B.STATEMENTID=BS.STATEMENTID LEFT JOIN   (SELECT X.CARDNO,     SUM(X.TOTALAMOUNT) AS TOTALOUTSTANING   FROM     (SELECT BLS.CARDNO,       SUM(       CASE         WHEN ECF.CRDR = 'CR'         THEN -1 * ECF.FEEAMOUNT         ELSE ECF.FEEAMOUNT       END) AS TOTALAMOUNT     FROM BILLINGLASTSTATEMENTSUMMARY BLS     INNER JOIN BILLINGSTATEMENT BS     ON BLS.STATEMENTID = BS.STATEMENTID     LEFT JOIN EODCARDFEE ECF     ON BS.ACCOUNTNO      = ECF.ACCOUNTNO     WHERE ECF.EFFECTDATE > BLS.STATEMENTENDDATE     AND ECF.EFFECTDATE  <= TO_DATE(?,'DD-MM-YY')    AND ECF.STATUS       = ?     AND BLS.CARDNO       = ?     GROUP BY BLS.CARDNO     UNION ALL     SELECT BLS.CARDNO,       SUM(       CASE         WHEN E.CRDR = 'CR'         THEN -1 * E.TRANSACTIONAMOUNT         ELSE E.TRANSACTIONAMOUNT       END) AS TOTALAMOUNT     FROM BILLINGLASTSTATEMENTSUMMARY BLS     INNER JOIN BILLINGSTATEMENT BS     ON BLS.STATEMENTID = BS.STATEMENTID     LEFT JOIN EODTRANSACTION E     ON BS.ACCOUNTNO        = E.ACCOUNTNO     WHERE E.SETTLEMENTDATE > BLS.STATEMENTENDDATE     AND E.SETTLEMENTDATE  <= TO_DATE(?,'DD-MM-YY')     AND E.STATUS           = ?     AND BLS.CARDNO         = ?     GROUP BY BLS.CARDNO     ) X   GROUP BY X.CARDNO   ) Y ON B.CARDNO = Y.CARDNO WHERE 1           =1 AND B.CARDNO      =?";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
            if (manualNp = true) {
                count = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                        (ResultSet rs) -> {
                            int temp = 0;
                            while (rs.next()) {
                                delinquentAccountBean.setNpInterest(rs.getDouble("INTEREST"));
                                delinquentAccountBean.setNpOutstanding(rs.getDouble("THISBILLCLOSINGBALANCE"));
                                delinquentAccountBean.setNpDate(Configurations.EOD_DATE);
                                temp = 1;
                            }
                            return temp;
                        },
                        sdf.format(Configurations.EOD_DATE),
                        Configurations.EOD_DONE_STATUS,
                        delinquentAccountBean.getCardNumber(),
                        sdf.format(Configurations.EOD_DATE),
                        Configurations.EOD_DONE_STATUS,
                        delinquentAccountBean.getCardNumber(),
                        delinquentAccountBean.getCardNumber()
                ));

            } else {
                count = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                        (ResultSet rs) -> {
                            int temp = 0;
                            while (rs.next()) {
                                delinquentAccountBean.setNpInterest(rs.getDouble("INTEREST"));
                                delinquentAccountBean.setNpOutstanding(rs.getDouble("THISBILLCLOSINGBALANCE"));
                                delinquentAccountBean.setNpDate(Configurations.EOD_DATE);
                                temp = 1;
                            }
                            return temp;
                        },
                        delinquentAccountBean.getCardNumber(),
                        sdf.format(Configurations.EOD_DATE),
                        Configurations.EOD_DONE_STATUS,
                        delinquentAccountBean.getCardNumber(),
                        sdf.format(Configurations.EOD_DATE),
                        Configurations.EOD_DONE_STATUS,
                        delinquentAccountBean.getCardNumber(),
                        delinquentAccountBean.getCardNumber()
                ));
            }

        } catch (Exception e) {
            logManager.logError("Exception in Get NP Details From Last Billing Statement ", errorLogger);
            throw e;
        }
        return count;
    }

    @Override
    public DelinquentAccountBean setDelinquentAccountDetails(StringBuffer cardNo) throws Exception {
        DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        try {
            String sql = "SELECT C.CARDCATEGORYCODE, C.NAMEONCARD, C.IDTYPE, C.IDNUMBER, CAC.ACCOUNTNO, CA.STATUS, CAC.CUSTOMERID, B.STATEMENTENDDATE, B.DUEDATE,  B.MINAMOUNT, NVL(DA.NDIA,0) AS NDIA  FROM CARD C, CARDACCOUNTCUSTOMER CAC, BILLINGLASTSTATEMENTSUMMARY B, CARDACCOUNT CA LEFT JOIN DELINQUENTACCOUNT DA ON DA.ACCOUNTNO = CA.ACCOUNTNO  WHERE CAC.CARDNUMBER = C.CARDNUMBER AND CA.CARDNUMBER = C.MAINCARDNO AND C.CARDNUMBER = C.MAINCARDNO AND B.CARDNO = C.MAINCARDNO  AND C.MAINCARDNO = ? ";
            DelinquentAccountBean tempDelinquentAccountBean = new DelinquentAccountBean();
            delinquentAccountBean = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            tempDelinquentAccountBean.setCardCategory(rs.getString("CARDCATEGORYCODE"));
                            tempDelinquentAccountBean.setAccNo(rs.getString("ACCOUNTNO"));
                            tempDelinquentAccountBean.setCif(rs.getString("CUSTOMERID"));
                            tempDelinquentAccountBean.setLastStatementDate(rs.getDate("STATEMENTENDDATE"));
                            tempDelinquentAccountBean.setDueDate(rs.getDate("DUEDATE"));
                            tempDelinquentAccountBean.setDueAmount(rs.getString("MINAMOUNT"));
                            tempDelinquentAccountBean.setNameOnCard(rs.getString("NAMEONCARD"));
                            tempDelinquentAccountBean.setIdNumber(rs.getString("IDNUMBER"));
                            tempDelinquentAccountBean.setIdType(rs.getString("IDTYPE"));
                            tempDelinquentAccountBean.setCif(rs.getString("CUSTOMERID"));
                            tempDelinquentAccountBean.setAccStatus(rs.getString("STATUS"));
                            tempDelinquentAccountBean.setNDIA(rs.getInt("NDIA"));
                        }
                        return tempDelinquentAccountBean;
                    },
                    cardNo.toString()
            ));

            if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_MAIN)
                    || delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY)
                    || delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                sql = "SELECT NAMEWITHINITIAL ,CONTACTNO ,EMAIL FROM CARDMAINCUSTOMERDETAIL WHERE CUSTOMERID = ?";
                delinquentAccountBean = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                tempDelinquentAccountBean.setNameInFull(rs.getString("NAMEWITHINITIAL"));
                                tempDelinquentAccountBean.setContactNo(rs.getString("CONTACTNO"));
                                tempDelinquentAccountBean.setEmail(rs.getString("EMAIL"));
                            }
                            return tempDelinquentAccountBean;
                        },
                        delinquentAccountBean.getCif()
                ));

            } else if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD)) {
                sql = "SELECT CUSTOMERNAME ,CONTACTNO ,EMAIL FROM CARDFDCUSTOMERDETAIL WHERE CUSTOMERID = ?";
                delinquentAccountBean = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                tempDelinquentAccountBean.setNameInFull(rs.getString("NAMEWITHINITIAL"));
                                tempDelinquentAccountBean.setContactNo(rs.getString("CONTACTNO"));
                                tempDelinquentAccountBean.setEmail(rs.getString("EMAIL"));
                            }
                            return tempDelinquentAccountBean;
                        },
                        delinquentAccountBean.getCif()
                ));

            } else if (delinquentAccountBean.getCardCategory().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                sql = "SELECT NAMEOFTHECOMPANY ,CONTACTNUMBERSLAND ,CONTACTEMAIL FROM CARDESTCUSTOMERDETAILS WHERE CUSTOMERID = ?";
                delinquentAccountBean = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                tempDelinquentAccountBean.setNameInFull(rs.getString("NAMEWITHINITIAL"));
                                tempDelinquentAccountBean.setContactNo(rs.getString("CONTACTNO"));
                                tempDelinquentAccountBean.setEmail(rs.getString("EMAIL"));
                            }
                            return tempDelinquentAccountBean;
                        },
                        delinquentAccountBean.getCif()
                ));
            }
        } catch (Exception e) {
            logManager.logError("Exception in Set Delinquent Account Details ", errorLogger);
            throw e;
        }
        return delinquentAccountBean;
    }


    @Override
    public double getTotalPaymentSinceLastDue(String accountNum, java.util.Date EOD_DATE, java.util.Date dueDate) throws Exception {
        double payment = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "SELECT SUM(TRANSACTIONAMOUNT) AS TOTAL FROM EODTRANSACTION WHERE ACCOUNTNO = ? AND TRANSACTIONTYPE IN (?,?,?,?)  AND SETTLEMENTDATE > TO_DATE(?, 'DD-MM-YY')  AND SETTLEMENTDATE <= TO_DATE(?, 'DD-MM-YY')  AND STATUS NOT IN (?)";
            payment = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        double temp = 0;
                        while (rs.next()) {
                            temp = rs.getDouble("TOTAL");
                        }
                        return temp;
                    },
                    accountNum,
                    Configurations.TXN_TYPE_PAYMENT,
                    Configurations.TXN_TYPE_REVERSAL,
                    Configurations.TXN_TYPE_REFUND,
                    Configurations.TXN_TYPE_MVISA_REFUND,
                    sdf.format(dueDate),
                    sdf.format(EOD_DATE),
                    statusList.getCHEQUE_RETURN_STATUS()
            ));
        } catch (Exception e) {
            logManager.logError("Exception in Get Total Payment Sync Last Due ", errorLogger);
            throw e;
        }
        return payment;
    }

    @Override
    public String[] getRiskclassOnNdia(int noOfDates) throws Exception {
        String[] newRiskClass = new String[3];
        String sql = null;
        try {
            sql = "SELECT BUCKETID,MINNDIA FROM BUCKET WHERE MINNDIA <= ? AND MAXNDIA >= ? ";
            newRiskClass[0] = Integer.toString(noOfDates);
            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        newRiskClass[1] = rs.getString("BUCKETID");
                        newRiskClass[2] = rs.getString("MINNDIA");
                        while (rs.next()) {
                            rs.getDouble("TOTAL");
                        }
                    },
                    noOfDates,
                    noOfDates
            );
        } catch (Exception e) {
            logManager.logError("Exception in Get Risk Class On Ndia ", errorLogger);
            throw e;
        }
        return newRiskClass;
    }

    @Override
    public int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "INSERT INTO EODGLACCOUNT (EODID,GLDATE,CARDNO,GLTYPE,AMOUNT,CRDR,PAYMENTTYPE) VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?,?)";
            count = backendJdbcTemplate.update(sql,
                    eodID,
                    sdf.format(glDate),
                    cardNo,
                    glType,
                    String.valueOf(amount),
                    cdStatus,
                    payType
            );
        } catch (Exception e) {
            logManager.logError("Exception in Insert Into EOD Gl Account ", errorLogger);
            throw e;
        }
        return count;
    }

    @Override
    public int addDetailsForManualNPToDelinquentAccountTable(DelinquentAccountBean delinquentAccountBean) throws Exception {

        int count = 0;
        boolean status = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "SELECT DELINQSTATUS FROM DELINQUENTACCOUNT WHERE CARDNUMBER = ?";
            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(sql, countCallback, delinquentAccountBean.getCardNumber());
            int rowCount = countCallback.getRowCount();
            if (rowCount > 0) {
                status = true;
            }
            if (status) {
                sql = "UPDATE DELINQUENTACCOUNT SET NPINTEREST = ?, NPOUTSTANDING = ?, NPDATE = TO_DATE(?,'DD-MM-YY'),  NPACCRUEDINTEREST = ?, NPACCRUEDFEES = ?, NPACCRUEDOVERLIMITFEES = ?, NPACCRUEDLATEPAYFEES = ?, DELINQSTATUS = ?, ACCSTATUS = ? WHERE ACCOUNTNO = ? ";
                count = backendJdbcTemplate.update(sql,
                        delinquentAccountBean.getNpInterest(),
                        delinquentAccountBean.getNpOutstanding(),
                        sdf.format(delinquentAccountBean.getNpDate()),
                        delinquentAccountBean.getAccruedInterest(),
                        delinquentAccountBean.getAccruedFees(),
                        delinquentAccountBean.getAccruedOverLimit(),
                        delinquentAccountBean.getAccruedlatePay(),
                        delinquentAccountBean.getDelinqstatus(),
                        delinquentAccountBean.getAccStatus(),
                        delinquentAccountBean.getAccNo()
                );
            } else {
                sql = "INSERT INTO DELINQUENTACCOUNT(CARDNUMBER,MAINCARDNO,ACCOUNTNO,CIF,NAMEONCARD,NAMEINFULL,IDTYPE,IDNUMBER,NDIA,MIA,RISKCLASS,DUEAMOUNT,ACCSTATUS,CARDCATEGORYCODE,LASTSTATEMENTDATE,LASTUPDATEDUSER,LASTUPDATEDTIME,CREATEDTIME,CONTACTNO,DUEDATE,DELINQSTATUS,ASSIGNEE,ASSIGNSTATUS,SUPERVISOR,REMAINDUE,LASTUPDATEDEODID)  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),?,TO_DATE(SYSDATE, 'DD-MM-YY') ,TO_DATE(SYSDATE, 'DD-MM-YY'),?,TO_DATE(?, 'DD-MM-YY'),?,?,?,?,?,?)";
                count = backendJdbcTemplate.update(sql,
                        delinquentAccountBean.getCardNumber(),
                        delinquentAccountBean.getCardNumber(),
                        sdf.format(delinquentAccountBean.getAccNo()),
                        delinquentAccountBean.getCif(),
                        delinquentAccountBean.getNameOnCard(),
                        delinquentAccountBean.getNameInFull(),
                        delinquentAccountBean.getIdType(),
                        delinquentAccountBean.getIdNumber(),
                        delinquentAccountBean.getNDIA(),
                        delinquentAccountBean.getMIA(),
                        delinquentAccountBean.getRiskClass(),
                        Double.parseDouble(delinquentAccountBean.getDueAmount()),
                        delinquentAccountBean.getAccStatus(),
                        delinquentAccountBean.getCardCategory(),
                        sdf.format(delinquentAccountBean.getLastStatementDate()),
                        Configurations.EOD_USER,
                        delinquentAccountBean.getContactNo(),
                        sdf.format(delinquentAccountBean.getDueDate()),
                        statusList.getONLY_MANUAL_NP_STATUS(),
                        delinquentAccountBean.getAssignee(),
                        delinquentAccountBean.getAssignStatus(),
                        delinquentAccountBean.getSupervisor(),
                        delinquentAccountBean.getRemainDue(),
                        Configurations.EOD_ID
                );
            }
        } catch (Exception e) {
            logManager.logError("Exception in Add Details For Manual N PTo Delinquent Account Table ", errorLogger);
            throw e;
        }
        return count;
    }

    @Override
    public int updateManualNPtoComplete(int reqID, String status) throws Exception {
        String sql = null;
        int flag = 0;
        try {
            sql = "UPDATE MANUALNPREQUEST SET STATUS = ? WHERE REQUESTID = ?";
            flag = backendJdbcTemplate.update(sql,
                    status,
                    reqID
            );
        } catch (Exception e) {
            logManager.logError("Exception in Update Manual Np To Compelete ", errorLogger);
            throw e;
        }
        return flag;
    }

    @Override
    public String getNPRiskClass() throws Exception {
        String npRiskClass = null;
        try {
            String query = "SELECT NVL(NONPERFORMINGRISKCLASS,'4') AS NONPERFORMINGRISKCLASS FROM COMMONCARDPARAMETER";
            npRiskClass = Objects.requireNonNull(backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        String tempNpRiskClass = null;
                        while (rs.next()) {
                            tempNpRiskClass = rs.getString("NONPERFORMINGRISKCLASS");
                        }
                        return tempNpRiskClass;
                    }
            ));
        } catch (Exception e) {
            logManager.logError("Exception in Get NP Risk CLass ", errorLogger);
            throw e;
        }
        return npRiskClass;
    }

    @Override
    public String[] getNDIAOnRiskClass(String riskClass) throws Exception {
        String[] bucket = new String[3];
        String query = null;
        try {
            query = "SELECT BUCKETID,MINNDIA,MAXNDIA,NOOFDAYSINAREERS FROM BUCKET WHERE BUCKETID =? ";
            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            bucket[0] = rs.getString("BUCKETID");
                            bucket[1] = rs.getString("MINNDIA");
                            bucket[2] = rs.getString("MAXNDIA");
                        }
                        return bucket;
                    },
                    riskClass
            );
        } catch (Exception e) {
            logManager.logError("Exception in Get NDIA On Risk Class ", errorLogger);
            throw e;
        }
        return bucket;
    }

    @Override
    public int getNPDetailsForNpGl(String accNo, DelinquentAccountBean delinquentAccountBean) throws Exception {
        int count = 0;
        try {
            String sql = "SELECT NPINTEREST, NPOUTSTANDING, NPDATE, NPACCRUEDINTEREST, NPACCRUEDFEES, NPACCRUEDOVERLIMITFEES, NPACCRUEDLATEPAYFEES FROM DELINQUENTACCOUNT WHERE ACCOUNTNO = ?";
            count = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        int temp = 0;
                        while (rs.next()) {
                            delinquentAccountBean.setNpInterest(rs.getDouble("NPINTEREST"));
                            delinquentAccountBean.setNpOutstanding(rs.getDouble("NPOUTSTANDING"));
                            delinquentAccountBean.setAccruedInterest(rs.getDouble("NPACCRUEDINTEREST"));
                            delinquentAccountBean.setAccruedFees(rs.getDouble("NPACCRUEDFEES"));
                            delinquentAccountBean.setAccruedlatePay(rs.getDouble("NPACCRUEDLATEPAYFEES"));
                            delinquentAccountBean.setAccruedOverLimit(rs.getDouble("NPACCRUEDOVERLIMITFEES"));
                            delinquentAccountBean.setNpDate(rs.getDate("NPDATE"));
                            temp = 1;
                        }
                        return temp;
                    },
                    accNo
            ));
        } catch (Exception e) {
            logManager.logError("Exception in Get Np Details for Np GL ", errorLogger);
            throw e;
        }
        return count;
    }

    @Override
    public int updateDelinquentAccountForManualNP(String accNo, DelinquentAccountBean bean) throws Exception {
        String query = null;
        int flag = 0;
        try {
            query = "UPDATE DELINQUENTACCOUNT SET NPINTEREST = ?, NPOUTSTANDING = ?, NPDATE = NULL,  NPACCRUEDINTEREST = ?, NPACCRUEDFEES = ?, NPACCRUEDOVERLIMITFEES = ?, NPACCRUEDLATEPAYFEES = ?, DELINQSTATUS = ?, ACCSTATUS = ? WHERE ACCOUNTNO = ? ";
            flag = backendJdbcTemplate.update(query,
                    bean.getNpInterest(),
                    bean.getNpOutstanding(),
                    bean.getAccruedInterest(),
                    bean.getAccruedFees(),
                    bean.getAccruedOverLimit(),
                    bean.getAccruedlatePay(),
                    bean.getDelinqstatus(),
                    bean.getAccStatus(),
                    accNo
            );
        } catch (Exception e) {
            logManager.logError("Exception in Update Delinquent Account For Manual Np ", errorLogger);
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
            logManager.logError("Exception in Update Account Status ", errorLogger);
            throw e;
        }
        return flag;
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
                /**Only for troubleshoot*/
                logManager.logInfo("================ updateOnlineAccountStatus ===================" + Configurations.EOD_ID,infoLogger);
                logManager.logInfo(sql,infoLogger);
                logManager.logInfo(Integer.toString(status),infoLogger);
                logManager.logInfo(accNo,infoLogger);
                logManager.logInfo("================ updateOnlineAccountStatus END ===================",infoLogger);
            }
        } catch (Exception e) {
            logManager.logError("Exception in Update Online Account Status ", errorLogger);
            throw e;
        }
        return flag;
    }
}
