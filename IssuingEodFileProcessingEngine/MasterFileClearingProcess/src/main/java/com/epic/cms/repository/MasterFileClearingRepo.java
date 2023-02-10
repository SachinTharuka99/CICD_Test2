/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:34 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MasterFileClearingDao;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.MasterFieldsDataBean;
import com.epic.cms.model.bean.MasterPDSBean;
import com.epic.cms.model.bean.MasterRejectBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class MasterFileClearingRepo implements MasterFileClearingDao {
    @Autowired
    LogManager logManager;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private JdbcTemplate onlineJdbcTemplate;

    @Override
    public boolean isFilesAvailable(String status) throws Exception {
        int recordCount = 0;
        try {
            String query = "SELECT COUNT(*) AS TOTAL FROM EODMASTERFILE WHERE STATUS = ?";
            recordCount = backendJdbcTemplate.queryForObject(query, Integer.class, status);
            if (recordCount > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            errorLogger.error(ex.toString());
            return false;
        }
    }

    @Override
    public ArrayList<FileBean> getFileDetails(String status) throws Exception {
        ArrayList<FileBean> fileNames = new ArrayList<>();
        try {
            String query = "SELECT FILENAME,FILEID FROM EODMASTERFILE WHERE STATUS = ?";
            fileNames = (ArrayList<FileBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        FileBean bean = new FileBean();
                        bean.setFileName(result.getString("FILENAME"));
                        bean.setFileId(result.getString("FILEID"));
                        return bean;
                    }),
                    status
            );
            return fileNames;
        } catch (Exception ex) {
            errorLogger.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void loadFilePaths() throws Exception {
        try {
            String query = "SELECT FILETYPE,FILEPATHWINDOWS,FILEPATHLINUX,BACKUPPATHWINDOWS,BACKUPPATHLINUX FROM EODFILEINFO WHERE FILETYPE = ?";
            List<Map<String, Object>> rows = backendJdbcTemplate.queryForList(query, Configurations.CARD_ASSOCIATION_MASTER);
            for (Map row : rows) {
                Configurations.PATH_MASTER_FILE_WINDOWS = (String) row.get("FILEPATHWINDOWS");
                Configurations.PATH_MASTER_FILE_LINUX = (String) row.get("FILEPATHLINUX");
                Configurations.PATH_BACKUP_WINDOWS = (String) row.get("BACKUPPATHWINDOWS");
                Configurations.PATH_BACKUP_LINUX = (String) row.get("BACKUPPATHLINUX");
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public FileBean getMasterFileInfo(String fileId) throws Exception {
        FileBean fileBean = new FileBean();
        try {
            String query = "SELECT FILEID,FILENAME FROM EODMASTERFILE WHERE FILEID=? AND STATUS=?";
            fileBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                        @Override
                        public FileBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            FileBean bean = new FileBean();
                            bean.setFileId(rs.getString("FILEID"));
                            bean.setFileName(rs.getString("FILENAME"));
                            return bean;
                        }
                    },
                    fileId,
                    status.getINITIAL_STATUS()
            );

        } catch (EmptyResultDataAccessException ex) {
            infoLogger.info(ex.getMessage());
        } catch (Exception ex) {
            throw ex;
        }
        return fileBean;
    }

    @Override
    public void updateFileStartTime(String fileId) throws DataAccessException {
        try {
            String query = "UPDATE EODMASTERFILE SET STARTTIME = SYSDATE WHERE FILEID = ?";
            backendJdbcTemplate.update(query, fileId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @Override
    public void updateFileStatus(String fileId, String status) throws DataAccessException {
        try {
            String query = "UPDATE EODMASTERFILE SET STATUS=?,EODID=? WHERE FILEID = ?";
            backendJdbcTemplate.update(query, status, Configurations.EOD_ID, fileId);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @Override
    public void insertFileDetailsIntoEODMasterInputRowData(String fileID, String lineNumber, String content) throws DataAccessException {
        try {
            String query = "INSERT INTO EODMASTERINPUTROWDATA"
                    + " (FILEID,LINENUMBER,RECORDCONTENT) VALUES (?,?,?)";
            backendJdbcTemplate.update(query, fileID, lineNumber, content);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @Override
    public void insertFileDetailsIntoEODMasterFieldIdentity(MasterFieldsDataBean masterBean) throws DataAccessException {
        try {
            String query = "INSERT INTO EODMASTERFIELDIDENTITY "
                    + "(FILEID,LINENUMBER,TXNID,STATUS,MTI,PAN,PROCESSINGCODE,TXNAMOUNT,RECONAMOUNT,BILLINGAMOUNT,"
                    + "TRANSMISSIONTIME,BILLINGFEE,BILLINGCONVERSIONRATE,RECONCONVERSIONRATE,TRACENUMBER,TXNDATE,EFFECTIVEDATE,"
                    + "EXPIRATIONDATE,SETTLEMENTDATE,CONVERSIONDATE,CAPTUREDATE,MERCHANTTYPE,PANCOUNTRYCODE,ACQURECOUNTRYCODE,"
                    + "FORWARDINGCOUNTRYCODE,POSCODE,CARDSEQNUMBER,ORIGINALAMOUNTS,ACQUIRERINSTITUTEID,FORWADINGINSTITUTEID,"
                    + "ACQUIRERREFDATA,RECONDATE,RECONINDICATOR,FUNCTIONCODE,MESSAGEREASONCODE,ACCEPTORBUSINESSCODE,"
                    + "APPROVALCODELENGTH,EXTENDEDPAN,TRACK2DATA,TRACK3DATA,RRNUMBER,APPROVALCODE,ACTIONCODE,SERVICECODE,"
                    + "ACCEPTORTERMINALID,ACCEPTOERID,ACCEPTORNAME,ADDITIONALRESPONSEDATA,TRACK1DATA,FEES,NATIONALADDITIONALDATA,"
                    + "PRIVATEADDITIONALDATA,TXNCURRENCYCODE,RECONCURRENCYCODE,BIILINGCURRECYCODE,PINDATA,SECURITYINFORMATION,"
                    + "ADDITIONALAMOUNTS,ICCDATA,ORIGINALDATAELEMENTS,AUTHCODE,AUTHINSTITUTEID,TRANSPORTDATA,"
                    + "RESERVEDFORNATIONAL1,RESERVEDFORNATIONAL2,ADDITIONALDATA2,TXNLIFECYCLEID,MACFIELD1,RESERVEDISO1,"
                    + "ORIGINALFESS,EXTENDEDPAYMENT,RECEIVINGCOUNTRYCODE,SETTLEMENTCOUNTRYCODE,AUTHCOUNTRYCODE,MESSAGENUMBER,"
                    + "DATARECORD,ACTIONDATE,CREDITSNO,CREDITSREVERSALNO,DEBITSNO,DEBITSREVERSALNO,TRANSFERNO,TRANSFERREVERSALNO,"
                    + "INQUIRIESNO,AUTHORIZATIONSNO,INQUIRIESREVERSALNO,PAYMENTSNO,PAYMENTSREVERSALNO,FEECOLLECTIONNO,CREDITSAMOUNT,"
                    + "CREDITSREVERSALAMOUNT,DEBITSAMOUNT,DEBITSREVERSALAMOUNT,AUTHREVERSALNO,TXNDESTINATIONCOUNTRYCODE,"
                    + "TXNSOURCECOUNTRYCODE,TXNDESTINATIONCODE,TXNSOURCECODE,ISSUERREFDATA,KEYMGTDATA,NETRECONAMOUNT,PAYEE,"
                    + "SETTLEMENTINSTITUTEID,RECEIVINGINSTITUTEID,FILENAME,ACCOUNTIDENTIFICATION1,ACCOUNTIDENTIFICATION2,"
                    + "TXNDESCRIPTION,CREDITCHARGEBACKAMOUNT,DEBITCHARGEBACKAMOUNT,CREDITCHARGEBACKNO,DEBITCHARGEBACKNO,"
                    + "CREDITFEEAMOUNTS,DEBITFEEAMOUNTS,CONVERSIONASSESSAMOUNT,RESERVEDISO2,RESERVEDISO3,RESERVEDISO4,"
                    + "RESERVEDISO5,RESERVEDFORNATIONAL3,RESERVEDFORNATIONAL4,RESERVEDFORNATIONAL5,RESERVEDFORNATIONAL6,"
                    + "RESERVEDFORNATIONAL7,RESERVEDFORNATIONAL8,RESERVEDFORNATIONAL9,ADDITIONALDATA3,ADDITIONALDATA4,"
                    + "ADDITIONALDATA5,RESERVEDPRIVATE,NETWORKDATA,MACFIELD2,TXNTIME)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            Object[] param = new Object[]{
                    masterBean.getFileid(),
                    masterBean.getLineNumber(),
                    masterBean.getTxnId(),
                    masterBean.getStatus(),
                    masterBean.getMti(),
                    masterBean.getPan().toString(),
                    masterBean.getProcessingCode(),
                    masterBean.getTxnAmount(),
                    masterBean.getReconAmount(),
                    masterBean.getBillingAmount(),
                    masterBean.getTransmissionTime(),
                    masterBean.getBillingFee(),
                    masterBean.getBillingConversionRate(),
                    masterBean.getReconConversionRate(),
                    masterBean.getTraceNumber(),
                    masterBean.getTxnDate(),
                    masterBean.getEffectiveDate(),
                    masterBean.getExpirationDate(),
                    masterBean.getSettlementDate(),
                    masterBean.getConversionDate(),
                    masterBean.getCaptureDate(),
                    masterBean.getMerchantType(),
                    masterBean.getPancountryCode(),
                    masterBean.getAcqureCountryCode(),
                    masterBean.getForwardingCountryCode(),
                    masterBean.getPosCode(),
                    masterBean.getCardSeqNumber(),
                    masterBean.getOriginalAmounts(),
                    masterBean.getAcquirerInstituteId(),
                    masterBean.getForwadingInstituteId(),
                    masterBean.getAcquirerRefData(),
                    masterBean.getReconDate(),
                    masterBean.getReconIndicator(),
                    masterBean.getFunctionCode(),
                    masterBean.getMessageReasonCode(),
                    masterBean.getAcceptorBusinessCode(),
                    masterBean.getApprovalCodeLength(),
                    masterBean.getExtendedPan(),
                    masterBean.getTrack2Data(),
                    masterBean.getTrack3Data(),
                    masterBean.getRrNumber(),
                    masterBean.getApprovalCode(),
                    masterBean.getActionCode(),
                    masterBean.getServiceCode(),
                    masterBean.getAcceptorTerminalId(),
                    masterBean.getAcceptoerId(),
                    masterBean.getAcceptorName(),
                    masterBean.getAdditionalResponseData(),
                    masterBean.getTrack1Data(),
                    masterBean.getFees(),
                    masterBean.getNationalAdditionalData(),
                    masterBean.getPrivateAdditionalData(),
                    masterBean.getTxnCurrencyCode(),
                    masterBean.getReconCurrencyCode(),
                    masterBean.getBillingCurrencyCode(),
                    masterBean.getPinData(),
                    masterBean.getSecurityInformation(),
                    masterBean.getAdditionalAmounts(),
                    masterBean.getIccData(),
                    masterBean.getOriginalDataElements(),
                    masterBean.getAuthCode(),
                    masterBean.getAuthInstituteId(),
                    masterBean.getTransportData(),
                    masterBean.getReservedForNational1(),
                    masterBean.getReservedForNational2(),
                    masterBean.getAdditionalData2(),
                    masterBean.getTxnLifeCycleId(),
                    masterBean.getMacField1(),
                    masterBean.getReservedIso1(),
                    masterBean.getOriginalFees(),
                    masterBean.getExtendedPayment(),
                    masterBean.getReceivingCountryCode(),
                    masterBean.getSettlementCountryCode(),
                    masterBean.getAuthCountryCode(),
                    masterBean.getMessageNumber(),
                    masterBean.getDataRecord(),
                    masterBean.getActionDate(),
                    masterBean.getCreditsNo(),
                    masterBean.getCreditsReversalNo(),
                    masterBean.getDebitsNo(),
                    masterBean.getDebitsReversalNo(),
                    masterBean.getTransferNo(),
                    masterBean.getTransferReversalNo(),
                    masterBean.getInquiriesNo(),
                    masterBean.getAuthorizationsNo(),
                    masterBean.getInquiriesReversalNo(),
                    masterBean.getPaymentsNo(),
                    masterBean.getPaymentsReversalNo(),
                    masterBean.getFeeCollectionNo(),
                    masterBean.getCreditsAmount(),
                    masterBean.getCreditsReversalAmount(),
                    masterBean.getDebitsAmount(),
                    masterBean.getDebitsReversalAmount(),
                    masterBean.getAuthReversalNo(),
                    masterBean.getTxnDestinationCountryCode(),
                    masterBean.getTxnSourceCountryCode(),
                    masterBean.getTxnDestinationCode(),
                    masterBean.getTxnSourceCode(),
                    masterBean.getIssuerRefData(),
                    masterBean.getKeyMgtData(),
                    masterBean.getNetReconAmount(),
                    masterBean.getPayee(),
                    masterBean.getSettlementInstituteId(),
                    masterBean.getReceivingInstituteId(),
                    masterBean.getFileName(),
                    masterBean.getAccountIdentification1(),
                    masterBean.getAccountIdentification2(),
                    masterBean.getTxnDescription(),
                    masterBean.getCreditchargebackamount(),
                    masterBean.getDebitChargebackAmount(),
                    masterBean.getCreditChargebackNo(),
                    masterBean.getDebitChargebackNo(),
                    masterBean.getCreditFeeAmounts(),
                    masterBean.getDebitFeeAmounts(),
                    masterBean.getConversionAssessAmount(),
                    masterBean.getReservedIso2(),
                    masterBean.getReservedIso3(),
                    masterBean.getReservedIso4(),
                    masterBean.getReservedIso5(),
                    masterBean.getReservedForNational3(),
                    masterBean.getReservedForNational4(),
                    masterBean.getReservedForNational5(),
                    masterBean.getReservedForNational6(),
                    masterBean.getReservedForNational7(),
                    masterBean.getReservedForNational8(),
                    masterBean.getReservedForNational9(),
                    masterBean.getAdditionalData3(),
                    masterBean.getAdditionalData4(),
                    masterBean.getAdditionalData5(),
                    masterBean.getReservedPrivate(),
                    masterBean.getNetworkData(),
                    masterBean.getMacField2(),
                    masterBean.getTxnTime()
            };
            backendJdbcTemplate.update(query, param);
        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @Override
    public void insertFileDetailsIntoEODMasterTransaction(MasterFieldsDataBean masterBean) throws Exception {
        try {
            String query = "INSERT INTO EODMASTERTRANSACTION "
                    + "(FILEID,LINENUMBER,TXNID,STATUS,MTI,PAN,PROCESSINGCODE,TXNAMOUNT,RECONAMOUNT,BILLINGAMOUNT,"
                    + "BILLINGFEE,BILLINGCONVERSIONRATE,RECONCONVERSIONRATE,TXNDATE,EXPIRATIONDATE,POSCODE,CARDSEQNUMBER,"
                    + "ORIGINALAMOUNTS,ACQUIRERINSTITUTEID,FORWADINGINSTITUTEID,ACQUIRERREFDATA,FUNCTIONCODE,MESSAGEREASONCODE,"
                    + "ACCEPTORBUSINESSCODE,RRNUMBER,APPROVALCODE,SERVICECODE,ACCEPTORTERMINALID,ACCEPTOERID,ACCEPTORNAME,"
                    + "PRIVATEADDITIONALDATA,TXNCURRENCYCODE,RECONCURRENCYCODE,BIILINGCURRECYCODE,ICCDATA,ADDITIONALDATA2,"
                    + "TXNLIFECYCLEID,MESSAGENUMBER,DATARECORD,ACTIONDATE,TXNDESTINATIONCODE,TXNSOURCECODE,ISSUERREFDATA,"
                    + "RECEIVINGINSTITUTEID,ADDITIONALDATA3,ADDITIONALDATA4,ADDITIONALDATA5,NETWORKDATA,"
                    + "TXNTYPE,TXNTIME,MERCHANTCITY,MERCHANTNAME,MERCHANTCOUNTRYCODE,EODSTATUS)"
                    + " VALUES ("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            Object[] param = new Object[]{
                    masterBean.getFileid(),
                    masterBean.getLineNumber(),
                    masterBean.getTxnId(),
                    "TINIT",
                    masterBean.getMti(),
                    masterBean.getPan().toString(),
                    masterBean.getProcessingCode(),
                    this.getDecimalAmountByConsideringExponent(masterBean.getCurrencyExponentList(), masterBean.getTxnCurrencyCode(), masterBean.getTxnAmount()),  //convert TxnAmount with relevant exponent
                    this.getDecimalAmountByConsideringExponent(masterBean.getCurrencyExponentList(), masterBean.getReconCurrencyCode(), masterBean.getReconAmount()),  //convert ReconAmount with relevant exponent
                    this.getDecimalAmountByConsideringExponent(masterBean.getCurrencyExponentList(), masterBean.getBillingCurrencyCode(), masterBean.getBillingAmount()),  //convert BillingAmount with relevant exponent
                    masterBean.getBillingFee(),
                    masterBean.getBillingConversionRate(),
                    masterBean.getReconConversionRate(),
                    masterBean.getTxnDate(),
                    masterBean.getExpirationDate(),
                    masterBean.getPosCode(),
                    masterBean.getCardSeqNumber(),
                    masterBean.getOriginalAmounts(),
                    masterBean.getAcquirerInstituteId(),
                    masterBean.getForwadingInstituteId(),
                    masterBean.getAcquirerRefData(),
                    masterBean.getFunctionCode(),
                    masterBean.getMessageReasonCode(),
                    masterBean.getAcceptorBusinessCode(),
                    masterBean.getRrNumber(),
                    masterBean.getApprovalCode() == null ? " " : masterBean.getApprovalCode(), // null approval code will replace with single space
                    masterBean.getServiceCode(),
                    masterBean.getAcceptorTerminalId(),
                    masterBean.getAcceptoerId(),
                    masterBean.getAcceptorName(),
                    masterBean.getPrivateAdditionalData(),
                    masterBean.getTxnCurrencyCode(),
                    masterBean.getReconCurrencyCode(),
                    masterBean.getBillingCurrencyCode(),
                    masterBean.getIccData(),
                    masterBean.getAdditionalData2(),
                    masterBean.getTxnLifeCycleId(),
                    masterBean.getMessageNumber(),
                    masterBean.getDataRecord(),
                    masterBean.getActionDate(),
                    masterBean.getTxnDestinationCode(),
                    masterBean.getTxnSourceCode(),
                    masterBean.getIssuerRefData(),
                    masterBean.getReceivingInstituteId(),
                    masterBean.getAdditionalData3(),
                    masterBean.getAdditionalData4(),
                    masterBean.getAdditionalData5(),
                    masterBean.getNetworkData(),
                    this.txnType(masterBean.getReversalPds(), masterBean.getReversalData(), masterBean.getTxnTypeProcessingCode()),//check reversal ,ATM ,POS
                    masterBean.getTxnTime(),
                    masterBean.getMerchantCity(),
                    masterBean.getMerchantName(),
                    masterBean.getMerchantCountryCode(),
                    Configurations.EOD_PENDING_STATUS
            };

            backendJdbcTemplate.update(query, param);

        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int insertExceptionalTransactionData(String fileID, String txnID, String TC, StringBuffer cardNumber, String authCode, String MID, String sourceAmount, String sourceCurrencyCode, String txnDate, String txnTime, String processingDate, String lstUpdateUser, Date lstUpdateDate, String destinationAmount, String destinationCurrencyCode, String financialStatus, String merchantName, String merchantCity, String merchantCountryCode, String MCC, String merchantZipCode, String merchantState, String rrn, String tid, String posEntryMode, String fileType) throws Exception {
        int count = 0;
        try {
            String query = "INSERT INTO EODEXCEPTIONALTRANSACTION (FILEID,TXNID,TC,CARDNUMBER,"
                    + "AUTHCODE,MID,SOURCEAMOUNT,SOURCECURRENCYCODE,TXNDATE,TXNTIME,PROCESSINGDATE,"
                    + "LASTUPDATEDUSER,LASTUPDATEDDATE,DESTINATIONAMOUNT,"
                    + "DESTINATIONCURRENCYCODE,FINANCIALSTATUS,MERCHANTNAME,MERCHANTCITY,MERCHANTCOUNTRYCODE,"
                    + "MCC,MERCHANTZIPCODE,MERCHANTSTATE,RRN,TID,POSENTRYMODE,FILETYPE,EODID) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            Object[] param = new Object[]{
                    fileID,
                    txnID,
                    TC,
                    cardNumber.toString(),
                    authCode,
                    MID,
                    sourceAmount,
                    sourceCurrencyCode,
                    txnDate,
                    txnTime,
                    processingDate,
                    lstUpdateUser,
                    lstUpdateDate,
                    destinationAmount,
                    destinationCurrencyCode,
                    financialStatus,
                    merchantName,
                    merchantCity,
                    merchantCountryCode,
                    MCC,
                    merchantZipCode,
                    merchantState,
                    rrn,
                    tid,
                    posEntryMode,
                    fileType,
                    Configurations.EOD_ID
            };
            count = backendJdbcTemplate.update(query, param);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public void insertRejectedMasterDetails(MasterRejectBean rejectBean, String user) throws Exception {
        try {
            String query = "INSERT INTO EODMASTERREJECT"
                    + " (FILEID,LINENUMBER,FIELDID,VALIDATIONID,LASTUPDATEDUSER,"
                    + "FIELDCONTENT,LINECONTENT,LASTUPDATEDDATE) "
                    + "VALUES (?,?,?,(SELECT VALIDATIONID FROM EODMASTERFIELD WHERE FIELDID = ?),?,?,?,SYSDATE)";
            Object[] param = new Object[]{
                    rejectBean.getFileId(),
                    rejectBean.getLineNumber(),
                    rejectBean.getFieldId(),
                    rejectBean.getFieldId(),
                    user,
                    rejectBean.getFieldContent(),
                    rejectBean.getLineContent()
            };
            backendJdbcTemplate.update(query, param);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    @Transactional(value = "backendDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public void updateFileRecordCount(String fileId, String count) throws Exception {
        try {
            String query = "UPDATE EODMASTERFILE SET NOOFRECORDS = ? WHERE FILEID = ?";
            backendJdbcTemplate.update(query, count, fileId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void updateFileTxnCount(String fileId, String tCount) throws Exception {
        try {
            String query = "UPDATE EODMASTERFILE SET NOOFTRANSACTION = ? WHERE FILEID = ?";
            backendJdbcTemplate.update(query, tCount, fileId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public int loadMasterTransactionCount(String fileId) throws Exception {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) AS TOTAL FROM EODMASTERTRANSACTION WHERE FILEID = ?";
            count = backendJdbcTemplate.queryForObject(query, Integer.class, fileId);
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }

    @Override
    public void insertMasterPDSDetailsIntoEODMASTERPDSDATA(MasterPDSBean masterPDSBean) throws Exception {
        try {
            String query = "INSERT INTO EODMASTERPDSDATA(TXNID,FILEID,MTI,FIELDID,PDS,"
                    + "LENGTH,DATA,STATUS) VALUES (?,?,?,?,?,?,?,?)";

            Object[] param = new Object[]{
                    masterPDSBean.getTxnId(),
                    masterPDSBean.getFileId(),
                    masterPDSBean.getMti(),
                    masterPDSBean.getFieldId(),
                    masterPDSBean.getPds(),
                    masterPDSBean.getLength(),
                    masterPDSBean.getData(),
                    Configurations.EOD_PENDING_STATUS
            };
            backendJdbcTemplate.update(query, param);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * convert amount with relevant exponent
     *
     * @param hashMap
     * @param currencyCode
     * @param amount
     * @return
     */
    public synchronized String getDecimalAmountByConsideringExponent(Map<String, String> hashMap, String currencyCode, String amount) {

        String convertAmount;
        try {
            if (amount == null || amount.equals("") || hashMap == null || hashMap.equals("")) {

                return amount;
            } else {

                String exponent = hashMap.get(currencyCode);
                int i = Integer.valueOf(exponent);
                int length = amount.length() - i;

                // apply the exponent to amount
                StringBuilder sb = new StringBuilder(100);
                convertAmount = sb.append(amount.substring(0, length)).append(".")
                        .append(amount.substring(length, amount.length()))
                        .toString();

                return convertAmount.replaceFirst("^0+(?!$)", "");
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * check reversal ,ATM ,POS
     *
     * @param pds
     * @param data
     * @param processingCode
     * @return
     */
    public synchronized String txnType(String pds, String data, String processingCode) {

        String reversalPds = "0025";// 0025  and R is indicate the reversal transaction
        String reversalData = "R";
        String processingCodeAtmCashWithdraw = "01"; // 01 indicate Atm transaction
        String processingCodeAtmCashDisburse = "12";
        String processingCodePos = "00"; // 00 indicate Pos transaction
        String processingCodePurchaseReturn = "20";
        String processingCodePaymentTransaction = "28";

        try {
            if ((pds == null || pds.equals("")) && (data == null || data.equals("")) && (processingCode == null || processingCode.equals("") || processingCode.equals(processingCodePos))) {
                // if ReversalPds and ReversalData became null or empty and ProcessingCode == 00
                return Configurations.TXN_TYPE_SALE;

            } else if ((pds == null || pds.equals("")) && (data == null || data.equals("")) && (processingCode.equals(processingCodeAtmCashWithdraw) || processingCode.equals(processingCodeAtmCashDisburse))) {
                // if ReversalPds and ReversalData became null or empty and ProcessingCode == 01,12
                return Configurations.TXN_TYPE_CASH_ADVANCE;

            } else if ((pds == null || pds.equals("")) && (data == null || data.equals("")) && processingCode.equals(processingCodePurchaseReturn)) {
                // if ReversalPds and ReversalData became null or empty and ProcessingCode == 20
                return Configurations.TXN_TYPE_REFUND;

            } else if ((pds == null || pds.equals("")) && (data == null || data.equals("")) && processingCode.equals(processingCodePaymentTransaction)) {
                // if ReversalPds and ReversalData became null or empty and ProcessingCode == 28
                return Configurations.TXN_TYPE_MONEY_SEND;

            } else if (pds.equals(reversalPds) && data.equals(reversalData) && processingCode.equals(processingCodePos)) {
                // if ReversalPds==0025 and ReversalData=R and ProcessingCode == 00
                return Configurations.TXN_TYPE_REVERSAL;

            } else if (pds.equals(reversalPds) && data.equals(reversalData) && (processingCode.equals(processingCodeAtmCashWithdraw) || processingCode.equals(processingCodeAtmCashDisburse))) {
                // if ReversalPds==0025 and ReversalData=R and ProcessingCode == 01,12
                return Configurations.TXN_TYPE_REVERSAL;
            } else if (pds.equals(reversalPds) && data.equals(reversalData) && processingCode.equals(processingCodePaymentTransaction)) {
                // if ReversalPds==0025 and ReversalData=R and ProcessingCode == 28
                return Configurations.TXN_TYPE_MONEY_SEND_REVERSAL;
            } else {
                return Configurations.TXN_TYPE_SALE;
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
}
