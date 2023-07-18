package com.epic.cms.repository;

import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import com.epic.cms.dao.RecipientPushTxnUpdateDao;
import org.jpos.iso.ISOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.jpos.iso.ISOUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


@Repository
public class RecipientPushTxnUpdateRepo implements RecipientPushTxnUpdateDao {
    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<EodTransactionBean> getAllRecipientPushTxn() throws Exception {

        ArrayList<EodTransactionBean> txnList = new ArrayList<EodTransactionBean>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {

            String query = "SELECT TXNID,NVL(ONOFFSTSTUS,1) AS ONOFFSTSTUS,CARDNO,TXNCURRENCY,CB_SEQ_NO, NVL((TXNAMOUNT)/100,0) AS TRANSACTIONAMOUNT, NVL(NVL((BILLINGAMOUNT),0)/100,0) AS BILLINGAMOUNT,BATCHNO,TID,MID,MCC,COUNTRYCODE,TRACENO,RRN,AUTHCODE,POSENTRYMODE,CAIC,BACKENDTXNTYPE,TOACCOUNT,CREATETIME,REQUESTFROM,SETTLEMENTDATE,CHANNELTYPE,LISTENERTYPE,FROMACCOUNT,CHANNELTYPE FROM TRANSACTION  WHERE ACQORISS = ? AND RESPONSECODE = ? AND EODSTATUS = ? AND STATUS IN (?,?) AND TRUNC(CREATETIME) <= TRUNC(TO_DATE(?)) AND BACKENDTXNTYPE=?";


            txnList = (ArrayList<EodTransactionBean>) backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((rs, rowNum) -> {

                        int onOffStatus = rs.getInt("ONOFFSTSTUS");
                        StringBuffer cardNumber = new StringBuffer(rs.getString("CARDNO"));
                        if (cardNumber == null || cardNumber.equals("")) {
                            if (rs.getString("FROMACCOUNT").length() <= 16) {
                                try {
                                    cardNumber = new StringBuffer(ISOUtil.zeropad(rs.getString("FROMACCOUNT").trim(), 16));
                                } catch (ISOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                cardNumber = new StringBuffer(rs.getString("FROMACCOUNT").substring(0, 16));
                            }
                        }
                        EodTransactionBean eodTransactionBean = new EodTransactionBean();
                        // eodTransactionBean.setAuthCode(rs.getString("T"));
                        eodTransactionBean.setAuthCode(rs.getString("AUTHCODE"));
                        eodTransactionBean.setBatchNo(rs.getString("BATCHNO"));
                        eodTransactionBean.setCardNo(new StringBuffer(cardNumber));
                        eodTransactionBean.setCountryNumCode(rs.getString("COUNTRYCODE"));
                        eodTransactionBean.setCurrencyType(rs.getString("TXNCURRENCY"));
                        eodTransactionBean.setMid(rs.getString("MID"));
                        eodTransactionBean.setOnOffStatus(rs.getInt(onOffStatus));
                        eodTransactionBean.setPosEntryMode(rs.getString("POSENTRYMODE"));
                        eodTransactionBean.setRrn(rs.getString("RRN"));
                        eodTransactionBean.setSequenceNumber(rs.getString("CB_SEQ_NO"));
                        eodTransactionBean.setSettlementDate(rs.getDate("CREATETIME"));
                        eodTransactionBean.setTid(rs.getString("TID"));
                        eodTransactionBean.setToAccNo(rs.getString("TOACCOUNT"));
                        eodTransactionBean.setTraceId(rs.getString("TRACENO"));
                        eodTransactionBean.setTxnAmount(rs.getString("TRANSACTIONAMOUNT"));
                        eodTransactionBean.setTxnDate(rs.getDate("CREATETIME"));
                        eodTransactionBean.setTxnDescription(rs.getString("CAIC"));
                        eodTransactionBean.setTxnId(rs.getString("TXNID"));
                        eodTransactionBean.setTxnType(rs.getString("BACKENDTXNTYPE"));
                        eodTransactionBean.setMcc(rs.getString("MCC"));
                        eodTransactionBean.setBillingAmount(rs.getString("BILLINGAMOUNT"));
                        eodTransactionBean.setRequestFrom(rs.getString("REQUESTFROM"));
                        eodTransactionBean.setChannelType(rs.getInt("CHANNELTYPE"));
                        return eodTransactionBean;
                    })
                    //, statusList.getCARD_RENEWAL_ACCEPTED()
                    , Configurations.EOD_ACQUIRING_STATUS//recipient as a acq tran
                    , statusList.getRESPONSE_CODE_00()//Response Code
                    , Configurations.EOD_PENDING_STATUS//EPEN Status
                    , statusList.getTXN_SETTLLED_STATUS()//Settle Txn Status
                    , statusList.getTXN_RECIPIENT_REFUND_STATUS()//Recipient Refund Txn Status. (when mvisa refund txn initiated online change status of the original recipient txn too. but the recipient txn still need to post to merchant)
                    , sdf.format(Configurations.EOD_DATE)
                    , Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT);

        } catch (Exception e) {
            throw e;
        }
        return txnList;

    }

    @Override
    public HashMap<String, String> getFinancialStatus() throws Exception {

        HashMap<String, String> visaTxnFields = new HashMap<>();
        HashMap<String, String> result = new HashMap<>();
        try {

            String query = "SELECT TRANSACTIONCODE,FINANCIALSTATUS FROM TRANSACTIONTYPE";
            result = backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            visaTxnFields.put(
                                    rs.getString("TRANSACTIONCODE"),
                                    rs.getString("FINANCIALSTATUS"));
                        }
                        return visaTxnFields;
                    });

        } catch (Exception e) {
            throw e;
        }
        return result;

    }

    @Override
    public String getCardProduct(String bin) throws Exception {

        String cardProduct = null;

        String query = "SELECT PRODUCTID FROM BINTABLE WHERE BINRANGESTART<=? AND BINRANGEEND>= ?";
        try {
            cardProduct = backendJdbcTemplate.queryForObject(query, String.class, bin, bin);
        } catch (EmptyResultDataAccessException ex) {
            return cardProduct;
        } catch (
                Exception e) {
            throw e;
        }

        return cardProduct;
    }

    @Override
    public int insertIntoEodMerchantTransaction(EodTransactionBean eodTransactionBean, String status) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String query = "INSERT INTO EODMERCHANTTRANSACTION (AUTHCODE,BATCHNO,COUNTRYNUMCODE,CRDR,CREATEDTIME,CURRENCYTYPE,EODID,FOREXMARKUPAMOUNT,GLSTATUS,LASTUPDATEDTIME,LASTUPDATEDUSER,MID,ONOFFSTATUS,PAYMENTTYPE,POSENTRYMODE,RRN,SEQUENCENUMBER,SETTLEMENTDATE,STATUS,TID,TOACCOUNTNO,TRACEID,TRANSACTIONAMOUNT,TRANSACTIONDATE,TRANSACTIONDESCRIPTION,TRANSACTIONID,TRANSACTIONTYPE,MCC,BIN,CARDNUMBER,ADJUSTMENTFLAG,REQUESTFROM,FUELSURCHARGEAMOUNT,CARDASSOCIATION,CARDPRODUCT) VALUES(?,?,?,?,SYSDATE,?,?,?,?,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),?,?,?,?,?,?,?,?,?,?,?)";

            count = backendJdbcTemplate.update(query, eodTransactionBean.getAuthCode(),
                    eodTransactionBean.getBatchNo(),
                    eodTransactionBean.getCountryNumCode(),
                    eodTransactionBean.getCrDr(),
                    eodTransactionBean.getCurrencyType(),
                    Configurations.EOD_ID,
                    eodTransactionBean.getForexMarkupAmount(),
                    0,
                    Configurations.EOD_USER,
                    eodTransactionBean.getMid(),
                    eodTransactionBean.getPaymentType(),
                    eodTransactionBean.getPosEntryMode(),//Response Code
                    eodTransactionBean.getRrn(),//EPEN Status
                    eodTransactionBean.getSequenceNumber(),//Settle Txn Status
                    eodTransactionBean.getSettlementDate(),//Base Currency
                    status,//ONUS ACQ Status
                    eodTransactionBean.getTid(),//Response Code
                    eodTransactionBean.getToAccNo(),//EPEN Status
                    eodTransactionBean.getTraceId(),//Settle Txn Status
                    eodTransactionBean.getTxnAmount(),//Base Currency
                    eodTransactionBean.getTxnDate(),//ONUS ACQ Status
                    eodTransactionBean.getTxnDescription(),//Response Code
                    eodTransactionBean.getTxnId(),//EPEN Status
                    eodTransactionBean.getTxnType(),
                    eodTransactionBean.getMcc(),//Settle Txn Status
                    eodTransactionBean.getBin(),
                    eodTransactionBean.getCardNo().toString(),
                    (eodTransactionBean.getAdjustmentFlag() != null) ? Integer.parseInt(eodTransactionBean.getAdjustmentFlag()) : 0,
                    "2",//acq transaction
                    eodTransactionBean.getFuelSurchargeAmount(), //fuel surcharge amount
                    eodTransactionBean.getCardAssociation(), //VISA OR MASTER
                    eodTransactionBean.getCardProduct()
            );
//

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateTransactionToEDON(String txnId, StringBuffer cardNo) throws Exception {
        int count = 0;

        try {
            String transactionStatus = "UPDATE TRANSACTION SET EODSTATUS=? WHERE TXNID=? ";

            count = backendJdbcTemplate.update(transactionStatus,

                    cardNo,
                    cardNo.toString()
            );

        } catch (Exception e) {
            throw e;

        }
        return count;
    }

    @Override
    public int updateEodProcessSummery(int eodId, String status, int processId, int successCount, int failedCount, String progress) throws Exception {

        int count = 0;
        String Query = "UPDATE EODPROCESSSUMMERY SET ENDTIME = SYSDATE , STATUS = ?,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ?,SUCCESSCOUNT = ?,FAILEDCOUNT = ?,PROCESSPROGRESS= ? WHERE EODID = ? AND PROCESSID = ?";
        try {
            count = backendJdbcTemplate.update(Query,
                    eodId,
                    status,
                    processId,
                    successCount,
                    failedCount,
                    progress
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNo) throws Exception {

        String cardNoOnAccount = null;
        try {
            String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";

            cardNoOnAccount = backendJdbcTemplate.query(query,
                    (ResultSet rs) ->
                    {
                        String accNo = null;
                        while (rs.next()) {
                            accNo = rs.getString("ACCOUNTNO");
                        }
                        return accNo;
                    },

                    cardNo.toString());

        } catch (Exception e) {
            throw e;
        }
        return cardNoOnAccount;
    }

}
