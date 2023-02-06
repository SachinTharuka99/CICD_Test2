/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 1:46 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.Repository;

import com.epic.cms.dao.MerchantEasyPaymentRequestDao;
import com.epic.cms.model.bean.MerchantEasyPaymentRequestBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class MerchantEasyPaymentRequestRepo implements MerchantEasyPaymentRequestDao {
    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<MerchantEasyPaymentRequestBean> getAllEasypaymentTransactions() throws Exception {
        ArrayList<MerchantEasyPaymentRequestBean> easyPaymentTranList = new ArrayList<MerchantEasyPaymentRequestBean>();

        try {
            String query = "SELECT ET.CARDNUMBER,ET.MID,ET.TID,ET.TRANSACTIONAMOUNT AS BACKENDTXNAMOUNT,MEPP.PLANCODE,ET.TRANSACTIONID,T.RRN,ET.CURRENCYTYPE, T.TXNAMOUNT AS ONLINETXNAMOUNT,PP.DURATION,PP.INTERESTRATEORFEE,PP.MINIMUMAMOUNT,PP.MAXIMUMAMOUNT,PP.FIRSTMONTHINCLUDE, PP.FEEAPPLYINFIRSTMONTH,PP.PROCESSINGFEETYPE FROM EODTRANSACTION ET INNER JOIN MERCHANTEPP MEPP ON ET.MID=MEPP.MID INNER JOIN PAYMENTPLAN PP ON MEPP.PLANCODE=PP.PAYMENTPLANCODE INNER JOIN TRANSACTION T ON ET.TRANSACTIONID=T.TXNID WHERE ET.TID=MEPP.TID AND MEPP.REQUESTSTATUS=? AND PLANSTATUS=? AND PP.CUSTOMERORMERCHANT='M' AND ET.EODID=? ";

            query += CommonMethods.checkForErrorMerchants("ET.MID");

            easyPaymentTranList = (ArrayList<MerchantEasyPaymentRequestBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        MerchantEasyPaymentRequestBean epTranBean = new MerchantEasyPaymentRequestBean();
                        epTranBean.setCardNumber(new StringBuffer(rs.getString("CARDNUMBER")));
                        epTranBean.setMid(rs.getString("MID"));
                        epTranBean.setTid(rs.getString("TID"));
                        epTranBean.setBackendTxnAmount(new BigDecimal(rs.getString("BACKENDTXNAMOUNT")));
                        epTranBean.setPlanCode(rs.getString("PLANCODE"));
                        epTranBean.setTxnId(rs.getString("TRANSACTIONID"));
                        epTranBean.setRrn(rs.getString("RRN"));
                        epTranBean.setCurrencyNumCode(rs.getString("CURRENCYTYPE"));
                        epTranBean.setOnlineTxnAmount(new BigDecimal(rs.getString("ONLINETXNAMOUNT")));
                        epTranBean.setDuration(rs.getInt("DURATION"));
                        epTranBean.setInterestRateOrFee(rs.getDouble("INTERESTRATEORFEE"));
                        epTranBean.setMinimumAmount(rs.getDouble("MINIMUMAMOUNT"));
                        epTranBean.setMaximumAmount(rs.getDouble("MAXIMUMAMOUNT"));
                        epTranBean.setFirstMonthInclude(rs.getString("FIRSTMONTHINCLUDE"));
                        epTranBean.setFeeApplyInFirstMonth(rs.getString("FEEAPPLYINFIRSTMONTH"));
                        epTranBean.setProcessingFeeType(rs.getString("PROCESSINGFEETYPE"));
                        return epTranBean;
                    }),
                    statusList.getCOMMON_REQUEST_ACCEPTED(), //RQAC
                    statusList.getACTIVE_STATUS(), //ACT
                    Configurations.EOD_ID
            );
        } catch (Exception e) {
            errorLogger.error("Get All Easy Payment Transactions Error", e);
            throw e;
        }
        return easyPaymentTranList;
    }

    @Override
    public int insertEasyPaymentRequest(MerchantEasyPaymentRequestBean bean) throws Exception {
        String query;
        int count = 0;
        double value15 = 0;
        double value16 = 0;

        try {
            if (bean.getProcessingFeeType() != null && !bean.getProcessingFeeType().equals("")) {
                if (bean.getProcessingFeeType().equals("FEE") && bean.getFeeApplyInFirstMonth().equals("YES")) {
                    value15 = bean.getInterestRateOrFee();
                    value16 = bean.getInterestRateOrFee();

                } else if (bean.getProcessingFeeType().equals("FEE") && bean.getFeeApplyInFirstMonth().equals("NO")) {
                    double feePortion = bean.getInterestRateOrFee() / bean.getDuration();
                    value15 = feePortion;
                    value16 = bean.getInterestRateOrFee();

                } else if (bean.getProcessingFeeType().equals("INT")) {
                    BigDecimal intPortion = (bean.getBackendTxnAmount().multiply(BigDecimal.valueOf(bean.getInterestRateOrFee()))).divide(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(bean.getDuration())), MathContext.DECIMAL32);
                    BigDecimal totalInt = bean.getBackendTxnAmount().multiply(BigDecimal.valueOf(bean.getInterestRateOrFee())).divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);
                    value15 = Double.parseDouble(intPortion.setScale(2, RoundingMode.DOWN).toString());
                    value16 = Double.parseDouble(totalInt.setScale(2, RoundingMode.DOWN).toString());

                } else {
                    value15 = Double.parseDouble("0");
                    value16 = Double.parseDouble("0");
                }
            } else {
                value15 = Double.parseDouble("0");
                value16 = Double.parseDouble("0");
            }

            query = "INSERT INTO EASYPAYMENTREQUEST (CARDNUMBER,TXNAMOUNT,STATUS,LASTUPDATEDUSER,PAYMENTPLAN,REQUESTEDUSER,INSTALLMENTAMOUNT,TXNID,RRN,REMARKS,TXNAMOUNTONLINE,CURRENCYNUMCODE,FIRSTINSTALLMENTAMOUNT,REMAININGCOUNT,INTERESTORFEEAMOUNT,INTERESTORFEETOTALAMOUNT) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            count = backendJdbcTemplate.update(query,
                    bean.getCardNumber().toString(),
                    bean.getBackendTxnAmount().setScale(2, RoundingMode.DOWN).toString(),
                    statusList.getCOMMON_REQUEST_INITIATE(),
                    Configurations.EOD_USER,
                    bean.getPlanCode(),
                    Configurations.EOD_USER,
                    bean.getNextInstallmentAmount().setScale(2, RoundingMode.DOWN).toString(),
                    bean.getTxnId(),
                    bean.getRrn(),
                    "",
                    bean.getOnlineTxnAmount().toString(),
                    Integer.parseInt(bean.getCurrencyNumCode()),
                    bean.getFirstInstallmentAmount(),
                    bean.getDuration(),
                    value15,
                    value16
            );
        } catch (Exception e) {
            errorLogger.error("Insert Easy Payment Request Error", e);
            throw e;
        }
        return count;
    }

    @Override
    public int updateEodTransactionForEasyPaymentStatus(String txnid) throws Exception {
        String query;
        int count = 0;

        try {
            query = "UPDATE EODTRANSACTION SET EPSTATUS=1 WHERE TRANSACTIONID=?";

            count = backendJdbcTemplate.update(query, txnid);

        } catch (Exception e) {
            errorLogger.error("Update Eod Transaction For Easy Payment Status Error", e);
            throw e;
        }
        return count;
    }

    @Override
    public int updateEodMerchantTransactionForEasyPaymentStatus(String txnid) throws Exception {
        String query;
        int count = 0;

        try {
            query = "UPDATE EODMERCHANTTRANSACTION SET EPSTATUS=1 WHERE TRANSACTIONID=?";

            count = backendJdbcTemplate.update(query, txnid);

        } catch (Exception e) {
            errorLogger.error("Update Eod Merchant Transaction For EasyPayment Status Error", e);
            throw e;
        }
        return count;
    }

    @Override
    public int insertEasyPaymentRejectRequest(MerchantEasyPaymentRequestBean bean) throws Exception {
        String query;
        int count = 0;
        double value15 = 0;
        double value16 = 0;
        String value17 = "";
        String value18 = "";

        try {
            if (bean.getProcessingFeeType() != null && !bean.getProcessingFeeType().equals("")) {
                if (bean.getProcessingFeeType().equals("FEE") && bean.getFeeApplyInFirstMonth().equals("YES")) {
                    value15 = bean.getInterestRateOrFee();
                    value16 = bean.getInterestRateOrFee();

                } else if (bean.getProcessingFeeType().equals("FEE") && bean.getFeeApplyInFirstMonth().equals("NO")) {
                    double feePortion = bean.getInterestRateOrFee() / bean.getDuration();
                    value15 = feePortion;
                    value16 = bean.getInterestRateOrFee();

                } else if (bean.getProcessingFeeType().equals("INT")) {
                    BigDecimal intPortion = (bean.getBackendTxnAmount().multiply(BigDecimal.valueOf(bean.getInterestRateOrFee()))).divide(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(bean.getDuration())), MathContext.DECIMAL32);
                    BigDecimal totalInt = bean.getBackendTxnAmount().multiply(BigDecimal.valueOf(bean.getInterestRateOrFee())).divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);
                    intPortion.setScale(2, RoundingMode.DOWN).toString();
                    totalInt.setScale(2, RoundingMode.DOWN).toString();

                } else {
                    value15 = Double.parseDouble("0");
                    value16 = Double.parseDouble("0");
                }
            } else {
                value15 = Double.parseDouble("0");
                value16 = Double.parseDouble("0");
            }
            value17 = "Txn Amount Min/Max Condition Failed";
            value18 = Configurations.EOD_USER;

            query = "INSERT INTO EASYPAYMENTREQUEST (CARDNUMBER,TXNAMOUNT,STATUS,LASTUPDATEDUSER,PAYMENTPLAN,REQUESTEDUSER,INSTALLMENTAMOUNT,TXNID,RRN,REMARKS,TXNAMOUNTONLINE,CURRENCYNUMCODE,FIRSTINSTALLMENTAMOUNT,REMAININGCOUNT,INTERESTORFEEAMOUNT,INTERESTORFEETOTALAMOUNT,REJECTREMARK,APPROVEDUSER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            count = backendJdbcTemplate.update(query,
                    bean.getCardNumber().toString(),
                    bean.getBackendTxnAmount().setScale(2, RoundingMode.DOWN).toString(),
                    statusList.getCOMMON_REQUEST_REJECT(),
                    Configurations.EOD_USER,
                    bean.getPlanCode(),
                    Configurations.EOD_USER,
                    bean.getNextInstallmentAmount().setScale(2, RoundingMode.DOWN).toString(),
                    bean.getTxnId(),
                    bean.getRrn(),
                    "",
                    bean.getOnlineTxnAmount().toString(),
                    Integer.parseInt(bean.getCurrencyNumCode()),
                    bean.getFirstInstallmentAmount().setScale(2, RoundingMode.DOWN).toString(),
                    bean.getDuration()
            );

        } catch (Exception e) {
            errorLogger.error("Insert Easy Payment Reject Request Error", e);
            throw e;
        }
        return count;
    }
}
