/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 6:22 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MerchantGLSummaryFileDao;
import com.epic.cms.model.model.EodOuputFileBean;
import com.epic.cms.model.model.GlAccountBean;
import com.epic.cms.model.model.GlBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
@Repository
public class MerchantGLSummaryFileRepo implements MerchantGLSummaryFileDao {
    @Autowired
    StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<GlAccountBean> getCommissionDataToEODGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<GlAccountBean>();

        try {
            String sql = "SELECT DISTINCT EODTRANSACTIONID,   MID,   MERCHANTCOMMSSION,   CRDR,   EODDATE,   ? AS TRANSACTIONTYPE FROM EODMERCHANTCOMMISSION EMC WHERE EMC.STATUS = ? AND EMC.GLSTATUS = 0 ";

//            list =
            backendJdbcTemplate.query(sql
                    , (ResultSet rs) -> {
                        GlAccountBean bean = null;
                        while (rs.next()) {
                            bean = new GlAccountBean();
                            bean.setKey(rs.getString("EODTRANSACTIONID"));
                            bean.setMerchantID(rs.getString("MID"));
                            bean.setAmount(rs.getDouble("MERCHANTCOMMSSION"));
                            bean.setGlAmount(rs.getString("MERCHANTCOMMSSION"));
                            bean.setCrDr(rs.getString("CRDR"));
                            bean.setGlDate(rs.getString("EODDATE"));
                            bean.setGlType(rs.getString("TRANSACTIONTYPE"));
                            list.add(bean);
                        }
                        return list;
                    }, status.getCOMMISSION_STATUS(), Configurations.EOD_DONE_STATUS);
        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public int insertIntoEodMerchantGLAccount(int eodID, Date glDate, String merchantID, String glType, String amount, String cdStatus) throws Exception {

        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        //  String glAmount=String.format("%.2f",String.valueOf(amount));

        try {
            String sql = "INSERT INTO EODMERCHANTGLACCOUNT (EODID,GLDATE,MERCHANTID,GLTYPE,AMOUNT,CRDR) VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?)";

            count = backendJdbcTemplate.update(sql, eodID, sdf.format(glDate), merchantID, glType, String.valueOf(amount), cdStatus);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCommissions(String key, int i) throws Exception {
        int count = 0;
        try {
            String sql = "UPDATE EODMERCHANTCOMMISSION SET GLSTATUS = ? WHERE EODTRANSACTIONID = TO_NUMBER(?) ";

            count = backendJdbcTemplate.update(sql, i, key);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<GlAccountBean> getMerchantFeeDataToEODGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<GlAccountBean>();

        PreparedStatement pst = null;
//        ResultSet rs = null;
        try {
            String sql = "SELECT DISTINCT EMF.EODFEEID,EMF.MERCHANTID, "
                    + "   EMF.FEEAMOUNT,EMF.CRDR,EMF.EFFECTDATE, "
                    + "   EMF.FEETYPE AS TRANSACTIONTYPE "
                    + "FROM EODMERCHANTFEE EMF "
                    + "WHERE EMF.STATUS = ? "
                    + "AND EMF.GLSTATUS = 0 ";

            backendJdbcTemplate.query(sql
                    , (ResultSet rs) -> {
                        GlAccountBean bean = null;
                        while (rs.next()) {
                            bean = new GlAccountBean();
                            bean.setKey(rs.getString("EODFEEID"));
                            bean.setMerchantID(rs.getString("MERCHANTID"));
                            bean.setAmount(rs.getDouble("FEEAMOUNT"));
                            bean.setGlAmount(rs.getString("FEEAMOUNT"));
                            bean.setCrDr(rs.getString("CRDR"));
                            bean.setGlDate(rs.getString("EFFECTDATE"));
                            bean.setGlType(rs.getString("TRANSACTIONTYPE"));

                            list.add(bean);
                        }
                        return list;
                    }, Configurations.EOD_DONE_STATUS);
        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public int updateMerchantFeeGlStatus(String key, int i) throws Exception {
        PreparedStatement pst = null;
        int count = 0;
        try {
            String sql = "UPDATE EODMERCHANTFEE SET GLSTATUS = ? WHERE EODFEEID = TO_NUMBER(?) ";
            count = backendJdbcTemplate.update(sql, i, key);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<GlAccountBean> getEODMerchantTxnDataToGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<GlAccountBean>();

        try {
            String sql = "SELECT DISTINCT EMT.EODTRANSACTIONID, "
                    + "  EMT.MID, "
                    + "  EMT.CRDR, "
                    + "  EMT.TRANSACTIONAMOUNT, "
                    + "  EMT.SETTLEMENTDATE, "
                    + "  EMT.TRANSACTIONTYPE, "
                    + "  EMT.ONOFFSTATUS AS ONOFFSTATUS, "
                    + "  EMT.EPSTATUS, "
                    + "  EMT.FUELSURCHARGEAMOUNT, "
                    + "  T.REQUESTFROM, "
                    + "  NVL(T.CHANNELTYPE,0)  AS CHANNELTYPE, "
                    + "  NVL(T.LISTENERTYPE,0) AS LISTENERTYPE, "
                    + "  EMT.CARDASSOCIATION "
                    + "FROM EODMERCHANTTRANSACTION EMT "
                    + "INNER JOIN TRANSACTION T "
                    + "ON EMT.TRANSACTIONID = T.TXNID "
                    + "WHERE EMT.STATUS     = ? "
                    + "AND EMT.GLSTATUS     = 0 ";


            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        GlAccountBean bean = null;
                        GlAccountBean bean1 = null;
                        GlAccountBean bean2 = null;
                        while (rs.next()) {
                            bean = new GlAccountBean();
                            int epStatus = rs.getInt("EPSTATUS");
                            bean.setKey(rs.getString("EODTRANSACTIONID"));
                            bean.setMerchantID(rs.getString("MID"));
                            bean.setAmount(rs.getDouble("TRANSACTIONAMOUNT"));
                            bean.setGlAmount(rs.getString("TRANSACTIONAMOUNT"));
                            bean.setCrDr(rs.getString("CRDR"));
                            bean.setGlDate(rs.getString("SETTLEMENTDATE"));
                            bean.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                            int requestFrom = rs.getInt("REQUESTFROM");
                            int channeltype = rs.getInt("CHANNELTYPE");
                            int listnerType = rs.getInt("LISTENERTYPE");

                            String cardAssociation = rs.getString("CARDASSOCIATION");

                            boolean isLankaQR = false;

                /*both mvisa recipient and lanka QR transactions comming with same TXNTYPE.
                 REQUESTFROM=2(listner) LISTENERTYPE=8(mobile) --> Lanka QR mobile (onus customers)
                 this changed as following (since core bank migration)->>  REQUESTFROM=1(channel) CHANNELTYPE=21(CEFT) --> Lanka QR CEFT (offus customers)
                REQUESTFROM=2(listner) LISTENERTYPE=9(manual) --> Lanka QR CEFT (offus customers)*/
                            if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT)
                                    && ((requestFrom == status.getREQUESTFROM_LISTNER() && listnerType == 8)
                                    || (requestFrom == status.getREQUESTFROM_LISTNER() && listnerType == 9))) {
                                isLankaQR = true;
                            }

                            if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && rs.getInt("EPSTATUS") == 0
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) { //SEPRATE WITH SALE & EZPAY
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_ON_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && rs.getInt("EPSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) { //SEPRATE WITH SALE & EZPAY
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_ON_US_EASYPAY);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && rs.getInt("EPSTATUS") == 0
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) { //SEPRATE WITH SALE & EZPAY
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_ON_US_MASTER);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && rs.getInt("EPSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) { //SEPRATE WITH SALE & EZPAY
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_ON_US_EASYPAY_MASTER);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && rs.getInt("EPSTATUS") == 0
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) { //SEPRATE WITH SALE & EZPAY
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_ON_US_CUP);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && rs.getInt("EPSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) { //SEPRATE WITH SALE & EZPAY
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_ON_US_EASYPAY_CUP);

                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_OFF_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ACQ_ON_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ACQ_OFF_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_OFF_US_MASTER);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ACQ_ON_US_MASTER);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ACQ_OFF_US_MASTER);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_ACQ_OFF_US_CUP);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ACQ_ON_US_CUP);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) {
                                bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ACQ_OFF_US_CUP);

                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT)
                                    && rs.getInt("ONOFFSTATUS") == 1 && !isLankaQR) {
                                bean.setGlType(Configurations.TXN_TYPE_MVISA_RECI_ON_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT)
                                    && rs.getInt("ONOFFSTATUS") != 1 && !isLankaQR) {
                                bean.setGlType(Configurations.TXN_TYPE_MVISA_RECI_OFF_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT)
                                    && (requestFrom == status.getREQUESTFROM_LISTNER() && listnerType == 8)) { // lanka QR transactions comming from channel and channel_type=mobile(8), onus bank customers
                                bean.setGlType(Configurations.TXN_TYPE_MVISA_RECI_LK_QR_ON_US);
                            } else if (rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT)
                                    && (requestFrom == status.getREQUESTFROM_CHANNEL() && channeltype == 21)) { // lanka QR transactions comming from listner and listner_type=CEFT(21), offus bank customers
                                bean.setGlType(Configurations.TXN_TYPE_MVISA_RECI_LK_QR_OFF_US);
                            } else {
                                bean.setGlType(rs.getString("TRANSACTIONTYPE"));
                            }

                            list.add(bean);

                            //if there are fuel surcharge, insert it as separate GL. Ex :txn amount=1000,fuelsurcharge=50 -> create two separate GL for two txn
                            //fuel surcharge onus & offus
                            if (bean.getFuelSurchargeAmount() > 0 && rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) {

                                bean1 = new GlAccountBean();

                                bean1.setKey(rs.getString("EODTRANSACTIONID"));
                                bean1.setMerchantID(rs.getString("MID"));
                                bean1.setGlDate(rs.getString("SETTLEMENTDATE"));
                                bean1.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                                bean1.setAmount(bean.getFuelSurchargeAmount());
                                bean1.setGlAmount(rs.getString("FUELSURCHARGEAMOUNT"));
                                bean1.setGlType(Configurations.TXN_TYPE_FUEL_SURCHARGE_ON_US);
                                bean1.setCrDr("DR");

                                list.add(bean1);
                            } else if (bean.getFuelSurchargeAmount() > 0 && rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.VISA_ASSOCIATION)) {

                                bean2 = new GlAccountBean();

                                bean2.setKey(rs.getString("EODTRANSACTIONID"));
                                bean2.setMerchantID(rs.getString("MID"));
                                bean2.setGlDate(rs.getString("SETTLEMENTDATE"));
                                bean2.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                                bean2.setAmount(bean.getFuelSurchargeAmount());
                                bean2.setGlAmount(rs.getString("FUELSURCHARGEAMOUNT"));
                                bean2.setGlType(Configurations.TXN_TYPE_FUEL_SURCHARGE_OFF_US);
                                bean2.setCrDr("DR");

                                list.add(bean2);

                            } else if (bean.getFuelSurchargeAmount() > 0 && rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) {

                                bean1 = new GlAccountBean();

                                bean1.setKey(rs.getString("EODTRANSACTIONID"));
                                bean1.setMerchantID(rs.getString("MID"));
                                bean1.setGlDate(rs.getString("SETTLEMENTDATE"));
                                bean1.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                                bean1.setAmount(bean.getFuelSurchargeAmount());
                                bean1.setGlAmount(rs.getString("FUELSURCHARGEAMOUNT"));
                                bean1.setGlType(Configurations.TXN_TYPE_FUEL_SURCHARGE_ON_US_MASTER);
                                bean1.setCrDr("DR");

                                list.add(bean1);
                            } else if (bean.getFuelSurchargeAmount() > 0 && rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)) {

                                bean2 = new GlAccountBean();

                                bean2.setKey(rs.getString("EODTRANSACTIONID"));
                                bean2.setMerchantID(rs.getString("MID"));
                                bean2.setGlDate(rs.getString("SETTLEMENTDATE"));
                                bean2.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                                bean2.setAmount(bean.getFuelSurchargeAmount());
                                bean2.setGlAmount(rs.getString("FUELSURCHARGEAMOUNT"));
                                bean2.setGlType(Configurations.TXN_TYPE_FUEL_SURCHARGE_OFF_US_MASTER);
                                bean2.setCrDr("DR");

                                list.add(bean2);
                            } else if (bean.getFuelSurchargeAmount() > 0 && rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") == 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) {

                                bean1 = new GlAccountBean();

                                bean1.setKey(rs.getString("EODTRANSACTIONID"));
                                bean1.setMerchantID(rs.getString("MID"));
                                bean1.setGlDate(rs.getString("SETTLEMENTDATE"));
                                bean1.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                                bean1.setAmount(bean.getFuelSurchargeAmount());
                                bean1.setGlAmount(rs.getString("FUELSURCHARGEAMOUNT"));
                                bean1.setGlType(Configurations.TXN_TYPE_FUEL_SURCHARGE_ON_US_CUP);
                                bean1.setCrDr("DR");

                                list.add(bean1);
                            } else if (bean.getFuelSurchargeAmount() > 0 && rs.getString("TRANSACTIONTYPE").equalsIgnoreCase(Configurations.TXN_TYPE_SALE)
                                    && rs.getInt("ONOFFSTATUS") != 1
                                    && cardAssociation.equalsIgnoreCase(Configurations.CUP_ASSOCIATION)) {

                                bean2 = new GlAccountBean();

                                bean2.setKey(rs.getString("EODTRANSACTIONID"));
                                bean2.setMerchantID(rs.getString("MID"));
                                bean2.setGlDate(rs.getString("SETTLEMENTDATE"));
                                bean2.setFuelSurchargeAmount(rs.getDouble("FUELSURCHARGEAMOUNT"));

                                bean2.setAmount(bean.getFuelSurchargeAmount());
                                bean2.setGlAmount(rs.getString("FUELSURCHARGEAMOUNT"));
                                bean2.setGlType(Configurations.TXN_TYPE_FUEL_SURCHARGE_OFF_US_CUP);
                                bean2.setCrDr("DR");

                                list.add(bean2);
                            }

                        }
                        return list;
                    }
                    , status.getEOD_DONE_STATUS());

        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public int updateEODMerchantTxn(String key, int i) throws Exception {
        int count = 0;
        try {
            String sql = "UPDATE EODMERCHANTTRANSACTION SET GLSTATUS = ? WHERE EODTRANSACTIONID = TO_NUMBER(?)";
            count = backendJdbcTemplate.update(sql, i, key);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODMerchantPayment(String key, int i) throws Exception {
        int count = 0;
        try {
            String sql = "UPDATE EODMERCHANTPAYMENT SET GLSTATUS = ? WHERE EODPAYID = TO_NUMBER(?)";
            count = backendJdbcTemplate.update(sql, i, key);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<GlAccountBean> getEODMerchantPaymentDataToGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<GlAccountBean>();
        try {
            String sql = "SELECT DISTINCT EMP.EODPAYID, "
                    + "  EMP.MERCHANTID, "
                    + "  EMP.CRDRNET, "
                    + "  EMP.NETPAYAMMOUNT, "
                    + "  EMP.PAYMENTDATE, "
                    + "  ( "
                    + "  CASE "
                    + "    WHEN MC.PAYMENTMAINTEINANCESTATUS='YES' "
                    + "    THEN MC.PAYMENTMODE "
                    + "    ELSE ML.PAYMENTMODE "
                    + "  END) AS PAYMENTMODE "
                    + "FROM EODMERCHANTPAYMENT EMP "
                    + "INNER JOIN MERCHANTLOCATION ML "
                    + "ON EMP.MERCHANTID = ML.MERCHANTID "
                    + "INNER JOIN MERCHANTCUSTOMER MC "
                    + "ON ML.MERCHANTCUSTOMERNO = MC.MERCHANTCUSTOMERNO "
                    + "WHERE EMP.STATUS         = ? "
                    + "AND EMP.GLSTATUS         = 0 ";


            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        GlAccountBean bean = null;
                        while (rs.next()) {
                            bean = new GlAccountBean();
                            bean.setKey(rs.getString("EODPAYID"));
                            bean.setMerchantID(rs.getString("MERCHANTID"));
                            bean.setAmount(rs.getDouble("NETPAYAMMOUNT"));
                            bean.setGlAmount(rs.getString("NETPAYAMMOUNT"));
                            bean.setCrDr(rs.getString("CRDRNET"));
                            bean.setGlDate(rs.getString("PAYMENTDATE"));

                            if (rs.getString("PAYMENTMODE").equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_DIRECT)) {
                                bean.setGlType(Configurations.TXN_TYPE_PAYMENT_DIRECT);
                            } else if (rs.getString("PAYMENTMODE").equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_SLIPS)) {
                                bean.setGlType(Configurations.TXN_TYPE_PAYMENT_SLIPS);
                            } else if (rs.getString("PAYMENTMODE").equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_CHEQUE)) {
                                bean.setGlType(Configurations.TXN_TYPE_PAYMENT_CHEQUE);
                            }
                            list.add(bean);
                        }
                        return list;
                    }
                    , status.getEOD_PENDING_STATUS());
        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public HashMap<String, ArrayList<GlAccountBean>> getDataFromEODMERCHANTGl() throws Exception {
        HashMap<String, ArrayList<GlAccountBean>> hmap = new HashMap<String, ArrayList<GlAccountBean>>();
        ArrayList<GlAccountBean> list;

        ArrayList<String> glTypes = new ArrayList<String>();

        try {
            String sql = "SELECT ID,EODID,GLDATE,MERCHANTID,CRDR, "
                    + "  NVL(GLTYPE,'--') AS GLTYPE,AMOUNT "
                    + "FROM EODMERCHANTGLACCOUNT "
                    + "WHERE EODSTATUS = ? ";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        ArrayList<GlAccountBean> list2;
                        while (rs.next()) {
                            GlAccountBean bean = new GlAccountBean();

                            String glType = rs.getString("GLTYPE");
                            bean.setId(rs.getInt("ID"));
                            bean.setAmount(rs.getDouble("AMOUNT"));
                            bean.setGlAmount(rs.getString("AMOUNT"));
                            bean.setKey(rs.getString("EODID"));
                            bean.setCrDr(rs.getString("CRDR"));
                            bean.setGlType(rs.getString("GLTYPE"));

                            if (glTypes.contains(glType.toString())) {
                                list2 = hmap.get(glType);
                                list2.add(bean);
                                hmap.put(glType, list2);

                            } else {
                                glTypes.add(glType);
                                ArrayList<GlAccountBean> set = new ArrayList<GlAccountBean>();
                                set.add(bean);
                                hmap.put(glType, set);
                            }
                        }
                        return hmap;
                    }
                    , status.getEOD_PENDING_STATUS());


        } catch (Exception e) {
            throw e;
        }
        return hmap;
    }

    @Override
    public int updateEodMerchantGLAccount(int key) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODMERCHANTGLACCOUNT SET EODSTATUS = ? WHERE ID = ? ";

            count = backendJdbcTemplate.update(query, status.getEOD_DONE_STATUS(), key);
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

            backendJdbcTemplate.update(query, fileType, fileId, "", Configurations.NO_STATUS, Configurations.EOD_USER, "", ""
                    , Configurations.EOD_DATE_String, Configurations.EOD_DATE_String, Configurations.EOD_USER, "", "", fileId);
        } catch (Exception e) {
            throw e;
        }
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

                    count = backendJdbcTemplate.update(insertToTable, "GL", outputfilebean.getFileName(),
                            Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());
                    break;

                case "RB36":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "RB36", outputfilebean.getFileName(),
                            Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());
                    break;

                case "OUTCTF":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime)VALUES("
                            + "?,?,?,?,sysdate)";

                    count = backendJdbcTemplate.update(insertToTable, "OUTCTF", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getNoOfRecords());
                    break;

                case "CUSTOMERCSV":
                    insertToTable = "INSERT INTO eodoutputfiles(filetype,filename,eodid,noofrecords,createdtime,subfolder)VALUES("
                            + "?,?,?,?,sysdate,?)";

                    count = backendJdbcTemplate.update(insertToTable, "CUSTOMERCSV", outputfilebean.getFileName(), Configurations.ERROR_EOD_ID, outputfilebean.getSubFolder());

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
//            LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        /**
         *
         */
        return count;
    }

    @Override
    public HashMap<String, GlBean> getGLAccData() throws Exception {
        HashMap<String, GlBean> hmap = new HashMap<String, GlBean>();
        try {
            String sql = "SELECT NVL(G.GLACCOUNTCODE,'00') AS GLACCOUNTCODE,"
                    + " NVL(G.BRANCH,'00')             AS BRANCH,"
                    + " NVL(G.CLIENTNO,'00')           AS CLIENTNO,"
                    + " NVL(C.CURRENCYALPHACODE,'00')           AS CURRENCY,"
                    + " NVL(G.PROFITCENTRE,'00')       AS PROFITCENTRE,"
                    + " NVL(G.PRODUCTCATEGORY,'00')       AS PRODCATEGORY"
                    + " FROM GLACCOUNT G, CURRENCY C"
                    + " WHERE G.currency=C.CURRENCYNUMCODE"
                    + " AND G.STATUS = ? ";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            GlBean bean = new GlBean();
                            String glAcc = rs.getString("GLACCOUNTCODE");
                            bean.setClientNo(rs.getString("CLIENTNO"));
                            bean.setCurrencyCode(rs.getString("CURRENCY"));
                            bean.setProfitCenter(rs.getString("PROFITCENTRE"));
                            bean.setBranch(rs.getString("BRANCH"));
                            bean.setProdCategory(rs.getString("PRODCATEGORY"));

                            hmap.put(glAcc, bean);
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
    public HashMap<String, String[]> getGLTxnTypes() throws Exception {
        HashMap<String, String[]> hmap = new HashMap<String, String[]>();

        try {
            String sql = "SELECT GLTXNTYPECODE,DESCRIPTION,NVL(POSITIONTYPE, 'TR') AS POSITIONTYPE FROM GLTXNTYPE";

            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    String[] glTxn = new String[2];
                    String txnTypeCode = rs.getString("GLTXNTYPECODE");
                    glTxn[0] = rs.getString("DESCRIPTION");
                    glTxn[1] = rs.getString("POSITIONTYPE");

                    hmap.put(txnTypeCode, glTxn);
                }
                return hmap;
            });

        } catch (Exception e) {
            throw e;
        }
        return hmap;
    }

    @Override
    public HashMap<String, String[]> getMerchantGLTypesData() throws Exception {
        HashMap<String, String[]> hmap = new HashMap<String, String[]>();

        try {
            String sql = "SELECT TRANSACTIONCODE,CREDITACCOUNT,DEBITACCOUNT,CRDR FROM GLTRANSACTION WHERE CATEGORY = ? ";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            String[] accountNo = new String[3];
                            String glType = rs.getString("TRANSACTIONCODE");
                            accountNo[0] = rs.getString("CREDITACCOUNT");
                            accountNo[1] = rs.getString("DEBITACCOUNT");
                            accountNo[2] = rs.getString("CRDR");

                            hmap.put(glType, accountNo);
                        }
                        return hmap;
                    }
                    , status.getYES_STATUS_1());
        } catch (Exception e) {
            throw e;
        }
        return hmap;
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
    public String getCRDRFromGlTxn(String key) throws Exception {
        String crdr = null;

        try {
            String sql = "SELECT NVL(CRDR,'DR') AS CRDR FROM GLTXNTYPE WHERE GLTXNTYPECODE=? ";

            crdr = backendJdbcTemplate.queryForObject(sql, String.class, key);

        } catch (Exception e) {
            throw e;
        }
        return crdr;
    }

    public boolean isHoliday(java.util.Date today) throws Exception {

        boolean holiday = false;
        String query = "SELECT COUNT(*) FROM HOLIDAY WHERE YEAR = ? AND MONTH=? AND DAY=?";

        try {
            holiday = backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        if (result.next()) {
                            int count = Integer.parseInt(result.getString(1).trim());
                            if (count > 0) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    , String.valueOf(today.getYear() + 1900), String.valueOf(today.getMonth() + 1), String.valueOf(today.getDate()));

        } catch (Exception e) {
//            e.printStackTrace();
//            LogFileCreator.writeErrorToLog(e);
            return false;
        }
        return holiday;
    }
}
