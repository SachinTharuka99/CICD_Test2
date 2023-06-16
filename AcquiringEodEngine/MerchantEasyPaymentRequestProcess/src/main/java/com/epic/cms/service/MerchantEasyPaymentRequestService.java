/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 1:48 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.Repository.MerchantEasyPaymentRequestRepo;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantEasyPaymentRequestBean;
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
import java.util.LinkedHashMap;


@Service
public class MerchantEasyPaymentRequestService {
    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Autowired
    MerchantEasyPaymentRequestRepo merchantEasyPaymentRequestRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void merchantEasyPayment(MerchantEasyPaymentRequestBean tranBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            int failedTxn = 0;
            int rejectedTxn = 0;

            try {
                details.put("Transaction ID ", tranBean.getTxnId() + " Considering for EasyPayment Request");
                if (tranBean.getBackendTxnAmount().doubleValue() <= tranBean.getMaximumAmount() && tranBean.getBackendTxnAmount().doubleValue() >= tranBean.getMinimumAmount()) {
                    tranBean.setFirstInstallmentAmount(getFirstInstallmentAmount(tranBean.getBackendTxnAmount(), tranBean.getDuration(), tranBean.getInterestRateOrFee(), tranBean.getProcessingFeeType(), tranBean.getFeeApplyInFirstMonth()));
                    tranBean.setNextInstallmentAmount(getNextInstallmentAmount(tranBean.getBackendTxnAmount(), tranBean.getDuration(), tranBean.getInterestRateOrFee(), tranBean.getProcessingFeeType(), tranBean.getFeeApplyInFirstMonth()));
                    merchantEasyPaymentRequestRepo.insertEasyPaymentRequest(tranBean); // insert easy payment request to EASYPAYMENTREQUEST table (RQIN)

                    //update eodtransaction and eodmerchanttransaction EPSTATUS column value
                    merchantEasyPaymentRequestRepo.updateEodTransactionForEasyPaymentStatus(tranBean.getTxnId());
                    merchantEasyPaymentRequestRepo.updateEodMerchantTransactionForEasyPaymentStatus(tranBean.getTxnId());
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } else {
                    rejectedTxn++;
                    Configurations.PROCESS_FAILD_COUNT++;
                    logError.error("easypayment transaction min-max conditions failed for txnid: " + tranBean.getTxnId());
                    tranBean.setFirstInstallmentAmount(getFirstInstallmentAmount(tranBean.getBackendTxnAmount(), tranBean.getDuration(), tranBean.getInterestRateOrFee(), tranBean.getProcessingFeeType(), tranBean.getFeeApplyInFirstMonth()));
                    tranBean.setNextInstallmentAmount(getNextInstallmentAmount(tranBean.getBackendTxnAmount(), tranBean.getDuration(), tranBean.getInterestRateOrFee(), tranBean.getProcessingFeeType(), tranBean.getFeeApplyInFirstMonth()));
                    merchantEasyPaymentRequestRepo.insertEasyPaymentRejectRequest(tranBean); // insert request as reject (RQRJ)
                }
            } catch (Exception e) {
                failedTxn++;
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, tranBean.getMid(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, MerchantCustomer.MERCHANTLOCATION));
                details.put("Process Status", "Failed");
                logError.error(Configurations.RUNNING_PROCESS_DESCRIPTION + " failed for txnid " + tranBean.getTxnId(), e);
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    public BigDecimal getFirstInstallmentAmount(BigDecimal txnAmount, int duration, double interestRate, String processingFeeType, String applyprocessingfee) {
        BigDecimal installmentAmount = new BigDecimal(0.0);
        if (processingFeeType.equals("INT")) {
            //return Math.floor((((((txnamout * interestrate) / 100) + txnamout)) / duration)
            installmentAmount = txnAmount.add(txnAmount.multiply(BigDecimal.valueOf(interestRate)).divide(BigDecimal.valueOf(100.0), MathContext.DECIMAL32)).divide(BigDecimal.valueOf(duration), MathContext.DECIMAL32);

        } else if (processingFeeType.equals("FEE") && applyprocessingfee.equals("YES")) {
            //return Math.floor((((txnamout / duration)) + interestrate)
            installmentAmount = txnAmount.divide(BigDecimal.valueOf(duration), MathContext.DECIMAL32).add(BigDecimal.valueOf(interestRate));
        } else if (processingFeeType.equals("FEE") && applyprocessingfee.equals("NO")) {
            //return Math.floor((((txnamout + interestrate)) / duration)
            installmentAmount = txnAmount.add(BigDecimal.valueOf(interestRate)).divide(BigDecimal.valueOf(duration), MathContext.DECIMAL32);
        }
        return installmentAmount;
    }

    public BigDecimal getNextInstallmentAmount(BigDecimal txnAmount, int duration, double interestRate, String processingFeeType, String applyProcessingFee) {
        BigDecimal installmentAmount = new BigDecimal(0.0);
        if (processingFeeType.equals("INT")) {
            //return Math.floor((((((txnamout * interestrate) / 100) + txnamout)) / duration)
            installmentAmount = txnAmount.add(txnAmount.multiply(BigDecimal.valueOf(interestRate)).divide(BigDecimal.valueOf(100.0)), MathContext.DECIMAL32).divide(BigDecimal.valueOf(duration), MathContext.DECIMAL32);

        } else if (processingFeeType.equals("FEE") && applyProcessingFee.equals("YES")) {
            //return Math.floor((((txnamout / duration)))
            installmentAmount = txnAmount.divide(BigDecimal.valueOf(duration), MathContext.DECIMAL32);
        } else if (processingFeeType.equals("FEE") && applyProcessingFee.equals("NO")) {
            //return Math.floor((((txnamout + interestrate)) / duration)
            installmentAmount = txnAmount.add(BigDecimal.valueOf(interestRate)).divide(BigDecimal.valueOf(duration), MathContext.DECIMAL32);
        }
        return installmentAmount;
    }
}
