/**
 * Author : lahiru_p
 * Date : 1/30/2023
 * Time : 9:51 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.CommissionProfileBean;
import com.epic.cms.model.bean.CommissionTxnBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.repository.MerchantCommissionCalculationRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.MerchantCustomer;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Queue;

@Service
public class MerchantCommissionCalculationService {

    @Autowired
    MerchantCommissionCalculationRepo commissionCalculationRepo;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_100")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void calculateMerchantCommission(MerchantLocationBean merchantLocationBean) throws Exception{
        Boolean isMerchantCommissionYes;
        try {
            isMerchantCommissionYes = commissionCalculationRepo.getCustomerCommStatus(merchantLocationBean.getMerchantCustomerNo());
            if (Boolean.TRUE.equals(isMerchantCommissionYes)) {
                merchantLocationBean.setComisionProfile(commissionCalculationRepo.getCommissionProfile(merchantLocationBean.getMerchantCustomerNo()));
            }

            getMerchantWiseCommision(merchantLocationBean.getComisionProfile(), merchantLocationBean.getMerchantId());
            Configurations.PROCESS_SUCCESS_COUNT++;
        }catch (Exception ex){
            logError.error("Commission calculation process failed for merchantId:" + merchantLocationBean.getMerchantId(), ex);
            Configurations.merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, merchantLocationBean.getMerchantId(), ex.getMessage(), Configurations.PROCESS_ID_COMMISSION_CALCULATION, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, MerchantCustomer.MERCHANTLOCATION));
            Configurations.PROCESS_FAILD_COUNT++;
        }
    }

    private synchronized void getMerchantWiseCommision(String commissionProfile, String merchantId) throws Exception {
        double commissionValue;
        Queue<CommissionProfileBean> commissionProfileListQueue;
        ArrayList<CommissionTxnBean> commissionTxnList;
        String calMethod = commissionCalculationRepo.getCalMethod(commissionProfile);
        try {
            //calculate commission transaction wise
            if (calMethod.equalsIgnoreCase(statusVarList.getCOMMISSION_TRANSACTION_WISE())) {

                commissionProfileListQueue = commissionCalculationRepo.getAllCommCombination(commissionProfile, Configurations.COMMISSION_TRANSACTION_TABLE, Configurations.COMMISSION_SEGMENT_TRANSACTION, Configurations.COMMISSION_DEFAULT_TXN);

                for (CommissionProfileBean commissionProfileBean : commissionProfileListQueue) {

                    commissionTxnList = commissionCalculationRepo.getTransactionForCommission(merchantId, commissionProfileBean.getBinType(), commissionProfileBean.getCardProduct(), commissionProfileBean.getSegment(), statusVarList.getCOMMISSION_TRANSACTION_WISE(), Configurations.TRANSACTION_SEGMENT_TXNTYPE, Configurations.COMMISSION_DEFAULT_TXN);

                    for (CommissionTxnBean commissionTxnBean : commissionTxnList) {
                        commissionValue = 0;
                        BigDecimal amount = new BigDecimal(commissionTxnBean.getTransactionamount());
                        BigDecimal flatValue = new BigDecimal(commissionProfileBean.getFlatValue());
                        BigDecimal calCommission = amount.multiply(new BigDecimal(commissionProfileBean.getPercentage())).divide(new BigDecimal("100"), MathContext.DECIMAL32);
                        commissionValue = getCommissionValue(flatValue, calCommission, commissionProfileBean.getCombination(),
                                commissionTxnBean, commissionProfileBean.getPercentage());
                        //commissionValue = Double.parseDouble(commonMethods.ValuesRoundup(commissionValue));
                        commissionTxnBean.setMerchantcommssion(commissionValue);
                        commissionCalculationRepo.getMerchantDetails(commissionTxnBean);
                        commissionCalculationRepo.insertToEodMerchantComission(commissionTxnBean.getMerchantcustid(), commissionTxnBean.getCustaccountno(),
                                merchantId, commissionTxnBean.getMeraccountno(), commissionTxnBean.getTid(), commissionTxnBean.getTransactionamount(),
                                commissionValue, commissionTxnBean.getCurrencytype(), commissionTxnBean.getCrdr(), commissionTxnBean.getTransactiondate(),
                                commissionTxnBean.getTransactiontype(), commissionTxnBean.getBatchno(), commissionTxnBean.getTransactionid(),
                                commissionProfileBean.getBinType(), calMethod, commissionTxnBean.getCardassociation(),
                                commissionProfileBean.getCardProduct(), commissionProfileBean.getSegment(), commissionTxnBean.getCardProduct(),
                                commissionTxnBean.getCalculatedMdrPercentage(), commissionTxnBean.getCalculatedMdrFlatAmount());
                        commissionCalculationRepo.updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusVarList.getEOD_DONE_STATUS());

                        LinkedHashMap details = new LinkedHashMap();
                        details.put("Merchant ID :", merchantId);
                        details.put("CalMethod :", calMethod);
                        details.put("Transaction Type :", commissionTxnBean.getTransactiontype());
                        details.put("Transaction Amount :", commissionTxnBean.getTransactionamount());
                        details.put("Transaction commission :", commissionValue);
                        details.put("Transaction commission segment :", commissionProfileBean.getSegment());
                        logInfo.info(logManager.logDetails(details));

                    }
                }
                //calculate commission MCC wise
            } else if (calMethod.equalsIgnoreCase(statusVarList.getCOMMISSION_MCC_WISE())) {

                commissionProfileListQueue = commissionCalculationRepo.getAllCommCombination(commissionProfile, Configurations.COMMISSION_MCC_TABLE, Configurations.COMMISSION_SEGMENT_MCC, Configurations.COMMISSION_DEFAULT_MCC);

                for (CommissionProfileBean commissionProfileBean : commissionProfileListQueue) {

                    commissionTxnList = commissionCalculationRepo.getTransactionForCommission(merchantId, commissionProfileBean.getBinType(), commissionProfileBean.getCardProduct(), commissionProfileBean.getSegment(), statusVarList.getCOMMISSION_MCC_WISE(), Configurations.TRANSACTION_SEGMENT_MCC, Configurations.COMMISSION_DEFAULT_MCC);

                    for (CommissionTxnBean commissionTxnBean : commissionTxnList) {
                        commissionValue = 0;
                        BigDecimal amount = new BigDecimal(commissionTxnBean.getTransactionamount());
                        BigDecimal flatValue = new BigDecimal(commissionProfileBean.getFlatValue());
                        BigDecimal calCommission = amount.multiply(new BigDecimal(commissionProfileBean.getPercentage())).divide(new BigDecimal("100"), MathContext.DECIMAL32);
                        commissionValue = getCommissionValue(flatValue, calCommission, commissionProfileBean.getCombination(),
                                commissionTxnBean, commissionProfileBean.getPercentage());
                        //commissionValue = Double.parseDouble(commonMethods.ValuesRoundup(commissionValue));
                        commissionTxnBean.setMerchantcommssion(commissionValue);
                        commissionCalculationRepo.getMerchantDetails(commissionTxnBean);
                        commissionCalculationRepo.insertToEodMerchantComission(commissionTxnBean.getMerchantcustid(), commissionTxnBean.getCustaccountno(),
                                merchantId, commissionTxnBean.getMeraccountno(), commissionTxnBean.getTid(), commissionTxnBean.getTransactionamount(),
                                commissionValue, commissionTxnBean.getCurrencytype(), commissionTxnBean.getCrdr(), commissionTxnBean.getTransactiondate(),
                                commissionTxnBean.getTransactiontype(), commissionTxnBean.getBatchno(), commissionTxnBean.getTransactionid(),
                                commissionProfileBean.getBinType(), calMethod, commissionTxnBean.getCardassociation(),
                                commissionProfileBean.getCardProduct(), commissionProfileBean.getSegment(), commissionTxnBean.getCardProduct(),
                                commissionTxnBean.getCalculatedMdrPercentage(), commissionTxnBean.getCalculatedMdrFlatAmount());
                        commissionCalculationRepo.updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusVarList.getEOD_DONE_STATUS());

                        LinkedHashMap details = new LinkedHashMap();
                        details.put("Merchant ID :", merchantId);
                        details.put("CalMethod :", calMethod);
                        details.put("Transaction Type :", commissionTxnBean.getTransactiontype());
                        details.put("Transaction Amount :", commissionTxnBean.getTransactionamount());
                        details.put("Transaction commission :", commissionValue);
                        details.put("Transaction commission segment :", commissionProfileBean.getSegment());
                        logInfo.info(logManager.logDetails(details));
                    }
                }

                //calculate commission Volume wise
            } else if (calMethod.equalsIgnoreCase(statusVarList.getCOMMISSION_VOLUME_WISE())) {
                double totalTxnAmount = 0;
                String productCode = "";
                String binType = "";
                String volumeId = "";
                commissionTxnList = null;
                CommissionProfileBean preCommissionProfileBean;
                //Get all the combinations from commission volume
                commissionProfileListQueue = commissionCalculationRepo.getAllCommCombinationForVolume(commissionProfile, Configurations.COMMISSION_VOLUME_TABLE, Configurations.COMMISSION_SEGMENT_VOLUME, Configurations.COMMISSION_DEFAULT_VOLUME);
                for (CommissionProfileBean commissionProfileBean : commissionProfileListQueue) {

                    // Process commission calculation for previous txnList
                    if (!(binType.equalsIgnoreCase(commissionProfileBean.getBinType()))) {//If new bin loop start

//                        if (!(productCode.equalsIgnoreCase(commissionProfileBean.getCardProduct()))) {//If new card product start within same bin
                        if (commissionTxnList != null && commissionTxnList.size() > 0) {
                            volumeId = commissionCalculationRepo.getVolumeId(totalTxnAmount);
                            volumeId = (volumeId == null) ? volumeId = Configurations.COMMISSION_DEFAULT_VOLUME : volumeId;
                            preCommissionProfileBean = commissionCalculationRepo.getCommissionProfile(commissionProfile, binType, productCode, volumeId);

                            for (CommissionTxnBean commissionTxnBean : commissionTxnList) {
                                commissionValue = 0;
                                BigDecimal amount = new BigDecimal(commissionTxnBean.getTransactionamount());
                                BigDecimal flatValue = new BigDecimal(preCommissionProfileBean.getFlatValue());
                                BigDecimal calCommission = amount.multiply(new BigDecimal(preCommissionProfileBean.getPercentage())).divide(new BigDecimal("100"), MathContext.DECIMAL32);
                                commissionValue = getCommissionValue(flatValue, calCommission, preCommissionProfileBean.getCombination(),
                                        commissionTxnBean, preCommissionProfileBean.getPercentage());
                                //commissionValue = Double.parseDouble(commonMethods.ValuesRoundup(commissionValue));
                                commissionTxnBean.setMerchantcommssion(commissionValue);
                                commissionCalculationRepo.getMerchantDetails(commissionTxnBean);
                                commissionCalculationRepo.insertToEodMerchantComission(commissionTxnBean.getMerchantcustid(), commissionTxnBean.getCustaccountno(),
                                        merchantId, commissionTxnBean.getMeraccountno(), commissionTxnBean.getTid(),
                                        commissionTxnBean.getTransactionamount(), commissionValue, commissionTxnBean.getCurrencytype(),
                                        commissionTxnBean.getCrdr(), commissionTxnBean.getTransactiondate(),
                                        commissionTxnBean.getTransactiontype(), commissionTxnBean.getBatchno(), commissionTxnBean.getTransactionid(),
                                        binType, calMethod, commissionTxnBean.getCardassociation(), productCode, preCommissionProfileBean.getVolumeId(),
                                        commissionTxnBean.getCardProduct(), commissionTxnBean.getCalculatedMdrPercentage(), commissionTxnBean.getCalculatedMdrFlatAmount());
                                commissionCalculationRepo.updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusVarList.getEOD_DONE_STATUS());

                                LinkedHashMap details = new LinkedHashMap();
                                details.put("Merchant ID :", merchantId);
                                details.put("CalMethod :", calMethod);
                                details.put("Transaction Type :", commissionTxnBean.getTransactiontype());
                                details.put("Transaction Amount :", commissionTxnBean.getTransactionamount());
                                details.put("Transaction commission :", commissionValue);
                                logInfo.info(logManager.logDetails(details));
                            }

                        }
                        //set total txn amount to zero for next loop
                        totalTxnAmount = 0;
                        commissionTxnList = new ArrayList<CommissionTxnBean>();//new txn list for new cardProduct loop
                        productCode = commissionProfileBean.getCardProduct();
                        binType = commissionProfileBean.getBinType();
                    } else {
                        if (!(productCode.equalsIgnoreCase(commissionProfileBean.getCardProduct()))) {//If new card product start within same bin

                            if (commissionTxnList != null && commissionTxnList.size() > 0) {
                                volumeId = commissionCalculationRepo.getVolumeId(totalTxnAmount);
                                volumeId = (volumeId == null) ? volumeId = "VDEF" : volumeId;
                                preCommissionProfileBean = commissionCalculationRepo.getCommissionProfile(commissionProfile, binType, productCode, volumeId);

                                for (CommissionTxnBean commissionTxnBean : commissionTxnList) {
                                    commissionValue = 0;
                                    BigDecimal amount = new BigDecimal(commissionTxnBean.getTransactionamount());
                                    BigDecimal flatValue = new BigDecimal(preCommissionProfileBean.getFlatValue());
                                    BigDecimal calCommission = amount.multiply(new BigDecimal(preCommissionProfileBean.getPercentage())).divide(new BigDecimal("100"), MathContext.DECIMAL32);
                                    commissionValue = getCommissionValue(flatValue, calCommission, preCommissionProfileBean.getCombination(),
                                            commissionTxnBean, preCommissionProfileBean.getPercentage());
                                    //commissionValue = Double.parseDouble(commonMethods.ValuesRoundup(commissionValue));
                                    commissionTxnBean.setMerchantcommssion(commissionValue);
                                    commissionCalculationRepo.getMerchantDetails(commissionTxnBean);
                                    commissionCalculationRepo.insertToEodMerchantComission(commissionTxnBean.getMerchantcustid(),
                                            commissionTxnBean.getCustaccountno(), merchantId, commissionTxnBean.getMeraccountno(),
                                            commissionTxnBean.getTid(), commissionTxnBean.getTransactionamount(), commissionValue,
                                            commissionTxnBean.getCurrencytype(), commissionTxnBean.getCrdr(), commissionTxnBean.getTransactiondate(),
                                            commissionTxnBean.getTransactiontype(), commissionTxnBean.getBatchno(), commissionTxnBean.getTransactionid(), binType,
                                            calMethod, commissionTxnBean.getCardassociation(), productCode, preCommissionProfileBean.getVolumeId(),
                                            commissionTxnBean.getCardProduct(), commissionTxnBean.getCalculatedMdrPercentage(), commissionTxnBean.getCalculatedMdrFlatAmount());
                                    commissionCalculationRepo.updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusVarList.getEOD_DONE_STATUS());

                                    LinkedHashMap details = new LinkedHashMap();
                                    details.put("Merchant ID :", merchantId);
                                    details.put("CalMethod :", calMethod);
                                    details.put("Transaction Type :", commissionTxnBean.getTransactiontype());
                                    details.put("Transaction Amount :", commissionTxnBean.getTransactionamount());
                                    details.put("Transaction commission :", commissionValue);
                                    logInfo.info(logManager.logDetails(details));
                                }

                            }
                            //set total txn amount to zero for next loop
                            totalTxnAmount = 0;
                            commissionTxnList = new ArrayList<CommissionTxnBean>();//new txn list for new cardProduct loop
                            productCode = commissionProfileBean.getCardProduct();
                        }
                    }
                    //Txn list for new commission profiles
                    commissionTxnList = commissionCalculationRepo.getTransactionForCommissionVolumeWise(merchantId, commissionProfileBean.getBinType(), commissionProfileBean.getCardProduct(), calMethod, commissionTxnList);
                    totalTxnAmount = 0;
                    for (CommissionTxnBean commissionTxnBean : commissionTxnList) {
                        totalTxnAmount += Double.parseDouble(commissionTxnBean.getTransactionamount());
                    }
                }
                if (commissionTxnList != null && commissionTxnList.size() > 0) {
                    volumeId = commissionCalculationRepo.getVolumeId(totalTxnAmount);
                    volumeId = (volumeId == null) ? volumeId = "VDEF" : volumeId;
                    preCommissionProfileBean = commissionCalculationRepo.getCommissionProfile(commissionProfile, binType, productCode, volumeId);

                    for (CommissionTxnBean commissionTxnBean : commissionTxnList) {
                        commissionValue = 0;
                        BigDecimal amount = new BigDecimal(commissionTxnBean.getTransactionamount());
                        BigDecimal flatValue = new BigDecimal(preCommissionProfileBean.getFlatValue());
                        BigDecimal calCommission = amount.multiply(new BigDecimal(preCommissionProfileBean.getPercentage())).divide(new BigDecimal("100"), MathContext.DECIMAL32);
                        commissionValue = getCommissionValue(flatValue, calCommission, preCommissionProfileBean.getCombination(),
                                commissionTxnBean, preCommissionProfileBean.getPercentage());
                        //commissionValue = Double.parseDouble(commonMethods.ValuesRoundup(commissionValue));
                        commissionTxnBean.setMerchantcommssion(commissionValue);
                        commissionCalculationRepo.getMerchantDetails(commissionTxnBean);

                        commissionCalculationRepo.insertToEodMerchantComission(commissionTxnBean.getMerchantcustid(),
                                commissionTxnBean.getCustaccountno(), merchantId, commissionTxnBean.getMeraccountno(),
                                commissionTxnBean.getTid(), commissionTxnBean.getTransactionamount(), commissionValue, commissionTxnBean.getCurrencytype(),
                                commissionTxnBean.getCrdr(), commissionTxnBean.getTransactiondate(), commissionTxnBean.getTransactiontype(), commissionTxnBean.getBatchno(),
                                commissionTxnBean.getTransactionid(), binType, calMethod, commissionTxnBean.getCardassociation(), productCode,
                                preCommissionProfileBean.getVolumeId(), commissionTxnBean.getCardProduct(), commissionTxnBean.getCalculatedMdrPercentage(), commissionTxnBean.getCalculatedMdrFlatAmount());
                        commissionCalculationRepo.updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusVarList.getEOD_DONE_STATUS());

                        LinkedHashMap details = new LinkedHashMap();
                        details.put("Merchant ID :", merchantId);
                        details.put("CalMethod :", calMethod);
                        details.put("Transaction Type :", commissionTxnBean.getTransactiontype());
                        details.put("Transaction Amount :", commissionTxnBean.getTransactionamount());
                        details.put("Transaction commission :", commissionValue);
                        logInfo.info(logManager.logDetails(details));
                    }

                }
            }
        } catch (Exception e) {
            logError.error("Exception occured when get MerchantWiseCommision ",e);
            throw e;
        }
    }

    private synchronized double getCommissionValue(BigDecimal flatValue, BigDecimal calCommission, String combination,
                                      CommissionTxnBean commissionTxnBean, double commissionPercentage) {
        BigDecimal commissionValue = new BigDecimal("0");
        try {
            if (combination.equals(statusVarList.getCOMISSION_COMBINATION_MIN())) {
                if (flatValue.doubleValue() >= calCommission.doubleValue()) {
                    //set calculated percentage as commission
                    commissionValue = calCommission.setScale(2, RoundingMode.DOWN);
                    commissionTxnBean.setCalculatedMdrPercentage(String.valueOf(commissionPercentage));
                } else {
                    //set flat amount as commission
                    commissionValue = flatValue.setScale(2, RoundingMode.DOWN);
                    commissionTxnBean.setCalculatedMdrFlatAmount(commissionValue.toString());
                }
            } else if (combination.equals(statusVarList.getCOMISSION_COMBINATION_MAX())) {
                if (flatValue.doubleValue() >= calCommission.doubleValue()) {
                    //set flat amount as commission
                    commissionValue = flatValue.setScale(2, RoundingMode.DOWN);
                    commissionTxnBean.setCalculatedMdrFlatAmount(commissionValue.toString());
                } else {
                    //set calculated percentage as commission
                    commissionValue = calCommission.setScale(2, RoundingMode.DOWN);
                    commissionTxnBean.setCalculatedMdrPercentage(String.valueOf(commissionPercentage));
                }
            } else if (combination.equals(statusVarList.getCOMISSION_COMBINATION_ADD())) {
                //set sum of calculated percentage and flat amount as commission
                commissionTxnBean.setCalculatedMdrPercentage(String.valueOf(commissionPercentage));
                commissionTxnBean.setCalculatedMdrFlatAmount(flatValue.setScale(2, RoundingMode.DOWN).toString());
                commissionValue = flatValue.add(calCommission).setScale(2, RoundingMode.DOWN);
            }
        } catch (Exception e) {
            logError.error("Exception in getCommissionValue ", e);
            throw e;
        }
        return commissionValue.doubleValue();
    }
}
