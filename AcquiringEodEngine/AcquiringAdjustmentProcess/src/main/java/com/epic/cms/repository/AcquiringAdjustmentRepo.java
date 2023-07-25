/**
 * Author : sharuka_j
 * Date : 1/25/2023
 * Time : 7:04 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.AcquiringAdjustmentDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.jpos.iso.ISOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


@Repository
public class AcquiringAdjustmentRepo implements AcquiringAdjustmentDao {
    @Autowired
    StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<AcqAdjustmentBean> getConfirmedAjustments() throws Exception {
        String query = null;
        ArrayList<AcqAdjustmentBean> adjustmentBeanList = new ArrayList<AcqAdjustmentBean>();

        try {

            query = "SELECT REQUESTID ,CARDNUMBER,MERCHANTID, CARDORMERCHANT , TXNID , AMOUNT , REMARKS, CRDR , MCC, CURRENCYTYPE,ADJUSTDATE, TRANSACTIONTYPE , ADJUSTMENTTYPE FROM ACQADJUSTMENT WHERE EODSTATUS = ? AND TRUNC(ADJUSTDATE) <= TO_DATE(?,'DD-MM-YY') AND STATUS      = ?";

//            adjustmentBeanList =
            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
//                        String cardOrMerchant;
                        while (result.next()) {
//                            cardOrMerchant = result.getString("CARDORMERCHANT");

                            AcqAdjustmentBean adjustmentBean = new AcqAdjustmentBean();
                            adjustmentBean.setTxnId(result.getString("TXNID"));
                            adjustmentBean.setId(Integer.toString(result.getInt("REQUESTID")));
                            adjustmentBean.setAdjustAmount(result.getString("AMOUNT"));
                            adjustmentBean.setAdjustDes(result.getString("REMARKS"));
                            adjustmentBean.setAdjustType(result.getString("ADJUSTMENTTYPE"));
                            adjustmentBean.setMerchantId(result.getString("MERCHANTID"));
                            adjustmentBean.setCrDr(result.getString("CRDR"));
                            adjustmentBean.setTxnType(result.getString("TRANSACTIONTYPE"));
                            adjustmentBean.setCurruncyType(result.getString("CURRENCYTYPE"));
                            adjustmentBean.setCardNumber(result.getString("CARDNUMBER") != null ? new StringBuffer(result.getString("CARDNUMBER")) : new StringBuffer(""));
//                            adjustmentBean.setCardOrMerchant(cardOrMerchant);
                            adjustmentBean.setMcc(result.getString("MCC"));
                            adjustmentBean.setAdjustDate(result.getDate("ADJUSTDATE"));
                            adjustmentBeanList.add(adjustmentBean);
                        }
                        return adjustmentBeanList;
                    },status.getEOD_PENDING_STATUS()
                    ,Configurations.EOD_DATE_String
                    ,status.getMANUAL_ADJUSTMENT_ACCEPT()
            );

        } catch (Exception e) {
            throw e;
        }
        return adjustmentBeanList;

    }

    @Override
    public int insertToEodMerchantPayment(MerchantPayBean merchantPaymentBean, String adjustmentFlag) throws Exception {
        String query = null;
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            query = "INSERT INTO EODMERCHANTPAYMENT (EODID,MERCHANTID,MERCHANTCUSTID,MERACCOUNTNO,CUSTACCOUNTNO,TOTALPAYAMOUNT,CRDR,CURRENCYTYPE,TOTALFEEAMOUNT,CRDRFEE,TOTALCOMMISSION,CRDRCOMMISSION,NETPAYAMMOUNT,CRDRNET,TOACCOUNTNO,STATUS,LASTUPDATEDTIME,CREATEDTIME,LASTUPDATEDUSER,PAYMENTDATE,TXNCOUNT,ADJUSTMENTFLAG) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,TO_DATE(?,'DD-MM-YY'),?,?)";

            count = backendJdbcTemplate.update(query
                    , Configurations.EOD_ID //1
                    , merchantPaymentBean.getMerchantId() //2
                    , merchantPaymentBean.getMerchantCusId() //3
                    , merchantPaymentBean.getMerchantAccNo() //4
                    , merchantPaymentBean.getMerchantCusAccNo() //5
                    , merchantPaymentBean.getPaymentAmount() //6
                    , merchantPaymentBean.getPaymentCrDr() //7
                    , merchantPaymentBean.getCurrencyType() //8
                    , merchantPaymentBean.getFeeAmount() //9
                    , merchantPaymentBean.getPaymentCrDr() //10
                    , merchantPaymentBean.getCommAmount() //11
                    , merchantPaymentBean.getCrDrCommision()
                    , merchantPaymentBean.getNetPayAmount()
                    , merchantPaymentBean.getPaymentCrDr()
                    , merchantPaymentBean.getAccountNo() //TOACCOUNTNO
                    , Configurations.EOD_PENDING_STATUS //16
                    , Configurations.EOD_USER
                    , sdf.format(Configurations.EOD_DATE)
                    , merchantPaymentBean.getTxncount()
                    , Integer.parseInt(adjustmentFlag) //20
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertToEodMerchantComission(String merchantCusNo, String merhantCusAcountNo, String mId, String merchantAccNo, String tId, String txnAmount, String merchantComission, String currency, String crDr, Date txnDate, String txnTypeCode, String batchNo, String txnId, String binStatus, String calMethod, String cardAssociation, String cardProduct, String segment, String originCardProduct, int adjustmentFlag) throws Exception {
        String query = null;
        DateFormat formatter;
        int count = 0;
        try {
            query = "INSERT INTO EODMERCHANTCOMMISSION (EODID,MERCHANTCUSTID,CUSTACCOUNTNO,MID,MERACCOUNTNO,TID,TRANSACTIONAMOUNT,MERCHANTCOMMSSION,MERCHANTDUEAMOUNT,CURRENCYTYPE,CRDR,TRANSACTIONDATE,TRANSACTIONTYPE,BATCHNO,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,STATUS,BINTYPE,CALMETHOD,CARDASSOCIATION,PRODUCTID,SEGMENT,EODDATE,CARDPRODUCT,ADJUSTMENTFLAG) VALUES (?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),?,?)";

            formatter = new SimpleDateFormat("dd-MMM-yy");
            count = backendJdbcTemplate.update(query
                    , Configurations.EOD_ID
                    , merchantCusNo
                    , merhantCusAcountNo
                    , mId
                    , merchantAccNo //5
                    , tId
                    , txnAmount
                    , merchantComission
                    , (new BigDecimal(merchantComission).setScale(2, RoundingMode.DOWN).toString())
                    , currency //10
                    , crDr
                    , txnDate
                    , txnTypeCode
                    , batchNo
                    , txnId //15
                    , Configurations.EOD_USER
                    , status.getINITIAL_STATUS() //17
                    , binStatus
                    , calMethod
                    , cardAssociation //20
                    , cardProduct //21
                    , segment //22
                    , formatter.format(Configurations.EOD_DATE) //23
                    , originCardProduct //24
                    , adjustmentFlag //25
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void insertToEODMerchantFee(MerchantFeeBean merchantFeeBean, String amount, java.util.Date effectDate) throws Exception {
        String query = null;

        try {
            query = "INSERT INTO EODMERCHANTFEE(EODID,MERCHANTID,CRDR,EFFECTDATE,FEEAMOUNT,CUSTACCOUNTNO,MERACCOUNTNO,MERCHANTCUSTID,STATUS,FEETYPE,LASTUPDATEDUSER,LASTUPDATEDDATE)VALUES (?,?,?,?,?,?,?,?,?,?,?,SYSDATE)";

            backendJdbcTemplate.update(query
                    , Configurations.EOD_ID //1
                    , merchantFeeBean.getMID() //2
                    , merchantFeeBean.getCrORdr() //3
                    , (java.sql.Date) effectDate //4
                    , amount //5
                    , merchantFeeBean.getCustAccountNo() //6
                    , merchantFeeBean.getMerchantAccountNo() //7
                    , merchantFeeBean.getMerchantCustomerNo() //8
                    , Configurations.EOD_PENDING_STATUS //9
                    , merchantFeeBean.getFeeCode() //10
                    , Configurations.EOD_USER //11
            );
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean getOnUsStatus(String txnId) throws Exception {
        int onoffStatus;
        String query = "SELECT NVL(ONOFFSTSTUS,0) AS ONOFFSTSTUS FROM TRANSACTION WHERE TXNID=?";
        try {
//          st=  backendJdbcTemplate.query(query
//                    , (ResultSet rs) -> {
//                        int onoffStatus = 0;
//                        if (rs.next()) {
//                            onoffStatus = rs.getInt("ONOFFSTSTUS");
////                            if (onoffStatus == status.getONUS_STATUS()) {
////                                return true;
////                            } else {
////                                return false;
////                            } ((isProcessError == 0) ? false : true);
//                            (onoffStatus == status.getONUS_STATUS()) ? true :false;
//                        }
//                    }
//            );

            onoffStatus = backendJdbcTemplate.queryForObject(query, Integer.class, txnId);
            if (onoffStatus == status.ONUS_STATUS) {
                return true;
            } else {
                return false;
            }
        } catch (EmptyResultDataAccessException e) {
            throw e;
        }
//        return false;
    }

    @Override
    public int insertReversalTxnIntoTxnTable(String oldTxnId, String newTxnID) throws Exception {
        PreparedStatement pst = null;
        String query;
        int count = 0;

        try {
            query = "INSERT INTO TRANSACTION(TXNID,MTI,RESPONSEMTI,PROCESSINGCODE,OFFMTI,OFFRESPONSEMTI,OFFPROCESSINGCODE,NII,TXNTYPECODE,BIN,REQUESTFROM,LISTENERTYPE,ONOFFSTSTUS,SERVICECODE,CARDNO,EXPIRYDATE,TXNCURRENCY,TXNAMOUNT,BILLINGCURRENCY,BILLINGAMOUNT,SETTLEMENTCURRENCY,SETTLEMENTAMOUNT,SETTLEMENTTXNCOUNT,SETTLEMENTDATE,BATCHNO,TID,MID,MCC,COUNTRYCODE,TRACENO,INVOICENO,RRN,AUTHCODE,RESPONSECODE,STATUS,POSCONDITIONCODE,POSENTRYMODE,AIIC,FIIC,TAG5F2A,TAG9A,TAG9C,TAG9F34,TAG9F02,TAG9F03,TAG9F1A,TAG9F1E,TAG9F27,TAG9F33,TAG9F35,TAG9F41,TXNTIME,TXNDATE,LOCALTIME,LOCALDATE,CREATETIME,LASTUPDATETIME,CARDSEQUENSENUMBER,FROMACCOUNT,TOACCOUNT,CHANNELTYPE,EODSTATUS,ACCEPTORNAME,BILLACCOUNTNO,BILLPROVIDERID,BILLREFNO,CAIC,F60TERMTYPE,F62TXNID,TXNSUBTYPE,CHQBANKNAME,CHQBRANCHNAME,CHQNO,CHQRETURNDATE,PAYMENTINITTYPE,PAYMENTMODE,VISATXNSTATUS,BACKENDTXNTYPE,AUTOSETTLEMENTSTATUS,ONLINECREATEDTIME,CB_SEQ_NO,ECI,REMARKS,ORIGINALTXNID,CVM,VISACVV2RESULT,VISAREQRESREASONCODE,EMV_9F33,EMV_95,EMV_82,EMV_9A,EMV_9C,EMV_5F2A,EMV_9F02,EMV_9F03,EMV_9F10,EMV_9F1A,EMV_9F26,EMV_9F36,EMV_9F37,EMV_9F6E,LISTENERID,PURCHASE_ID,SECOND_PARTY_PAN,ACQORISS,EODCONSIDERSTATUS,MVV,EMV_9F27,F60_DATA,F15_SETTLE_DATE,TXNTRANDATEANDTIME)  SELECT ?,MTI,RESPONSEMTI,PROCESSINGCODE,OFFMTI,OFFRESPONSEMTI,OFFPROCESSINGCODE,NII,?,BIN,REQUESTFROM,LISTENERTYPE,ONOFFSTSTUS,SERVICECODE,CARDNO,EXPIRYDATE,TXNCURRENCY,TXNAMOUNT,BILLINGCURRENCY,BILLINGAMOUNT,SETTLEMENTCURRENCY,SETTLEMENTAMOUNT,SETTLEMENTTXNCOUNT,SETTLEMENTDATE,BATCHNO,TID,MID,MCC,COUNTRYCODE,NULL,INVOICENO,RRN,AUTHCODE,RESPONSECODE,STATUS,POSCONDITIONCODE,POSENTRYMODE,AIIC,FIIC,TAG5F2A,TAG9A,TAG9C,TAG9F34,TAG9F02,TAG9F03,TAG9F1A,TAG9F1E,TAG9F27,TAG9F33,TAG9F35,TAG9F41,TXNTIME,TXNDATE,LOCALTIME,LOCALDATE,CREATETIME,LASTUPDATETIME,CARDSEQUENSENUMBER,FROMACCOUNT,TOACCOUNT,CHANNELTYPE,?,ACCEPTORNAME,BILLACCOUNTNO,BILLPROVIDERID,BILLREFNO,CAIC,F60TERMTYPE,F62TXNID,TXNSUBTYPE,CHQBANKNAME,CHQBRANCHNAME,CHQNO,CHQRETURNDATE,PAYMENTINITTYPE,PAYMENTMODE,VISATXNSTATUS,?,AUTOSETTLEMENTSTATUS,ONLINECREATEDTIME,CB_SEQ_NO,ECI,REMARKS,?,CVM,VISACVV2RESULT,VISAREQRESREASONCODE,EMV_9F33,EMV_95,EMV_82,EMV_9A,EMV_9C,EMV_5F2A,EMV_9F02,EMV_9F03,EMV_9F10,EMV_9F1A,EMV_9F26,EMV_9F36,EMV_9F37,EMV_9F6E,LISTENERID,PURCHASE_ID,SECOND_PARTY_PAN,ACQORISS,EODCONSIDERSTATUS,MVV,EMV_9F27,F60_DATA,F15_SETTLE_DATE,TO_CHAR(SYSDATE,'MMDDHHMISS') FROM TRANSACTION WHERE TXNID=?";

            count = backendJdbcTemplate.update(query
                    , newTxnID //TXNID
                    , Configurations.TXN_TYPE_ONLINE_REVERSAL //TXNTYPECODE
                    , status.getEOD_DONE_STATUS() //EODSTATUS
                    , Configurations.TXN_TYPE_REVERSAL  //BACKENDTXNTYPE
                    , oldTxnId //ORIGINALTXNID
                    , oldTxnId //TXNID
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertReversalTxnIntoMerchantTxnTable(String oldTxnId, String newTxnID) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String query = "INSERT INTO EODMERCHANTTRANSACTION ( EODID,MID,TID,TRANSACTIONAMOUNT,CURRENCYTYPE,CRDR,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE ,BATCHNO,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,TOACCOUNTNO, STATUS,TRANSACTIONDESCRIPTION,COUNTRYNUMCODE,ONOFFSTATUS,PAYMENTTYPE,POSENTRYMODE, SEQUENCENUMBER,TRACEID,FOREXMARKUPAMOUNT,AUTHCODE,RRN,MCC,OUTGOINGFILESTATUS,CARDNUMBER,BIN,ADJUSTMENTFLAG,CARDASSOCIATION,REQUESTFROM,CARDPRODUCT)  SELECT  ?,MID,TID,TRANSACTIONAMOUNT,CURRENCYTYPE,?,?,TRANSACTIONDATE,? ,BATCHNO,?,?,SYSDATE,SYSDATE,TOACCOUNTNO ,?,TRANSACTIONDESCRIPTION ,COUNTRYNUMCODE,ONOFFSTATUS,PAYMENTTYPE,POSENTRYMODE ,SEQUENCENUMBER,TRACEID,FOREXMARKUPAMOUNT,AUTHCODE,RRN,MCC,'0',CARDNUMBER,BIN,?,CARDASSOCIATION,REQUESTFROM,CARDPRODUCT  FROM EODMERCHANTTRANSACTION WHERE TRANSACTIONID=?";

            count = backendJdbcTemplate.update(query
                    , Configurations.EOD_ID
                    , Configurations.CREDIT
                    , sdf.format(Configurations.EOD_DATE)
                    , Configurations.TXN_TYPE_REVERSAL
                    , newTxnID
                    , Configurations.EOD_USER
                    , Configurations.EOD_DONE_STATUS
                    , Integer.parseInt(Configurations.ACQ_ADJUSTMENT_TYPE_REVERSAL)
                    , oldTxnId);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertReversalCommission(String oldTxnId, String newTxnID) throws Exception {
        String query;
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            query = "INSERT INTO EODMERCHANTCOMMISSION ( EODID,MERCHANTCUSTID,CUSTACCOUNTNO,MID,MERACCOUNTNO,TID,TRANSACTIONAMOUNT,MERCHANTCOMMSSION,MERCHANTDUEAMOUNT,CURRENCYTYPE ,CRDR,TRANSACTIONDATE,TRANSACTIONTYPE,BATCHNO,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,TOACCOUNTNO ,STATUS,BINTYPE,CALMETHOD,CARDASSOCIATION,PRODUCTID,SEGMENT,BINSTATUS,EODDATE,ADJUSTMENTFLAG,CARDPRODUCT,MDRFLATAMOUNT,MDRPERCENTAGE )  SELECT  ?,MERCHANTCUSTID,CUSTACCOUNTNO,MID,MERACCOUNTNO,TID,TRANSACTIONAMOUNT,MERCHANTCOMMSSION,MERCHANTDUEAMOUNT,CURRENCYTYPE ,?,?,?,BATCHNO,?,?,SYSDATE,SYSDATE,TOACCOUNTNO ,?,BINTYPE,CALMETHOD,CARDASSOCIATION,PRODUCTID,SEGMENT,BINSTATUS,?,?,CARDPRODUCT,MDRFLATAMOUNT,MDRPERCENTAGE  FROM EODMERCHANTCOMMISSION WHERE TRANSACTIONID=?";

            count = backendJdbcTemplate.update(query,
                    Configurations.EOD_ID,
                    Configurations.CREDIT,
                    sdf.format(Configurations.EOD_DATE),
                    Configurations.TXN_TYPE_REVERSAL, newTxnID, Configurations.EOD_USER,
                    Configurations.EOD_DONE_STATUS,
                    sdf.format(Configurations.EOD_DATE),
                    Configurations.ACQ_ADJUSTMENT_TYPE_REVERSAL,
                    oldTxnId);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNo) throws Exception {
        String accNo = null;
        try {
            String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";
            accNo = backendJdbcTemplate.queryForObject(query, String.class, cardNo.toString());
        } catch (EmptyResultDataAccessException e) {
            throw e;
        }
        return accNo;
    }

    @Override
    public String getCardAssociationFromBinRange(String cardNumber) throws Exception {
        String cardAssociation = null;
        try {

            String query = "SELECT CARDASSOCIATION FROM BINROUTINGACQ WHERE BINLOW<=? AND ? <= BINHIGH ORDER BY PRIORITY DESC";

            cardAssociation = backendJdbcTemplate.queryForObject(query, String.class
                    , ISOUtil.zeropadRight(cardNumber, 19)
                    , ISOUtil.zeropadRight(cardNumber, 19));
        } catch (EmptyResultDataAccessException e) {
            throw e;
        }
        return cardAssociation;
    }

    @Override
    public MerchantDetailsBean getMerchanDetails(MerchantDetailsBean merchantDetailsBean) throws Exception {

        String query = "SELECT MC.MERCHANTCUSTOMERNO AS MERCHANTCUSTOMERNO,MC.ACCOUNTNUMBER AS MCACCOUNTNO,ML.ACCOUNTNUMBER AS MLACCOUNTNO,ML.COUNTRY AS MLCOUNTRY  FROM MERCHANTLOCATION ML INNER JOIN MERCHANTCUSTOMER MC ON ML.MERCHANTCUSTOMERNO = MC.MERCHANTCUSTOMERNO WHERE MERCHANTID=?";
        try {

//            merchantDetailsBean =
            backendJdbcTemplate.query(query
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            merchantDetailsBean.setMerchantCusAccNo(rs.getString("MCACCOUNTNO"));
                            merchantDetailsBean.setMerchantAccountNo(rs.getString("MLACCOUNTNO"));
                            merchantDetailsBean.setMerchantCustomerId(rs.getString("MERCHANTCUSTOMERNO"));
                            merchantDetailsBean.setMerchantCountry(rs.getString("MLCOUNTRY"));
                        }
                    }, merchantDetailsBean.getMid()
            );
        } catch (Exception e) {
            throw e;
        }
        return merchantDetailsBean;
    }

    @Override
    public int insertToEODTransaction(StringBuffer cardnumber, String accountNo, String mId, String tId, String txnAmount, int currencyType, String crDr, java.util.Date settlementDate, java.util.Date txnDate, String txnType, String batchNo, String txnId, String toAccNo, Double loyaltyPoint, String Description, String countryCode, int onOffStatus, String poStringsEntryMode, String traceId, String authCode, int adjustemntFlag, String cardAssociation) throws Exception {

        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            /**
             * TODO Loyalty point??
             */
            String query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,MID,TID,TRANSACTIONAMOUNT, CURRENCYTYPE,CRDR,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE,BATCHNO, TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,TOACCOUNTNO,STATUS,TRANSACTIONDESCRIPTION, COUNTRYNUMCODE,ONOFFSTATUS,POSENTRYMODE,TRACEID,AUTHCODE,ACQADJUSTMENTFLAG,CARDASSOCIATION) VALUES (?,?,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),TO_DATE(?,'DD-MM-YY'),?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?,?,?,?,?)";

            count = backendJdbcTemplate.update(query
                    , Configurations.EOD_ID
                    , cardnumber.toString()
                    , accountNo
                    , mId
                    , tId
                    , txnAmount //6
                    , currencyType
                    , crDr
                    , sdf.format(settlementDate)
                    , sdf.format(txnDate)
                    , txnType
                    , batchNo
                    , txnId
                    , Configurations.EOD_USER
                    , toAccNo
                    , status.getINITIAL_STATUS() //16
                    , Description
                    , countryCode
                    , onOffStatus
                    , poStringsEntryMode
                    , onOffStatus
                    , authCode
                    , adjustemntFlag
                    , cardAssociation);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertReversalTxnIntoTxnTable(AcqAdjustmentBean bean, String newTxnID, int onOffStatus) throws Exception {
        String query;
        int count = 0;
        try {
            query = "INSERT INTO TRANSACTION(TXNID,MTI,RESPONSEMTI,PROCESSINGCODE,TXNTYPECODE,BIN,REQUESTFROM,LISTENERTYPE,ONOFFSTSTUS,CARDNO,TXNCURRENCY,TXNAMOUNT,BILLINGCURRENCY,BILLINGAMOUNT,SETTLEMENTTXNCOUNT,TID,MID,MCC,RRN,AUTHCODE,RESPONSECODE,STATUS,POSCONDITIONCODE,TXNDATE,CREATETIME,LASTUPDATETIME,CHANNELTYPE,EODSTATUS,CAIC,BACKENDTXNTYPE,AUTOSETTLEMENTSTATUS,ONLINECREATEDTIME,ORIGINALTXNID,AIIC,FIIC,TXNTRANDATEANDTIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,SYSDATE,?,?,?,TO_CHAR(SYSDATE,'MMDDHHMISS')) ";
            int channelType;
            String aiic = "";
            String fiic = "";
            if (bean.getCardAssociation().equals(Configurations.CUP_ASSOCIATION)) {
                channelType = Configurations.CHANNEL_TYPE_CUP;
                aiic = Configurations.INSTITUTION_IDENTIFICATION_NUMBER;
                fiic = aiic;
            } else if (bean.getCardAssociation().equals(Configurations.MASTER_ASSOCIATION)) {
                channelType = Configurations.CHANNEL_TYPE_MASTER;
                // aiic = Configurations.MASTER_ACQ_BIN;
            } else {
                channelType = Configurations.CHANNEL_TYPE_VISA;
                aiic = Configurations.VISA_ACQ_BIN;
            }

            //remove decimal character from adjustment amount to insert to transaction table(transaction amount column not store decimal places)
            DecimalFormat df = new DecimalFormat("#.00");
            String adjustmentAmountWithoutDecimalChar = ISOUtil.zeropad(df.format(Double.valueOf(bean.getAdjustAmount())).replace(".", ""), 12);

            count = backendJdbcTemplate.update(query
                    , newTxnID //1
                    , "0200"
                    , "0210"
                    , "530040"
                    , Configurations.TXN_TYPE_ONLINE_REFUND //5
                    , bean.getCardNumber().substring(0, 6)
                    , "2"
                    , "9"
                    , onOffStatus //9
                    , bean.getCardNumber().toString()
                    , bean.getCurruncyType()
                    , adjustmentAmountWithoutDecimalChar //12
                    , bean.getCurruncyType()
                    , adjustmentAmountWithoutDecimalChar
                    , "0"
                    , ""
                    , bean.getMerchantId()
                    , bean.getMcc() //18
                    , ""
                    , "000000"
                    , "00"
                    , "0" //22
                    , "00"
                    , new SimpleDateFormat("MMdd").format(bean.getAdjustDate())
                    , channelType//25
                    , Configurations.EOD_DONE_STATUS
                    , ""
                    , Configurations.TXN_TYPE_REFUND
                    , "0"
                    , "" //30
                    , aiic
                    , fiic);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoEodMerchantTransaction(EodTransactionBean eodTransactionBean, String status) throws Exception {
        int count = 0;
        try {
            String query = "INSERT INTO EODMERCHANTTRANSACTION (AUTHCODE,BATCHNO,COUNTRYNUMCODE,CRDR,CREATEDTIME,CURRENCYTYPE,EODID,FOREXMARKUPAMOUNT,GLSTATUS,LASTUPDATEDTIME,LASTUPDATEDUSER,MID,ONOFFSTATUS,PAYMENTTYPE,POSENTRYMODE,RRN,SEQUENCENUMBER,SETTLEMENTDATE,STATUS,TID,TOACCOUNTNO,TRACEID,TRANSACTIONAMOUNT,TRANSACTIONDATE,TRANSACTIONDESCRIPTION,TRANSACTIONID,TRANSACTIONTYPE,MCC,BIN,CARDNUMBER,ADJUSTMENTFLAG,REQUESTFROM,FUELSURCHARGEAMOUNT,CARDASSOCIATION,CARDPRODUCT) VALUES(?,?,?,?,SYSDATE,?,?,?,?,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),?,?,?,?,?,?,?,?,?,?,?)";
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
            count = backendJdbcTemplate.update(query
                    , eodTransactionBean.getAuthCode()
                    , eodTransactionBean.getBatchNo()
                    , eodTransactionBean.getCountryNumCode()
                    , eodTransactionBean.getCrDr()
                    , eodTransactionBean.getCurrencyType()
                    , Configurations.EOD_ID
                    , eodTransactionBean.getForexMarkupAmount() //7
                    , 0
                    , Configurations.EOD_USER
                    , eodTransactionBean.getMid()
                    , eodTransactionBean.getOnOffStatus()
                    , eodTransactionBean.getPaymentType()
                    , eodTransactionBean.getPosEntryMode() //13
                    , eodTransactionBean.getRrn()
                    , eodTransactionBean.getSequenceNumber()
                    , sdf.format(eodTransactionBean.getSettlementDate())
                    , status
                    , eodTransactionBean.getTid()
                    , eodTransactionBean.getToAccNo()
                    , eodTransactionBean.getTraceId()
                    , eodTransactionBean.getTxnAmount()
                    , sdf.format(eodTransactionBean.getTxnDate()) //22
                    , eodTransactionBean.getTxnDescription()
                    , eodTransactionBean.getTxnId()
                    , eodTransactionBean.getTxnType()
                    , eodTransactionBean.getMcc() //26
                    , eodTransactionBean.getBin()
                    , eodTransactionBean.getCardNo().toString()
                    , (!(eodTransactionBean.getAdjustmentFlag() == null)) ? Integer.parseInt(eodTransactionBean.getAdjustmentFlag()) : 0
                    , "2"
                    , eodTransactionBean.getFuelSurchargeAmount()
                    , eodTransactionBean.getCardAssociation()
                    , eodTransactionBean.getCardProduct());

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int getBinType(String sixDigitBin, String eightDigitBin) throws Exception {
        int isOnus = 0;

        //String query = "SELECT BINTYPE FROM BINTABLE WHERE BIN=?";
        String query = "SELECT COUNT(BIN) AS CNT FROM BIN WHERE (BIN=? OR BIN=?) AND ONUSSTATUS='YES'";
        try {
            isOnus = backendJdbcTemplate.queryForObject(query, Integer.class, sixDigitBin, eightDigitBin);
        }catch (EmptyResultDataAccessException ex){
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return isOnus;
    }

    @Override
    public void getPaymentAmount(String txnId, MerchantPayBean paymentBean) throws Exception {
        String query = "SELECT NVL(TRANSACTIONAMOUNT,0) AS TRANSACTIONAMOUNT,NVL(MERCHANTCOMMSSION,0) AS MERCHANTCOMMSSION,(NVL(TRANSACTIONAMOUNT,0)-NVL(MERCHANTCOMMSSION,0)) AS PAYMENTAMOUNT FROM EODMERCHANTCOMMISSION WHERE TRANSACTIONID=?";
        try {
            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        if (rs.next()) {
                            paymentBean.setNetPayAmount(rs.getDouble("PAYMENTAMOUNT"));
                            paymentBean.setPaymentAmount(rs.getDouble("TRANSACTIONAMOUNT"));
                            paymentBean.setCommAmount(rs.getDouble("MERCHANTCOMMSSION"));
                        }
                    },txnId
                    );

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateAdjustmentToEdon(String id, String txnId) throws Exception {
        String query;
        int count = 0;

        try {
            query = "UPDATE ACQADJUSTMENT SET EODSTATUS=?,ADJUSTMENTTXNID =? WHERE REQUESTID=?";
            count = backendJdbcTemplate.update(query, status.getEOD_DONE_STATUS(), txnId, id);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int setCardProductToEodMerTxn() throws Exception {
        int count = 0;

        String query = "MERGE INTO EODMERCHANTTRANSACTION EMT USING ( SELECT BINRANGESTART,BINRANGEEND,PRODUCTID FROM BINTABLE) BT ON (EMT.BIN>=BT.BINRANGESTART AND EMT.BIN<=BT.BINRANGEEND) WHEN MATCHED THEN UPDATE SET EMT.CARDPRODUCT=BT.PRODUCTID WHERE EMT.CARDPRODUCT IS NULL ";
        try {
            count = backendJdbcTemplate.update(query);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
