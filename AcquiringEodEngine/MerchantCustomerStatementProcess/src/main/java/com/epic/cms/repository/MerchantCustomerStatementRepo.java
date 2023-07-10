/**
 * Author : lahiru_p
 * Date : 6/26/2023
 * Time : 3:39 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MerchantCustomerStatementDao;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class MerchantCustomerStatementRepo implements MerchantCustomerStatementDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public HashMap<String, MerchantCustomerBean> getMerchantCustomersToBill() throws Exception {
        String type = "customer";

        HashMap<String, MerchantCustomerBean> merchantCustomerList = new HashMap<String, MerchantCustomerBean>();

        String query = "SELECT MC.MERCHANTCUSTOMERNO, "
                + "  MC.MERCHANTNAME, "
                + "  MC.LEGALNAME, "
                + "  MC.ADDRESS1, "
                + "  MC.ADDRESS2, "
                + "  MC.ADDRESS3, "
                + "  MC.STATEMENTMAINTEINANCESTATUS, "
                + "  MC.STATEMENTCYCLE, "
                + "  MC.STATUS, "
                + "  MC.NEXTBILLINGDATE, "
                + "  MC.ACCOUNTNUMBER, "
                + "  MC.ACCOUNTNAME, "
                + "  MC.ACCOUNTTYPE, "
                + "  MC.CREATEDTIME, "
                + "  MC.MERCHANTACCOUNTNO "
                + "FROM MERCHANTCUSTOMER MC "
                + "WHERE TRUNC(MC.NEXTBILLINGDATE) = ?  "
                + "AND MC.STATUS NOT IN (?,?) AND MC.STATEMENTMAINTEINANCESTATUS='" + statusList.getSTATUS_YES() + "' AND ";

        if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
            query += " MC.MERCHANTCUSTOMERNO NOT IN (SELECT EM.MERCHANTCUSTOMERNO FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusList.getEOD_PENDING_STATUS() + "')";
        } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
            query += " MC.MERCHANTCUSTOMERNO IN (SELECT EM.MERCHANTCUSTOMERNO FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusList.getEOD_PENDING_STATUS() + "' AND EODID < " + Configurations.ERROR_EOD_ID + " AND EM.processstepid <= (select er.STEPID from EODPROCESSFLOW er where er.PROCESSID = '" + Configurations.PROCESS_ID_MERCHANT_CUSTOMER_STATEMENT + "'))";
        }
        query += " ORDER BY MC.MERCHANTCUSTOMERNO ";

        try {
            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        while (result.next()) {
                            MerchantCustomerBean mcb = new MerchantCustomerBean();
                            mcb.setMerchantCusNo(result.getString("MERCHANTCUSTOMERNO"));
                            mcb.setMerchantCusDes(result.getString("MERCHANTNAME"));
                            mcb.setLegalName(result.getString("LEGALNAME"));
                            mcb.setAddress1(result.getString("ADDRESS1"));
                            mcb.setAddress2(result.getString("ADDRESS2"));
                            mcb.setAddress3(result.getString("ADDRESS3"));
                            mcb.setStatementMaintananceStatus(result.getString("STATEMENTMAINTEINANCESTATUS"));
                            mcb.setStatementCycleCode(result.getString("STATEMENTCYCLE"));
                            mcb.setStatus(result.getString("STATUS"));
                            mcb.setBillingDate(result.getDate("NEXTBILLINGDATE"));
                            mcb.setAccountNo(result.getString("ACCOUNTNUMBER"));

                            try {
                                mcb.setFirstStatement(isFirstMerchantStatement(result.getString("MERCHANTCUSTOMERNO"),type));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            if (mcb.isFirstStatement()) {
                                mcb.setLastBillingDate(result.getDate("CREATEDTIME"));
                            } else {
                                try {
                                    mcb.setLastBillingDate(getLastMerchantBillingDate(result.getString("MERCHANTCUSTOMERNO"), type));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            merchantCustomerList.put(mcb.getMerchantCusNo(), mcb);
                        }
                    });

        } catch (Exception e) {
            logError.error("Error Occured, when Selecting merchant Customers For Billing");
        }
        return merchantCustomerList;
    }

    public Date getLastMerchantBillingDate(String id, String type) throws Exception {
        String query = "";
        Date startEodDate;
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

            startEodDate = backendJdbcTemplate.queryForObject(query, Date.class, id);

            return startEodDate;

        }catch (Exception e){
            throw e;
        }
    }

    public boolean isFirstMerchantStatement(String id, String type) throws SQLException, Exception  {
        boolean flag = true;
        String query = "";
        String merchant = "";
        try {
            if (type.equals("location")) {
                query = "SELECT MS.MID "
                        + "FROM MERCHANTSTATEMENT MS "
                        + "WHERE MS.MID = ? ";

            } else if (type.equals("customer")) {
                query = "SELECT MCS.MERCHANTCUSNO "
                        + "FROM MERCHANTCUSTOMERSTATEMENT MCS "
                        + "WHERE MCS.MERCHANTCUSNO = ? ";
            }
            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(query, countCallback, id);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                flag = false;
            }

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e){
            throw e;
        }
        return flag;
    }

    @Override
    public void insertMerchantCustomerStatement(MerchantCustomerBean merchantBean) throws Exception {
        try {

        }catch (Exception e){

        }
    }

    @Override
    public int insertMerchantEodStatus(String type, String status) throws Exception {
        int count = 0;
        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);

            String sql = "INSERT INTO MERCHANT_STMT_EODSTATUS(EODDATE, TYPE, STATUS) VALUES( ?, ?, ?)";

            count = backendJdbcTemplate.update(sql, Integer.parseInt(date), type, status);
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public int insertAuditMerchantEodStatus(String type, String status) throws Exception {
        int count = 0;
        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);

            String sql = "INSERT INTO AUDIT_MERCHANT_STMT_EODSTATUS(EODDATE, TYPE, STATUS) VALUES( ?, ?, ?)";

            count = backendJdbcTemplate.update(sql, Integer.parseInt(date), type, status);
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public void callMerchantCustomerStatementProcedure() throws Exception {
        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);
            String newEodid = date + "00";

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("MERCHANT_CUST_STMT_AP_PROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EID", Integer.parseInt(newEodid));
            simpleJdbcCall.execute(in);

        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void callAuditMerchantCustomerStatementProcedure() throws Exception {
        try {
            String eodid = String.valueOf(Configurations.EOD_ID);
            String date = eodid.substring(0, 6);
            String newEodid = date + "00";

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(backendJdbcTemplate)
                    .withProcedureName("AUDIT_MERCHANT_STMT_PROC");
            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("EID", Integer.parseInt(newEodid));
            simpleJdbcCall.execute(in);

        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public MerchantCustomerBean getMerchantCustomerPayments(MerchantCustomerBean bean) throws Exception {

        ArrayList<String> paymentList = new ArrayList<String>();
        String type = "customer";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        String query = null;
        try {
            if (!bean.isFirstStatement()) {

                query = "SELECT NETPAYAMMOUNT,CRDRNET,TOTALPAYAMOUNT,CRDR,TOTALCOMMISSION,CRDRCOMMISSION,TOTALFEEAMOUNT,CRDRFEE,EODPAYID,TXNCOUNT "
                        + "FROM EODMERCHANTPAYMENT "
                        + "WHERE PAYMENTDATE > TO_DATE(?,'DD-MM-YY') "
                        + "AND TO_DATE(?,'DD-MM-YY') + 1 > PAYMENTDATE AND MERCHANTCUSTID = ? AND STATEMENTFILESTATUS <> ?";

                bean = backendJdbcTemplate.query(query,
                        (ResultSet result) -> {
                            double netPayment = 0.0;
                            double payment = 0.0;
                            double commission = 0.0;
                            double fees = 0.0;
                            int txnCount = 0;

                            MerchantCustomerBean merchantCustomerBean = new MerchantCustomerBean();
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

                            merchantCustomerBean.setNetPaymentAmount(netPayment);
                            merchantCustomerBean.setPaymentAmount(payment);
                            merchantCustomerBean.setCommissionAmount(commission);
                            merchantCustomerBean.setFeeAmount(fees);
                            merchantCustomerBean.setTxnCount(txnCount);

                            if (paymentList.size() > 0) {
                                int value = 0;
                                try {
                                    value = this.updateMerchantPayment(paymentList, type);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("Updated " + value + "from list of " + paymentList.size());
                            }
                            return merchantCustomerBean;
                        },
                        sdf.format(bean.getLastBillingDate())
                        , bean.getBillingDate()
                        , bean.getMerchantCusNo(), 2
                );
            } else {

                query = "SELECT NETPAYAMMOUNT,CRDRNET,TOTALPAYAMOUNT,CRDR,TOTALCOMMISSION,CRDRCOMMISSION,TOTALFEEAMOUNT,CRDRFEE,EODPAYID,TXNCOUNT "
                        + "FROM EODMERCHANTPAYMENT "
                        + "WHERE TO_DATE(?,'DD-MM-YY') + 1 > PAYMENTDATE AND MERCHANTCUSTID = ? AND STATEMENTFILESTATUS <> ?";

                bean = backendJdbcTemplate.query(query,
                        (ResultSet result) -> {
                            double netPayment = 0.0;
                            double payment = 0.0;
                            double commission = 0.0;
                            double fees = 0.0;
                            int txnCount = 0;

                            MerchantCustomerBean merchantCustomerBean = new MerchantCustomerBean();
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

                            merchantCustomerBean.setNetPaymentAmount(netPayment);
                            merchantCustomerBean.setPaymentAmount(payment);
                            merchantCustomerBean.setCommissionAmount(commission);
                            merchantCustomerBean.setFeeAmount(fees);
                            merchantCustomerBean.setTxnCount(txnCount);

                            if (paymentList.size() > 0) {
                                int value = 0;
                                try {
                                    value = this.updateMerchantPayment(paymentList, type);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("Updated " + value + "from list of " + paymentList.size());
                            }
                            return merchantCustomerBean;
                        },
                        sdf.format(bean.getBillingDate())
                        , bean.getMerchantCusNo()
                        , 2
                );
            }
        } catch (Exception ex) {
            throw ex;
        }
        return bean;
    }

    private int updateMerchantPayment(ArrayList<String> paymentList, String type) throws Exception {
        String query = "UPDATE EODMERCHANTPAYMENT SET STATEMENTFILESTATUS = ? WHERE EODPAYID = ?";
        int i = 0;
        try {
            for (String id : paymentList) {
                i += backendJdbcTemplate.update(query,
                        (type.equals("location")) ? 1 : 2, id);
            }
        } catch (Exception e) {
            throw e;
        }
        return i;
    }

    @Override
    public MerchantCustomerBean getLastmerchantCustomerStatementDetails(MerchantCustomerBean bean) throws Exception {
        int eodId = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String query = "SELECT MCS.ENDEODID "
                    + "FROM MERCHANTCUSTOMERSTATEMENT MCS "
                    + "WHERE MCS.MERCHANTCUSNO = ? AND MCS.STATEMENTENDDATE = TO_DATE(?,'DD-MM-YY')";

            eodId = backendJdbcTemplate.queryForObject(query, Integer.class,
                    bean.getMerchantCusNo(),
                    sdf.format(bean.getLastBillingDate()));

            bean.setStartEodId(eodId);
        } catch (EmptyResultDataAccessException ex) {
            return bean;
        } catch (Exception e) {
            throw e;
        }
        return bean;
    }

    @Override
    public boolean insertInToMerchantCustomerStatementTable(MerchantCustomerBean bean) throws Exception {
        Map<String, Object> details = new LinkedHashMap<String, Object>();
        boolean flag = true;
        try {
            String query = "INSERT INTO MERCHANTCUSTOMERSTATEMENT( "
                    + "    STATEMENTID,MERCHANTCUSNO,STATEMENTSTARTDATE,STATEMENTENDDATE, "
                    + "    COMMISIONS,FEES,PAYMENT,TOTALTXNAMOUNT,STATUS, "
                    + "    BILLINGCYCLEID,LASTUPDATEUSERID,STARTEODID,ENDEODID, "
                    + "    ACCOUNTNO,STATEMENTGENERATEDSTATUS, "
                    + "    CASHDEPOSITS,CASHADVANCE,OPENINGLOYALTYPOINT,EARNLOYALTYPOINT, "
                    + "    AVLOYALTYPOINT,ADJUSTLOYALTYPOINT,REDEEMLOYALTYPOINT,CLOSINGLOYALTYPOINT,TIMESTAMP,TXNCOUNT) "
                    + "  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

            backendJdbcTemplate.update(query,
                    bean.getStatementID(),//STATEMENTID
                    bean.getMerchantCusNo(),//MID
                    DateUtil.getSqldate(bean.getLastBillingDate()),//STATEMENT START DATE
                    DateUtil.getSqldate(bean.getBillingDate()),//STATEMENT END DATE
                    bean.getCommissionAmount(),//COMMISSION
                    bean.getFeeAmount(),//FEES
                    bean.getNetPaymentAmount(),//PAYMENT
                    bean.getPaymentAmount(),//TOTALTXNAMOUNT(TOTALPAYAMOUNT)
                    "0",//STATUS
                    bean.getStatementCycleCode(),//BILLINGCYCLEID
                    0,//LASTUPDATEDUSERID
                    bean.getStartEodId(),//STARTEODID
                    bean.getEndEodId(),//ENDEODID
                    bean.getAccountNo(),//ACCOUNTNO
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
                    bean.getTxnCount()//TOTAL TXN COUNT
            );

            details.put("Mrechant Customer No", bean.getMerchantCusDes());
            details.put("Merchant Name", bean.getMerchantCusDes());
            details.put("Account Number", bean.getAccountNo());
            details.put("Total Txn Amount", bean.getPaymentAmount());
            details.put("Total Payment", Double.toString(bean.getNetPaymentAmount()));
            details.put("Total Fees", Double.toString(bean.getFeeAmount()));
            details.put("Total Commisions", Double.toString(bean.getCommissionAmount()));
            details.put("Statement Cycle Code", bean.getStatementCycleCode());

            logInfo.info(logManager.logDetails(details));
        } catch (Exception e) {
            flag = false;
            throw e;
        }
        return flag;
    }

    @Override
    public boolean updateMerchantCustomerBillingDate(MerchantCustomerBean bean) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        Date newNextBillingDate = null;
        String query = "SELECT "
                + "  CASE "
                + "    WHEN MBC.BILLINGOPTION = '1' "
                + "    THEN TO_DATE(ML.NEXTBILLINGDATE) + 1 "
                + "    WHEN MBC.BILLINGOPTION = '2' "
                + "    THEN TO_DATE(ML.NEXTBILLINGDATE) + 7 "
                + "    WHEN MBC.BILLINGOPTION = '3' "
                + "    THEN LAST_DAY((ML.NEXTBILLINGDATE)) + MBC.BILLINGDATE "
                + "    WHEN MBC.BILLINGOPTION = '4' "
                + "    THEN TO_DATE(ML.NEXTBILLINGDATE) + 365 "
                + "  END AS NEWNEXTBILLINGDATE "
                /*+ "  ML.NEXTBILLINGDATE, "
                + "  MBC.BILLINGOPTION, "
                + "  MBC.BILLINGDATE "*/
                + "FROM "
                + "  MERCHANTCUSTOMER ML "
                + "INNER JOIN MERCHANTBILLINGCYCLE MBC "
                + "ON "
                + "  ML.STATEMENTCYCLE = MBC.BILLINGCYCLECODE "
                + "WHERE "
                + "  ML.MERCHANTCUSTOMERNO=?";


        try {
            newNextBillingDate = backendJdbcTemplate.queryForObject(query, Date.class, bean.getMerchantCusNo());
            bean.setNextBillingDate(newNextBillingDate);

            if (bean.getNextBillingDate() != null) {
                String updateQuery = "UPDATE MERCHANTCUSTOMER "
                        + "SET NEXTBILLINGDATE = TO_DATE(?,'DD-MM-YY') "
                        + "WHERE MERCHANTCUSTOMERNO = ? ";

                int i = backendJdbcTemplate.update(updateQuery, sdf.format(bean.getNextBillingDate()), bean.getMerchantCusNo());

                if (i > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                logInfo.info("Next Billing Date is null for merchant Customer No " + bean.getMerchantCusNo() + ".");
                return false;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
