package com.epic.cms.repository;

import com.epic.cms.Exception.FailedCardException;
import com.epic.cms.dao.MerchantStatementDao;
import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.*;




@Repository
public class MerchantStatementRepository implements MerchantStatementDao {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    StatusVarList statusVarList;

    @Autowired
    JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Override
    public HashMap<String, MerchantLocationBean> getMerchantlocationstobill() {

        String type = "location";
        HashMap<String, MerchantLocationBean> merchantLocationList = new HashMap<String, MerchantLocationBean>();
        String query = "SELECT ML.MERCHANTID, ML.DESCRIPTION, ML.MERCHANTCUSTOMERNO, MC.MERCHANTNAME, NVL(ML.ADDRESS1, ' ') AS ADDRESS1, NVL(ML.ADDRESS2, ' ') AS ADDRESS2, NVL(ML.ADDRESS3, ' ') AS ADDRESS3, NVL(ML.POSTALCODE, ' ') AS POSTALCODE, NVL(ML.MERCHANTTYPE, ' ') AS MERCHANTTYPE, NVL(ML.MERCHANTEMAIL, ' ') AS MERCHANTEMAIL, C.CURRENCYALPHACODE AS CURRENCYTYPE, B.BANKNAME, PM.DESCRIPTION AS PAYMENTMODE, ML.STATEMENTMAINTEINANCESTATUS, ML.STATEMENTCYCLE, ML.STATUS, ML.NEXTBILLINGDATE, ML.ACCOUNTNUMBER, ML.ACCOUNTNAME, ML.ACCOUNTTYPE,ML.CREATEDTIME, ML.MERCHANTACCOUNTNO FROM MERCHANTLOCATION ML INNER JOIN MERCHANTCUSTOMER MC ON ML.MERCHANTCUSTOMERNO = MC.MERCHANTCUSTOMERNO INNER JOIN CURRENCY C ON ML.CURRENCYCODE = C.CURRENCYNUMCODE INNER JOIN BANK B ON ML.BANKCODE = B.BANKCODE INNER JOIN PAYMENTMODE PM ON ML.PAYMENTMODE = PM.PAYMENTMODECODE WHERE TRUNC(ML.NEXTBILLINGDATE) = ? AND ML.STATUS NOT IN(?,?,?)   AND ML.STATEMENTMAINTEINANCESTATUS='" + statusVarList.getSTATUS_YES() + "' AND ";

        if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
            query += "ML.MERCHANTID NOT IN (SELECT NVL(EM.MID,'000') FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusVarList.getEOD_PENDING_STATUS() + "')";
        } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
            query += " ML.MERCHANTID IN (SELECT NVL(EM.MID,'000') FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusVarList.getEOD_PENDING_STATUS() + "' AND EODID < " + Configurations.ERROR_EOD_ID + " AND EM.processstepid <= (select er.STEPID from EODPROCESSFLOW er where er.PROCESSID = '" + Configurations.PROCESS_ID_MERCHANT_STATEMENT + "'))";
        }
        query += " ORDER BY ML.MERCHANTID ";

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");
            String dateEID = format.format(Configurations.EOD_DATE);

            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        while (result.next()) {

                            MerchantLocationBean mlb = new MerchantLocationBean();

                            mlb.setMerchantId(result.getString("MERCHANTID"));
                            mlb.setMerchantDes(result.getString("DESCRIPTION"));
                            mlb.setMerchantCusNo(result.getString("MERCHANTCUSTOMERNO"));
                            mlb.setAddress1(result.getString("ADDRESS1"));
                            mlb.setAddress2(result.getString("ADDRESS2"));
                            mlb.setAddress3(result.getString("ADDRESS3"));
                            mlb.setStatementmaintenanceStatus(result.getString("STATEMENTMAINTEINANCESTATUS"));
                            mlb.setMerchantStatCycleCode(result.getString("STATEMENTCYCLE"));
                            mlb.setStatus(result.getString("STATUS"));
                            mlb.setBillingDate(result.getDate("NEXTBILLINGDATE"));
                            mlb.setAccNumber(result.getString("ACCOUNTNUMBER"));
                            mlb.setMerchantCusName(result.getString("MERCHANTNAME"));
                            mlb.setPostalCode(result.getString("POSTALCODE"));
                            mlb.setMerchantType(result.getString("MERCHANTTYPE"));
                            mlb.setMerchantCurrency(result.getString("CURRENCYTYPE"));
                            mlb.setMerchantEmail(result.getString("MERCHANTEMAIL"));
                            mlb.setBankName(result.getString("BANKNAME"));
                            mlb.setPaymentMode(result.getString("PAYMENTMODE"));


                            try {
                                mlb.isFirstStatement(isFirstMerchantStatement(result.getString("MERCHANTID"), type));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            if (mlb.isFirstStatement()) {
                                mlb.setLastBillingDate(result.getDate("CREATEDTIME"));
                            } else {
                                try {
                                    mlb.setLastBillingDate(getLastMerchantBillingDate(result.getString("MERCHANTID"), type));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            merchantLocationList.put(mlb.getMerchantId(), mlb);
                        }
                        return merchantLocationList;

                    }
                    , dateEID
                    , statusVarList.getMERCHANT_DELETE_STATUS() //DELMER
                    , statusVarList.getMERCHANT_CANCEL_STATUS() //CANMER
                    , statusVarList.getMERCHANT_DEACTIVE_STATUS() // newly Add
            );

        } catch (Exception e) {
            throw e;
        }

        return merchantLocationList;
    }

    @Override
    public MerchantLocationBean insertMerchantStatement(MerchantLocationBean bean) throws Exception {
        try {
            this.getMerchantPayments(bean);

            if (!bean.isFirstStatement()) {
                this.getLastmerchantStatementDetails(bean);
            } else {
                bean.setStartEodId(0);
            }

            String StatementID = new SimpleDateFormat("yyMMHHmmssSSS").format(new java.util.Date()) + bean.getMerchantId();
            bean.setStatementID(StatementID);
            bean.setEndEodId(Configurations.EOD_ID);

            double prvbal = getPreviuosBalance(bean.getMerchantId(), bean.getStartEodId());
            bean.setOpeningBalance(prvbal);

            double cloBal = prvbal + bean.getNetPaymentAmount();
            bean.setClosingBalance(cloBal);

            boolean isInsertedMerchantStatement = insertInToMerchantStatementTable(bean);
            //this is new modification.//
            if (!isInsertedMerchantStatement) {
                throw new FailedCardException("merchant ID " + bean.getMerchantId() + "fails to insert data into merchant statement table");
            }

            if (!this.updateMerchantBillingDate(bean)) {
                //logLevel3.info("Error Occured in update next billing date for MID " + bean.getMerchantId() + ". ");
                logError.error(logManager.logStartEnd("Error Occured in update next billing date for MID " + bean.getMerchantId() + ". "));

            }

        } catch (Exception ex) {
            throw ex;
        }
        return bean;
    }

    @Override
    public MerchantLocationBean getMerchantPayments(MerchantLocationBean bean) throws Exception {

        String query1 = null;
        ArrayList<String> paymentList = new ArrayList<String>();
        String type = "location";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            if (!bean.isFirstStatement()) {
                query1 = "SELECT NETPAYAMMOUNT,CRDRNET,TOTALPAYAMOUNT,CRDR,TOTALCOMMISSION,CRDRCOMMISSION,TOTALFEEAMOUNT,CRDRFEE,EODPAYID,TXNCOUNT FROM EODMERCHANTPAYMENT WHERE PAYMENTDATE > TO_DATE(?,'DD-MM-YY') AND TO_DATE(?,'DD-MM-YY') + 1 > PAYMENTDATE AND MERCHANTID = ? AND STATEMENTFILESTATUS = ?";

                bean = backendJdbcTemplate.query(query1,
                        (ResultSet result) -> {
                            double netPayment = 0.0;
                            double payment = 0.0;
                            double commission = 0.0;
                            double fees = 0.0;
                            int txnCount = 0;

                            MerchantLocationBean merchantLocationBean = new MerchantLocationBean();
                            while (result.next()) {
                                if (result.getString("CRDRNET").equalsIgnoreCase("CR")) {
                                    netPayment += result.getDouble("NETPAYAMMOUNT");
                                } else if (result.getString("CRDRNET").equalsIgnoreCase("DR")) {
                                    netPayment -= result.getDouble("NETPAYAMMOUNT");
                                }

                                if (result.getString("CRDR").equalsIgnoreCase("CR")) {
                                    payment += result.getDouble("TOTALPAYAMOUNT");
                                } else if (result.getString("CRDR").equalsIgnoreCase("DR")) {
                                    payment -= result.getDouble("TOTALPAYAMOUNT");
                                }

                                if (result.getString("CRDRCOMMISSION").equalsIgnoreCase("DR")) {
                                    commission += result.getDouble("TOTALCOMMISSION");
                                } else if (result.getString("CRDRCOMMISSION").equalsIgnoreCase("CR")) {
                                    commission -= result.getDouble("TOTALCOMMISSION");
                                }

                                if (result.getString("CRDRFEE").equalsIgnoreCase("DR")) {
                                    fees += result.getDouble("TOTALFEEAMOUNT");
                                } else if (result.getString("CRDRFEE").equalsIgnoreCase("CR")) {
                                    fees -= result.getDouble("TOTALFEEAMOUNT");
                                }

                                txnCount += result.getInt("TXNCOUNT");

                                paymentList.add(result.getString("EODPAYID"));
                            }

                            merchantLocationBean.setNetPaymentAmount(netPayment);
                            merchantLocationBean.setPaymentAmount(payment);
                            merchantLocationBean.setCommissionAmount(commission);
                            merchantLocationBean.setFeeAmount(fees);
                            merchantLocationBean.setTxnCount(txnCount);

                            if (paymentList.size() > 0) {
                                int value = 0;
                                try {
                                    value = this.updateMerchantPayment(paymentList, type);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("Updated " + value + "from list of " + paymentList.size());
                            }
                            return merchantLocationBean;
                        },
                        sdf.format(bean.getLastBillingDate())
                        , sdf.format(bean.getBillingDate())
                        , bean.getMerchantId()
                        , 0
                );

            } else {
                query1 = "SELECT NETPAYAMMOUNT,CRDRNET,TOTALPAYAMOUNT,CRDR,TOTALCOMMISSION,CRDRCOMMISSION,TOTALFEEAMOUNT,CRDRFEE,EODPAYID,TXNCOUNT "
                        + "FROM EODMERCHANTPAYMENT "
                        + "WHERE PAYMENTDATE < TO_DATE(?,'DD-MM-YY') + 1 AND MERCHANTID = ? AND STATEMENTFILESTATUS = ?";

                bean = backendJdbcTemplate.query(query1,
                        (ResultSet result) -> {
                            double netPayment = 0.0;
                            double payment = 0.0;
                            double commission = 0.0;
                            double fees = 0.0;
                            int txnCount = 0;

                            MerchantLocationBean merchantLocationBean = new MerchantLocationBean();
                            while (result.next()) {
                                if (result.getString("CRDRNET").equalsIgnoreCase("CR")) {
                                    netPayment += result.getDouble("NETPAYAMMOUNT");
                                } else if (result.getString("CRDRNET").equalsIgnoreCase("DR")) {
                                    netPayment -= result.getDouble("NETPAYAMMOUNT");
                                }

                                if (result.getString("CRDR").equalsIgnoreCase("CR")) {
                                    payment += result.getDouble("TOTALPAYAMOUNT");
                                } else if (result.getString("CRDR").equalsIgnoreCase("DR")) {
                                    payment -= result.getDouble("TOTALPAYAMOUNT");
                                }

                                if (result.getString("CRDRCOMMISSION").equalsIgnoreCase("DR")) {
                                    commission += result.getDouble("TOTALCOMMISSION");
                                } else if (result.getString("CRDRCOMMISSION").equalsIgnoreCase("CR")) {
                                    commission -= result.getDouble("TOTALCOMMISSION");
                                }

                                if (result.getString("CRDRFEE").equalsIgnoreCase("DR")) {
                                    fees += result.getDouble("TOTALFEEAMOUNT");
                                } else if (result.getString("CRDRFEE").equalsIgnoreCase("CR")) {
                                    fees -= result.getDouble("TOTALFEEAMOUNT");
                                }

                                txnCount += result.getInt("TXNCOUNT");
                                paymentList.add(result.getString("EODPAYID"));
                            }
                            merchantLocationBean.setNetPaymentAmount(netPayment);
                            merchantLocationBean.setPaymentAmount(payment);
                            merchantLocationBean.setCommissionAmount(commission);
                            merchantLocationBean.setFeeAmount(fees);
                            merchantLocationBean.setTxnCount(txnCount);

                            if (paymentList.size() > 0) {
                                int value = 0;
                                try {
                                    value = this.updateMerchantPayment(paymentList, type);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("Updated " + value + "from list of " + paymentList.size());
                            }
                            return merchantLocationBean;
                        },
                        sdf.format(bean.getBillingDate())
                        , bean.getMerchantId()
                        , 0
                );

            }
            if (paymentList.size() > 0) {
                int value = this.updateMerchantPayment(paymentList, type);
                System.out.println("Updated " + value + "from list of " + paymentList.size());
            }

        } catch (Exception ex) {
            throw ex;
        }
        return bean;
    }

    @Override
    public List<Object[]> getMerchantStatementTxnList(String mID) throws Exception {

        List<Object[]> txnList = new ArrayList<Object[]>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        String query = "SELECT EMC.EODID, MS.STATEMENTID, EMC.MID, EMC.MERCHANTCUSTID, EMT.CARDNUMBER,     EMC.TID,     EMT.AUTHCODE,     C.CURRENCYALPHACODE AS CURRENCYTYPE,     EMT.TRANSACTIONDESCRIPTION,     CASE       WHEN EMC.CRDR = 'CR'       THEN (0 - EMC.TRANSACTIONAMOUNT)       ELSE EMC.TRANSACTIONAMOUNT     END AS TRANSACTIONAMOUNT,     CASE       WHEN EMC.CRDR = 'CR'       THEN (0 - EMC.MERCHANTCOMMSSION)       ELSE EMC.MERCHANTCOMMSSION     END AS MERCHANTCOMISSION,     ((     CASE       WHEN EMC.CRDR = 'CR'       THEN (0 - EMC.TRANSACTIONAMOUNT)       ELSE EMC.TRANSACTIONAMOUNT     END) - (     CASE       WHEN EMC.CRDR = 'CR'       THEN (0 - EMC.MERCHANTCOMMSSION)       ELSE EMC.MERCHANTCOMMSSION     END)) AS NETAMOUNT,     EMC.TRANSACTIONDATE,     EMT.SETTLEMENTDATE   FROM EODMERCHANTCOMMISSION EMC   INNER JOIN MERCHANTLOCATION ML   ON EMC.MID = ML.MERCHANTID   INNER JOIN EODMERCHANTTRANSACTION EMT   ON EMC.TRANSACTIONID = EMT.TRANSACTIONID   INNER JOIN MERCHANTSTATEMENT MS   ON EMC.MID = MS.MID   INNER JOIN CURRENCY C   ON EMC.CURRENCYTYPE       = C.CURRENCYNUMCODE   WHERE MS.ENDEODID         =  ? AND EMC.MID = ?   AND ML.ESTATEMENTSERVICE IN (1,2)   AND EMC.EODID             > MS.STARTEODID   AND EMC.EODID            <= MS.ENDEODID   AND EMC.STATUS            = 'EDON'   AND EMC.ADJUSTMENTFLAG   <> 2 ";

        try {

            txnList = backendJdbcTemplate.query(query, new Object[]{Configurations.EOD_ID, mID}, (ResultSet rs) -> {
                List<Object[]> resultList = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = sdf.format(rs.getDate("SETTLEMENTDATE"));
                    row[1] = sdf.format(rs.getDate("TRANSACTIONDATE"));
                    row[2] = CommonMethods.cardNumberMask(new StringBuffer(rs.getString("CARDNUMBER")));
                    row[3] = rs.getString("TID");
                    row[4] = rs.getString("AUTHCODE");
                    row[5] = rs.getDouble("TRANSACTIONAMOUNT");
                    row[6] = rs.getDouble("MERCHANTCOMISSION");
                    row[7] = rs.getDouble("NETAMOUNT");
                    resultList.add(row);
                }
                return resultList;
            });


        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }

    @Override
    public List<Object[]> getMerchantStatementAdjustmentList(String mID) throws Exception {
        PreparedStatement stmt = null;
        ResultSet result = null;
        List<Object[]> txnList = new ArrayList<Object[]>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        String query = "SELECT CASE  "
                + "    WHEN ADJUSTMENTTYPE = '1' "
                + "    THEN 'Payment' "
                + "    WHEN ADJUSTMENTTYPE = '2' "
                + "    THEN 'Commission' "
                + "    WHEN ADJUSTMENTTYPE = '3' "
                + "    THEN 'Reversal' "
                + "    WHEN ADJUSTMENTTYPE = '4' "
                + "    THEN 'Refund' "
                + "    WHEN ADJUSTMENTTYPE = '5' "
                + "    THEN 'Fees' "
                + "    END AS ADJUSTMENTTYPE, "
                + "    AA.MERCHANTID, "
                + "    AA.REMARKS          AS TRANSACTIONDESCRIPTION, "
                + "    AA.AMOUNT           AS TRANSACTIONAMOUNT, "
                + "    CASE "
                + "    WHEN AA.ADJUSTMENTTYPE IN (4) AND AA.CRDR = 'CR' "
                + "    THEN 'DR' "
                + "    WHEN AA.ADJUSTMENTTYPE IN (4) AND AA.CRDR = 'DR' "
                + "    THEN 'CR' "
                + "    ELSE AA.CRDR "
                + "    END AS CRDR, "
                + "    AA.ADJUSTDATE       AS SETTLEMENTDATE "
                + "  FROM ACQADJUSTMENT AA "
                + "  INNER JOIN MERCHANTLOCATION ML "
                + "  ON AA.MERCHANTID = ML.MERCHANTID "
                + "  INNER JOIN MERCHANTSTATEMENT MS "
                + "  ON AA.MERCHANTID = MS.MID "
                + "  WHERE 1                   =1 AND AA.MERCHANTID = ? "
                + "  AND MS.ENDEODID           =  ? "
                + "  AND ML.ESTATEMENTSERVICE IN (1,2) "
                + "  AND TRUNC(AA.ADJUSTDATE)  > MS.STATEMENTSTARTDATE "
                + "  AND TRUNC(AA.ADJUSTDATE) <= MS.STATEMENTENDDATE "
                + "  AND AA.EODSTATUS          = 'EDON' "
                + "  AND AA.ADJUSTMENTTYPE  IN (1,2,4) ";
//          Adjustment table maintain the CRDR status as from the customer perepective for the refund & reversal. That's why changed the CRDR for refund
//          In this scenario only considering refund only. Reversal will be captured in the txn list

        try {


            txnList = backendJdbcTemplate.query(query, new Object[]{Configurations.EOD_ID, mID}, (ResultSet rs) -> {
                List<Object[]> resultList = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[5];
                    row[0] = rs.getString("ADJUSTMENTTYPE");
                    row[1] = sdf.format(rs.getDate("SETTLEMENTDATE"));
                    row[2] = rs.getString("TRANSACTIONDESCRIPTION");
                    row[3] = rs.getString("TRANSACTIONAMOUNT");
                    row[4] = rs.getString("CRDR");
                    resultList.add(row);
                }
                return resultList;
            });


        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }

    @Override
    public List<Object[]> getMerchantStatementFeesList(String mID) throws Exception {
        PreparedStatement stmt = null;
        ResultSet result = null;
        List<Object[]> txnList = new ArrayList<Object[]>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        String query = "SELECT EMF.MERCHANTID, "
                + "    EMF.CRDR, "
                + "    F.DESCRIPTION       AS TRANSACTIONDESCRIPTION, "
                + "    EMF.FEEAMOUNT       AS TRANSACTIONAMOUNT, "
                + "    EMF.EFFECTDATE      AS SETTLEMENTDATE "
                + "  FROM EODMERCHANTFEE EMF "
                + "  INNER JOIN MERCHANTLOCATION ML "
                + "  ON EMF.MERCHANTID = ML.MERCHANTID "
                + "  INNER JOIN MERCHANTSTATEMENT MS "
                + "  ON EMF.MERCHANTID = MS.MID "
                + "  INNER JOIN FEE F "
                + "  ON EMF.FEETYPE             = F.FEECODE "
                + "  WHERE 1                    =1 AND EMF.MERCHANTID = ? "
                + "  AND MS.ENDEODID            =  ? "
                + "  AND ML.ESTATEMENTSERVICE  IN (1,2) "
                + "  AND TRUNC(EMF.EFFECTDATE)  > MS.STATEMENTSTARTDATE "
                + "  AND TRUNC(EMF.EFFECTDATE) <= MS.STATEMENTENDDATE "
                + "  AND EMF.STATUS             = 'EDON' ";

        try {


            txnList = backendJdbcTemplate.query(query, new Object[]{Configurations.EOD_ID, mID}, (ResultSet rs) -> {
                List<Object[]> resultList = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[4];
                    row[0] = sdf.format(rs.getDate("SETTLEMENTDATE"));
                    row[1] = rs.getString("TRANSACTIONDESCRIPTION");
                    row[2] = rs.getString("TRANSACTIONAMOUNT");
                    row[3] = rs.getString("CRDR");
                    resultList.add(row);
                }
                return resultList;
            });

        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }

    @Override
    public int insertMerchantEodStatus(String type, String status) throws Exception {
        PreparedStatement pst = null;
        int count = 0;

        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);

            String sql = "INSERT INTO MERCHANT_STMT_EODSTATUS(EODDATE, TYPE, STATUS) VALUES( ?, ?, ?)";
            count = backendJdbcTemplate.update(sql, Integer.parseInt(date),
                    type,
                    status
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertAuditMerchantEodStatus(String type, String status) throws Exception {
        PreparedStatement pst = null;
        int count = 0;

        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);

            String sql = "INSERT INTO AUDIT_MERCHANT_STMT_EODSTATUS(EODDATE, TYPE, STATUS) VALUES( ?, ?, ?)";

            count = backendJdbcTemplate.update(sql, Integer.parseInt(date),
                    type,
                    status
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void callMerchantStatementProcedure() throws Exception {
        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);
            String newEodid = date + "00";

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("MERCHANT_STMT_AP_PROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EID", newEodid);

            simpleJdbcCall.execute(in);

        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    public void callAuditMerchantStatementProcedure() throws Exception {
        CallableStatement cstmt = null;
        String query = null;
        int count = 0;
        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);
            String newEodid = date + "00";

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("AUDIT_MERCHANT_STMT_PROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EID", newEodid);

            simpleJdbcCall.execute(in);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public MerchantLocationBean getLastmerchantStatementDetails(MerchantLocationBean bean) throws Exception {
        PreparedStatement stmt = null;
        //ResultSet rs = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        MerchantLocationBean merchantLocationBean = new MerchantLocationBean();

        String query = "SELECT MS.ENDEODID "
                + "FROM MERCHANTSTATEMENT MS "
                + "WHERE MS.MID = ? AND MS.STATEMENTENDDATE = TO_DATE(?,'DD-MM-YY')";

        try {

            bean = Objects.requireNonNull(backendJdbcTemplate.query(query,

                    (ResultSet rs) -> {
                        while (rs.next()) {

                            merchantLocationBean.setStartEodId(rs.getInt("ENDEODID"));

                        }
                        return merchantLocationBean;
                    },
                    bean.getMerchantId(),
                    sdf.format(bean.getLastBillingDate())
            ));

        } catch (Exception e) {
            throw e;
        }
        return bean;
    }

    @Override
    public double getPreviuosBalance(String mId, int startEodId) throws Exception {
        String sql = null;

        double count = 0;

        try {
            sql = "SELECT NVL(SUM( "
                    + "  CASE "
                    + "    WHEN CRDR = 'DR' "
                    + "    THEN (-NETPAYAMMOUNT) "
                    + "    ELSE NETPAYAMMOUNT "
                    + "  END),0) AS TOTALNETPAY "
                    + "FROM EODMERCHANTPAYMENT "
                    + "WHERE MERCHANTID      = ? "
                    + "AND PAYMENTFILESTATUS = 0 "
                    + "AND EODID < ? ";


            count = backendJdbcTemplate.queryForObject(sql,
                    (result, rowNum) -> {
                        double prevbal = 0.0;

                        while (result.next()) {

                            prevbal = result.getDouble("TOTALNETPAY");
                        }
                        return prevbal;
                    },
                    mId,
                    startEodId);

        } catch (EmptyResultDataAccessException ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public boolean insertInToMerchantStatementTable(MerchantLocationBean bean) throws Exception {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        boolean flag = false;
        int count = 0;
        String query = "INSERT INTO MERCHANTSTATEMENT(STATEMENTID,MID,STATEMENTSTARTDATE,STATEMENTENDDATE,     COMMISIONS,FEES,PAYMENT,TOTALTXNAMOUNT,STATUS,     BILLINGCYCLEID,LASTUPDATEUSERID,STARTEODID,ENDEODID,     ACCOUNTNO,STATEMENTGENERATEDSTATUS,     CASHDEPOSITS,CASHADVANCE,OPENINGLOYALTYPOINT,EARNLOYALTYPOINT,     AVLOYALTYPOINT,ADJUSTLOYALTYPOINT,REDEEMLOYALTYPOINT,CLOSINGLOYALTYPOINT,TIMESTAMP,MERCHANTCUSNO,TXNCOUNT)   VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

        try {

            count = backendJdbcTemplate.update(query,
                    bean.getStatementID(),
                    bean.getMerchantId(),//MID
                    DateUtil.getSqldate(bean.getLastBillingDate()),//STATEMENT START DATE
                    DateUtil.getSqldate(bean.getBillingDate()),//STATEMENT END DATE
                    bean.getCommissionAmount(),//COMMISSION
                    bean.getFeeAmount(),//FEES
                    bean.getNetPaymentAmount(),//PAYMENT
                    bean.getPaymentAmount(),//TOTALTXNAMOUNT(TOTALPAYAMOUNT)
                    "0",//STATUS
                    bean.getMerchantStatCycleCode(),//BILLINGCYCLEID
                    0,//LASTUPDATEDUSERID,
                    bean.getStartEodId(),//STARTEODID
                    bean.getEndEodId(),//ENDEODID
                    bean.getAccNumber(),//ACCOUNTNO
                    0,//STATEMENTGENRATEDSTATUS
                    0,//CASHDEPOSITS
                    0,//CASHADVANCE
                    0,//OPENINGLOYALTYPOINT
                    0,//EARNLOYALTYPOINT
                    0,//AVLOYALTYPOINT
                    0,//ADJUSTLOYALTYPOINT
                    0,//REDEEMLOYALTYPOINT
                    0,//CLOSINGLOYALTYPOINT
                    DateUtil.getSqldate(Configurations.EOD_DATE),//TIMESTAMP (Date) Configurations.EOD_DATE
                    bean.getMerchantCusNo(),//MERCHANT CUSTOMER NO
                    bean.getTxnCount()

            );

            details.put("Mrechant ID", bean.getMerchantId());
            details.put("Total Txn Amount", bean.getPaymentAmount());
            details.put("Total Payment", Double.toString(bean.getNetPaymentAmount()));
            details.put("Total Fees", Double.toString(bean.getFeeAmount()));
            details.put("Mrechant Description", bean.getMerchantDes());
            details.put("Merchant Customer No", bean.getMerchantCusNo());
            details.put("Account Number", bean.getAccNumber());
            details.put("Total Commisions", Double.toString(bean.getCommissionAmount()));
            details.put("Statement Cycle Code", bean.getMerchantStatCycleCode());

            //logLevel3.info(logLevels.processDetailsStyles(details));
            logInfo.info(logManager.logDetails(details));

            if (count > 0) {
                flag = true;
            }

        } catch (Exception e) {
            throw e;
        }
        return flag;

    }

    @Override
    public boolean updateMerchantBillingDate(MerchantLocationBean bean) throws Exception {
        int count = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        String query = "SELECT "
                + "  CASE "
                + "    WHEN MBC.BILLINGOPTION = '1' "
                + "    THEN TO_DATE(ML.NEXTBILLINGDATE) + 1 "
                + "    WHEN MBC.BILLINGOPTION = '2' "
                + "    THEN TO_DATE(ML.NEXTBILLINGDATE) + 7 "
                + "    WHEN MBC.BILLINGOPTION = '3' "
                + "    THEN LAST_DAY((ML.NEXTBILLINGDATE)) + MBC.BILLINGDATE "
                + "    WHEN MBC.BILLINGOPTION = '4' "
                + "    THEN TO_DATE(ML.NEXTBILLINGDATE) + MBC.NOOFDAYS "
                + "  END AS NEWNEXTBILLINGDATE, "
                + "  ML.NEXTBILLINGDATE, "
                + "  MBC.BILLINGOPTION, "
                + "  MBC.BILLINGDATE "
                + "FROM "
                + "  MERCHANTLOCATION ML "
                + "INNER JOIN MERCHANTBILLINGCYCLE MBC "
                + "ON "
                + "  ML.STATEMENTCYCLE = MBC.BILLINGCYCLECODE "
                + "WHERE "
                + "  MERCHANTID=?";

        try {


            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        if (rs.next()) {
                            bean.setNextBillingDate(rs.getDate("NEWNEXTBILLINGDATE"));
                        }
                    },
                    bean.getMerchantId()
            );

            if (bean.getNextBillingDate() != null) {
                String updateQuery = "UPDATE MERCHANTLOCATION "
                        + "SET NEXTBILLINGDATE = TO_DATE(?,'DD-MM-YY') "
                        + "WHERE MERCHANTID = ? ";

                count = backendJdbcTemplate.update(updateQuery,
                        sdf.format(bean.getNextBillingDate()),
                        bean.getMerchantId()
                );

                if (count > 0) {
                    return true;
                } else {
                    return false;
                }
            }
            if (bean.getNextBillingDate() != null) {
                String updateQuery = "UPDATE MERCHANTLOCATION "
                        + "SET NEXTBILLINGDATE = TO_DATE(?,'DD-MM-YY') "
                        + "WHERE MERCHANTID = ? ";

                count = backendJdbcTemplate.update(updateQuery,
                        sdf.format(bean.getNextBillingDate()),
                        bean.getMerchantId()
                );

                if (count > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateMerchantPayment(ArrayList<String> paymentList, String type) throws Exception {

        int i = 0;

        try {
            String query = "UPDATE EODMERCHANTPAYMENT SET STATEMENTFILESTATUS = ? WHERE EODPAYID = ?";

            for (String id : paymentList) {
                if (type.equals("location")) {
                    i = backendJdbcTemplate.update(query, 1);
                } else if (type.equals("customer")) {
                    i = backendJdbcTemplate.update(query, 2);
                }

                i = backendJdbcTemplate.update(query, id);
            }

        } catch (Exception ex) {
            throw ex;
        }
        return i;
    }

    public boolean isFirstMerchantStatement(String id, String type) throws Exception {
        boolean flag = false;
        String query = "";
        int reportCount = 0;

        try {
            if (type.equals("location")) {
                query = "SELECT MS.MID FROM MERCHANTSTATEMENT MS WHERE MS.MID = ? ";
            } else if (type.equals("customer")) {
                query = "SELECT MCS.MERCHANTCUSNO FROM MERCHANTCUSTOMERSTATEMENT MCS WHERE MCS.MERCHANTCUSNO = ? ";
            }

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(query, countCallback, id);
            reportCount = countCallback.getRowCount();

            if (reportCount > 0) {
                flag = false;
            } else {
                flag = true;
            }

        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    public Date getLastMerchantBillingDate(String id, String type) throws Exception {

        String query = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        Date startEodDate = null;
        Date formattedDate = null;

        try {
            if (type.equals("location")) {
                query = "SELECT MAX(MS.STATEMENTENDDATE) AS STARTEODID "
                        + "FROM MERCHANTSTATEMENT MS "
                        + "WHERE MS.MID = ? "
                        + "GROUP BY MS.MID";
            } else if (type.equals("customer")) {
                query = "SELECT MAX(MCS.STATEMENTENDDATE) AS STARTEODID "
                        + "FROM MERCHANTCUSTOMERSTATEMENT MCS "
                        + "WHERE MCS.MERCHANTCUSNO = ? "
                        + "GROUP BY MCS.MERCHANTCUSNO";
            }
            try {
                startEodDate = backendJdbcTemplate.queryForObject(query, Date.class, id);

                if (startEodDate != null) {
                    try {
                        String formattedDateString = sdf.format(startEodDate);
                        formattedDate = sdf.parse(formattedDateString);
                    } catch (ParseException e) {
                        throw e;
                    }
                }
            } catch (EmptyResultDataAccessException e) {

                return null;
            } catch (DataAccessException e) {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
        return formattedDate;
    }
}
