/**
 * Author : yasiru_l
 * Date : 6/30/2023
 * Time : 9:43 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.dao.OutgoingCUPFileDao;
import com.epic.cms.model.bean.OutgoingCUPFileTransactionBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class OutgoingCUPFileRepo implements OutgoingCUPFileDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusVarList;

    @Override
    public ArrayList<OutgoingCUPFileTransactionBean> getOutgoingStatementFileTransactionData() throws Exception {

        String query = null;
        ArrayList<OutgoingCUPFileTransactionBean> statementTransactionBeanList = new ArrayList<OutgoingCUPFileTransactionBean>();

        try{

            query = "SELECT DISTINCT T.TXNID,T.MTI,T.RESPONSEMTI,T.PROCESSINGCODE,T.OFFMTI,T.OFFRESPONSEMTI,T.OFFPROCESSINGCODE,T.NII,T.TXNTYPECODE,T.BIN,T.REQUESTFROM,T.LISTENERTYPE,T.ONOFFSTSTUS,T.SERVICECODE,T.CARDNO,T.EXPIRYDATE,T.TXNCURRENCY,(EMT.TRANSACTIONAMOUNT+EMT.FUELSURCHARGEAMOUNT) AS BACKENDTXNAMOUNT,T.TXNAMOUNT AS ONLINETXNAMOUNT,T.BILLINGCURRENCY,T.BILLINGAMOUNT,T.SETTLEMENTCURRENCY,T.SETTLEMENTAMOUNT,T.SETTLEMENTTXNCOUNT,T.SETTLEMENTDATE,T.BATCHNO,T.TID,T.MID,T.MCC,T.COUNTRYCODE,T.TRACENO,T.INVOICENO,T.RRN,T.AUTHCODE,T.RESPONSECODE,T.STATUS,T.POSCONDITIONCODE,T.POSENTRYMODE,T.AIIC,T.FIIC,T.TAG5F2A,T.TAG9A,T.TAG9C,T.TAG9F34,T.TAG9F02,T.TAG9F03,T.TAG9F1A,T.TAG9F1E,T.TAG9F27,T.TAG9F33,T.TAG9F35,T.TAG9F41,T.TXNTIME,T.TXNDATE,T.CARDSEQUENSENUMBER,T.FROMACCOUNT,T.TOACCOUNT,T.CHANNELTYPE,T.EODSTATUS,T.ACCEPTORNAME,T.BILLACCOUNTNO,T.BILLPROVIDERID,T.BILLREFNO,T.CAIC,T.F60TERMTYPE,T.F62TXNID,T.TXNSUBTYPE,T.CHQBANKNAME,T.CHQBRANCHNAME,T.CHQNO,T.CHQRETURNDATE,T.PAYMENTINITTYPE,T.PAYMENTMODE,T.VISATXNSTATUS,T.BACKENDTXNTYPE AS BACKENDTXNTYPE,T.AUTOSETTLEMENTSTATUS,T.ONLINECREATEDTIME,T.TXNTRANDATEANDTIME AS TRANSACTIONTIME ,T.REMARKS,T.CB_SEQ_NO,T.ECI,EMT.CURRENCYTYPE,EMT.CRDR,EMT.TRANSACTIONDATE,EMT.TRANSACTIONTYPE,EMT.TRANSACTIONID,EMT.TOACCOUNTNO,EMT.COUNTRYNUMCODE,EMT.ONOFFSTATUS,EMT.PAYMENTTYPE,EMT.SEQUENCENUMBER,EMT.TRACEID,ML.DESCRIPTION AS MERCHANTNAME,A.AREANAME  AS CITY, ML.CITY AS ZIPCODE, C.COUNTRYNUMCODE AS MERCHANTCOUNTRYCODE, ML.TELEPHONE,T.CVM, T.VISACVV2RESULT, T.VISAREQRESREASONCODE,T.EMV_9F33,T.EMV_95,T.EMV_82,T.EMV_9A,T.EMV_9C,T.EMV_5F2A,T.EMV_9F02,T.EMV_9F03,T.EMV_9F10,T.EMV_9F1A,T.EMV_9F26,T.EMV_9F36,T.EMV_9F37,T.EMV_9F6E,TER.TCAPABILITY,T.PURCHASE_ID,T.SECOND_PARTY_PAN,T.ORIGINALTXNID,T.MVV,T.F15_SETTLE_DATE,T.EMV_9F27,T.F60_DATA,T.UMPS_TID,T.UMPS_MID FROM EODMERCHANTTRANSACTION EMT INNER JOIN TRANSACTION T ON EMT.TRANSACTIONID    =T.TXNID LEFT JOIN MERCHANTLOCATION ML ON T.MID=ML.MERCHANTID LEFT JOIN AREA A ON ML.CITY=A.AREACODE LEFT JOIN COUNTRY C ON ML.COUNTRY=C.COUNTRYNUMCODE LEFT JOIN TERMINAL TER ON T.TID=TER.TERMINALID WHERE OUTGOINGFILESTATUS='0' AND EMT.ONOFFSTATUS!=? AND EMT.TRANSACTIONTYPE NOT IN(?) AND ((T.MID=TER.MERCHANTID AND TER.MERCHANTID IS NOT NULL) OR (T.TID IS NULL AND TER.MERCHANTID IS NULL)) AND EMT.CARDASSOCIATION=? AND TRUNC(TO_DATE(CONCAT(SUBSTR(T.CREATETIME,8,2), T.TXNDATE),'YYMMDD'))<=TRUNC(TO_DATE(?,'YYMMDD')) ORDER BY TXNTYPECODE";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {

                        while (rs.next()) {

                            OutgoingCUPFileTransactionBean txnBean = new OutgoingCUPFileTransactionBean();

                            txnBean.setTransactionId(rs.getString("TXNID"));
                            txnBean.setTransactionType(rs.getString("BACKENDTXNTYPE"));
//                            txnBean.setTransactionCode(rs.getString("TXNTYPECODE"));
                            txnBean.setPAN(new StringBuffer(rs.getString("CARDNO")));
                            txnBean.setAmount(rs.getString("BACKENDTXNAMOUNT"));
                            txnBean.setCurrencyCode(rs.getString("TXNCURRENCY"));
                            txnBean.setTransmissionDate(rs.getString("TRANSACTIONTIME"));
                            txnBean.setSystemTraceAuditNumber(rs.getString("TRACENO"));
                            txnBean.setAuthorizationIdentificationResponse(rs.getString("AUTHCODE"));
                            txnBean.setAuthorizationDate(rs.getString("F15_SETTLE_DATE"));
                            txnBean.setRetrievalRefNumber(rs.getString("RRN"));
                            txnBean.setAcquiringInstitutionIdentificationNumber(rs.getString("AIIC"));
                            txnBean.setForwardingInstitutionIdentificationCode(rs.getString("FIIC"));
                            txnBean.setCardAcceptorTerminalIdentification(rs.getString("TID"));
                            txnBean.setCardAcceptorIdentificationCode(rs.getString("MID"));
                            txnBean.setMerchantType(rs.getString("MCC"));
                            txnBean.setCardAcceptorNameOrLocation(rs.getString("CAIC"));
//
                            txnBean.setPosConditionCode(rs.getString("POSCONDITIONCODE"));
                            txnBean.setPosEntryMode(rs.getString("POSENTRYMODE"));
                            txnBean.setMerchantName(rs.getString("MERCHANTNAME"));
                            txnBean.setMerchantCity(rs.getString("CITY"));
                            txnBean.setMerchantCountryCode(rs.getString("MERCHANTCOUNTRYCODE"));
//
//                            // TODO: SHOULD BE VARIFIED ONLINE USE TAG OR EMV_TAG DATA ....
                            txnBean.setCurrencyCodeBlock2(rs.getString("EMV_5F2A"));
                            txnBean.setTransactionDate(rs.getString("EMV_9A"));
                            txnBean.setTransactionCategory(rs.getString("EMV_9C"));
//
                            txnBean.setAuthorizedAmount(rs.getString("EMV_9F02"));
                            txnBean.setOtherAmount(rs.getString("EMV_9F03"));
                            txnBean.setTerminalCountryCode(rs.getString("EMV_9F1A"));
//
                            txnBean.setCipherTextInformationData(rs.getString("EMV_9F27"));
                            txnBean.setTerminalCapabilities(rs.getString("EMV_9F33"));
                            txnBean.setUnpredictableNumber(rs.getString("EMV_9F37"));
//
                            txnBean.setF60_DATA(rs.getString("F60_DATA"));
                            txnBean.setAppliedCryptogram(rs.getString("EMV_9F26"));
                            txnBean.setTerminalVerificationResults(rs.getString("EMV_95"));
                            txnBean.setIssuingBankApplicationData(rs.getString("EMV_9F10"));
                            txnBean.setApplicationTransactionCounter(rs.getString("EMV_9F36"));
                            txnBean.setApplicationAlternationCharacteristic(rs.getString("EMV_82"));

                            txnBean.setOriginalTxnId(rs.getString("ORIGINALTXNID"));
                            txnBean.setUMPS_TID(rs.getString("UMPS_TID"));
                            txnBean.setUMPS_MID(rs.getString("UMPS_MID"));

                            statementTransactionBeanList.add(txnBean);
                        }
                        return statementTransactionBeanList;
                        },
                    statusVarList.getONUS_STATUS(),
                    Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT,
                    Configurations.CUP_ASSOCIATION,
                    Integer.toString(Configurations.EOD_ID).substring(0, 6)
            );

        }catch (Exception e){
            throw e;
        }

        return statementTransactionBeanList;

    }

    @Override
    public HashMap<String, String> getStatementFileBlockFields() throws Exception {
        HashMap<String, String> upiBlockTxnFields = new HashMap<>();
        String query = null;

        try{

            query = "SELECT TC,BLOCKNUMBER,FIELDS FROM EODCUPBLOCKTXNFIELD";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            upiBlockTxnFields.put(rs.getString("TC") + "|" + rs.getString("BLOCKNUMBER"), rs.getString("FIELDS"));
                        }
                    }
            );

        }catch (Exception e){
            throw e;
        }
        return upiBlockTxnFields;
    }

    @Override
    public HashMap<String, String> getOutgoingRejectReasonTable() throws Exception {

        HashMap<String, String> visaTxnFields = new HashMap<>();
        String query = null;
        try{

            query = "SELECT REASONID,DESCRIPTION FROM OUTGOINGCUPREJECTREASON ORDER BY REASONID ASC";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            visaTxnFields.put(rs.getString("REASONID"), rs.getString("DESCRIPTION"));
                        }
                        }
                    );
        } catch (Exception e){
            throw e;
        }
        return visaTxnFields;
    }

    @Override
    public OutgoingCUPFileTransactionBean getOriginalTxnInfoForReversalTxn(String originalTxnId) throws Exception {
        OutgoingCUPFileTransactionBean outgoingCUPFileTransactionBean = new OutgoingCUPFileTransactionBean();
        String query = null;

        try{

            query = "SELECT FIELD01,FIELD06, FIELD07,FIELD09,FIELD24 FROM OUTGOINGCUPFIELDIDENTITY WHERE TRANSACTIONID=? AND BLOCKNUMBER=0";

            outgoingCUPFileTransactionBean = Objects.requireNonNull(backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        OutgoingCUPFileTransactionBean returnBean = new OutgoingCUPFileTransactionBean();
                        while (rs.next()) {
                            returnBean.setTransactionCode(rs.getString("FIELD01"));
                            returnBean.setTransmissionDate(rs.getString("FIELD06"));
                            returnBean.setSystemTraceAuditNumber(rs.getString("FIELD07"));
                            returnBean.setAuthorizationDate(rs.getString("FIELD09"));
                            returnBean.setOriginalTxnInitChannel(rs.getString("FIELD24"));
                        }
                        return returnBean;
                        }
                    ));
        }catch (Exception e){
            throw e;
        }

        return outgoingCUPFileTransactionBean;
    }

    @Override
    public int insertOutgoingStatementFieldIdentity(String transactionId, int blockNumber, String tc, String[] blockFieldValues) throws Exception {

        int count = 0;

        try{
            StringBuilder queryBuffer = new StringBuilder("INSERT INTO OUTGOINGCUPFIELDIDENTITY (TRANSACTIONID,BLOCKNUMBER,FILEID,TC,FILESTATUS,");

            for (int i = 1; i <= blockFieldValues.length; i++) {
                if (i < 10) {
                    if (i == (blockFieldValues.length)) {
                        queryBuffer.append("FIELD0").append(i).append(") VALUES (");
                    } else {
                        queryBuffer.append("FIELD0").append(i).append(",");
                    }
                } else {
                    if (i == (blockFieldValues.length)) {
                        queryBuffer.append("FIELD").append(i).append(") VALUES (");
                    } else {
                        queryBuffer.append("FIELD").append(i).append(",");
                    }
                }
            }

            queryBuffer.append("?,?,?,?,?,");

            for (int i = 1; i <= blockFieldValues.length; i++) {
                if (i == (blockFieldValues.length)) {
                    queryBuffer.append("? )");
                } else {
                    queryBuffer.append("?,");
                }
            }

//            for (int i = 0; i < blockFieldValues.length; i++) {
//                stmt.setString((i + 6), blockFieldValues[i]);
//            }

            count = backendJdbcTemplate.update(String.valueOf(queryBuffer),transactionId,blockNumber,null,tc,0);

        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public int updateEodMerchantTransactionFileStatus(String txnId) throws Exception {
        int count = 0;
        String query = null;

        try{

            query = "UPDATE EODMERCHANTTRANSACTION  SET OUTGOINGFILESTATUS =1,LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE TRANSACTIONID=?";

            count = backendJdbcTemplate.update(query, Configurations.EOD_USER, txnId);

        }catch (Exception e){
            throw e;
        }

        return count;
    }

    @Override
    public int insertRejectOutgoingCUPTransaction(String eodId, String txnId, String rejectException) throws Exception {
        int count = 0;
        String query = null;

        try {

            query = "INSERT INTO OUTGOINGCUPTXNREJECT(EODID,TRANSACTIONID,REJECTREASON) VALUES(?,?,?)";

            count = backendJdbcTemplate.update(query, eodId, txnId, rejectException);


        }catch (Exception e){
            throw e;
        }

        return count;
    }

    @Override
    public Set<String> getPendingOutgoingUPIStatementTxnIDList() throws Exception {
        Set<String> txnIdList = new LinkedHashSet<>();
        String query = null;

        try{

            query = "SELECT TRANSACTIONID FROM OUTGOINGCUPFIELDIDENTITY WHERE FILESTATUS=0 ORDER BY TC";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            txnIdList.add(rs.getString("TRANSACTIONID"));
                        }
                        }
                    );
        }catch (Exception e){
            throw e;
        }
        return txnIdList;
    }

    @Override
    public StringBuffer getUPIStatementFileTxnFieldValues(String transactionId) throws Exception {

        StringBuffer txnFieldValueBuffer = new StringBuffer("");
        String query = null;

        try{

            query = "SELECT FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,FIELD06,FIELD07,FIELD08,FIELD09,"
                    + "FIELD10,FIELD11,FIELD12,FIELD13,FIELD14,FIELD15,FIELD16,FIELD17,FIELD18,FIELD19,FIELD20,"
                    + "FIELD21,FIELD22,FIELD23,FIELD24,FIELD25,FIELD26,FIELD27,FIELD28,FIELD29,FIELD30,FIELD31,FIELD32,FIELD33,"
                    + "FIELD34,FIELD35,FIELD36,FIELD37,FIELD38,FIELD39,FIELD40,FIELD41,FIELD42,FIELD43,FIELD44 FROM OUTGOINGCUPFIELDIDENTITY WHERE TRANSACTIONID=? ORDER BY  BLOCKNUMBER ASC";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            if (rs.getString("FIELD01") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD01"));
                            }
                            if (rs.getString("FIELD02") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD02"));
                            }
                            if (rs.getString("FIELD03") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD03"));
                            }
                            if (rs.getString("FIELD04") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD04"));
                            }
                            if (rs.getString("FIELD05") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD05"));
                            }
                            if (rs.getString("FIELD06") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD06"));
                            }
                            if (rs.getString("FIELD07") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD07"));
                            }
                            if (rs.getString("FIELD08") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD08"));
                            }
                            if (rs.getString("FIELD09") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD09"));
                            }
                            if (rs.getString("FIELD10") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD10"));
                            }
                            if (rs.getString("FIELD11") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD11"));
                            }
                            if (rs.getString("FIELD12") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD12"));
                            }
                            if (rs.getString("FIELD13") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD13"));
                            }
                            if (rs.getString("FIELD14") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD14"));
                            }
                            if (rs.getString("FIELD15") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD15"));
                            }
                            if (rs.getString("FIELD16") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD16"));
                            }
                            if (rs.getString("FIELD17") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD17"));
                            }
                            if (rs.getString("FIELD18") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD18"));
                            }
                            if (rs.getString("FIELD19") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD19"));
                            }
                            if (rs.getString("FIELD20") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD20"));
                            }
                            if (rs.getString("FIELD21") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD21"));
                            }
                            if (rs.getString("FIELD22") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD22"));
                            }
                            if (rs.getString("FIELD23") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD23"));
                            }
                            if (rs.getString("FIELD24") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD24"));
                            }
                            if (rs.getString("FIELD25") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD25"));
                            }
                            if (rs.getString("FIELD26") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD26"));
                            }
                            if (rs.getString("FIELD27") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD27"));
                            }
                            if (rs.getString("FIELD28") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD28"));
                            }
                            if (rs.getString("FIELD29") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD29"));
                            }
                            if (rs.getString("FIELD30") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD30"));
                            }
                            if (rs.getString("FIELD31") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD31"));
                            }
                            if (rs.getString("FIELD32") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD32"));
                            }
                            if (rs.getString("FIELD33") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD33"));
                            }
                            if (rs.getString("FIELD34") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD34"));
                            }
                            if (rs.getString("FIELD35") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD35"));
                            }
                            if (rs.getString("FIELD36") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD36"));
                            }
                            if (rs.getString("FIELD37") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD37"));
                            }
                            if (rs.getString("FIELD38") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD38"));
                            }
                            if (rs.getString("FIELD39") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD39"));
                            }
                            if (rs.getString("FIELD40") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD40"));
                            }
                            if (rs.getString("FIELD41") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD41"));
                            }
                            if (rs.getString("FIELD42") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD42"));
                            }
                            if (rs.getString("FIELD43") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD43"));
                            }
                            if (rs.getString("FIELD44") != null) {
                                txnFieldValueBuffer.append(rs.getString("FIELD44"));
                            }
                        }
                        },
                    transactionId
                    );

        }catch (Exception e){
            throw e;
        }
        return txnFieldValueBuffer;
    }

    @Override
    public int updateOutgoingUpiStatementFieldIdentityFileStatus(String txnId, String fileName) throws Exception {
        int count = 0;
        String transactionStatus = null;

        try{

            transactionStatus = "UPDATE OUTGOINGCUPFIELDIDENTITY SET FILESTATUS=? , FILEID=?  WHERE TRANSACTIONID=?";

            count = backendJdbcTemplate.update(transactionStatus, 1, fileName, txnId);

        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public int insertOutgoingStatementFilePathToDownloadFile(String outgoinStatementFileName) throws Exception {
        String query = null;
        int count = 0;

        try{

            query = "Insert into DOWNLOADFILE (FIETYPE,FILENAME,STATUS,GENERATEDUSER,LASTUPDATEDTIME,CREATEDTIME,LASTUPDATEDUSER,FILEID) values ('OUTGOINGCUP',?,'NO',?,SYSDATE,SYSDATE,?,NULL)";

            count = backendJdbcTemplate.update(query,
                    outgoinStatementFileName,
                    Configurations.EOD_USER,
                    Configurations.EOD_USER);

        }catch (Exception e){
            throw e;
        }

        return count;
    }
}
