/**
 * Author : sharuka_j
 * Date : 2/2/2023
 * Time : 9:33 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MerchantPaymentFileDao;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.model.bean.MerchantPayBean;
import com.epic.cms.model.bean.MerchantPaymentCycleBean;
import com.epic.cms.model.model.EodOuputFileBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class MerchantPaymentFileRepo implements MerchantPaymentFileDao {

    @Autowired
    StatusVarList status;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Override
    public HashMap<String, String> getCurrencyList() throws Exception {
        HashMap<String, String> hmap = new HashMap<String, String>();

        try {
            String sql = "SELECT DISTINCT CURRENCYNUMCODE,CURRENCYALPHACODE "
                    + "FROM CURRENCY "
                    + "WHERE STATUS = ? ";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            String currNumCode = rs.getString("CURRENCYNUMCODE");
                            String currAlphaCode = rs.getString("CURRENCYALPHACODE");
                            hmap.put(currNumCode, currAlphaCode);
                        }
                        return hmap;
                    }
                    , status.getACTIVE_STATUS());
        } catch (Exception e) {
            throw e;
        }
        return hmap;
    }

    @Override
    public HashMap<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> getMerchantsForPayment() throws Exception {
        PreparedStatement stmt = null;
        ResultSet result = null;
        HashMap<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> merchantListOnPayMode
                = new HashMap<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String query = "SELECT ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN 1 "
                    + "    ELSE 0 "
                    + "  END)                  AS PAYSTATUS, "
                    + "  MC.MERCHANTCUSTOMERNO AS MERCHANTCUSTOMERNO, "
                    + "  ML.MERCHANTID         AS MERCHANTID, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.NEXTPAYEMENTDATE "
                    + "    ELSE ML.NEXTPAYEMENTDATE "
                    + "  END) AS NEXTPAYEMENTDATE, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.PAYMENTMODE "
                    + "    ELSE ML.PAYMENTMODE "
                    + "  END) AS PAYMENTMODE, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.PAYMENTCYCLE "
                    + "    ELSE ML.PAYMENTCYCLE "
                    + "  END) AS PAYMENTCYCLE, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.ACCOUNTNUMBER "
                    + "    ELSE ML.ACCOUNTNUMBER "
                    + "  END) AS ACCOUNTNO, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.ACCOUNTNAME "
                    + "    ELSE ML.ACCOUNTNAME "
                    + "  END) AS ACCOUNTNAME, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.BANKCODE "
                    + "    ELSE ML.BANKCODE "
                    + "  END) AS BANKCODE, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.BRANCHNAME "
                    + "    ELSE ML.BRANCHNAME "
                    + "  END) AS BRANCHNAME, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.CURRENCYCODE "
                    + "    ELSE ML.CURRENCYCODE "
                    + "  END)      AS CURRENCYCODE, "
                    + "  MC.STATUS AS MERCUSSTATUS, "
                    + "  ML.STATUS AS MERSTATUS "
                    + "FROM MERCHANTCUSTOMER MC "
                    + "INNER JOIN MERCHANTLOCATION ML "
                    + "ON ML.MERCHANTCUSTOMERNO = MC.MERCHANTCUSTOMERNO "
                    + "WHERE (( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN TRUNC(MC.NEXTPAYEMENTDATE) "
                    + "    ELSE TRUNC(ML.NEXTPAYEMENTDATE) "
                    + "  END) <= TO_DATE(?,'DD-MM-YY')) AND (( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS=? "
                    + "    THEN MC.STATUS "
                    + "    ELSE ML.STATUS "
                    + "  END) NOT IN (?,?)) ";

            if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
                query += " AND ( "
                        + "  CASE "
                        + "  WHEN MC.PAYMENTMAINTEINANCESTATUS='YES' AND MC.MERCHANTCUSTOMERNO NOT IN "
                        + "    (SELECT EM.MERCHANTCUSTOMERNO "
                        + "    FROM EODERRORMERCHANT EM "
                        + "    WHERE EM.STATUS='" + status.getEOD_PENDING_STATUS() + "' "
                        + "    ) THEN "
                        + "    1 "
                        + "  WHEN MC.PAYMENTMAINTEINANCESTATUS='NO' AND ML.MERCHANTID NOT IN "
                        + "    (SELECT EM.MID "
                        + "    FROM EODERRORMERCHANT EM "
                        + "    WHERE EM.STATUS='" + status.getEOD_PENDING_STATUS() + "' "
                        + "    ) THEN "
                        + "    1 "
                        + "  ELSE "
                        + "    0 "
                        + "  END) = 1";
            } else if (Configurations.STARTING_EOD_STATUS.equals(status.getERROR_STATUS())) {
                query += " AND ( "
                        + "  CASE "
                        + "  WHEN MC.PAYMENTMAINTEINANCESTATUS='YES' AND MC.MERCHANTCUSTOMERNO IN "
                        + "    (SELECT EM.MERCHANTCUSTOMERNO "
                        + "    FROM EODERRORMERCHANT EM "
                        + "    WHERE EM.STATUS='" + status.getEOD_PENDING_STATUS() + "' "
                        + "    AND EODID < " + Configurations.ERROR_EOD_ID + " "
                        + "    AND EM.processstepid <=" + Configurations.PROCESS_STEP_ID + ") "
                        + "    THEN "
                        + "    1 "
                        + "  WHEN MC.PAYMENTMAINTEINANCESTATUS='NO' AND ML.MERCHANTID IN "
                        + "    (SELECT EM.MID "
                        + "    FROM EODERRORMERCHANT EM "
                        + "    WHERE EM.STATUS='" + status.getEOD_PENDING_STATUS() + "' "
                        + "    AND EODID < " + Configurations.ERROR_EOD_ID + " "
                        + "    AND EM.processstepid <=" + Configurations.PROCESS_STEP_ID + ") "
                        + "    THEN "
                        + "    1 "
                        + "  ELSE "
                        + "    0 "
                        + "  END) = 1";
            }

            query += " ORDER BY PAYMENTMAINTEINANCESTATUS, MC.MERCHANTCUSTOMERNO ";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (result.next()) {
                            int paystatus = result.getInt("PAYSTATUS");
                            MerchantPaymentCycleBean merchantPaymentCycleBean = new MerchantPaymentCycleBean();
                            merchantPaymentCycleBean.setMerchantId(result.getString("MERCHANTID"));
                            String merchantCustomer = result.getString("MERCHANTCUSTOMERNO");
                            merchantPaymentCycleBean.setMerchantCustomer(merchantCustomer);
                            String paymentMode = result.getString("PAYMENTMODE");
                            merchantPaymentCycleBean.setPayMode(paymentMode);
                            merchantPaymentCycleBean.setPaymentCycleCode(result.getString("PAYMENTCYCLE"));
                            merchantPaymentCycleBean.setMerchantStatus(result.getString("MERSTATUS"));
                            merchantPaymentCycleBean.setMerchantCustomerStatus(result.getString("MERCUSSTATUS"));
                            merchantPaymentCycleBean.setAccountNo(result.getString("ACCOUNTNO"));
                            merchantPaymentCycleBean.setAccountName(result.getString("ACCOUNTNAME"));
                            merchantPaymentCycleBean.setBankCode(result.getString("BANKCODE"));
                            merchantPaymentCycleBean.setBranchCode(result.getString("BRANCHNAME"));
                            merchantPaymentCycleBean.setCurrencyCode(result.getString("CURRENCYCODE"));

                            if (paymentMode.equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_DIRECT)) {

                                ArrayList<MerchantPaymentCycleBean> merchantList;
                                HashMap<String, ArrayList<MerchantPaymentCycleBean>> merchantCusList;
                                HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>> merchantListOnPayStatus;

                                if (merchantListOnPayMode.containsKey(Configurations.MERCHANT_PAY_MODE_DIRECT)) {
                                    merchantListOnPayStatus = merchantListOnPayMode.get(Configurations.MERCHANT_PAY_MODE_DIRECT);

                                    if (merchantListOnPayStatus.containsKey(paystatus)) {
                                        merchantCusList = merchantListOnPayStatus.get(paystatus);

                                        if (merchantCusList.containsKey(merchantCustomer)) {
                                            merchantList = merchantCusList.get(merchantCustomer);
                                            merchantList.add(merchantPaymentCycleBean);

                                        } else {
                                            merchantList = new ArrayList<MerchantPaymentCycleBean>();

                                            merchantList.add(merchantPaymentCycleBean);
                                        }

                                        merchantCusList.put(merchantCustomer, merchantList);

                                    } else {
                                        merchantList = new ArrayList<MerchantPaymentCycleBean>();
                                        merchantCusList = new HashMap<String, ArrayList<MerchantPaymentCycleBean>>();

                                        merchantList.add(merchantPaymentCycleBean);
                                        merchantCusList.put(merchantCustomer, merchantList);
                                    }

                                    merchantListOnPayStatus.put(paystatus, merchantCusList);

                                } else {
                                    merchantList = new ArrayList<MerchantPaymentCycleBean>();
                                    merchantCusList = new HashMap<String, ArrayList<MerchantPaymentCycleBean>>();
                                    merchantListOnPayStatus = new HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>();

                                    merchantList.add(merchantPaymentCycleBean);
                                    merchantCusList.put(merchantCustomer, merchantList);
                                    merchantListOnPayStatus.put(paystatus, merchantCusList);
                                }

                                merchantListOnPayMode.put(Configurations.MERCHANT_PAY_MODE_DIRECT, merchantListOnPayStatus);

                            } else if (paymentMode.equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_CHEQUE)) {

                                ArrayList<MerchantPaymentCycleBean> merchantList;
                                HashMap<String, ArrayList<MerchantPaymentCycleBean>> merchantCusList;
                                HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>> merchantListOnPayStatus;

                                if (merchantListOnPayMode.containsKey(Configurations.MERCHANT_PAY_MODE_CHEQUE)) {
                                    merchantListOnPayStatus = merchantListOnPayMode.get(Configurations.MERCHANT_PAY_MODE_CHEQUE);

                                    if (merchantListOnPayStatus.containsKey(paystatus)) {
                                        merchantCusList = merchantListOnPayStatus.get(paystatus);

                                        if (merchantCusList.containsKey(merchantCustomer)) {
                                            merchantList = merchantCusList.get(merchantCustomer);
                                            merchantList.add(merchantPaymentCycleBean);

                                        } else {
                                            merchantList = new ArrayList<MerchantPaymentCycleBean>();

                                            merchantList.add(merchantPaymentCycleBean);
                                        }

                                        merchantCusList.put(merchantCustomer, merchantList);

                                    } else {
                                        merchantList = new ArrayList<MerchantPaymentCycleBean>();
                                        merchantCusList = new HashMap<String, ArrayList<MerchantPaymentCycleBean>>();

                                        merchantList.add(merchantPaymentCycleBean);
                                        merchantCusList.put(merchantCustomer, merchantList);
                                    }

                                    merchantListOnPayStatus.put(paystatus, merchantCusList);

                                } else {
                                    merchantList = new ArrayList<MerchantPaymentCycleBean>();
                                    merchantCusList = new HashMap<String, ArrayList<MerchantPaymentCycleBean>>();
                                    merchantListOnPayStatus = new HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>();

                                    merchantList.add(merchantPaymentCycleBean);
                                    merchantCusList.put(merchantCustomer, merchantList);
                                    merchantListOnPayStatus.put(paystatus, merchantCusList);
                                }

                                merchantListOnPayMode.put(Configurations.MERCHANT_PAY_MODE_CHEQUE, merchantListOnPayStatus);

                            } else if (paymentMode.equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_SLIPS)) {

                                ArrayList<MerchantPaymentCycleBean> merchantList;
                                HashMap<String, ArrayList<MerchantPaymentCycleBean>> merchantCusList;
                                HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>> merchantListOnPayStatus;

                                if (merchantListOnPayMode.containsKey(Configurations.MERCHANT_PAY_MODE_SLIPS)) {
                                    merchantListOnPayStatus = merchantListOnPayMode.get(Configurations.MERCHANT_PAY_MODE_SLIPS);

                                    if (merchantListOnPayStatus.containsKey(paystatus)) {
                                        merchantCusList = merchantListOnPayStatus.get(paystatus);

                                        if (merchantCusList.containsKey(merchantCustomer)) {
                                            merchantList = merchantCusList.get(merchantCustomer);
                                            merchantList.add(merchantPaymentCycleBean);

                                        } else {
                                            merchantList = new ArrayList<MerchantPaymentCycleBean>();

                                            merchantList.add(merchantPaymentCycleBean);
                                        }

                                        merchantCusList.put(merchantCustomer, merchantList);

                                    } else {
                                        merchantList = new ArrayList<MerchantPaymentCycleBean>();
                                        merchantCusList = new HashMap<String, ArrayList<MerchantPaymentCycleBean>>();

                                        merchantList.add(merchantPaymentCycleBean);
                                        merchantCusList.put(merchantCustomer, merchantList);
                                    }

                                    merchantListOnPayStatus.put(paystatus, merchantCusList);

                                } else {
                                    merchantList = new ArrayList<MerchantPaymentCycleBean>();
                                    merchantCusList = new HashMap<String, ArrayList<MerchantPaymentCycleBean>>();
                                    merchantListOnPayStatus = new HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>();

                                    merchantList.add(merchantPaymentCycleBean);
                                    merchantCusList.put(merchantCustomer, merchantList);
                                    merchantListOnPayStatus.put(paystatus, merchantCusList);
                                }

                                merchantListOnPayMode.put(Configurations.MERCHANT_PAY_MODE_SLIPS, merchantListOnPayStatus);

                            }

                        }
                        return merchantListOnPayMode;
                    }
                    , Configurations.YES_STATUS //1
                    , Configurations.YES_STATUS
                    , Configurations.YES_STATUS
                    , Configurations.YES_STATUS
                    , Configurations.YES_STATUS
                    , Configurations.YES_STATUS //6
                    , Configurations.YES_STATUS //7
                    , Configurations.YES_STATUS
                    , Configurations.YES_STATUS
                    , Configurations.YES_STATUS //10
                    , sdf.format(Configurations.EOD_DATE)
                    , Configurations.YES_STATUS
                    , status.getMERCHANT_DELETE_STATUS()
                    , status.getMERCHANT_CANCEL_STATUS()//14
            );
        } catch (Exception e) {
            throw e;
        }
        return merchantListOnPayMode;
    }

    @Override
    public void updateMerchantLocationNextPaymentDate(ArrayList<String> merchantList) throws SQLException, Exception {
        try {

            String nextBillingDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

            String query = "SELECT ML.MERCHANTCUSTOMERNO, "
                    + "  ML.MERCHANTID, "
                    + "  CASE "
                    + "    WHEN MBC.PAYMENTOPTION = '1' "
                    + "    THEN TO_DATE(ML.NEXTPAYEMENTDATE) + 1 "
                    + "    WHEN MBC.PAYMENTOPTION = '2' "
                    + "    THEN TO_DATE(ML.NEXTPAYEMENTDATE) + 7 "
                    + "    WHEN MBC.PAYMENTOPTION = '3' "
                    + "    THEN LAST_DAY((ML.NEXTPAYEMENTDATE)) + MBC.PAYMENTDATE "
                    + "    WHEN MBC.PAYMENTOPTION = '4' "
                    + "    THEN TO_DATE(ML.NEXTPAYEMENTDATE) + MBC.NOOFDAYS "
                    + "  END AS NEWNEXTPAYEMENTDATE, "
                    + "  ML.NEXTPAYEMENTDATE, "
                    + "  MBC.PAYMENTOPTION, "
                    + "  MBC.PAYMENTDATE, "
                    + "  MBC.NOOFDAYS "
                    + "FROM MERCHANTLOCATION ML "
                    + "INNER JOIN MERCHANTPAYMENTCYCLE MBC "
                    + "ON ML.PAYMENTCYCLE = MBC.PAYMENTCYCLECODE "
                    + "WHERE ML.MERCHANTID= ?";

            for (String cusNo : merchantList) {

                nextBillingDate = backendJdbcTemplate.queryForObject(query, String.class, cusNo);
//                stmt = connection.prepareStatement(query);
//                stmt.setString(1, cusNo);
//                result = stmt.executeQuery();
//                if (result.next()) {
//                    nextBillingDate = sdf.format(result.getDate("NEWNEXTPAYEMENTDATE"));
//                }

//                this.CloseStatement(stmt, result);

                if (!nextBillingDate.equals(null)) {

                    String updateQuery = "UPDATE MERCHANTLOCATION "
                            + "SET NEXTPAYEMENTDATE = TO_DATE(?,'DD-MM-YY') "
                            + "WHERE MERCHANTID = ? ";

                    backendJdbcTemplate.update(updateQuery, nextBillingDate, cusNo);

                } else {
                    logManager.logError("No merchant location for mID " + cusNo + ".",errorLogger);
                }
            }

        } catch (Exception e) {
            logManager.logError("Exeption ", errorLogger);
            throw e;
        }
    }

    @Override
    public void updateMerchantCustomerNextPaymentDate(ArrayList<String> merchantList) throws SQLException, Exception {
        try {
            String nextBillingDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

            String query = "SELECT MC.MERCHANTCUSTOMERNO, "
                    + "  CASE "
                    + "    WHEN MBC.PAYMENTOPTION = '1' "
                    + "    THEN TO_DATE(MC.NEXTPAYEMENTDATE) + 1 "
                    + "    WHEN MBC.PAYMENTOPTION = '2' "
                    + "    THEN TO_DATE(MC.NEXTPAYEMENTDATE) + 7 "
                    + "    WHEN MBC.PAYMENTOPTION = '3' "
                    + "    THEN LAST_DAY((MC.NEXTPAYEMENTDATE)) + MBC.PAYMENTDATE "
                    + "    WHEN MBC.PAYMENTOPTION = '4' "
                    + "    THEN TO_DATE(MC.NEXTPAYEMENTDATE) + MBC.NOOFDAYS "
                    + "  END AS NEWNEXTPAYEMENTDATE, "
                    + "  MC.NEXTPAYEMENTDATE, "
                    + "  MBC.PAYMENTOPTION, "
                    + "  MBC.PAYMENTDATE, "
                    + "  MBC.NOOFDAYS "
                    + "FROM MERCHANTCUSTOMER MC "
                    + "INNER JOIN MERCHANTPAYMENTCYCLE MBC "
                    + "ON MC.PAYMENTCYCLE         = MBC.PAYMENTCYCLECODE "
                    + "WHERE MC.MERCHANTCUSTOMERNO= ?";

            for (String cusNo : merchantList) {

                nextBillingDate = backendJdbcTemplate.queryForObject(query, String.class, cusNo);

//
//                if (result.next()) {
//                    nextBillingDate = sdf.format(result.getDate("NEWNEXTPAYEMENTDATE"));
//                }


                if (!nextBillingDate.equals(null)) {

                    String updateQuery = "UPDATE MERCHANTCUSTOMER "
                            + "SET NEXTPAYEMENTDATE = TO_DATE(?,'DD-MM-YY') "
                            + "WHERE MERCHANTCUSTOMERNO = ? ";

                    backendJdbcTemplate.update(updateQuery, nextBillingDate, cusNo);

                } else {
                    logManager.logInfo("No merchant customer for customerNo " + cusNo + ".",infoLogger);
                }
            }
        } catch (Exception e) {
            logManager.logError("Exeption ", errorLogger);
            throw e;
        }
    }

    @Override
    public MerchantCustomerBean getMerchantCustomerDetails(String CusId) throws Exception {
        MerchantCustomerBean bean = new MerchantCustomerBean();
        try {
            String sql = "SELECT MERCHANTCUSTOMERNO,CURRENCYCODE,ACCOUNTNUMBER,ACCOUNTNAME,BANKCODE,BRANCHNAME FROM MERCHANTCUSTOMER "
                    + "WHERE MERCHANTCUSTOMERNO = ? ";


            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            bean.setMerchantCusNo(rs.getString("MERCHANTCUSTOMERNO"));
                            bean.setCurrencyCode(rs.getString("CURRENCYCODE"));
                            bean.setAccountNo(rs.getString("ACCOUNTNUMBER"));
                            bean.setAccountName(rs.getString("ACCOUNTNAME"));
                            bean.setBankCode(rs.getString("BANKCODE"));
                            bean.setBranchCode(rs.getString("BRANCHNAME"));
                        }
                        return bean;
                    }
                    , CusId);
        } catch (Exception e) {
            throw e;
        }
        return bean;
    }

    @Override
    public ArrayList<MerchantPayBean> getPaymentsForCustomerFromEodMerchantpayment(String key) throws Exception {
        ArrayList<MerchantPayBean> list = new ArrayList<MerchantPayBean>();

        ArrayList<String> glTypes = new ArrayList<String>();

        try {
            String sql = "SELECT EODPAYID,MERCHANTCUSTID,MERCHANTID,NETPAYAMMOUNT,CRDRNET "
                    + "FROM EODMERCHANTPAYMENT "
                    + "WHERE PAYMENTFILESTATUS = ? AND MERCHANTCUSTID = ?";


            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            MerchantPayBean bean = new MerchantPayBean();
                            bean.setEodPayId(rs.getInt("EODPAYID"));
                            bean.setMerchantCusId(rs.getString("MERCHANTCUSTID"));
                            bean.setMerchantId(rs.getString("MERCHANTID"));
                            bean.setNetPayAmount(rs.getString("NETPAYAMMOUNT"));
                            bean.setCrDrnetPayment(rs.getString("CRDRNET"));
                            list.add(bean);
                        }
                        return list;
                    }
                    , status.getNO_STATUS_0(), key);

        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public int updatePaymentFileStatus(ArrayList<String> paymentIdList) throws Exception, SQLException {
        int i = 0;

        try {
            String query = "UPDATE EODMERCHANTPAYMENT SET PAYMENTFILESTATUS = ? WHERE EODPAYID = ? ";

            for (String id : paymentIdList) {
                i += backendJdbcTemplate.update(query, status.getYES_STATUS_1(), id);
            }

        } catch (Exception ex) {
            throw ex;
        }
        return i;
    }

    @Override
    public Date getNextWorkingDay(Date DueDate) throws Exception {
        boolean holiday = this.isHoliday(DueDate);
        java.util.Date nextDate = DueDate;
        int x = 1;
        while (holiday) {
            nextDate = CommonMethods.getNextDateForFreq(DueDate, x);
            if (this.isHoliday(nextDate)) {
                x = x + 1;
            } else {
                holiday = false;
            }
        }
        return nextDate;
    }

    @Override
    public ArrayList<MerchantPayBean> getPaymentsFromEodMerchantpayment(String key) throws Exception {
        ArrayList<MerchantPayBean> list = new ArrayList<MerchantPayBean>();
        ArrayList<String> glTypes = new ArrayList<String>();

        try {
            String sql = "SELECT EODPAYID,MERCHANTID,NETPAYAMMOUNT,CRDRNET "
                    + "FROM EODMERCHANTPAYMENT "
                    + "WHERE PAYMENTFILESTATUS = ? AND MERCHANTID = ?";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            MerchantPayBean bean = new MerchantPayBean();
                            bean.setEodPayId(rs.getInt("EODPAYID"));
                            bean.setMerchantId(rs.getString("MERCHANTID"));
                            bean.setNetPayAmount(rs.getString("NETPAYAMMOUNT"));
                            bean.setCrDrnetPayment(rs.getString("CRDRNET"));
                            list.add(bean);
                        }
                        return list;
                    }
                    , status.getNO_STATUS_0(), key);

        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public int InsertMerchantPaymentFilesIntoDownloadTable(String fileId, String fileType) throws Exception {
        int count;
        try {
            String query = "Insert into DOWNLOADFILE (FIETYPE,FILENAME,LETTERTYPE, "
                    + "STATUS,GENERATEDUSER,STATEMENTMONTH,STATEMENTYEAR,LASTUPDATEDTIME, "
                    + "CREATEDTIME,LASTUPDATEDUSER,CARDTYPE,CARDPRODUCT,FILEID "
                    + ") values "
                    + "(?,?,?,?,?,?,?,to_date(?,'DD-MM-YY'),to_date(?,'DD-MM-YY'),?,?,?,?)";

            count = backendJdbcTemplate.update(query, fileType
                    , fileId
                    , ""
                    , Configurations.NO_STATUS //4
                    , Configurations.EOD_USER
                    , ""
                    , ""
                    , Configurations.EOD_DATE_String //8
                    , Configurations.EOD_DATE_String
                    , Configurations.EOD_USER
                    , ""
                    , ""
                    , fileId);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertOutputFiles(EodOuputFileBean outputfilebean, String fileType) throws Exception {
        int count = 0;
        PreparedStatement pst = null;
        String insertToTable;
        try {
            switch (fileType) {
                case "GL":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "GL", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());
                    break;

                case "RB36":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "RB36", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());

                    break;

                case "OUTCTF":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";
                    count = backendJdbcTemplate.update(insertToTable, "OUTCTF", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());

                    break;

                case "CUSTOMERCSV":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime,subfolder)VALUES("
                            + "?,?,?,?,sysdate,?)";
                    count = backendJdbcTemplate.update(insertToTable, "CUSTOMERCSV", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords(), outputfilebean.getSubFolder());
                    break;

                case "OUTMASTER":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";
                    count = backendJdbcTemplate.update(insertToTable, "OUTMASTER", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());

                    break;

                case "MERCHANTGL":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "MERCHANTGL", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());

                    break;

                case "CASHBACK":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";
                    count = backendJdbcTemplate.update(insertToTable, "CASHBACK", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());

                    break;

                case "AUTOSETTLEMENT":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";
                    count = backendJdbcTemplate.update(insertToTable, "AUTOSETTLEMENT", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());

                    break;

                case "EODLOGS":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime,subfolder)VALUES("
                            + "?,?,?,?,sysdate,?)";

                    count = backendJdbcTemplate.update(insertToTable, "EODLOGS", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords(), outputfilebean.getSubFolder());

                    break;

                case "MERCHANTPAYMENTDIRECT":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,subfolder,createdtime)VALUES("
                            + "?,?,?,?,?,sysdate)";
                    count = backendJdbcTemplate.update(insertToTable, "MERCHANTPAYMENTDIRECT", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords(), outputfilebean.getSubFolder());

                    break;

                case "MERCHANTPAYMENTSLIP":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,subfolder,createdtime)VALUES("
                            + "?,?,?,?,?,sysdate)";
                    count = backendJdbcTemplate.update(insertToTable, "MERCHANTPAYMENTSLIP", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords(), outputfilebean.getSubFolder());

                    break;

            }

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void InsertMerchantFilesIntoDownloadTable(String fileId, String fileType) throws Exception {
        try {
            String query = "Insert into DOWNLOADFILE (FIETYPE,FILENAME,LETTERTYPE, "
                    + "STATUS,GENERATEDUSER,STATEMENTMONTH,STATEMENTYEAR,LASTUPDATEDTIME, "
                    + "CREATEDTIME,LASTUPDATEDUSER,CARDTYPE,CARDPRODUCT,FILEID "
                    + ") values "
                    + "(?,?,?,?,?,?,?,to_date(?,'DD-MM-YY'),to_date(?,'DD-MM-YY'),?,?,?,?)";

            backendJdbcTemplate.update(query
                    , fileType
                    , fileId
                    , ""
                    , Configurations.NO_STATUS //4
                    , Configurations.EOD_USER
                    , ""
                    , ""
                    , Configurations.EOD_DATE_String
                    , Configurations.EOD_DATE_String //9
                    , Configurations.EOD_USER
                    , ""
                    , "" //12
                    , fileId
            );
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean isHoliday(Date today) throws Exception {
        try {
            String query = "SELECT COUNT(*) FROM HOLIDAY WHERE YEAR = ? AND MONTH=? AND DAY=?";
            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        if (rs.next()) {
                            int count = Integer.parseInt(rs.getString(1).trim());
                            return count > 0;
                        } else {
                            return false;
                        }
                    }
            );
        } catch (Exception e) {
            throw e;
        }
        return true;
    }

}
