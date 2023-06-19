/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:52 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.TransactionPostDao;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.model.rowmapper.TransactionpostRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class TransactionPostRepo implements TransactionPostDao {
    @Autowired
    StatusVarList statusList;

    ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Override
    public ArrayList<OtbBean> getInitEodTxnPostCustAcc() throws Exception {
        ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();
        OtbBean bean = new OtbBean();
        try {
            String query = "SELECT DISTINCT CAC.CUSTOMERID, CAC.ACCOUNTNO "
                    + "FROM EODTRANSACTION ET "
                    + "INNER JOIN CARD C ON C.CARDNUMBER = ET.CARDNUMBER "
                    + "INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = C.MAINCARDNO "
                    //                    + "LEFT OUTER JOIN EODERRORCARDS EEC ON EEC.ACCOUNTNO = ET.ACCOUNTNO "
                    + "WHERE ET.STATUS = ? " +
                    "AND ((ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?)) "
                    + "AND CAC.ACCOUNTNO not in "
                    + " (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? ) "
                    + " ORDER BY CAC.CUSTOMERID, CAC.ACCOUNTNO";

            backendJdbcTemplate.query(query
                    , (ResultSet result) -> {
                        while (result.next()) {
                            bean.setCustomerid(result.getString("CUSTOMERID"));
                            bean.setAccountnumber(result.getString("ACCOUNTNO"));
                            custAccList.add(bean);
                        }
                        return custAccList;
                    }
                    , Configurations.INITIAL_STATUS
                    , Configurations.TXN_TYPE_SALE
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_CASH_ADVANCE
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_FEE_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_REVERSAL
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MVISA_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_MONEY_SEND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MONEY_SEND_REVERSAL
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_AFT
                    , Configurations.DEBIT
                    , statusList.getEOD_PENDING_STATUS()
            );

        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public ArrayList<OtbBean> getErrorEodTxnPostCustAcc() throws Exception {
        ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();
        OtbBean bean = new OtbBean();
        try {
            String query = "SELECT DISTINCT CAC.CUSTOMERID, CAC.ACCOUNTNO "
                    + "FROM EODTRANSACTION ET "
                    + "INNER JOIN CARD C ON C.CARDNUMBER = ET.CARDNUMBER "
                    + "INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = C.MAINCARDNO "
                    + "INNER JOIN EODERRORCARDS EEC ON EEC.ACCOUNTNO = ET.ACCOUNTNO "
                    + "WHERE ET.STATUS = ? "
                    + "AND EEC.STATUS = ? AND EEC.EODID < ? AND EEC.PROCESSSTEPID <= ? "
                    + "AND ((ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) "
                    + "OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?)) "
                    + "ORDER BY CAC.CUSTOMERID, CAC.ACCOUNTNO";

            backendJdbcTemplate.query(query
                    , (ResultSet result) -> {
                        while (result.next()) {
                            bean.setCustomerid(result.getString("CUSTOMERID"));
                            bean.setAccountnumber(result.getString("ACCOUNTNO"));
                            custAccList.add(bean);
                        }
                        return custAccList;
                    }
                    , Configurations.INITIAL_STATUS
                    , Configurations.EOD_PENDING_STATUS
                    , Configurations.ERROR_EOD_ID
                    , Configurations.PROCESS_STEP_ID
                    , Configurations.TXN_TYPE_SALE
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_CASH_ADVANCE
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_FEE_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_REVERSAL
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MVISA_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_MONEY_SEND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MONEY_SEND_REVERSAL
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_AFT
                    , Configurations.DEBIT
            );
        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public ArrayList<OtbBean> getTxnAmount(String accountnumber) throws Exception {
        ArrayList<OtbBean> txnList = new ArrayList<OtbBean>();
        try {
            String query = "SELECT ET.CARDNUMBER, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) PAYMENT, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE IN (?,?)  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) SALE, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) CASHADVANCE, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) EASYPAYREV, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) EASYPAY, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) EASYPAYFEE, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) REFUND, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) REVERSAL, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) MVISAREFUND, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) MONEYSEND, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) MONEYSENDREVERSAL, "
                    + "SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) AFT "
                    + "FROM EODTRANSACTION ET "
                    + "WHERE ET.STATUS = ? AND ET.ACCOUNTNO = ? "
                    + "GROUP BY ET.CARDNUMBER "
                    + "HAVING SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE IN (?,?)  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 "
                    + "OR SUM(CASE WHEN ET.CRDR = ? AND ET.TRANSACTIONTYPE = ?  THEN ET.TRANSACTIONAMOUNT ELSE 0 END) > 0 ";

//            txnList = (ArrayList<OtbBean>) backendJdbcTemplate.query(query, new TransactionpostRowMapper()
//                    , Configurations.CREDIT //1
//                    , Configurations.TXN_TYPE_PAYMENT //2
//                    , Configurations.DEBIT //3
//                    , Configurations.TXN_TYPE_SALE //4
//                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR //5
//                    , Configurations.DEBIT //6
//                    , Configurations.TXN_TYPE_CASH_ADVANCE //7
//                    , Configurations.CREDIT //8
//                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT //9
//                    , Configurations.DEBIT //10
//                    , Configurations.TXN_TYPE_INSTALLMENT //11
//                    , Configurations.DEBIT //12
//                    , Configurations.TXN_TYPE_FEE_INSTALLMENT //13
//                    , Configurations.CREDIT //14
//                    , Configurations.TXN_TYPE_REFUND //15
//                    , Configurations.CREDIT //16
//                    , Configurations.TXN_TYPE_REVERSAL //17
//                    , Configurations.CREDIT //18
//                    , Configurations.TXN_TYPE_MVISA_REFUND //19
//                    , statusList.getINITIAL_STATUS() //20
//                    , accountnumber //21
//                    , Configurations.CREDIT //22
//                    , Configurations.TXN_TYPE_PAYMENT //23
//                    , Configurations.DEBIT //24
//                    , Configurations.TXN_TYPE_SALE //25
//                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR //26
//                    , Configurations.DEBIT //27
//                    , Configurations.TXN_TYPE_CASH_ADVANCE //28
//                    , Configurations.CREDIT
//                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT //30
//                    , Configurations.DEBIT //31
//                    , Configurations.TXN_TYPE_INSTALLMENT //32
//                    , Configurations.DEBIT //33
//                    , Configurations.TXN_TYPE_FEE_INSTALLMENT //34
//                    , Configurations.CREDIT //35
//                    , Configurations.TXN_TYPE_REFUND //36
//                    , Configurations.CREDIT //37
//                    , Configurations.TXN_TYPE_REVERSAL //38
//                    , Configurations.CREDIT //39
//                    , Configurations.TXN_TYPE_MVISA_REFUND  //40
//            );
            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        OtbBean otbBean = new OtbBean();
                        while (result.next()) {
                            otbBean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                            otbBean.setPayment(result.getDouble("PAYMENT"));
                            otbBean.setSale(result.getDouble("SALE"));
                            otbBean.setCashadavance(result.getDouble("CASHADVANCE"));
                            otbBean.setEasypayrev(result.getDouble("EASYPAYREV"));
                            otbBean.setEasypay(result.getDouble("EASYPAY"));
                            otbBean.setEasypayfee(result.getDouble("EASYPAYFEE"));
                            otbBean.setMvisaRefund(result.getDouble("MVISAREFUND"));
                            otbBean.setRefund(result.getDouble("REFUND"));
                            otbBean.setReversal(result.getDouble("REVERSAL"));
                            otbBean.setMoneysend(result.getDouble("MONEYSEND"));
                            otbBean.setMoneysendreversal(result.getDouble("MONEYSENDREVERSAL"));
                            otbBean.setAft(result.getDouble("AFT"));
                            txnList.add(otbBean);
                        }
                        return txnList;
                    }
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_SALE
                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_CASH_ADVANCE
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_FEE_INSTALLMENT
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REVERSAL
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MVISA_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MONEY_SEND
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_MONEY_SEND_REVERSAL
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_AFT
                    , statusList.getINITIAL_STATUS()
                    , accountnumber
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_SALE
                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_CASH_ADVANCE
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_INSTALLMENT
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_FEE_INSTALLMENT
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_REVERSAL
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MVISA_REFUND
                    , Configurations.CREDIT
                    , Configurations.TXN_TYPE_MONEY_SEND
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_MONEY_SEND_REVERSAL
                    , Configurations.DEBIT
                    , Configurations.TXN_TYPE_AFT
            );


        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }

    @Override
    public int updateCardTemp(StringBuffer cardnumber, double payment) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE ECMS_ONLINE_CARD SET"
                    + " TEMPCREDITAMOUNT = TEMPCREDITAMOUNT - ?,"
                    + " LASTUPDATEUSER=?, LASTUPDATETIME=SYSDATE "
                    + " WHERE CARDNUMBER=?  ";

            count = onlineJdbcTemplate.update(query, payment, Configurations.EOD_USER, cardnumber);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCardOtbCredit(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE ECMS_ONLINE_CARD "
                    + "SET OTBCREDIT = OTBCREDIT + ?,"
                    + "LASTUPDATEUSER = ?, LASTUPDATETIME = SYSDATE "
                    + "WHERE CARDNUMBER=? ";

            count = onlineJdbcTemplate.update(query, cardBean.getTxnAmount(), Configurations.EOD_USER, cardBean.getCardnumber());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateAccountOtbCredit(OtbBean bean) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE ECMS_ONLINE_ACCOUNT "
                    + "SET OTBCREDIT = OTBCREDIT + ? "
                    + "WHERE ACCOUNTNUMBER=? ";
            count = onlineJdbcTemplate.update(query, bean.getOtbcredit(), bean.getAccountnumber());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                logManager.logInfo("================ updateAccountOtbCredit ===================" + Integer.toString(Configurations.EOD_ID), infoLogger);
                logManager.logInfo(query, infoLogger);
                logManager.logInfo(Double.toString(bean.getOtbcredit()), infoLogger);
                logManager.logInfo(bean.getAccountnumber(), infoLogger);
                logManager.logInfo("================ updateAccountOtbCredit END ===================", infoLogger);
            }
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCustomerOtbCredit(OtbBean bean) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE ECMS_ONLINE_CUSTOMER "
                    + "SET OTBCREDIT= OTBCREDIT + ? "
                    + "WHERE CUSTOMERID=?  ";

            count = onlineJdbcTemplate.update(query, bean.getOtbcredit(), bean.getCustomerid());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCardByPostedTransactions(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE CARD "
                    + "SET OTBCREDIT = OTBCREDIT - ?,"
                    + "OTBCASH = OTBCASH - ?,"
                    + "TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?,"
                    + "TEMPCASHAMOUNT = TEMPCASHAMOUNT + ?,"
                    + "LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE CARDNUMBER=? ";

            count = backendJdbcTemplate.update(query
                    , cardBean.getOtbcredit()
                    , cardBean.getOtbcash()
                    , cardBean.getTmpcredit()
                    , cardBean.getTmpcash()
                    , Configurations.EOD_USER
                    , cardBean.getCardnumber()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODCARDBALANCEByTxn(OtbBean cardBean) throws Exception {
        int count = 0;
        try {

            String query = "UPDATE EODCARDBALANCE "
                    + "SET CUMTRANSACTIONS = CUMTRANSACTIONS + ?,"
                    + "CUMCASHADVANCES = CUMCASHADVANCES + ?,"
                    + "EODCLOSINGBAL = EODCLOSINGBAL - ?,"
                    + "LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE CARDNUMBER = ? ";

            count = backendJdbcTemplate.update(query
                    , (cardBean.getSale() + cardBean.getEasypay() + cardBean.getEasypayfee()) - (cardBean.getEasypayrev() + cardBean.getRefund() + cardBean.getReversal() + cardBean.getMvisaRefund())
                    , cardBean.getCashadavance()
                    , (cardBean.getSale() + cardBean.getCashadavance() + cardBean.getEasypay() + cardBean.getEasypayfee()) - (cardBean.getPayment() + cardBean.getEasypayrev() + cardBean.getRefund() + cardBean.getReversal() + cardBean.getMvisaRefund())
                    , Configurations.EOD_USER
                    , cardBean.getCardnumber()
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODTRANSACTION(String accountNumber) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE EODTRANSACTION "
                    + "SET  STATUS= CASE WHEN (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?) "
                    + "OR (TRANSACTIONTYPE = ? AND CRDR = ?)  THEN ? ELSE STATUS END, "
                    + "LASTUPDATEDTIME=SYSDATE, "
                    + "LASTUPDATEDUSER =? "
                    + "WHERE STATUS = ? "
                    + "AND ACCOUNTNO= ? ";

            count = backendJdbcTemplate.update(query
                    , Configurations.TXN_TYPE_SALE //1
                    , Configurations.DEBIT //2
                    , Configurations.TXN_TYPE_CASH_ADVANCE //3
                    , Configurations.DEBIT //4
                    , Configurations.TXN_TYPE_PAYMENT //5
                    , Configurations.CREDIT //6
                    , Configurations.TXN_TYPE_REVERSAL_INSTALLMENT //7
                    , Configurations.CREDIT //8
                    , Configurations.TXN_TYPE_INSTALLMENT //9
                    , Configurations.DEBIT //10
                    , Configurations.TXN_TYPE_FEE_INSTALLMENT //11
                    , Configurations.DEBIT //12
                    , Configurations.TXN_TYPE_REVERSAL //13
                    , Configurations.CREDIT //14
                    , Configurations.TXN_TYPE_MVISA_REFUND //15
                    , Configurations.CREDIT //16
                    , Configurations.TXN_TYPE_REFUND //17
                    , Configurations.CREDIT //18
                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR //19
                    , Configurations.DEBIT //20
                    , Configurations.EOD_DONE_STATUS //21
                    , Configurations.EOD_USER //22
                    , Configurations.INITIAL_STATUS //23
                    , accountNumber //24

            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateAccountOtb(OtbBean otbBean) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE CARDACCOUNT "
                    + "SET OTBCREDIT = OTBCREDIT - ?,"
                    + "OTBCASH = OTBCASH - ?,"
                    + "LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE ACCOUNTNO=? ";

            count = backendJdbcTemplate.update(query
                    , otbBean.getOtbcredit()
                    , otbBean.getOtbcash()
                    , Configurations.EOD_USER
                    , otbBean.getAccountnumber());

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCustomerOtb(OtbBean bean) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE CARDCUSTOMER "
                    + "SET OTBCREDIT= OTBCREDIT - ?,"
                    + "OTBCASH= OTBCASH - ?,"
                    + "LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE "
                    + "WHERE CUSTOMERID=?  ";
            count = backendJdbcTemplate.update(query
                    , bean.getOtbcredit()
                    , bean.getOtbcash()
                    , Configurations.EOD_USER
                    , bean.getCustomerid());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public StringBuffer getNewCardNumber(StringBuffer oldCardNumber) throws Exception {
        StringBuffer cardNumber = oldCardNumber;
        try {
            String query = "SELECT CR1.NEWCARDNUMBER CARDNUMBER FROM CARDREPLACE CR1 " +
                    "LEFT JOIN CARDREPLACE CR2 ON CR2.OLDCARDNUMBER = CR1.NEWCARDNUMBER " +
                    "INNER JOIN CARD C ON C.CARDNUMBER = CR1.NEWCARDNUMBER WHERE C.CARDSTATUS " +
                    "NOT IN (?,?) START WITH CR1.OLDCARDNUMBER = ? CONNECT BY PRIOR CR1.NEWCARDNUMBER = CR1.OLDCARDNUMBER";
            cardNumber = backendJdbcTemplate.queryForObject(query, StringBuffer.class, statusList.getCARD_REPLACED_STATUS(), statusList.getCARD_PRODUCT_CHANGE_STATUS(), oldCardNumber);
            return cardNumber;
        } catch (EmptyResultDataAccessException ex) {
            return cardNumber;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
