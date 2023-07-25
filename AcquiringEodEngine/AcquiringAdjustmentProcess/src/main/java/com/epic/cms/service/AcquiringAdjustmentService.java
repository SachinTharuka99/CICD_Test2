/**
 * Author : sharuka_j
 * Date : 1/25/2023
 * Time : 7:04 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.AcquiringAdjustmentDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import static com.epic.cms.util.Configurations.merchantErrorList;

@Service
public class AcquiringAdjustmentService {
    MerchantPayBean paymentBean = null;
    MerchantDetailsBean merchantDetailsBean = null;
    ArrayList<AcqAdjustmentBean> adjentmentBeanList = null;
    CommissionTxnBean commissionTxnBean = null;
    EodTransactionBean eodTransactionBean = null;
    MerchantFeeBean merchantFeeBean = null;
    ArrayList<String> errorMerchantList = new ArrayList<String>();
    String txnID;
    boolean isOnUs;
    int totalAdjustment = 0;
    int failAdjustment = 0;
    LinkedHashMap details = new LinkedHashMap();

    @Autowired
    AcquiringAdjustmentDao acquiringAdjustmentDao;
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void acquringAdjustment(AcqAdjustmentBean acqAdjustmentBean) {
        if (!Configurations.isInterrupted) {
            try {
                paymentBean = new MerchantPayBean();
                merchantDetailsBean = new MerchantDetailsBean();
                commissionTxnBean = new CommissionTxnBean();
                eodTransactionBean = new EodTransactionBean();
                merchantFeeBean = new MerchantFeeBean();
                txnID = null;
                totalAdjustment++;
                details.put("Merchant ID :", acqAdjustmentBean.getMerchantId());

                if (acqAdjustmentBean.getAdjustType().equalsIgnoreCase(Configurations.ACQ_ADJUSTMENT_TYPE_PAYMENT)) {
                    //payment
                    details.put("Adjustment Type :", "PAYMENT");
                    this.getPaymentBean(acqAdjustmentBean);
                    paymentBean.setCrDrCommision(Configurations.CREDIT);
                    acquiringAdjustmentDao.insertToEodMerchantPayment(paymentBean, Configurations.ACQ_ADJUSTMENT_TYPE_PAYMENT);
                } else if (acqAdjustmentBean.getAdjustType().equalsIgnoreCase(Configurations.ACQ_ADJUSTMENT_TYPE_COMMISSION)) {
                    /** Commission */
                    details.put("Adjustment Type :", "COMMISSION");
                    this.getCommissionTxnBean(acqAdjustmentBean);
                    txnID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                    commissionTxnBean.setTransactionid(txnID);
                    acquiringAdjustmentDao.insertToEodMerchantComission(commissionTxnBean.getMerchantcustid(), commissionTxnBean.getCustaccountno(),
                            commissionTxnBean.getMid(), commissionTxnBean.getMeraccountno(), commissionTxnBean.getTid(), commissionTxnBean.getTransactionamount(),
                            Double.toString(commissionTxnBean.getMerchantcommssion()), commissionTxnBean.getCurrencytype(), commissionTxnBean.getCrdr(), commissionTxnBean.getTransactiondate(),
                            commissionTxnBean.getTransactiontype(), commissionTxnBean.getBatchno(), commissionTxnBean.getTransactionid(),
                            null, null, Configurations.VISA_ASSOCIATION, null,
                            null, commissionTxnBean.getCardProduct(), Integer.parseInt(Configurations.ACQ_ADJUSTMENT_TYPE_COMMISSION));
                } else if (acqAdjustmentBean.getAdjustType().equalsIgnoreCase(Configurations.ACQ_ADJUSTMENT_TYPE_FEE)) {
                    //FEE
                    details.put("Adjustment Type :", "FEE");
                    this.getMerchantFeeBean(acqAdjustmentBean);
                    acquiringAdjustmentDao.insertToEODMerchantFee(merchantFeeBean, acqAdjustmentBean.getAdjustAmount(), acqAdjustmentBean.getAdjustDate());
                } else if (acqAdjustmentBean.getAdjustType().equalsIgnoreCase(Configurations.ACQ_ADJUSTMENT_TYPE_REVERSAL)) {
                    //Reversal
                    details.put("Adjustment Type :", "TXN REVERSAL");
                    txnID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

                    //Check  if the card is onus
                    isOnUs = acquiringAdjustmentDao.getOnUsStatus(acqAdjustmentBean.getTxnId());

                    //This is done by online api call adjustment by web
                    /**if (isOnUs) {
                     details.put("Is Onus Txn :", "Yes");
                     //Reverse the Issuing Balance
                     acqBackendDbConn.insertReversalTxnIntoEODTxnTable(acqAdjustmentBean.getTxnId(), txnID);
                     }*/
                    if (!isOnUs) {
                        acquiringAdjustmentDao.insertReversalTxnIntoTxnTable(acqAdjustmentBean.getTxnId(), txnID);
                    }

                    //Reverse the acq txn
                    acquiringAdjustmentDao.insertReversalTxnIntoMerchantTxnTable(acqAdjustmentBean.getTxnId(), txnID);
                    //reverse the commission
                    acquiringAdjustmentDao.insertReversalCommission(acqAdjustmentBean.getTxnId(), txnID);
                    //reverse the payment amount
                    this.insertPaymentToBeReverse(acqAdjustmentBean);

                } else if (acqAdjustmentBean.getAdjustType().equalsIgnoreCase(Configurations.ACQ_ADJUSTMENT_TYPE_REFUND)) {
                    //Refund
                    txnID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                    details.put("Adjustment Type :", "REFUND");
                    isOnUs = this.checkForOnusStatus(acqAdjustmentBean.getCardNumber());
                    String accountNo = acquiringAdjustmentDao.getAccountNoOnCard(acqAdjustmentBean.getCardNumber());
                    String cardAssociation = acquiringAdjustmentDao.getCardAssociationFromBinRange(acqAdjustmentBean.getCardNumber().substring(0, 6));
                    acqAdjustmentBean.setCardAssociation(cardAssociation);
                    int onOffStatus = 2;
                    if (isOnUs) {
                        onOffStatus = 1;
                        details.put("Is Onus Txn :", "Yes");
                                    /*for onus card transactions only need the record in eodtransaction table because online balance corrected when transaction table record not there
                                    for match with eodtransaction table*/
                        acquiringAdjustmentDao.insertToEODTransaction(acqAdjustmentBean.getCardNumber(), accountNo, acqAdjustmentBean.getMerchantId(), null, acqAdjustmentBean.getAdjustAmount(),
                                Integer.parseInt(Configurations.BASE_CURRENCY), acqAdjustmentBean.getCrDr(), Configurations.EOD_DATE, acqAdjustmentBean.getAdjustDate(), Configurations.TXN_TYPE_REFUND, null, txnID, null, 0.0,
                                acqAdjustmentBean.getDescription(), Configurations.COUNTRY_CODE_SRILANKA, status.getONUS_STATUS(), null, null, null, Integer.parseInt(Configurations.ACQ_ADJUSTMENT_TYPE_REFUND), cardAssociation);
                    } else {
                                    /*for offus transactions need to have entries in both eodmerchanttransaction and transaction table to send outgoing file
                                    but for onus card transactions only need the record in eodtransaction table because online balance corrected when transaction table record not there
                                    for match with eodtransaction table*/
                        //Insert to transaction
                        acquiringAdjustmentDao.insertReversalTxnIntoTxnTable(acqAdjustmentBean, txnID, onOffStatus);

                    }

                    //Insert to eodmerchant
                    this.getEodTransactionBean(acqAdjustmentBean);
                    eodTransactionBean.setTxnId(txnID);
                    eodTransactionBean.setAdjustmentFlag(Configurations.ACQ_ADJUSTMENT_TYPE_REFUND);
                    eodTransactionBean.setCardAssociation(cardAssociation);
                    eodTransactionBean.setOnOffStatus(onOffStatus);
                    if (cardAssociation.equals(Configurations.CUP_ASSOCIATION)) { // for cup transactions need to set card product here to avoid conflicts with master card ranges
                        eodTransactionBean.setCardProduct(status.getPRODUCT_CODE_CUP_ALL());
                    }

                    acquiringAdjustmentDao.insertIntoEodMerchantTransaction(eodTransactionBean, status.getEOD_DONE_STATUS());

                    //Reverse payment from merchant
                    this.getPaymentBean(acqAdjustmentBean);
                    paymentBean.setPaymentCrDr(Configurations.DEBIT);
                    paymentBean.setCrDrCommision(Configurations.CREDIT);
                    acquiringAdjustmentDao.insertToEodMerchantPayment(paymentBean, Configurations.ACQ_ADJUSTMENT_TYPE_REFUND);

                }
                details.put("Adjustment Amount :", acqAdjustmentBean.getAdjustAmount());
                details.put("CRDR  :", acqAdjustmentBean.getCrDr());
                logInfo.info(logManager.logDetails(details));
                details.clear();
                //Update acq adjustment to EDON
                acquiringAdjustmentDao.updateAdjustmentToEdon(acqAdjustmentBean.getId(), txnID);

                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                logError.error(String.valueOf(e));
                if (!errorMerchantList.contains((String) acqAdjustmentBean.getMerchantId())) {
                    merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, acqAdjustmentBean.getMerchantId(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, MerchantCustomer.MERCHANTLOCATION));
                    errorMerchantList.add(acqAdjustmentBean.getMerchantId());
                }
                failAdjustment++;
                Configurations.PROCESS_FAILD_COUNT++;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    private void getPaymentBean(AcqAdjustmentBean acqAdjustmentBean) throws Exception {

        merchantDetailsBean.setMid(acqAdjustmentBean.getMerchantId());
        merchantDetailsBean = acquiringAdjustmentDao.getMerchanDetails(merchantDetailsBean);
        paymentBean.setAccountNo(merchantDetailsBean.getMerchantAccountNo());
        paymentBean.setCommAmount(0);
        paymentBean.setCrTxnAmount(0);
        paymentBean.setDrTxnAmount(0);
        paymentBean.setFeeAmount(0);
        paymentBean.setMerchantId(acqAdjustmentBean.getMerchantId());
        paymentBean.setMerchantAccNo(merchantDetailsBean.getMerchantAccountNo());
        paymentBean.setMerchantCusAccNo(merchantDetailsBean.getMerchantCusAccNo());
        paymentBean.setMerchantCusId(merchantDetailsBean.getMerchantCustomerId());
        paymentBean.setNetPayAmount(Double.parseDouble(acqAdjustmentBean.getAdjustAmount()));
        paymentBean.setPaymentAmount(0);
        paymentBean.setPaymentCrDr(acqAdjustmentBean.getCrDr());
        paymentBean.setTxncount(1);
        paymentBean.setAdjustType(1);
    }

    private void getCommissionTxnBean(AcqAdjustmentBean acqAdjustmentBean) throws Exception {

        //String bin = acqAdjustmentBean.getCardNumber().substring(0, 6);
        //String cardProduct = acqBackendDbConn.getCardProduct(bin);
        merchantDetailsBean.setMid(acqAdjustmentBean.getMerchantId());
        merchantDetailsBean = acquiringAdjustmentDao.getMerchanDetails(merchantDetailsBean);
        // int binType = acqBackendDbConn.getBinType(bin);
        commissionTxnBean.setBatchno(null);
        commissionTxnBean.setBin(null);
//        commissionTxnBean.setBintype(0);
        commissionTxnBean.setCrdr(acqAdjustmentBean.getCrDr());
        commissionTxnBean.setCurrencytype(acqAdjustmentBean.getCurruncyType());
        commissionTxnBean.setCustaccountno(merchantDetailsBean.getMerchantCusAccNo());
        commissionTxnBean.setMeraccountno(merchantDetailsBean.getMerchantAccountNo());
        commissionTxnBean.setMerchantcommssion(Double.parseDouble(acqAdjustmentBean.getAdjustAmount()));
        commissionTxnBean.setMerchantcustid(merchantDetailsBean.getMerchantCustomerId());
        commissionTxnBean.setProductid(null);
        commissionTxnBean.setSegment(null);
        commissionTxnBean.setTid(null);
        commissionTxnBean.setTransactionamount("0");
        commissionTxnBean.setTransactiondate(DateUtil.getSqldate(acqAdjustmentBean.getAdjustDate()));
//        commissionTxnBean.setTransactionid(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        commissionTxnBean.setTransactiontype(null);
        commissionTxnBean.setMid(acqAdjustmentBean.getMerchantId());
//        commissionTxnBean.setCardProduct(cardProduct);
    }

    private void getMerchantFeeBean(AcqAdjustmentBean acqAdjustmentBean) throws Exception {

        merchantDetailsBean.setMid(acqAdjustmentBean.getMerchantId());
        merchantDetailsBean = acquiringAdjustmentDao.getMerchanDetails(merchantDetailsBean);

        merchantFeeBean.setMID(acqAdjustmentBean.getMerchantId());
        merchantFeeBean.setCrORdr(acqAdjustmentBean.getCrDr());
        merchantFeeBean.setCustAccountNo(merchantDetailsBean.getMerchantCusAccNo());
        merchantFeeBean.setMerchantAccountNo(merchantDetailsBean.getMerchantAccountNo());
        merchantFeeBean.setMerchantCustomerNo(merchantDetailsBean.getMerchantCustomerId());
        merchantFeeBean.setFeeCode(acqAdjustmentBean.getTxnType());

    }

    private void insertPaymentToBeReverse(AcqAdjustmentBean acqAdjustmentBean) throws Exception {
        acquiringAdjustmentDao.getPaymentAmount(acqAdjustmentBean.getTxnId(), paymentBean);

        merchantDetailsBean.setMid(acqAdjustmentBean.getMerchantId());

        merchantDetailsBean = acquiringAdjustmentDao.getMerchanDetails(merchantDetailsBean);
        paymentBean.setAccountNo(merchantDetailsBean.getMerchantAccountNo());
        paymentBean.setCrTxnAmount(0);
        paymentBean.setDrTxnAmount(0);
        paymentBean.setFeeAmount(0);
        paymentBean.setMerchantId(acqAdjustmentBean.getMerchantId());
        paymentBean.setMerchantAccNo(merchantDetailsBean.getMerchantAccountNo());
        paymentBean.setMerchantCusAccNo(merchantDetailsBean.getMerchantCusAccNo());
        paymentBean.setMerchantCusId(merchantDetailsBean.getMerchantCustomerId());
        paymentBean.setPaymentCrDr(Configurations.DEBIT);
        paymentBean.setCrDrCommision(Configurations.CREDIT);
        paymentBean.setTxncount(1);
        paymentBean.setAdjustType(1);

        int count = acquiringAdjustmentDao.insertToEodMerchantPayment(paymentBean, Configurations.ACQ_ADJUSTMENT_TYPE_REVERSAL);

    }

    private boolean checkForOnusStatus(StringBuffer cardNumber) throws Exception {
        String sixDigitBin = cardNumber.substring(0, 6);
        String eightDigitBin = cardNumber.substring(0, 8);

        int isOnus = acquiringAdjustmentDao.getBinType(sixDigitBin, eightDigitBin);

        if (isOnus == 1) {
            return true; // our bank issued card
        } else {
            return false;
        }

    }

    private void getEodTransactionBean(AcqAdjustmentBean acqAdjustmentBean) throws Exception {
        merchantDetailsBean.setMid(acqAdjustmentBean.getMerchantId());

        merchantDetailsBean = acquiringAdjustmentDao.getMerchanDetails(merchantDetailsBean);
        eodTransactionBean.setAccountNo(acquiringAdjustmentDao.getAccountNoOnCard(acqAdjustmentBean.getCardNumber()));
        eodTransactionBean.setAuthCode(null);
        eodTransactionBean.setBatchNo(null);
        eodTransactionBean.setCardNo(acqAdjustmentBean.getCardNumber());
        eodTransactionBean.setBin(acqAdjustmentBean.getCardNumber().substring(0, 6));
        eodTransactionBean.setCountryNumCode(merchantDetailsBean.getMerchantCountry());
        eodTransactionBean.setCrDr(acqAdjustmentBean.getCrDr());
        eodTransactionBean.setCurrencyType(acqAdjustmentBean.getCurruncyType());
        eodTransactionBean.setMid(acqAdjustmentBean.getMerchantId());
        eodTransactionBean.setPosEntryMode(null);
        eodTransactionBean.setRrn(null);
        eodTransactionBean.setSettlementDate(Configurations.EOD_DATE);
        eodTransactionBean.setTid(null);
        eodTransactionBean.setToAccNo(null);
        eodTransactionBean.setTraceId(null);
        eodTransactionBean.setTxnAmount(acqAdjustmentBean.getAdjustAmount());
        eodTransactionBean.setTxnDate(acqAdjustmentBean.getAdjustDate());
        eodTransactionBean.setTxnDescription(acqAdjustmentBean.getAdjustDes());
        eodTransactionBean.setTxnType(Configurations.TXN_TYPE_REFUND);
        eodTransactionBean.setMcc(acqAdjustmentBean.getMcc());

    }
}
