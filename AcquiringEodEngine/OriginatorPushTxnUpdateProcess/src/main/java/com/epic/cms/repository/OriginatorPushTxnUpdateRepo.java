/**
 * Author : rasintha_j
 * Date : 6/20/2023
 * Time : 10:36 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.dao.OriginatorPushTxnUpdateDao;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Repository
public class OriginatorPushTxnUpdateRepo implements OriginatorPushTxnUpdateDao {
    @Autowired
    QueryParametersList queryParametersList;
    @Autowired
    StatusVarList status;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList query;

    @Override
    public ArrayList<EodTransactionBean> getAllOriginatorPushTxn() throws Exception {
        ArrayList<EodTransactionBean> txnList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String query = "SELECT TXNID,NVL(ONOFFSTSTUS,1) AS ONOFFSTSTUS,CARDNO,TXNCURRENCY,CB_SEQ_NO, NVL((TXNAMOUNT)/100,0) AS TRANSACTIONAMOUNT, NVL(NVL((BILLINGAMOUNT),0)/100,0) AS BILLINGAMOUNT,BATCHNO,TID,MID,MCC,COUNTRYCODE,TRACENO,RRN,AUTHCODE,POSENTRYMODE,CAIC,BACKENDTXNTYPE,TOACCOUNT,CREATETIME,REQUESTFROM,SECOND_PARTY_PAN,SETTLEMENTDATE,CHANNELTYPE FROM TRANSACTION WHERE ACQORISS = ?  AND RESPONSECODE = ? AND EODSTATUS = ? AND STATUS IN (?) AND TRUNC(CREATETIME) <= TRUNC(TO_DATE(?)) AND BACKENDTXNTYPE=?";

            txnList = (ArrayList<EodTransactionBean>) backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        int onOffStatus = result.getInt("ONOFFSTSTUS");
                        StringBuffer cardNumber = new StringBuffer(result.getString("CARDNO"));

                        EodTransactionBean eodTransactionBean = new EodTransactionBean();

                        //Set values to eodTransactionBean
                        eodTransactionBean.setAccountNo(this.getAccountNoOnCard(cardNumber));
                        eodTransactionBean.setAuthCode(result.getString("AUTHCODE"));
                        eodTransactionBean.setBatchNo(result.getString("BATCHNO"));
                        eodTransactionBean.setCardNo(cardNumber);
                        eodTransactionBean.setCountryNumCode(result.getString("COUNTRYCODE"));
                        eodTransactionBean.setCurrencyType(result.getString("TXNCURRENCY"));
                        eodTransactionBean.setMid(result.getString("MID"));
                        eodTransactionBean.setOnOffStatus(onOffStatus);
                        eodTransactionBean.setPosEntryMode(result.getString("POSENTRYMODE"));
                        eodTransactionBean.setRrn(result.getString("RRN"));
                        eodTransactionBean.setSequenceNumber(result.getString("CB_SEQ_NO"));
                        eodTransactionBean.setSettlementDate(result.getDate("CREATETIME"));
                        eodTransactionBean.setTid(result.getString("TID"));
                        eodTransactionBean.setToAccNo(result.getString("TOACCOUNT"));
                        eodTransactionBean.setTraceId(result.getString("TRACENO"));
                        eodTransactionBean.setTxnAmount(result.getString("TRANSACTIONAMOUNT"));
                        eodTransactionBean.setTxnDate(result.getDate("CREATETIME"));
                        eodTransactionBean.setTxnDescription(result.getString("CAIC"));
                        eodTransactionBean.setTxnId(result.getString("TXNID"));
                        eodTransactionBean.setTxnType(result.getString("BACKENDTXNTYPE"));
                        eodTransactionBean.setMcc(result.getString("MCC"));
                        eodTransactionBean.setBillingAmount(result.getString("BILLINGAMOUNT"));
                        eodTransactionBean.setRequestFrom(result.getString("REQUESTFROM"));
                        eodTransactionBean.setSecondPartyPan(result.getString("SECOND_PARTY_PAN"));
                        eodTransactionBean.setChannelType(result.getInt("CHANNELTYPE"));
                        return eodTransactionBean;
                    })
                    , Configurations.EOD_ISSUING_STATUS,
                    status.getRESPONSE_CODE_00(),
                    Configurations.EOD_PENDING_STATUS,
                    status.getTXN_COMPLETE_STATUS(),
                    sdf.format(Configurations.EOD_DATE),
                    Configurations.TXN_TYPE_MVISA_ORIGINATOR
            );
        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }

    @Override
    public String getForexPercentage() throws Exception {
        String forexRate = "0";

        try {
            String query = "SELECT NVL(VISATXNTOLLERANCERATE,0) AS VISATXNTOLLERANCERATE FROM COMMONPARAMETER";

            forexRate = backendJdbcTemplate.queryForObject(query, String.class);
        } catch (EmptyResultDataAccessException ex) {
            return forexRate;
        } catch (Exception e) {
            throw e;
        }
        return forexRate;
    }

    @Override
    public HashMap<String, String> getFinancialStatus() throws Exception {
        HashMap<String, String> visaTxnFields = new HashMap<>();
        HashMap<String, String> result = new HashMap<>();
        try {
            result = backendJdbcTemplate.query(query.getAcqTxnUpdate_getFinancialStatus(),
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            visaTxnFields.put(rs.getString("TRANSACTIONCODE"), rs.getString("FINANCIALSTATUS"));
                        }
                        return visaTxnFields;
                    });

        }catch (Exception e){
            throw e;
        }
        return result;
    }

    @Override
    public int insertToEODTransaction(StringBuffer cardnumber, String accountNo, String mId, String tId, String txnAmount, int currencyType, String crDr, Date settlementDate, Date txnDate, String txnType, String batchNo, String txnId, String toAccNo, Double loyaltyPoint, String Description, String countryCode, int onOffStatus, String poStringsEntryMode, String traceId, String authCode, int adjustmentFlag, String requestFrom, String secondPartyPan, String fualSurchargeAmount, String mcc, String cardAssociation) throws Exception {
        String query = null;
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        String settlementDateString = sdf.format(settlementDate);
        String txnDateString = sdf.format(txnDate);

        try {
            query = "INSERT INTO EODTRANSACTION (EODID,CARDNUMBER,ACCOUNTNO,MID,TID,TRANSACTIONAMOUNT,CURRENCYTYPE,CRDR,SETTLEMENTDATE,TRANSACTIONDATE,TRANSACTIONTYPE,BATCHNO,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,LASTUPDATEDTIME,TOACCOUNTNO,STATUS,TRANSACTIONDESCRIPTION,COUNTRYNUMCODE,ONOFFSTATUS,POSENTRYMODE,TRACEID,AUTHCODE,ACQADJUSTMENTFLAG,REQUESTFROM,SECOND_PARTY_PAN,FUELSURCHARGEAMOUNT,MCC,ORIGINALTRANSACTIONTYPE,CARDASSOCIATION) VALUES (?,?,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),TO_DATE(?,'DD-MM-YY'),?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            count = backendJdbcTemplate.update(query, Configurations.EOD_ID, cardnumber, accountNo, mId, tId, txnAmount, currencyType, crDr, settlementDateString, txnDateString, txnType, batchNo, txnId, Configurations.EOD_USER, toAccNo, status.getINITIAL_STATUS(), Description, countryCode, onOffStatus, poStringsEntryMode, onOffStatus, authCode, adjustmentFlag, requestFrom, secondPartyPan, fualSurchargeAmount, mcc, txnType, cardAssociation);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }


    @Override
    public int updateTransactionToEDON(String txnId, StringBuffer cardNo) throws Exception {
        int count = 0;

        try {
            String query = "UPDATE TRANSACTION SET EODSTATUS=? WHERE TXNID=? ";
            count = backendJdbcTemplate.update(query, status.getEOD_DONE_STATUS(), txnId);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNo){
        String accNo = null;
        try {
            String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";

            accNo = backendJdbcTemplate.queryForObject(query, String.class, cardNo);

        } catch (EmptyResultDataAccessException ex) {
            return accNo;
        } catch (Exception e) {
            throw e;
        }
        return accNo;
    }
}
