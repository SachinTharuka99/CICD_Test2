package com.epic.cms.repository;

import com.epic.cms.dao.ChequeReturnDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.model.rowmapper.MinimumPaymentRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class ChequeReturnRepo implements ChequeReturnDao {
    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;


    @Override
    public List<ReturnChequePaymentDetailsBean> getChequeReturns() throws Exception {
        List<ReturnChequePaymentDetailsBean> chqBeanList1 = new ArrayList<>();
        try {
            //String query = "SELECT * FROM PAYMENT WHERE TRANSACTIONTYPE=? AND STATUS=? AND EODID =? ";
            backendJdbcTemplate.query(queryParametersList.getChequeReturn_getChequeReturns(),
                    (ResultSet result) -> {
                        while (result.next()) {
                            ReturnChequePaymentDetailsBean bean = new ReturnChequePaymentDetailsBean();
                            bean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                            bean.setEodid(result.getInt("EODID"));
                            bean.setAmount(result.getDouble("TRANSACTIONAMOUNT"));
                            bean.setChequenumber(result.getString("CHEQUENUMBER"));
                            bean.setTraceid(result.getString("TRACEID"));
                            java.sql.Date chqDateRetun = CommonMethods.getSqldate(result.getDate("POSTINGDATE"));
                            bean.setChqRtnDate(chqDateRetun);
                            chqBeanList1.add(bean);
                        }
                        return chqBeanList1;
                    },
                    status.getCHEQUE_RETURN_STATUS(),
                    status.getINITIAL_STATUS(),
                    Configurations.EOD_ID);
        } catch (Exception ex) {
            throw ex;
        }
        return chqBeanList1;
    }

    @Override
    public int updateChequeReturns(StringBuffer cardNo, String sequenceNumber, Date returnDate) throws Exception {
        int count = 0;
        try {
            cardNo = this.getNewCardNumber(cardNo);
            //String query = "UPDATE CHEQUEPAYMENT SET CHEQUESTATUS=?, CHEQUERETURNDATE=?,LASTUPDATEDDATE=SYSDATE WHERE CARDNUMBER=? AND SEQUENCENUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateChequeReturns(), status.getCHEQUE_RETURN_STATUS(), returnDate, cardNo.toString(), sequenceNumber);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateChequeReturnsForEODPayment(StringBuffer cardNo, String sequenceNumber) throws Exception {
        int count = 0;
        try {
            cardNo = this.getNewCardNumber(cardNo);
            //String query = "UPDATE EODPAYMENT SET STATUS=?, LASTUPDATEDDATE=SYSDATE WHERE CARDNUMBER=? AND SEQUENCENUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateChequeReturnsForEODPayment(), status.getCHEQUE_RETURN_STATUS(), cardNo.toString(), sequenceNumber);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public StringBuffer getNewCardNumber(StringBuffer oldCardNumber) throws Exception {
        StringBuffer cardNumber = oldCardNumber;
        try {
            //String query = "SELECT CR1.NEWCARDNUMBER CARDNUMBER FROM CARDREPLACE CR1 LEFT JOIN CARDREPLACE CR2 ON CR2.OLDCARDNUMBER = CR1.NEWCARDNUMBER INNER JOIN CARD C ON C.CARDNUMBER = CR1.NEWCARDNUMBER WHERE C.CARDSTATUS NOT IN (?,?) START WITH CR1.OLDCARDNUMBER = ? CONNECT BY PRIOR CR1.NEWCARDNUMBER = CR1.OLDCARDNUMBER";
            cardNumber = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getNewCardNumber(), StringBuffer.class, status.getCARD_REPLACED_STATUS(), status.getCARD_PRODUCT_CHANGE_STATUS(), oldCardNumber);
        } catch (EmptyResultDataAccessException ex) {
            return cardNumber;
        } catch (Exception ex) {
            throw ex;
        }
        return cardNumber;
    }

    @Override
    public Map<StringBuffer, List<ReturnChequePaymentDetailsBean>> returnChequePaymentDetails() throws Exception {
        Map<StringBuffer, List<ReturnChequePaymentDetailsBean>> totalChequeReturns = new HashMap<>();
        List<ReturnChequePaymentDetailsBean> returnChequePaymentDetails = new ArrayList<>();
        String query = null;
        try {

            //query = "SELECT CP.*,P.CARDNUMBER AS OLDCARDNUMBER,P.SEQUENCENUMBER AS CQRTSEQ,P.CHEQUE_RET_CODE FROM CHEQUEPAYMENT CP LEFT JOIN PAYMENT P ON CP.SEQUENCENUMBER=P.TRACEID WHERE CP.CHEQUESTATUS=? AND CP.STATUS=? AND P.TRANSACTIONTYPE=? AND p.cardnumber = '4380439574186260'"; //Add cardnumber
            query = queryParametersList.getChequeReturn_returnChequePaymentDetails();
            query += CommonMethods.checkForErrorCards("CP.CARDNUMBER");
            //query += " ORDER BY CP.CARDNUMBER";
            query += queryParametersList.getChequeReturn_returnChequePaymentDetails_OrderBy();

            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        while (result.next()) {
                            try {
                                ReturnChequePaymentDetailsBean returnChequePaymentDetail = new ReturnChequePaymentDetailsBean();
                                StringBuffer cardNo = new StringBuffer(result.getString("cardnumber"));
                                /**
                                 * check replaced cards and set the card number here
                                 */
                                StringBuffer replacedCard = commonRepo.getNewCardNumber(cardNo);
                                if (replacedCard != null) {
                                    cardNo = replacedCard;
                                }
                                returnChequePaymentDetail.setCardnumber(cardNo);
                                returnChequePaymentDetail.setOldcardnumber(new StringBuffer(result.getString("OLDCARDNUMBER")));
                                returnChequePaymentDetail.setId(result.getInt("ID"));
                                returnChequePaymentDetail.setEodid(result.getInt("EODID"));
                                returnChequePaymentDetail.setAmount(result.getDouble("AMOUNT"));
                                returnChequePaymentDetail.setChequedate(result.getDate("CHEQUEDATE"));
                                returnChequePaymentDetail.setMinamount(result.getDouble("MINAMOUNT"));
                                returnChequePaymentDetail.setForwardinterest(result.getDouble("FORWARDINTEREST"));
                                returnChequePaymentDetail.setInterestrate(result.getDouble("INTERESTRATE"));
                                returnChequePaymentDetail.setChequestatus(result.getString("CHEQUESTATUS"));
                                returnChequePaymentDetail.setDelinquentclass(result.getString("DELINQUENTCLASS"));
                                returnChequePaymentDetail.setCardstatus(result.getString("CARDSTATUS"));
                                returnChequePaymentDetail.setDuedate(result.getDate("DUEDATE"));
                                returnChequePaymentDetail.setStatementstarteodid(result.getInt("STATEMENTSTARTEODID"));
                                returnChequePaymentDetail.setStatementendeodid(result.getInt("STATEMENTENDEODID"));
                                returnChequePaymentDetail.setReturnreason(result.getString("RETURNREASON"));
                                returnChequePaymentDetail.setSeqNo(result.getString("SEQUENCENUMBER"));
                                returnChequePaymentDetail.setCqrtseqNo(result.getString("CQRTSEQ"));
                                returnChequePaymentDetail.setChqRtnDate(result.getDate("CHEQUERETURNDATE"));
                                returnChequePaymentDetail.setCHEQUE_RET_CODE(result.getString("CHEQUE_RET_CODE"));
                                returnChequePaymentDetail.setNdia(result.getInt("NDIA"));

                                CardAccountCustomerBean bean = commonRepo.getCardAccountCustomer(cardNo);
                                returnChequePaymentDetail.setAccountNo(bean.getAccountNumber());

                                returnChequePaymentDetails.add(returnChequePaymentDetail);

                                if (totalChequeReturns.containsKey(cardNo)) {
                                    List<ReturnChequePaymentDetailsBean> returnChequePaymentDetailsOld = totalChequeReturns.get(cardNo);
                                    returnChequePaymentDetailsOld.add(returnChequePaymentDetail);
                                    totalChequeReturns.put(cardNo, returnChequePaymentDetailsOld);
                                } else {
                                    List<ReturnChequePaymentDetailsBean> returnChequePaymentDetailsNew = new ArrayList<ReturnChequePaymentDetailsBean>();
                                    returnChequePaymentDetailsNew.add(returnChequePaymentDetail);
                                    totalChequeReturns.put(cardNo, returnChequePaymentDetailsNew);
                                }
                            } catch (Exception ex) {
                            }
                        }
                        return totalChequeReturns;
                    },
                    status.getCHEQUE_RETURN_STATUS(), //CQRT
                    status.getEOD_PENDING_STATUS(), //EPEN
                    status.getCHEQUE_RETURN_STATUS() //CQRT
            );
        } catch (EmptyResultDataAccessException ex) {
            return totalChequeReturns;
        } catch (Exception ex) {
            throw ex;
        }
        return totalChequeReturns;
    }

    @Override
    public CardAccountCustomerBean getCardAccountCustomer(StringBuffer cardNo) throws Exception {
        CardAccountCustomerBean cardAccountCustomerBean = null;
        try {
            //String query = "SELECT CC.CUSTOMERID,CA.ACCOUNTNO,C.CARDNUMBER, C.MAINCARDNO FROM CARDACCOUNT CA,CARD C,CARDCUSTOMER CC,CARDACCOUNTCUSTOMER CAC WHERE C.CARDNUMBER = CAC.CARDNUMBER AND CA.ACCOUNTNO = CAC.ACCOUNTNO AND  CC.CUSTOMERID = CAC.CUSTOMERID AND C.CARDNUMBER =?";
            cardAccountCustomerBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getCardAccountCustomer(),
                    (rs, rowNum) -> {
                        CardAccountCustomerBean bean = new CardAccountCustomerBean();
                        bean.setAccountNumber(rs.getString("ACCOUNTNO"));
                        bean.setCustomerId(rs.getString("CUSTOMERID"));
                        bean.setMaincardNumber(new StringBuffer(rs.getString("maincardno")));
                        bean.setCardNumber(cardNo);
                        return bean;
                    },
                    cardNo.toString());
        } catch (EmptyResultDataAccessException ex) {
            return cardAccountCustomerBean;
        } catch (Exception ex) {
            throw ex;
        }
        return cardAccountCustomerBean;
    }

    @Override
    public ReturnChequePaymentDetailsBean getChequeKnockOffBean(StringBuffer cardNumber) throws Exception {
        ReturnChequePaymentDetailsBean chqPaymentKnockoffBean = null;
        try {
            //String query = "SELECT ECB.CARDNUMBER,ECB.MAINCARDNO,ECB.ISPRIMARY,CAC.CUSTOMERID,CAC.ACCOUNTNO,ECB.MAINFINCHARGEKNOCKOFF,ECB.MAINCASHADVANCEKNOCKOFF, ECB.MAINTRANSACTIONKNOCKOFF,ECB.SUPFINCHARGEKNOCKOFF,ECB.SUPCASHADVANCEKNOCKOFF, ECB.SUPTRANSACTIONKNOCKOFF,ECB.AMOUNT FROM EODPAYMENT ECB INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = ECB.CARDNUMBER WHERE ECB.CARDNUMBER = ? and ECB.STATUS=?";
            chqPaymentKnockoffBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getChequeKnockOffBean(),
                    (rs, rowNum) -> {
                        ReturnChequePaymentDetailsBean bean = new ReturnChequePaymentDetailsBean();
                        bean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
                        bean.setMaincardno(new StringBuffer(rs.getString("MAINCARDNO")));
                        bean.setIsPrimary(rs.getString("ISPRIMARY"));
                        bean.setCustomerid(rs.getString("CUSTOMERID"));
                        bean.setAccountNo(rs.getString("ACCOUNTNO"));
                        bean.setAmount(rs.getDouble("AMOUNT"));
                        bean.setMainFinChargeKnockoff(rs.getDouble("MAINFINCHARGEKNOCKOFF"));
                        bean.setMainCashAdvanceKnockoff(rs.getDouble("MAINCASHADVANCEKNOCKOFF"));
                        bean.setMainTransactionKnockoff(rs.getDouble("MAINTRANSACTIONKNOCKOFF"));
                        bean.setSupFinChargeKnockoff(rs.getDouble("SUPFINCHARGEKNOCKOFF"));
                        bean.setSupCashAdvanceKnockoff(rs.getDouble("SUPCASHADVANCEKNOCKOFF"));
                        bean.setSupTransactionKnockoff(rs.getDouble("SUPTRANSACTIONKNOCKOFF"));
                        return bean;
                    },
                    cardNumber.toString(),
                    status.getCHEQUE_RETURN_STATUS()); //CQRT

        } catch (EmptyResultDataAccessException ex) {
            return chqPaymentKnockoffBean;
        } catch (Exception ex) {
            throw ex;
        }
        return chqPaymentKnockoffBean;
    }

    @Override
    public OtbBean getEOMPendingKnockOffList(StringBuffer cardnumber) throws Exception {
        OtbBean eomOtbBean = null;
        try {
            //String query = "SELECT ECB.CARDNUMBER,ECB.MAINCARDNUMBER,CAC.CUSTOMERID,CAC.ACCOUNTNO,ECB.ISPRIMARY,ECB.CUMFINANCIALCHARGE,ECB.CUMCASHADVANCE, ECB.CUMTRANSACTION FROM EOMCARDBALANCE ECB INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = ECB.CARDNUMBER WHERE ECB.CARDNUMBER = ? AND ECB.STATUS=? AND (ECB.CUMFINANCIALCHARGE>0 OR ECB.CUMCASHADVANCE>0 OR ECB.CUMTRANSACTION >0)";
            eomOtbBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getEOMPendingKnockOffList(),
                    (rs, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
                        bean.setMaincardno(new StringBuffer(rs.getString("MAINCARDNUMBER")));
                        bean.setAccountnumber(rs.getString("ACCOUNTNO"));
                        bean.setCustomerid(rs.getString("CUSTOMERID"));
                        bean.setIsPrimary(rs.getString("ISPRIMARY"));
                        bean.setFinacialcharges(rs.getDouble("CUMFINANCIALCHARGE"));
                        bean.setCumcashadvance(rs.getDouble("CUMCASHADVANCE"));
                        bean.setCumtransactions(rs.getDouble("CUMTRANSACTION"));
                        return bean;
                    },
                    cardnumber.toString(),
                    status.getEOD_PENDING_STATUS()); //EPEN

        } catch (EmptyResultDataAccessException ex) {
            return eomOtbBean;
        } catch (Exception ex) {
            throw ex;
        }
        return eomOtbBean;
    }

    @Override
    public int updateEOMCARDBalanceKnockOn(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE EOMCARDBALANCE SET CUMCASHADVANCE = CUMCASHADVANCE + ?,CUMTRANSACTION = CUMTRANSACTION + ?,CUMFINANCIALCHARGE = CUMFINANCIALCHARGE + ?,EODID=?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateEOMCARDBalanceKnockOn(), cardBean.getCumcashadvance(), cardBean.getCumtransactions(), cardBean.getFinacialcharges(), Configurations.EOD_ID, Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateCustomerOtb(OtbBean bean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE CARDCUSTOMER SET OTBCREDIT= OTBCREDIT - ?,OTBCASH= OTBCASH - ?,LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE WHERE CUSTOMERID=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateCustomerOtb(), bean.getOtbcredit(), bean.getOtbcash(), Configurations.EOD_USER, bean.getCustomerid());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateAccountOtb(OtbBean otbBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE CARDACCOUNT SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateAccountOtb(), otbBean.getOtbcredit(), otbBean.getOtbcash(), Configurations.EOD_USER, otbBean.getAccountnumber());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateCardOtb(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE CARD SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?,TEMPCASHAMOUNT = TEMPCASHAMOUNT + ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateCardOtb(), cardBean.getOtbcredit(), cardBean.getOtbcash(), cardBean.getOtbcredit(), cardBean.getOtbcash(), Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateOnlineCustomerOtb(OtbBean bean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCREDIT= OTBCREDIT - ?,OTBCASH= OTBCASH - ? WHERE CUSTOMERID = ?";
            count = onlineJdbcTemplate.update(queryParametersList.getChequeReturn_updateOnlineCustomerOtb(), bean.getOtbcredit(), bean.getOtbcash(), bean.getCustomerid());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateOnlineAccountOtb(OtbBean otbBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ? WHERE ACCOUNTNUMBER=?";
            count = onlineJdbcTemplate.update(queryParametersList.getChequeReturn_updateOnlineAccountOtb(), otbBean.getOtbcredit(), otbBean.getOtbcash(), otbBean.getAccountnumber());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateOnlineCardOtb(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE ECMS_ONLINE_CARD SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?,TEMPCASHAMOUNT = TEMPCASHAMOUNT + ?,LASTUPDATEUSER=?, LASTUPDATETIME=SYSDATE WHERE CARDNUMBER=?";
            count = onlineJdbcTemplate.update(queryParametersList.getChequeReturn_updateOnlineCardOtb(), cardBean.getOtbcredit(), cardBean.getOtbcash(), cardBean.getTmpcredit(), cardBean.getTmpcash(), Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateEODCARDBalanceKnockOn(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE EODCARDBALANCE SET CUMCASHADVANCES = CUMCASHADVANCES + ?,CUMTRANSACTIONS = CUMTRANSACTIONS + ?,FINANCIALCHARGES = FINANCIALCHARGES + ?,EODCLOSINGBAL = EODCLOSINGBAL - ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateEODCARDBalanceKnockOn(), cardBean.getCumcashadvance(), cardBean.getCumtransactions(), cardBean.getFinacialcharges(), cardBean.getOtbcredit(), Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public InterestDetailBean getIntProf(String accountnumber) throws Exception {
        InterestDetailBean interestDetailBean = null;
        try {
            //String query = "SELECT IP.INTERESTRATE, IP.INTERESTPERIODVALUE FROM CARDACCOUNT CA INNER JOIN INTERESTPROFILE IP ON IP.INTERESTPROFILECODE = CA.INTERESTPROFILECODE WHERE CA.ACCOUNTNO= ?";
            interestDetailBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getIntProf(),
                    (rs, rowNum) -> {
                        InterestDetailBean bean = new InterestDetailBean();
                        bean.setInterest(rs.getDouble("INTERESTRATE"));
                        bean.setInterestperiod(rs.getDouble("INTERESTPERIODVALUE"));
                        return bean;
                    },
                    accountnumber);
        } catch (EmptyResultDataAccessException ex) {
            return interestDetailBean;
        } catch (Exception ex) {
            throw ex;
        }
        return interestDetailBean;
    }

    @Override
    public String getTxnIdForLastChequeByAccount(PaymentBean bean) throws EmptyResultDataAccessException {
        String txnID = null;
        try {
            //String query = "SELECT TRANSACTIONID FROM EODTRANSACTION WHERE ACCOUNTNO=? AND SEQUENCENUMBER=? AND TRANSACTIONTYPE=?";
            txnID = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getTxnIdForLastChequeByAccount(), String.class, bean.getAccountNo(), bean.getSequencenumber(), Configurations.TXN_TYPE_PAYMENT);
        } catch (EmptyResultDataAccessException ex) {
            return txnID;
        } catch (Exception ex) {
            throw ex;
        }
        return txnID;
    }

    @Override
    public String getTxnIdForLastCheque(PaymentBean bean) throws EmptyResultDataAccessException {
        String txnID = null;
        try {
            //String query = "SELECT TRANSACTIONID FROM EODTRANSACTION WHERE CARDNUMBER=? AND SEQUENCENUMBER=? AND TRANSACTIONTYPE=?";
            txnID = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getTxnIdForLastCheque(), String.class, bean.getCardnumber().toString(), bean.getSequencenumber(), Configurations.TXN_TYPE_PAYMENT);
        } catch (EmptyResultDataAccessException ex) {

        } catch (Exception ex) {
            throw ex;
        }
        return txnID;
    }

    @Override
    public boolean checkDuplicateChequeReturnEntry(StringBuffer cardNumber, Double txnAmount, String txnId, String traceid, String seqNo) throws EmptyResultDataAccessException {
        String transactionId = null;
        boolean existDuplicates = false;
        try {
            //String query = "SELECT TRANSACTIONID FROM EODTRANSACTION WHERE CARDNUMBER=? AND TRANSACTIONAMOUNT=? AND TRANSACTIONID=? AND STATUS=? AND SEQUENCENUMBER=? AND PAYMENTTYPE=? AND TRANSACTIONTYPE=?";
            transactionId = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_checkDuplicateChequeReturnEntry(), String.class, cardNumber.toString(), txnAmount, txnId, status.getCHEQUE_RETURN_STATUS(), seqNo, status.getCHEQUE_PAYMENT(), Configurations.TXN_TYPE_DEBIT_PAYMENT);
        } catch (EmptyResultDataAccessException ex) {
            return existDuplicates;
        } catch (Exception ex) {
            throw ex;
        }
        if (transactionId != null) {
            existDuplicates = true;
        }
        return existDuplicates;
    }

    @Override
    public int insertReturnChequeToEODTransaction(StringBuffer cardnumber, String accountNo, Double txnAmount, String txnId, String traceid, String seqNo, String cardAssociation) throws Exception {
        int count = 0;
        try {
            //String query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,TRANSACTIONAMOUNT,CURRENCYTYPE,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,STATUS,TRANSACTIONDESCRIPTION,CRDR,TRACEID,SEQUENCENUMBER,PAYMENTTYPE,TRANSACTIONTYPE,CARDASSOCIATION) VALUES (?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?,?,?)";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_insertReturnChequeToEODTransaction(), Configurations.EOD_ID, cardnumber.toString(), accountNo, txnAmount, "144", CommonMethods.getSqldate(Configurations.EOD_DATE), CommonMethods.getSqldate(Configurations.EOD_DATE), txnId, Configurations.EOD_USER, status.getCHEQUE_RETURN_STATUS(), "Cheque Returned - " + seqNo, Configurations.DEBIT, traceid, seqNo, status.getCHEQUE_PAYMENT(), Configurations.TXN_TYPE_DEBIT_PAYMENT, cardAssociation);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        boolean forward = false;
        //String query = null;
        try {
            forward = this.checkFeeExistForCard(cardNumber, feeCode);
            if (forward) {
                boolean isFeeUpdateRequired = this.getFeeCode(cardNumber, feeCode);
                if (isFeeUpdateRequired) {
                    //query = "UPDATE CARDFEECOUNT SET FEECOUNT = FEECOUNT + 1,CASHAMOUNT=CASHAMOUNT+?, LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE, STATUS =? WHERE CARDNUMBER = ? AND FEECODE = ?";
                    count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_addCardFeeCount_Update(), cashAmount, Configurations.EOD_USER, status.getEOD_PENDING_STATUS(), cardNumber.toString(), feeCode);
                } else {
                    //query = "INSERT INTO CARDFEECOUNT (CARDNUMBER,FEECODE,FEECOUNT,CASHAMOUNT,STATUS,CREATEDDATE,LASTUPDATEDTIME,LASTUPDATEDUSER) VALUES (?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),SYSDATE,?)";
                    count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_addCardFeeCount_Insert(), cardNumber.toString(), feeCode, 1, cashAmount, status.getEOD_PENDING_STATUS(), sdf.format(Configurations.EOD_DATE), Configurations.EOD_USER);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception {
        int recordCount = 0;
        boolean forward = false;
        try {
            //String query = "SELECT COUNT(C.CARDNUMBER) AS RECORDCOUNT FROM CARD C INNER JOIN FEEPROFILEFEE FPF ON C.FEEPROFILECODE  = FPF.FEEPROFILECODE WHERE C.CARDNUMBER   = ? AND FPF.FEECODE NOT IN (SELECT PFPF.FEECODE FROM CARD C INNER JOIN PROMOFEEPROFILE PFP ON C.PROMOFEEPROFILECODE = PFP.PROMOFEEPROFILECODE INNER JOIN PROMOFEEPROFILEFEE PFPF ON C.PROMOFEEPROFILECODE = PFPF.PROMOFEEPROFILECODE WHERE C.CARDNUMBER = ? AND STATUS <> ?) AND FPF.FEECODE = ?";
            recordCount = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_checkFeeExistForCard(), Integer.class, cardNumber.toString(), cardNumber.toString(), status.getFEE_PROMOTION_PROFILE_EXPIRE(), feeCode);
            if (recordCount > 0) {
                forward = true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return forward;
    }

    @Override
    public Boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws EmptyResultDataAccessException {
        boolean forward = false;
        int feeCount = 0;
        try {
            //String query = "SELECT C.FEECOUNT FROM CARDFEECOUNT C WHERE C.CARDNUMBER = ? AND C.FEECODE = ?";
            feeCount = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getFeeCode(), Integer.class, cardNumber.toString(), feeCode);
            if (feeCount > 0 && (!feeCode.equals(Configurations.LATE_PAYMENT_FEE) && !feeCode.equals(Configurations.ANNUAL_FEE))) {
                forward = true;
            }
        } catch (EmptyResultDataAccessException ex) {
            return forward;
        } catch (Exception ex) {
            throw ex;
        }
        return forward;
    }

    @Override
    public int updatePaymentStatus(StringBuffer cardno, String status, String seqNo) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE PAYMENT SET STATUS=? WHERE SEQUENCENUMBER=? AND CARDNUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updatePaymentStatus(), status, seqNo, cardno.toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateTransactionEODStatus(StringBuffer newCardNo, StringBuffer oldCardNo, String status, String seqNo) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE TRANSACTION SET EODSTATUS=? WHERE CB_SEQ_NO = ? AND CARDNO IN (?, ?)";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateTransactionEODStatus(), status, seqNo, newCardNo.toString(), oldCardNo.toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateChequeStatusForEODTxn(PaymentBean payBean, String accountNo) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE EODTRANSACTION SET STATUS=? WHERE ACCOUNTNO=? AND TRANSACTIONTYPE=? AND SEQUENCENUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateChequeStatusForEODTxn(), status.getCHEQUE_RETURN_STATUS(), accountNo, Configurations.TXN_TYPE_PAYMENT, payBean.getSequencenumber());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public int updateChequePaymentStatus(int id, String status) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE CHEQUEPAYMENT SET STATUS=? WHERE ID=?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateChequePaymentStatus(), status, id);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNo) throws Exception {
        String accNo = null;
        try {
            //String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";
            accNo = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getAccountNoOnCard(), String.class, cardNo.toString());
        } catch (Exception ex) {
            throw ex;
        }
        return accNo;
    }

    @Override
    public double getPaymentAmountBetweenDueDate(String accNO, int startEOD, String status, String dueDate) throws Exception {
        double paymentAmount = 0;
        try {
            //String query = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTALPAY FROM EODTRANSACTION WHERE TRANSACTIONTYPE =? AND EODID > (SELECT NVL(ENDEODID,0) AS ENDEODID FROM BILLINGSTATEMENT WHERE ACCOUNTNO = ? AND STARTEODID = ?) AND SETTLEMENTDATE <= TO_DATE(?,'DD-MM-YY') AND STATUS IN(?) AND ACCOUNTNO IN (?)";
            paymentAmount = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getPaymentAmountBetweenDueDate(), Double.class, Configurations.TXN_TYPE_PAYMENT, accNO, startEOD, dueDate, status, accNO);
        } catch (EmptyResultDataAccessException ex) {
            return paymentAmount;
        } catch (Exception ex) {
            throw ex;
        }
        return paymentAmount;
    }

    @Override
    public EodInterestBean getEodInterestForCard(StringBuffer cardNo) throws Exception {
        EodInterestBean eodInterestBean = null;
        try {
            //String query = "SELECT CARDNO, FORWARDAMOUNT, CURRENTINTEREST,ACTUALINTEREST, INTERESTRATE, DUEDATE FROM EODINTEREST WHERE CARDNO=?";
            eodInterestBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getEodInterestForCard(),
                    (rs, rowNum) -> {
                        EodInterestBean bean = new EodInterestBean();
                        bean.setCardNumber(new StringBuffer(rs.getString("CARDNO")));
                        bean.setForwardAmount(rs.getDouble("FORWARDAMOUNT"));
                        bean.setCurrentInterest(rs.getDouble("CURRENTINTEREST"));
                        bean.setActualInterest(rs.getDouble("ACTUALINTEREST"));
                        bean.setInterestRate(rs.getDouble("INTERESTRATE"));
                        bean.setDueDate(rs.getDate("DUEDATE"));
                        return bean;
                    },
                    cardNo.toString());
        } catch (EmptyResultDataAccessException ex) {
            return eodInterestBean;
        } catch (Exception ex) {
            throw ex;
        }
        return eodInterestBean;
    }

    @Override
    public int updateEodInterestForCard(StringBuffer cardNo, double interest) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE EODINTEREST SET ACTUALINTEREST =ACTUALINTEREST+?,CURRENTINTEREST=CURRENTINTEREST+? WHERE CARDNO = ?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateEodInterestForCard(), interest, interest, cardNo.toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public Boolean getFeeCodeIfThereExists(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;
        int feeCount = 0;
        try {
            //String query = "SELECT COUNT(C.CARDNUMBER) AS EECOUNT FROM CARDFEECOUNT C WHERE C.CARDNUMBER = ? AND C.FEECODE = ?";
            feeCount = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getFeeCodeIfThereExists(), Integer.class, cardNumber.toString(), feeCode);

            if (feeCount > 0 && (feeCode.equals(Configurations.LATE_PAYMENT_FEE) || feeCode.equals(Configurations.ANNUAL_FEE))) {
                forward = true;
            }
        } catch (EmptyResultDataAccessException ex) {
            return forward;
        } catch (Exception ex) {
            throw ex;
        }
        return forward;
    }

    @Override
    public boolean restoreMinimumPayment(StringBuffer cardNo) throws SQLException {
        int count = 0;
        boolean flag = false;
        try {
            //String query = "UPDATE MINIMUMPAYMENT M SET(CARDNO,M1,M2,M3,M4,M5,M6,M7,M8,M9,M10,M11,M12,STATUS,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,COUNT,M1DATE,M2DATE,M3DATE,M4DATE,M5DATE,M6DATE,M7DATE,M8DATE,M9DATE,M10DATE,M11DATE,M12DATE,LASTEODID)=(SELECT LM.* FROM BACKUPMINIMUMPAYMENT LM WHERE LM.CARDNO=? AND LM.EODID=M.LASTEODID AND ROWNUM=1) WHERE EXISTS (SELECT B.CARDNO FROM BACKUPMINIMUMPAYMENT B WHERE B.CARDNO=M.CARDNO AND B.EODID=M.LASTEODID  AND B.CARDNO=?)";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_restoreMinimumPayment(), cardNo.toString(), cardNo.toString());
            if (count == 1) {
                flag = true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return flag;
    }

    @Override
    public BlockCardBean getCardBlockOldCardStatus(StringBuffer cardNumber) throws Exception {
        BlockCardBean blockBean = null;
        try {
            //String query = "SELECT OLDSTATUS,NEWSTATUS,BLOCKREASON FROM CARDBLOCK WHERE CARDNUMBER = ? AND STATUS IN(?)";
            blockBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getCardBlockOldCardStatus(),
                    (rs, rowNum) -> {
                        BlockCardBean bean = new BlockCardBean();
                        bean.setOldStatus(rs.getString("OLDSTATUS"));
                        bean.setNewStatus(rs.getString("NEWSTATUS"));
                        bean.setCardNo(cardNumber);
                        return bean;
                    },
                    cardNumber.toString(),
                    status.getACTIVE_STATUS());

        } catch (EmptyResultDataAccessException ex) {
            return blockBean;
        } catch (Exception ex) {
            throw ex;
        }
        return blockBean;
    }

    @Override
    public int updateCardStatus(StringBuffer cardNumber, String status) throws Exception {
        int count = 0;
        try {
          //  String query = "UPDATE CARD SET CARDSTATUS=? , LASTUPDATEDTIME = SYSDATE , LASTUPDATEDUSER = ? WHERE CARDNUMBER = ?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateCardStatus(), status, Configurations.EOD_USER, cardNumber.toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public String[] getRiskClassOnNdia(int noOfDates) throws Exception {
        String[] newriskClass = new String[3];
        BucketBean bucketBean = null;
        try {
            //String query = "SELECT BUCKETID, MINNDIA FROM BUCKET WHERE CASE WHEN (SELECT MAX(MAXNDIA) FROM BUCKET WHERE STATUS = 'ACT') < ? THEN (SELECT MAX(BUCKETID) FROM BUCKET WHERE STATUS = 'ACT') ELSE (SELECT BUCKETID FROM BUCKET WHERE STATUS = 'ACT' AND MINNDIA <= ? AND MAXNDIA >= ?) END = BUCKETID";
            bucketBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_getRiskClassOnNdia(),
                    (rs, rowNum) -> {
                        BucketBean bean = new BucketBean();
                        bean.setBucketId(rs.getString("BUCKETID"));
                        bean.setMinNdia(rs.getString("MINNDIA"));
                        return bean;
                    },
                    noOfDates,
                    noOfDates,
                    noOfDates);

            String riskClass = bucketBean.getBucketId();
            String minNdia = bucketBean.getMinNdia();

            if (riskClass.equals(status.getRISK_CLASS_NINE())) {
                newriskClass[0] = minNdia;
            } else {
                newriskClass[0] = Integer.toString(noOfDates);
            }

            newriskClass[1] = riskClass;
            newriskClass[2] = minNdia;

        } catch (EmptyResultDataAccessException ex) {
            return newriskClass;
        } catch (Exception ex) {
            throw ex;
        }
        return newriskClass;
    }

    @Override
    public int updateDelinquencyStatus(StringBuffer cardNo, String delinqClass, int ndia) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE DELINQUENTACCOUNT SET RISKCLASS=?, NDIA=? WHERE CARDNUMBER = ?";
            count = backendJdbcTemplate.update(queryParametersList.getChequeReturn_updateDelinquencyStatus(), delinqClass, ndia, cardNo.toString());
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public Boolean insertToMinPayTableOld(StringBuffer cardNo, double fee, double totalTransactions, Date dueDate, double paymentAmount) throws Exception {
        MinimumPaymentBean minimumPaymentBean = null;
        Boolean flag = false;
        int month = 0;
        //String allPayments = "SELECT * FROM MINIMUMPAYMENT WHERE CARDNO=?";
        //String allPaymentsSameDay = "SELECT COUNT(*) AS RECORDCOUNT FROM MINIMUMPAYMENT WHERE CARDNO=? AND LASTEODID=?";
        //String insertQuery = "INSERT INTO MINIMUMPAYMENT (CARDNO,M1,M1DATE,STATUS,COUNT,LASTEODID) values (?,?,?,?,1,?)";
        try {
            minimumPaymentBean = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_insertToMinPayTableOld_allPayments_Select(), new MinimumPaymentRowMapper(), cardNo);

            int count = 0;
            boolean sameDay = false;
            int recordCount = backendJdbcTemplate.queryForObject(queryParametersList.getChequeReturn_insertToMinPayTableOld_allPaymentsSameDay_Select(), Integer.class, cardNo.toString(), Configurations.EOD_ID);
            if (recordCount > 0) {
                //EOD is executing in the same day.
                sameDay = true;
            }
            String mFee = null;//0f;

            for (int i = 1; i <= 12; i++) {
                String nextMonth = null;
                switch (i) {
                    case 1:
                        nextMonth = minimumPaymentBean.getM1();
                        break;
                    case 2:
                        nextMonth = minimumPaymentBean.getM2();
                        break;
                    case 3:
                        nextMonth = minimumPaymentBean.getM3();
                        break;
                    case 4:
                        nextMonth = minimumPaymentBean.getM4();
                        break;
                    case 5:
                        nextMonth = minimumPaymentBean.getM5();
                        break;
                    case 6:
                        nextMonth = minimumPaymentBean.getM6();
                        break;
                    case 7:
                        nextMonth = minimumPaymentBean.getM7();
                        break;
                    case 8:
                        nextMonth = minimumPaymentBean.getM8();
                        break;
                    case 9:
                        nextMonth = minimumPaymentBean.getM9();
                        break;
                    case 10:
                        nextMonth = minimumPaymentBean.getM10();
                        break;
                    case 11:
                        nextMonth = minimumPaymentBean.getM11();
                        break;
                    case 12:
                        nextMonth = minimumPaymentBean.getM12();
                        break;
                }
                if ((nextMonth == null) || (0 == Double.parseDouble(nextMonth))) {
                    month = i;
                    count++;
                    break;
                }
                count++;
            }

            if (month == 0) {
                month = 12;
                count = 12;
            }

            if (sameDay) {
                if (month != 1) {
                    if (month == 0) {
                        month = 12;
                        count = 12;
                    } else {
                        month -= 1;
                    }

                }
            }
            String updateQuery = "UPDATE MINIMUMPAYMENT SET M" + month + "=?,M" + month + "DATE=?, STATUS=?, COUNT =?, LASTEODID=?,LASTUPDATEDTIME=sysdate WHERE CARDNO=?";
            String status1 = status.getEOD_PENDING_STATUS();
            if (month >= Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK) {//TODO 3 should be a variable.
                /**
                 * if the pending months are greater than 3, then the min
                 * amount becomes the actual total transaction amount
                 */
                status1 = status.getCARD_TEMPORARY_BLOCK_Status();
                //Changed by the NP
                //fee = totalTransactions;
            }
            backendJdbcTemplate.update(updateQuery, fee - paymentAmount, dueDate, status1, count, Configurations.EOD_ID, cardNo.toString());
            flag = true;

        } catch (EmptyResultDataAccessException ex) {
            backendJdbcTemplate.update(queryParametersList.getChequeReturn_insertToMinPayTableOld_Insert(), cardNo.toString(), fee - paymentAmount, dueDate, status.getEOD_PENDING_STATUS(), Configurations.EOD_ID);
            //logManager.logError("--no result found--",errorLogger);
            backendJdbcTemplate.update(queryParametersList.getChequeReturn_insertToMinPayTableOld_Insert(), cardNo.toString(), fee - paymentAmount, dueDate, status.getEOD_PENDING_STATUS(), Configurations.EOD_ID);
            flag = true;
        } catch (Exception ex) {
            throw ex;
        }
        return flag;
    }
}
