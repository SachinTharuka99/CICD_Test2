/**
 * Author : lahiru_p
 * Date : 7/11/2023
 * Time : 12:35 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.repository;

import com.epic.cms.dao.OutgoingIPMFileGenDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class OutgoingIPMFileGenRepo implements OutgoingIPMFileGenDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    JdbcTemplate backendJdbcTemplate;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public List<TransactionDataBean> getOutgoingIPMTransactionData() {
        List<TransactionDataBean> transactionBeanList = new ArrayList<>();
        try {
            String query = "SELECT T.TXNID,T.MTI,T.RESPONSEMTI,T.PROCESSINGCODE,T.OFFMTI,T.OFFRESPONSEMTI,T.OFFPROCESSINGCODE,T.NII,"
                    + "T.TXNTYPECODE,T.BIN,T.REQUESTFROM,T.LISTENERTYPE,T.ONOFFSTSTUS,T.SERVICECODE,T.CARDNO,T.EXPIRYDATE,T.TXNCURRENCY,"
                    + "(EMT.TRANSACTIONAMOUNT+EMT.FUELSURCHARGEAMOUNT) AS BACKENDTXNAMOUNT,T.TXNAMOUNT AS ONLINETXNAMOUNT,T.BILLINGCURRENCY,T.BILLINGAMOUNT,T.SETTLEMENTCURRENCY,T.SETTLEMENTAMOUNT,T.SETTLEMENTTXNCOUNT,"
                    + "T.SETTLEMENTDATE,T.BATCHNO,T.TID,T.MID,T.MCC,T.COUNTRYCODE,T.TRACENO,T.INVOICENO,T.RRN,T.AUTHCODE,T.RESPONSECODE,"
                    + "T.STATUS,T.POSCONDITIONCODE,T.POSENTRYMODE,T.AIIC,T.FIIC,T.TAG5F2A,T.TAG9A,T.TAG9C,T.TAG9F34,T.TAG9F02,T.TAG9F03,"
                    + "T.TAG9F1A,T.TAG9F1E,T.TAG9F27,T.TAG9F33,T.TAG9F35,T.TAG9F41,T.TXNTIME,T.TXNDATE,T.CARDSEQUENSENUMBER,T.FROMACCOUNT,"
                    + "T.TOACCOUNT,T.CHANNELTYPE,T.EODSTATUS,T.ACCEPTORNAME,T.BILLACCOUNTNO,T.BILLPROVIDERID,T.BILLREFNO,T.CAIC,T.F60TERMTYPE,"
                    + "T.F62TXNID,T.TXNSUBTYPE,T.CHQBANKNAME,T.CHQBRANCHNAME,T.CHQNO,T.CHQRETURNDATE,T.PAYMENTINITTYPE,T.PAYMENTMODE,"
                    + "T.VISATXNSTATUS,T.BACKENDTXNTYPE,T.AUTOSETTLEMENTSTATUS,T.ONLINECREATEDTIME,T.REMARKS,T.CB_SEQ_NO,T.ECI,EMT.CURRENCYTYPE,"
                    + "EMT.CRDR,EMT.TRANSACTIONDATE,EMT.TRANSACTIONTYPE,EMT.TRANSACTIONID,EMT.TOACCOUNTNO,EMT.COUNTRYNUMCODE,EMT.ONOFFSTATUS,"
                    + "EMT.PAYMENTTYPE,EMT.SEQUENCENUMBER,EMT.TRACEID,ML.DESCRIPTION AS MERCHANTNAME,A.AREANAME  AS CITY, ML.CITY AS ZIPCODE, " // change description to AREANAME
                    + "C.COUNTRYALPHA3CODE AS MERCHANTCOUNTRYCODE, ML.TELEPHONE,T.CVM, T.VISACVV2RESULT, T.VISAREQRESREASONCODE,T.EMV_9F33,T.EMV_95,"
                    + "T.EMV_82,T.EMV_9A,T.EMV_9C,T.EMV_5F2A,T.EMV_9F02,T.EMV_9F03,T.EMV_9F10,T.EMV_9F1A,T.EMV_9F26,T.EMV_9F36,T.EMV_9F37,T.EMV_9F6E,T.EMV_9F27,"
                    + "TER.TCAPABILITY,T.PURCHASE_ID,T.SECOND_PARTY_PAN,T.ORIGINALTXNID,T.MVV,"
                    + "T.MC_F63_DATA,T.F15_SETTLE_DATE,T.EMV_9F34,T.EMV_84 FROM EODMERCHANTTRANSACTION EMT INNER JOIN TRANSACTION T "
                    + "ON EMT.TRANSACTIONID    =T.TXNID LEFT JOIN MERCHANTLOCATION ML ON T.MID=ML.MERCHANTID LEFT JOIN AREA A ON "
                    + "ML.CITY=A.AREACODE LEFT JOIN COUNTRY C ON ML.COUNTRY=C.COUNTRYNUMCODE LEFT JOIN TERMINAL TER ON T.TID=TER.TERMINALID WHERE OUTGOINGFILESTATUS='0' AND EMT.ONOFFSTATUS!=?"
                    + " AND EMT.TRANSACTIONTYPE NOT IN(?,?,?) AND ((T.MID=TER.MERCHANTID AND TER.MERCHANTID IS NOT NULL) OR (T.TID IS NULL AND TER.MERCHANTID IS NULL)) AND T.CHANNELTYPE=?"
                    + " AND TRUNC(TO_DATE(CONCAT(SUBSTR(T.CREATETIME,8,2), T.TXNDATE),'YYMMDD'))<=TRUNC(TO_DATE(?,'YYMMDD'))";

            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                        while (rs.next()) {
                            TransactionDataBean tempTranBean = new TransactionDataBean();
                            tempTranBean.setMerchantName(rs.getString("MERCHANTNAME"));
                            tempTranBean.setMerchantCity(rs.getString("CITY"));
                            tempTranBean.setMerchantCountryCode(rs.getString("MERCHANTCOUNTRYCODE"));
                            tempTranBean.setMerchantZipCode(rs.getString("ZIPCODE"));
                            tempTranBean.setMerchantStateCode("");
                            tempTranBean.setMerchantTelephoneNumber(rs.getString("TELEPHONE"));

                            tempTranBean.setTxnId(rs.getString("TXNID"));
                            tempTranBean.setMti(rs.getString("MTI"));
                            tempTranBean.setResponseMti(rs.getString("RESPONSEMTI"));
                            tempTranBean.setProcessingCode(rs.getString("PROCESSINGCODE"));
                            tempTranBean.setOffMti(rs.getString("OFFMTI"));
                            tempTranBean.setOffResponseMti(rs.getString("OFFRESPONSEMTI"));
                            tempTranBean.setOffProcessingCode(rs.getString("OFFPROCESSINGCODE"));
                            tempTranBean.setNii(rs.getString("NII"));
                            tempTranBean.setTxnTypeCode(rs.getString("TXNTYPECODE"));
                            tempTranBean.setBin(rs.getString("BIN"));
                            tempTranBean.setRequestFrom(rs.getString("REQUESTFROM"));
                            tempTranBean.setListenerType(rs.getString("LISTENERTYPE"));
                            tempTranBean.setOnOffStatus(rs.getString("ONOFFSTSTUS"));
                            tempTranBean.setServiceCode(rs.getString("SERVICECODE"));
                            tempTranBean.setCardno(new StringBuffer(rs.getString("CARDNO")));
                            tempTranBean.setExpiryDate(rs.getString("EXPIRYDATE"));
                            tempTranBean.setTxnCurrency(rs.getString("TXNCURRENCY"));
                            tempTranBean.setTxnAmount(rs.getString("BACKENDTXNAMOUNT")); //Transaction amount get from EODMERCHANTTRANSACTION table since fuel surcharge post to that table only
                            tempTranBean.setBillingCurrency(rs.getString("BILLINGCURRENCY"));
                            tempTranBean.setBillingAmount(rs.getString("BILLINGAMOUNT"));
                            tempTranBean.setSettlementCurrency(rs.getString("SETTLEMENTCURRENCY"));
                            tempTranBean.setSettlementAmount(rs.getString("SETTLEMENTAMOUNT"));
                            tempTranBean.setSettlementTxnCount(rs.getString("SETTLEMENTTXNCOUNT"));
                            tempTranBean.setSettlementDate(rs.getString("SETTLEMENTDATE"));
                            tempTranBean.setBatchNo(rs.getString("BATCHNO"));
                            tempTranBean.setTid(rs.getString("TID"));
                            tempTranBean.setMid(rs.getString("MID"));
                            tempTranBean.setMcc(rs.getString("MCC"));
                            tempTranBean.setCountryCode(rs.getString("COUNTRYCODE"));
                            tempTranBean.setTraceNo(rs.getString("TRACENO"));
                            tempTranBean.setInvoiceNo(rs.getString("INVOICENO"));
                            tempTranBean.setRrn(rs.getString("RRN"));
                            tempTranBean.setAuthCode(rs.getString("AUTHCODE"));
                            tempTranBean.setResponseCode(rs.getString("RESPONSECODE"));
                            tempTranBean.setStatus(rs.getString("STATUS"));
                            tempTranBean.setPosConditionCode(rs.getString("POSCONDITIONCODE"));
                            tempTranBean.setPosEntryMode(rs.getString("POSENTRYMODE"));
                            tempTranBean.setAiic(rs.getString("AIIC"));
                            tempTranBean.setFiic(rs.getString("FIIC"));
                            tempTranBean.setTag5f2a(rs.getString("TAG5F2A"));
                            tempTranBean.setTag9a(rs.getString("TAG9A"));
                            tempTranBean.setTag9c(rs.getString("TAG9C"));
                            tempTranBean.setTag9f34(rs.getString("TAG9F34"));
                            tempTranBean.setTag9f02(rs.getString("TAG9F02"));
                            tempTranBean.setTag9f03(rs.getString("TAG9F03"));
                            tempTranBean.setTag9f1a(rs.getString("TAG9F1A"));
                            tempTranBean.setTag9f1e(rs.getString("TAG9F1E"));
                            tempTranBean.setTag9f27(rs.getString("TAG9F27"));
                            tempTranBean.setTag9f33(rs.getString("TAG9F33"));
                            tempTranBean.setTag9f35(rs.getString("TAG9F35"));
                            tempTranBean.setTag9f41(rs.getString("TAG9F41"));
                            tempTranBean.setTxnTime(rs.getString("TXNTIME"));
                            tempTranBean.setTxnDate(rs.getString("TXNDATE"));
                            tempTranBean.setCardSequenceNumber(rs.getString("CARDSEQUENSENUMBER"));
                            tempTranBean.setFromAccount(rs.getString("FROMACCOUNT"));
                            tempTranBean.setToAccount(rs.getString("TOACCOUNT"));
                            tempTranBean.setChannelType(rs.getString("CHANNELTYPE"));
                            tempTranBean.setEodStatus(rs.getString("EODSTATUS"));
                            tempTranBean.setAcceptorName(rs.getString("ACCEPTORNAME"));
                            tempTranBean.setBillAccountNo(rs.getString("BILLACCOUNTNO"));
                            tempTranBean.setBillProviderId(rs.getString("BILLPROVIDERID"));
                            tempTranBean.setBillRefNo(rs.getString("BILLREFNO"));
                            tempTranBean.setCaic(rs.getString("CAIC"));
                            tempTranBean.setF60termtype(rs.getString("F60TERMTYPE"));
                            tempTranBean.setF62txnid(rs.getString("F62TXNID"));
                            tempTranBean.setTxnSubType(rs.getString("TXNSUBTYPE"));
                            tempTranBean.setChqBankName(rs.getString("CHQBANKNAME"));
                            tempTranBean.setChqBranchName(rs.getString("CHQBRANCHNAME"));
                            tempTranBean.setChqNo(rs.getString("CHQNO"));
                            tempTranBean.setChqReturnDate(rs.getString("CHQRETURNDATE"));
                            tempTranBean.setPaymentInitType(rs.getString("PAYMENTINITTYPE"));
                            tempTranBean.setPaymentMode(rs.getString("PAYMENTMODE"));
                            tempTranBean.setVisaTxnStatus(rs.getString("VISATXNSTATUS"));
                            tempTranBean.setBackendTxnType(rs.getString("BACKENDTXNTYPE"));
                            tempTranBean.setAutoSettlementStatus(rs.getString("AUTOSETTLEMENTSTATUS"));
                            tempTranBean.setOnlineCreatedTime(rs.getString("ONLINECREATEDTIME"));
                            tempTranBean.setRemarks(rs.getString("REMARKS"));
                            tempTranBean.setCbSeqNo(rs.getString("CB_SEQ_NO"));
                            tempTranBean.setEci(rs.getString("ECI"));
                            tempTranBean.setCurrencyType(rs.getString("CURRENCYTYPE"));
                            tempTranBean.setCrdr(rs.getString("CRDR"));
                            tempTranBean.setTransactionDate(rs.getString("TRANSACTIONDATE"));
                            tempTranBean.setTransactionType(rs.getString("TRANSACTIONTYPE"));
                            tempTranBean.setTransactionId(rs.getString("TRANSACTIONID"));
                            tempTranBean.setToAccountNo(rs.getString("TOACCOUNTNO"));
                            tempTranBean.setCountryNumCode(rs.getString("COUNTRYNUMCODE"));
                            tempTranBean.setPaymentType(rs.getString("PAYMENTTYPE"));
                            tempTranBean.setSequenceNumber(rs.getString("SEQUENCENUMBER"));
                            tempTranBean.setTraceId(rs.getString("TRACEID"));

                            //newly added fields from online side
                            tempTranBean.setCvm(rs.getString("CVM"));
                            tempTranBean.setVisaCvv2Result(rs.getString("VISACVV2RESULT"));
                            tempTranBean.setVisaReqResReasonCode(rs.getString("VISAREQRESREASONCODE"));

                            tempTranBean.setTransactionIdentifier(rs.getString("F62TXNID"));
                            tempTranBean.setAuthorizedAmount(rs.getString("ONLINETXNAMOUNT"));
                            tempTranBean.setAuthorizationCurrencyCode(rs.getString("TXNCURRENCY"));
                            tempTranBean.setAuthorizationResponseCode("  ");
                            tempTranBean.setTotalAuthorizedAmount(rs.getString("ONLINETXNAMOUNT"));

                            //chip data
                            tempTranBean.setEMV_9F33(rs.getString("EMV_9F33"));
                            tempTranBean.setEMV_95(rs.getString("EMV_95"));
                            tempTranBean.setEMV_82(rs.getString("EMV_82"));
                            tempTranBean.setEMV_9A(rs.getString("EMV_9A"));
                            tempTranBean.setEMV_9C(rs.getString("EMV_9C"));
                            tempTranBean.setEMV_5F2A(rs.getString("EMV_5F2A"));
                            tempTranBean.setEMV_9F02(rs.getString("EMV_9F02"));
                            tempTranBean.setEMV_9F03(rs.getString("EMV_9F03"));
                            tempTranBean.setEMV_9F10(rs.getString("EMV_9F10"));
                            tempTranBean.setEMV_9F1A(rs.getString("EMV_9F1A"));
                            tempTranBean.setEMV_9F26(rs.getString("EMV_9F26"));
                            tempTranBean.setEMV_9F36(rs.getString("EMV_9F36"));
                            tempTranBean.setEMV_9F37(rs.getString("EMV_9F37"));
                            tempTranBean.setEMV_9F6E(rs.getString("EMV_9F6E"));
                            tempTranBean.setEMV_9F27(rs.getString("EMV_9F27"));
                            tempTranBean.setEMV_9F34(rs.getString("EMV_9F34"));
                            tempTranBean.setEMV_84(rs.getString("EMV_84"));

                            tempTranBean.setTerminalCapability(rs.getString("TCAPABILITY"));
                            tempTranBean.setPurchaseIdentifier(rs.getString("PURCHASE_ID"));
                            tempTranBean.setSECOND_PARTY_PAN(rs.getString("SECOND_PARTY_PAN"));

                            tempTranBean.setOriginalTxnId(rs.getString("ORIGINALTXNID"));
                            tempTranBean.setMvv(rs.getString("MVV"));
                            tempTranBean.setMC_F63_DATA(rs.getString("MC_F63_DATA"));
                            tempTranBean.setF15_SETTLE_DATE(rs.getString("F15_SETTLE_DATE"));

                            transactionBeanList.add(tempTranBean);
                        }
                        return transactionBeanList;
                    }
                    , statusList.getONUS_STATUS()
                    , Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT
                    , Configurations.TXN_TYPE_CUP_QR_PAYMENT
                    , Configurations.TXN_TYPE_CUP_QR_REFUND
                    , Configurations.CHANNEL_TYPE_MASTER
                    , Integer.toString(Configurations.EOD_ID).substring(0, 6));

        } catch (Exception e) {
            throw e;
        }
        return transactionBeanList;
    }

    @Override
    public HashMap<String, String> getMasterOutgoingRejectReasonTable() {
        HashMap<String, String> visaTxnFields = new HashMap<>();
        try {
            String query = "SELECT REASONID,DESCRIPTION FROM EODMASTEROUTGOINGREJECTREASON ORDER BY REASONID ASC";
            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                while (rs.next()) {
                    visaTxnFields.put(rs.getString("REASONID"), rs.getString("DESCRIPTION"));
                }
                return visaTxnFields;
            });

        } catch (Exception e) {
            throw e;
        }
        return visaTxnFields;
    }

    @Override
    public HashMap<String, String> getCurrencyExponentTable() {
        HashMap<String, String> exponentList = new HashMap<>();
        try {
            String query = "SELECT CURRENCYNUMCODE,EXPONENT FROM CURRENCY ORDER BY CURRENCYNUMCODE ASC";
            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                while (rs.next()) {
                    exponentList.put(rs.getString("CURRENCYNUMCODE"), rs.getString("EXPONENT"));
                }
                return exponentList;
            });

        } catch (Exception e) {
            throw e;
        }
        return exponentList;
    }

    @Override
    public MPGSAdditionalDataBean getMPGSAdditionalData(String txnId) {
        MPGSAdditionalDataBean mpgsAdditionalBean = new MPGSAdditionalDataBean();
        try {
            String query = "SELECT * FROM EODFULLDCFFIELDIDENTITY WHERE TXNID=?";
            mpgsAdditionalBean = backendJdbcTemplate.query(query, (ResultSet rs) -> {
                String recordType = null;
                MPGSAdditionalDataBean mpgsAdditionalDataBean = new MPGSAdditionalDataBean();
                while (rs.next()) {
                    recordType = rs.getString("RECORDTYPE");
                    switch (recordType) {
                        case "6220": //Transaction Detail Record 1
                            mpgsAdditionalDataBean.setECI(rs.getString("FIELD22"));
                            mpgsAdditionalDataBean.setVisa3DSecureIndicator(rs.getString("FIELD25")); //U - VISA E-Commerce 3DS Transaction, W - VISA E-Commerce non 3DS transaction , Space - For all other
                            mpgsAdditionalDataBean.setPOSCardHolderPresenceIndicator(rs.getString("FIELD31"));
                            mpgsAdditionalDataBean.setCardHolderActivatedTerminalLevelInd(rs.getString("FIELD33"));
                            mpgsAdditionalDataBean.setPointOfServiceDataCode(rs.getString("FIELD38"));
                            break;
                        case "6221": //Transaction Detail Record 2
                            mpgsAdditionalDataBean.setWalletProgramData(rs.getString("FIELD13"));
                            mpgsAdditionalDataBean.setDirectoryServerTxnId(rs.getString("FIELD14"));
                            mpgsAdditionalDataBean.setProgramProtocol(rs.getString("FIELD15"));
                            mpgsAdditionalDataBean.setTransactionTypeIndicator(rs.getString("FIELD22"));
                            break;
                        case "6223": //VISA Specific Data
                            mpgsAdditionalDataBean.setVisaTransactionIdentification(rs.getString("FIELD08"));
                            //mpgsAdditionalDataBean.setVisa3DSecureIndicator(rs.getString("FIELD25")); //U - VISA E-Commerce 3DS Transaction, W - VISA E-Commerce non 3DS transaction , Space - For all other
                            break;
                        case "6225": //EMV Record Data
                            mpgsAdditionalDataBean.setPanSequenceNumber(rs.getString("FIELD02"));

                            break;
                        case "6222": //Mastercard Specific Data
                            mpgsAdditionalDataBean.setBanknetNetworkCode(rs.getString("FIELD02"));
                            mpgsAdditionalDataBean.setBanknetReferenceNumber(rs.getString("FIELD03"));
                            mpgsAdditionalDataBean.setBanknetDate(rs.getString("FIELD04"));
                            mpgsAdditionalDataBean.setElectronicAcceptanceIndicator(rs.getString("FIELD05"));
                            mpgsAdditionalDataBean.setPromotionCode(rs.getString("FIELD08"));
                            break;
                        case "6290": //Mastercard Payment Gateway Services Record

                            break;
                        default:
                            break;
                    }
                }
                return mpgsAdditionalDataBean;
            }, txnId);
        } catch (Exception e) {
            throw e;
        }
        return mpgsAdditionalBean;
    }

    @Override
    public String getGCMSProductIDFromCardNumber(String cardNumber) {
        String GCMSProductID = "";
        try {
            String query = "SELECT GCMSPRODUCTID FROM EODMASTERIP0040T1DATA WHERE LOWACCOUNTRANGE<=? AND HIGHACCOUNTRANGE>=? AND GCMSPRODUCTID<>?";
            GCMSProductID = backendJdbcTemplate.queryForObject(query, String.class, cardNumber, cardNumber, "CIR");

        } catch (Exception e) {
            //throw e;
        }
        return GCMSProductID;
    }

    @Override
    public IP0040T1Bean getIssuerAccountRangeDetails(String cardNumber, String cardProgram) {
        IP0040T1Bean ip0040t1Bean;
        try {
            String query = "SELECT CARDPROGRAMIDENTIFIER,GCMSPRODUCTID,COUNTRYCODEALPHA,REGION FROM EODMASTERIP0040T1DATA WHERE LOWACCOUNTRANGE<=? AND HIGHACCOUNTRANGE>=? AND (GCMSPRODUCTID=? OR LICENSEDPRODUCTID=?)";
            ip0040t1Bean = backendJdbcTemplate.query(query, (ResultSet result) -> {
                IP0040T1Bean tempIp0040t1Bean = new IP0040T1Bean();
                while (result.next()) {
                    //tempIp0040t1Bean= new IP0040T1Bean();
                    tempIp0040t1Bean.setCardProgramIdentifier(result.getString("CARDPROGRAMIDENTIFIER"));
                    tempIp0040t1Bean.setGCMSProductID(result.getString("GCMSPRODUCTID"));
                    tempIp0040t1Bean.setCountryCodeAlpha(result.getString("COUNTRYCODEALPHA"));
                    tempIp0040t1Bean.setRegion(result.getString("REGION"));
                }
                return tempIp0040t1Bean;
            }, cardNumber, cardNumber, cardProgram, cardProgram);


        } catch (Exception e) {
            throw e;
        }
        return ip0040t1Bean;
    }

    @Override
    public IP0040T1Bean getIssuerAccountRangeDetailsWithoutCardProgram(String cardNumber) {
        IP0040T1Bean ip0040t1Bean;
        try {
            String query = "SELECT CARDPROGRAMIDENTIFIER,GCMSPRODUCTID,COUNTRYCODEALPHA,REGION FROM EODMASTERIP0040T1DATA WHERE LOWACCOUNTRANGE<=? AND HIGHACCOUNTRANGE>=? AND GCMSPRODUCTID<>'CIR'";
            ip0040t1Bean = backendJdbcTemplate.query(query, (ResultSet result) -> {
                IP0040T1Bean tempIp0040t1Bean = null;
                int count = 0;
                while (result.next()) {
                    count++;
                    if (count > 1) { // exclude 'MCC' product if count>1
                        if (!result.getString("GCMSPRODUCTID").equals("MCC")) {
                            tempIp0040t1Bean = new IP0040T1Bean();
                            tempIp0040t1Bean.setCardProgramIdentifier(result.getString("CARDPROGRAMIDENTIFIER"));
                            tempIp0040t1Bean.setGCMSProductID(result.getString("GCMSPRODUCTID"));
                            tempIp0040t1Bean.setCountryCodeAlpha(result.getString("COUNTRYCODEALPHA"));
                            tempIp0040t1Bean.setRegion(result.getString("REGION"));
                        }

                    } else {
                        tempIp0040t1Bean = new IP0040T1Bean();
                        tempIp0040t1Bean.setCardProgramIdentifier(result.getString("CARDPROGRAMIDENTIFIER"));
                        tempIp0040t1Bean.setGCMSProductID(result.getString("GCMSPRODUCTID"));
                        tempIp0040t1Bean.setCountryCodeAlpha(result.getString("COUNTRYCODEALPHA"));
                        tempIp0040t1Bean.setRegion(result.getString("REGION"));
                    }

                }
                return tempIp0040t1Bean;
            }, cardNumber, cardNumber);
        } catch (Exception e) {
            throw e;
        }
        return ip0040t1Bean;
    }

    @Override
    public int insertToEODMasterOutgoingFieldIdentity(String MTI, String TXNID, List<String> dataElementValueList, Map<String, String> pdsMap, String CardProgramIdentifier, String GCMSProductID, String CardCountryAlpha, String cardRegion, String networkCode) {
        StringBuilder sb;
        int count = 0;
        int returnValue = 1; //1=success  0=fail

        sb = new StringBuilder();
        try {
            sb.append("INSERT INTO EODMASTEROUTGOINGFIELDIDENTITY (TXNID,FILESTATUS,CARDPROGRAMIDENTIFIER,GCMSPRODUCTID,CARDCOUNTRYALPHA,CARDREGION,NETWORKCODE");
            //for DE
            for (int i = 0; i <= 128; i++) {
                sb.append(",DE" + i);
            }

            //for PDS
            for (Map.Entry<String, String> entry : pdsMap.entrySet()) {
                sb.append(",PDS" + entry.getKey());
            }

            sb.append(") VALUES (:TXNID, :FILESTATUS, :CARDPROGRAMIDENTIFIER, :GCMSPRODUCTID, :CARDCOUNTRYALPHA, :CARDREGION, :NETWORKCODE");

            // for DE
            for (int i = 0; i <= 128; i++) {
                sb.append(", :DE" + i);
            }

            // for PDS
            for (Map.Entry<String, String> entry : pdsMap.entrySet()) {
                sb.append(", :PDS" + entry.getKey());
            }

            sb.append(")");
            MapSqlParameterSource paramSource = new MapSqlParameterSource();
            paramSource.addValue("TXNID", TXNID); //Transaction ID
            paramSource.addValue("FILESTATUS", 0); //FILE STATUS
            paramSource.addValue("CARDPROGRAMIDENTIFIER", "DMC"); //issuer card program identifier from account range eg:-DMC, MCC
            paramSource.addValue("GCMSPRODUCTID", "MCE"); //issuer GCMS Product ID from account range eg:-'MCE', 'MCG', 'MCS'
            paramSource.addValue("CARDCOUNTRYALPHA", "LKA"); //issuer country alpha code eg:-LKA
            paramSource.addValue("CARDREGION", "Canada"); //issuer country region eg:-Asia/Pacific,Canada,Europe
            paramSource.addValue("NETWORKCODE", "MCE"); //issuer network code from F63 first 3  eg:-MCE

            // for DE
            for (int i = 0; i <= 128; i++) {
                paramSource.addValue("DE" + i, dataElementValueList.get(i));
            }

            // for PDS
            for (Map.Entry<String, String> entry : pdsMap.entrySet()) {
                paramSource.addValue("PDS" + entry.getKey(), entry.getValue());
            }
            count = backendJdbcTemplate.update(sb.toString(), paramSource);

            if (count == 0) { //fail the insertion
                returnValue = 0;
            }
        } catch (Exception e) {
            throw e;
        }
        return returnValue;
    }

    @Override
    public int updateEodMerchantTransactionFileStatus(String txnId) {
        int count = 0;
        try {
            String query = "UPDATE EODMERCHANTTRANSACTION  SET OUTGOINGFILESTATUS =1,LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE TRANSACTIONID=?";
            count = backendJdbcTemplate.update(query, Configurations.EOD_USER, txnId);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertRejectMasterOutgoingTransaction(String eodId, String txnId, String rejectException) {
        int count = 0;
        try {
            String query = "INSERT INTO EODMASTEROUTGOINGREJECT(EODID,TRANSACTIONID,REJECTREASON) VALUES(?,?,?)";
            count = backendJdbcTemplate.update(query, eodId, txnId, rejectException);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<IRDCriteriaBean> getIRDCriteriaList(String irdCategory) {
        ArrayList<IRDCriteriaBean> IRDCriteriaBeanList = new ArrayList<>();
        try {
            String query = "SELECT ID,IRD,(' AND '||CRITERIA1||' AND '||CRITERIA2||' AND '||CRITERIA3||' AND '||CRITERIA4||' AND '||CRITERIA5||' AND '||CRITERIA6"
                    + "||' AND '||CRITERIA7||' AND '||CRITERIA8||' AND '||CRITERIA9||' AND '||CRITERIA10||' AND '||CRITERIA11||' AND '||CRITERIA12) AS CRITERIAS FROM EODMASTERIRDCRITERIA WHERE IRDCATEGORY=? ORDER BY CR_RATE_ALL_OTHER,CR_RATE_ALL_OTHER ASC";

            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                while (rs.next()) {
                    IRDCriteriaBean irdCriteriaBean = new IRDCriteriaBean();
                    irdCriteriaBean.setIRD(rs.getString("ID"));
                    irdCriteriaBean.setIRD(rs.getString("IRD"));
                    irdCriteriaBean.setCriterias(rs.getString("CRITERIAS"));
                    IRDCriteriaBeanList.add(irdCriteriaBean);
                }
                return IRDCriteriaBeanList;
            }, irdCategory);
        } catch (Exception e) {
            throw e;
        }
        return IRDCriteriaBeanList;
    }

    @Override
    public ArrayList<String> getMatchingTxnsForIRDCriteria(String dynamicWhereClause, String ruleID) {
        ArrayList<String> txnList = new ArrayList<>();
        String query = "";
        try {
            //replace predefined parameters with eod values for dynamic where clause
            dynamicWhereClause = dynamicWhereClause.replaceAll("\\|FILEDATE\\|", Integer.toString(Configurations.EOD_ID).substring(0, 6));

            query = "SELECT TXNID  FROM EODMASTEROUTGOINGFIELDIDENTITY WHERE SUBSTR(PDS158, -2)='**' AND (FILESTATUS = '0' OR FILESTATUS='2') ".concat(dynamicWhereClause);

            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                while (rs.next()) {
                    txnList.add(rs.getString("TXNID"));
                }
                return txnList;
            });
        } catch (Exception e) {
            logError.error("IRD Rule Intepretation Failed for ruleID:" + ruleID + " \n" + query, e);
            throw e;
        }
        return txnList;
    }

    @Override
    public int updateIRDValue(String IRD, String txnId) {
        int count = 0;
        try {
            String query = "UPDATE EODMASTEROUTGOINGFIELDIDENTITY  SET FILESTATUS=0 ,PDS158 = CONCAT(SUBSTR(PDS158, 0, length(PDS158)-2),?)  WHERE TXNID=?";
            count = backendJdbcTemplate.update(query, IRD, txnId);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateUndecidedIRDFileStatus() {
        int count = 0;
        try {
            String query = "UPDATE EODMASTEROUTGOINGFIELDIDENTITY  SET FILESTATUS=2  WHERE SUBSTR(PDS158, length(PDS158)-1)='**' ";
            count = backendJdbcTemplate.update(query);

            String query1 = "SELECT TXNID FROM EODMASTEROUTGOINGFIELDIDENTITY  WHERE FILESTATUS=2 ";
            backendJdbcTemplate.query(query1, (ResultSet rs) -> {
                String txnId = "";
                while (rs.next()) {
                    txnId = rs.getString("TXNID");
                    logError.error("IRD value not decided for ".concat("Txnid: [").concat(txnId).concat("] , Please update IRD Criterias"));
                }
                return txnId;
            });

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public List<MasterOutgoingFieldIdentityBean> getPendingIPMTxnList() {
        List<MasterOutgoingFieldIdentityBean> txnList = new ArrayList<>();
        try {
            String query = "SELECT TXNID,DE0,DE1,DE2,DE3,DE4,DE5,DE6,DE7,DE8,DE9,DE10,DE11,DE12,DE13,DE14,DE15,DE16,"
                    + "DE17,DE18,DE19,DE20,DE21,DE22,DE23,DE24,DE25,DE26,DE27,DE28,DE29,DE30,DE31,DE32,DE33,DE34,DE35,"
                    + "DE36,DE37,DE38,DE39,DE40,DE41,DE42,DE43,DE44,DE45,DE46,DE47,DE48,DE49,DE50,DE51,DE52,DE53,DE54,"
                    + "DE55,DE56,DE57,DE58,DE59,DE60,DE61,DE62,DE63,DE64,DE65,DE66,DE67,DE68,DE69,DE70,DE71,DE72,DE73,"
                    + "DE74,DE75,DE76,DE77,DE78,DE79,DE80,DE81,DE82,DE83,DE84,DE85,DE86,DE87,DE88,DE89,DE90,DE91,DE92,"
                    + "DE93,DE94,DE95,DE96,DE97,DE98,DE99,DE100,DE101,DE102,DE103,DE104,DE105,DE106,DE107,DE108,DE109,"
                    + "DE110,DE111,DE112,DE113,DE114,DE115,DE116,DE117,DE118,DE119,DE120,DE121,DE122,DE123,DE124,DE125,"
                    + "DE126,DE127,DE128,PDS1,PDS2,PDS3,PDS4,PDS5,PDS6,PDS7,PDS8,PDS9,PDS10,PDS11,PDS12,PDS13,PDS14,PDS15,"
                    + "PDS16,PDS17,PDS18,PDS19,PDS20,PDS21,PDS22,PDS23,PDS24,PDS25,PDS26,PDS27,PDS28,PDS29,PDS30,PDS31,"
                    + "PDS32,PDS33,PDS34,PDS35,PDS36,PDS37,PDS38,PDS39,PDS40,PDS41,PDS42,PDS43,PDS44,PDS45,PDS46,PDS47,"
                    + "PDS48,PDS49,PDS50,PDS51,PDS52,PDS53,PDS54,PDS55,PDS56,PDS57,PDS58,PDS59,PDS60,PDS61,PDS62,PDS63,"
                    + "PDS64,PDS65,PDS66,PDS67,PDS68,PDS69,PDS70,PDS71,PDS72,PDS73,PDS74,PDS75,PDS76,PDS77,PDS78,PDS79,"
                    + "PDS80,PDS81,PDS82,PDS83,PDS84,PDS85,PDS86,PDS87,PDS88,PDS89,PDS90,PDS91,PDS92,PDS93,PDS94,PDS95,"
                    + "PDS96,PDS97,PDS98,PDS99,PDS100,PDS101,PDS102,PDS103,PDS104,PDS105,PDS106,PDS107,PDS108,PDS109,"
                    + "PDS110,PDS111,PDS112,PDS113,PDS114,PDS115,PDS116,PDS117,PDS118,PDS119,PDS120,PDS121,PDS122,PDS123,"
                    + "PDS124,PDS125,PDS126,PDS127,PDS128,PDS129,PDS130,PDS131,PDS132,PDS133,PDS134,PDS135,PDS136,PDS137,"
                    + "PDS138,PDS139,PDS140,PDS141,PDS142,PDS143,PDS144,PDS145,PDS146,PDS147,PDS148,PDS149,PDS150,PDS151,"
                    + "PDS152,PDS153,PDS154,PDS155,PDS156,PDS157,PDS158,PDS159,PDS160,PDS161,PDS162,PDS163,PDS164,PDS165,"
                    + "PDS166,PDS167,PDS168,PDS169,PDS170,PDS171,PDS172,PDS173,PDS174,PDS175,PDS176,PDS177,PDS178,PDS179,"
                    + "PDS180,PDS181,PDS182,PDS183,PDS184,PDS185,PDS186,PDS187,PDS188,PDS189,PDS190,PDS191,PDS192,PDS193,"
                    + "PDS194,PDS195,PDS196,PDS197,PDS198,PDS199,PDS200,PDS201,PDS202,PDS203,PDS204,PDS205,PDS206,PDS207,"
                    + "PDS208,PDS209,PDS210,PDS211,PDS212,PDS213,PDS214,PDS215,PDS216,PDS217,PDS218,PDS219,PDS220,PDS221,"
                    + "PDS222,PDS223,PDS224,PDS225,PDS226,PDS227,PDS228,PDS229,PDS230,PDS231,PDS232,PDS233,PDS234,PDS235,"
                    + "PDS236,PDS237,PDS238,PDS239,PDS240,PDS241,PDS242,PDS243,PDS244,PDS245,PDS246,PDS247,PDS248,PDS249,"
                    + "PDS250,PDS251,PDS252,PDS253,PDS254,PDS255,PDS256,PDS257,PDS258,PDS259,PDS260,PDS261,PDS262,PDS263,"
                    + "PDS264,PDS265,PDS266,PDS267,PDS268,PDS269,PDS270,PDS271,PDS272,PDS273,PDS274,PDS275,PDS276,PDS277,"
                    + "PDS278,PDS279,PDS280,PDS281,PDS282,PDS283,PDS284,PDS285,PDS286,PDS287,PDS288,PDS289,PDS290,PDS291,"
                    + "PDS292,PDS293,PDS294,PDS295,PDS296,PDS297,PDS298,PDS299,PDS300,PDS301,PDS302,PDS303,PDS304,PDS305,"
                    + "PDS306,PDS307,PDS308,PDS309,PDS310,PDS311,PDS312,PDS313,PDS314,PDS315,PDS316,PDS317,PDS318,PDS319,"
                    + "PDS320,PDS321,PDS322,PDS323,PDS324,PDS325,PDS326,PDS327,PDS328,PDS329,PDS330,PDS331,PDS332,PDS333,"
                    + "PDS334,PDS335,PDS336,PDS337,PDS338,PDS339,PDS340,PDS341,PDS342,PDS343,PDS344,PDS345,PDS346,PDS347,"
                    + "PDS348,PDS349,PDS350,PDS351,PDS352,PDS353,PDS354,PDS355,PDS356,PDS357,PDS358,PDS359,PDS360,PDS361,"
                    + "PDS362,PDS363,PDS364,PDS365,PDS366,PDS367,PDS368,PDS369,PDS370,PDS371,PDS372,PDS373,PDS374,PDS375,"
                    + "PDS376,PDS377,PDS378,PDS379,PDS380,PDS381,PDS382,PDS383,PDS384,PDS385,PDS386,PDS387,PDS388,PDS389,"
                    + "PDS390,PDS391,PDS392,PDS393,PDS394,PDS395,PDS396,PDS397,PDS398,PDS399,PDS400,PDS401,PDS402,PDS403,"
                    + "PDS404,PDS405,PDS406,PDS407,PDS408,PDS409,PDS410,PDS411,PDS412,PDS413,PDS414,PDS415,PDS416,PDS417,"
                    + "PDS418,PDS419,PDS420,PDS421,PDS422,PDS423,PDS424,PDS425,PDS426,PDS427,PDS428,PDS429,PDS430,PDS431,"
                    + "PDS432,PDS433,PDS434,PDS435,PDS436,PDS437,PDS438,PDS439,PDS440,PDS441,PDS442,PDS443,PDS444,PDS445,"
                    + "PDS446,PDS447,PDS448,PDS449,PDS450,PDS451,PDS452,PDS453,PDS454,PDS455,PDS456,PDS457,PDS458,PDS459,"
                    + "PDS460,PDS461,PDS462,PDS463,PDS464,PDS465,PDS466,PDS467,PDS468,PDS469,PDS470,PDS471,PDS472,PDS473,"
                    + "PDS474,PDS475,PDS476,PDS477,PDS478,PDS479,PDS480,PDS481,PDS482,PDS483,PDS484,PDS485,PDS486,PDS487,"
                    + "PDS488,PDS489,PDS490,PDS491,PDS492,PDS493,PDS494,PDS495,PDS496,PDS497,PDS498,PDS499,PDS500,PDS501,"
                    + "PDS502,PDS503,PDS504,PDS505,PDS506,PDS507,PDS508,PDS509,PDS510,PDS511,PDS512,PDS513,PDS514,PDS515,"
                    + "PDS516,PDS517,PDS518,PDS519,PDS520,PDS521,PDS522,PDS523,PDS524,PDS525,PDS526,PDS527,PDS528,PDS529,"
                    + "PDS530,PDS531,PDS532,PDS533,PDS534,PDS535,PDS536,PDS537,PDS538,PDS539,PDS540,PDS541,PDS542,PDS543,"
                    + "PDS544,PDS545,PDS546,PDS547,PDS548,PDS549,PDS550,PDS551,PDS552,PDS553,PDS554,PDS555,PDS556,PDS557,"
                    + "PDS558,PDS559,PDS560,PDS561,PDS562,PDS563,PDS564,PDS565,PDS566,PDS567,PDS568,PDS569,PDS570,PDS571,"
                    + "PDS572,PDS573,PDS574,PDS575,PDS576,PDS577,PDS578,PDS579,PDS580,PDS581,PDS582,PDS583,PDS584,PDS585,"
                    + "PDS586,PDS587,PDS588,PDS589,PDS590,PDS591,PDS592,PDS593,PDS594,PDS595,PDS596,PDS597,PDS598,PDS599,"
                    + "PDS600,PDS601,PDS602,PDS603,PDS604,PDS605,PDS606,PDS607,PDS608,PDS609,PDS610,PDS611,PDS612,PDS613,"
                    + "PDS614,PDS615,PDS616,PDS617,PDS618,PDS619,PDS620,PDS621,PDS622,PDS623,PDS624,PDS625,PDS626,PDS627,"
                    + "PDS628,PDS629,PDS630,PDS631,PDS632,PDS633,PDS634,PDS635,PDS636,PDS637,PDS638,PDS639,PDS640,PDS641,"
                    + "PDS642,PDS643,PDS644,PDS645,PDS646,PDS647,PDS648,PDS649,PDS650,PDS651,PDS652,PDS653,PDS654,PDS655,"
                    + "PDS656,PDS657,PDS658,PDS659,PDS660,PDS661,PDS662,PDS663,PDS664,PDS665,PDS666,PDS667,PDS668,PDS669,"
                    + "PDS670,PDS671,PDS672,PDS673,PDS674,PDS675,PDS676,PDS677,PDS678,PDS679,PDS680,PDS681,PDS682,PDS683,"
                    + "PDS684,PDS685,PDS686,PDS687,PDS688,PDS689,PDS690,PDS691,PDS692,PDS693,PDS694,PDS695,PDS696,PDS697,"
                    + "PDS698,PDS699,PDS700,PDS701,PDS702,PDS703,PDS704,PDS705,PDS706,PDS707,PDS708,PDS709,PDS710,PDS711,"
                    + "PDS712,PDS713,PDS714,PDS715,PDS716,PDS717,PDS718,PDS719,PDS720,PDS721,PDS722,PDS723,PDS724,PDS725,"
                    + "PDS726,PDS727,PDS728,PDS729,PDS730,PDS731,PDS732,PDS733,PDS734,PDS735,PDS736,PDS737,PDS738,PDS739,"
                    + "PDS740,PDS741,PDS742,PDS743,PDS744,PDS745,PDS746,PDS747,PDS748,PDS749,PDS750,PDS751,PDS752,PDS753,"
                    + "PDS754,PDS755,PDS756,PDS757,PDS758,PDS759,PDS760,PDS761,PDS762,PDS763,PDS764,PDS765,PDS766,PDS767,"
                    + "PDS768,PDS769,PDS770,PDS771,PDS772,PDS773,PDS774,PDS775,PDS776 FROM EODMASTEROUTGOINGFIELDIDENTITY WHERE FILESTATUS = 0  ORDER BY LPAD(DE71,10) ASC";

            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                while (rs.next()) {
                    MasterOutgoingFieldIdentityBean bean = new MasterOutgoingFieldIdentityBean();
                    bean.setTxnId(rs.getString("TXNID"));
                    bean.setDE0(rs.getString("DE0"));
                    bean.setDE1(rs.getString("DE1"));
                    bean.setDE2(rs.getString("DE2"));
                    bean.setDE3(rs.getString("DE3"));
                    bean.setDE4(rs.getString("DE4"));
                    bean.setDE5(rs.getString("DE5"));
                    bean.setDE6(rs.getString("DE6"));
                    bean.setDE7(rs.getString("DE7"));
                    bean.setDE8(rs.getString("DE8"));
                    bean.setDE9(rs.getString("DE9"));
                    bean.setDE10(rs.getString("DE10"));
                    bean.setDE11(rs.getString("DE11"));
                    bean.setDE12(rs.getString("DE12"));
                    bean.setDE13(rs.getString("DE13"));
                    bean.setDE14(rs.getString("DE14"));
                    bean.setDE15(rs.getString("DE15"));
                    bean.setDE16(rs.getString("DE16"));
                    bean.setDE17(rs.getString("DE17"));
                    bean.setDE18(rs.getString("DE18"));
                    bean.setDE19(rs.getString("DE19"));
                    bean.setDE20(rs.getString("DE20"));
                    bean.setDE21(rs.getString("DE21"));
                    bean.setDE22(rs.getString("DE22"));
                    bean.setDE23(rs.getString("DE23"));
                    bean.setDE24(rs.getString("DE24"));
                    bean.setDE25(rs.getString("DE25"));
                    bean.setDE26(rs.getString("DE26"));
                    bean.setDE27(rs.getString("DE27"));
                    bean.setDE28(rs.getString("DE28"));
                    bean.setDE29(rs.getString("DE29"));
                    bean.setDE30(rs.getString("DE30"));
                    bean.setDE31(rs.getString("DE31"));
                    bean.setDE32(rs.getString("DE32"));
                    bean.setDE33(rs.getString("DE33"));
                    bean.setDE34(rs.getString("DE34"));
                    bean.setDE35(rs.getString("DE35"));
                    bean.setDE36(rs.getString("DE36"));
                    bean.setDE37(rs.getString("DE37"));
                    bean.setDE38(rs.getString("DE38"));
                    bean.setDE39(rs.getString("DE39"));
                    bean.setDE40(rs.getString("DE40"));
                    bean.setDE41(rs.getString("DE41"));
                    bean.setDE42(rs.getString("DE42"));
                    bean.setDE43(rs.getString("DE43"));
                    bean.setDE44(rs.getString("DE44"));
                    bean.setDE45(rs.getString("DE45"));
                    bean.setDE46(rs.getString("DE46"));
                    bean.setDE47(rs.getString("DE47"));
                    bean.setDE48(rs.getString("DE48"));
                    bean.setDE49(rs.getString("DE49"));
                    bean.setDE50(rs.getString("DE50"));
                    bean.setDE51(rs.getString("DE51"));
                    bean.setDE52(rs.getString("DE52"));
                    bean.setDE53(rs.getString("DE53"));
                    bean.setDE54(rs.getString("DE54"));
                    bean.setDE55(rs.getString("DE55"));
                    bean.setDE56(rs.getString("DE56"));
                    bean.setDE57(rs.getString("DE57"));
                    bean.setDE58(rs.getString("DE58"));
                    bean.setDE59(rs.getString("DE59"));
                    bean.setDE60(rs.getString("DE60"));
                    bean.setDE61(rs.getString("DE61"));
                    bean.setDE62(rs.getString("DE62"));
                    bean.setDE63(rs.getString("DE63"));
                    bean.setDE64(rs.getString("DE64"));
                    bean.setDE65(rs.getString("DE65"));
                    bean.setDE66(rs.getString("DE66"));
                    bean.setDE67(rs.getString("DE67"));
                    bean.setDE68(rs.getString("DE68"));
                    bean.setDE69(rs.getString("DE69"));
                    bean.setDE70(rs.getString("DE70"));
                    bean.setDE71(rs.getString("DE71"));
                    bean.setDE72(rs.getString("DE72"));
                    bean.setDE73(rs.getString("DE73"));
                    bean.setDE74(rs.getString("DE74"));
                    bean.setDE75(rs.getString("DE75"));
                    bean.setDE76(rs.getString("DE76"));
                    bean.setDE77(rs.getString("DE77"));
                    bean.setDE78(rs.getString("DE78"));
                    bean.setDE79(rs.getString("DE79"));
                    bean.setDE80(rs.getString("DE80"));
                    bean.setDE81(rs.getString("DE81"));
                    bean.setDE82(rs.getString("DE82"));
                    bean.setDE83(rs.getString("DE83"));
                    bean.setDE84(rs.getString("DE84"));
                    bean.setDE85(rs.getString("DE85"));
                    bean.setDE86(rs.getString("DE86"));
                    bean.setDE87(rs.getString("DE87"));
                    bean.setDE88(rs.getString("DE88"));
                    bean.setDE89(rs.getString("DE89"));
                    bean.setDE90(rs.getString("DE90"));
                    bean.setDE91(rs.getString("DE91"));
                    bean.setDE92(rs.getString("DE92"));
                    bean.setDE93(rs.getString("DE93"));
                    bean.setDE94(rs.getString("DE94"));
                    bean.setDE95(rs.getString("DE95"));
                    bean.setDE96(rs.getString("DE96"));
                    bean.setDE97(rs.getString("DE97"));
                    bean.setDE98(rs.getString("DE98"));
                    bean.setDE99(rs.getString("DE99"));
                    bean.setDE100(rs.getString("DE100"));
                    bean.setDE101(rs.getString("DE101"));
                    bean.setDE102(rs.getString("DE102"));
                    bean.setDE103(rs.getString("DE103"));
                    bean.setDE104(rs.getString("DE104"));
                    bean.setDE105(rs.getString("DE105"));
                    bean.setDE106(rs.getString("DE106"));
                    bean.setDE107(rs.getString("DE107"));
                    bean.setDE108(rs.getString("DE108"));
                    bean.setDE109(rs.getString("DE109"));
                    bean.setDE110(rs.getString("DE110"));
                    bean.setDE111(rs.getString("DE111"));
                    bean.setDE112(rs.getString("DE112"));
                    bean.setDE113(rs.getString("DE113"));
                    bean.setDE114(rs.getString("DE114"));
                    bean.setDE115(rs.getString("DE115"));
                    bean.setDE116(rs.getString("DE116"));
                    bean.setDE117(rs.getString("DE117"));
                    bean.setDE118(rs.getString("DE118"));
                    bean.setDE119(rs.getString("DE119"));
                    bean.setDE120(rs.getString("DE120"));
                    bean.setDE121(rs.getString("DE121"));
                    bean.setDE122(rs.getString("DE122"));
                    bean.setDE123(rs.getString("DE123"));
                    bean.setDE124(rs.getString("DE124"));
                    bean.setDE125(rs.getString("DE125"));
                    bean.setDE126(rs.getString("DE126"));
                    bean.setDE127(rs.getString("DE127"));
                    bean.setDE128(rs.getString("DE128"));

                    Map<String, String> pdsMap = new LinkedHashMap<>();
                    for (int j = 1; j <= 776; j++) {
                        String value = rs.getString("PDS".concat(Integer.toString(j)));
                        if (value != null) {
                            pdsMap.put(Integer.toString(j), value);
                        }
                    }
                    bean.setPdsMap(pdsMap);

                    txnList.add(bean);
                }
                return txnList;
            });
        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }

    @Override
    public int updateMasterOutgoingFieldIdentityFileStatus(String txnId, String fileName) {
        int count = 0;
        try {
            String transactionStatus = "UPDATE EODMASTEROUTGOINGFIELDIDENTITY SET FILESTATUS=? , FILEID=?  WHERE TXNID=? ";
            count = backendJdbcTemplate.update(transactionStatus, 1, fileName, txnId);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void insertIPMFileSummery(String ipmFileName, int txnCount) {
        try {
            String query = "INSERT INTO EODMASTEROUTGOINGFILESUMMERY (FILENAME,TXNCOUNT,CREATEDTIME) VALUES (?,?,SYSDATE)";
            backendJdbcTemplate.update(query, ipmFileName, txnCount);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int insertOutputFiles(EodOuputFileBean outputfilebean, String fileType) {
        int count = 0;
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
}
