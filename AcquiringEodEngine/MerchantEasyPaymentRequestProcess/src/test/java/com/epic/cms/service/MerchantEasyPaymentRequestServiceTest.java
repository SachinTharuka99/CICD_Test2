package com.epic.cms.service;

import com.epic.cms.Repository.MerchantEasyPaymentRequestRepo;
import com.epic.cms.model.bean.MerchantEasyPaymentRequestBean;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MerchantEasyPaymentRequestServiceTest {

    private MerchantEasyPaymentRequestService merchantEasyPaymentRequestServiceUnderTest;

    @BeforeEach
    void setUp() {
        merchantEasyPaymentRequestServiceUnderTest = new MerchantEasyPaymentRequestService();
        merchantEasyPaymentRequestServiceUnderTest.statusList = mock(StatusVarList.class);
        merchantEasyPaymentRequestServiceUnderTest.logManager = mock(LogManager.class);
        merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo = mock(
                MerchantEasyPaymentRequestRepo.class);
    }

    @Test
    void testMerchantEasyPayment() throws Exception {
        // Setup
        final MerchantEasyPaymentRequestBean tranBean = new MerchantEasyPaymentRequestBean();
        tranBean.setCardNumber(new StringBuffer("4380431766518012"));
        tranBean.setBackendTxnAmount(new BigDecimal("0.00"));
        tranBean.setOnlineTxnAmount(new BigDecimal("0.00"));
        tranBean.setPlanCode("planCode");
        tranBean.setTxnId("txnId");
        tranBean.setRrn("rrn");
        tranBean.setDuration(0);
        tranBean.setInterestRateOrFee(0.0);
        tranBean.setMinimumAmount(0.0);
        tranBean.setMaximumAmount(0.0);
        tranBean.setFeeApplyInFirstMonth("feeApplyInFirstMonth");
        tranBean.setProcessingFeeType("processingFeeType");
        tranBean.setMid("mechantID");
        tranBean.setNextInstallmentAmount(new BigDecimal("0.00"));
        tranBean.setFirstInstallmentAmount(new BigDecimal("0.00"));

        when(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo.insertEasyPaymentRequest(any(MerchantEasyPaymentRequestBean.class))).thenReturn(0);
        when(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo.updateEodTransactionForEasyPaymentStatus("txnId")).thenReturn(0);
        when(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo.updateEodMerchantTransactionForEasyPaymentStatus("txnId")).thenReturn(0);
        //when(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo.insertEasyPaymentRejectRequest(any(MerchantEasyPaymentRequestBean.class))).thenReturn(0);

        // Run the test
        merchantEasyPaymentRequestServiceUnderTest.merchantEasyPayment(tranBean);

        // Verify the results
        verify(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo).insertEasyPaymentRequest(any(MerchantEasyPaymentRequestBean.class));
        verify(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo).updateEodTransactionForEasyPaymentStatus("txnId");
        verify(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo).updateEodMerchantTransactionForEasyPaymentStatus("txnId");
        //verify(merchantEasyPaymentRequestServiceUnderTest.merchantEasyPaymentRequestRepo).insertEasyPaymentRejectRequest(any(MerchantEasyPaymentRequestBean.class));
    }
}
