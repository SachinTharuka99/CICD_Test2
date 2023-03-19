//package com.epic.cms.service;
//
//import com.epic.cms.model.bean.CommissionProfileBean;
//import com.epic.cms.model.bean.CommissionTxnBean;
//import com.epic.cms.model.bean.MerchantLocationBean;
//import com.epic.cms.repository.MerchantCommissionCalculationRepo;
//import com.epic.cms.util.LogManager;
//import com.epic.cms.util.StatusVarList;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class MerchantCommissionCalculationServiceTest {
//
//    private MerchantCommissionCalculationService merchantCommissionCalculationServiceUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        merchantCommissionCalculationServiceUnderTest = new MerchantCommissionCalculationService();
//        merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo = mock(
//                MerchantCommissionCalculationRepo.class);
//        merchantCommissionCalculationServiceUnderTest.statusVarList = mock(StatusVarList.class);
//        merchantCommissionCalculationServiceUnderTest.logManager = mock(LogManager.class);
//    }
//
//    @Test
//    void testCalculateMerchantCommission() throws Exception {
//        // Setup
//        final MerchantLocationBean merchantLocationBean = new MerchantLocationBean();
//        merchantLocationBean.setMerchantId("merchantId");
//        merchantLocationBean.setRiskProfile("riskProfile");
//        merchantLocationBean.setFeeProfile("feeProfile");
//        merchantLocationBean.setComisionProfile("commissionProfile");
//        merchantLocationBean.setStatementmaintenanceStatus("statementmaintenanceStatus");
//        merchantLocationBean.setMerchantCusNo("merchantCusNo");
//        merchantLocationBean.setMerchantCusName("merchantCusName");
//        merchantLocationBean.setPostalCode("postalCode");
//        merchantLocationBean.setMerchantType("merchantType");
//        merchantLocationBean.setMerchantEmail("merchantEmail");
//        merchantLocationBean.setMerchantCurrency("merchantCurrency");
//        merchantLocationBean.setBankName("bankName");
//        merchantLocationBean.setPaymentMode("paymentMode");
//        merchantLocationBean.setMerchantDes("merchantDes");
//        merchantLocationBean.setMerchantCustomerNo("merchantCustomerNo");
//
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getCustomerCommStatus(
//                "merchantCustomerNo")).thenReturn(false);
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getCommissionProfile(
//                "merchantCustomerNo")).thenReturn("commissionProfile");
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getCalMethod(
//                "commissionProfile")).thenReturn("calMethod");
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getCOMMISSION_TRANSACTION_WISE())
//                .thenReturn("result");
//
//        // Configure MerchantCommissionCalculationRepo.getAllCommCombination(...).
//        final CommissionProfileBean commissionProfileBean = new CommissionProfileBean();
//        commissionProfileBean.setCrdr("crdr");
//        commissionProfileBean.setCardAssociation("cardAssociation");
//        commissionProfileBean.setBinType("");
//        commissionProfileBean.setSegment("segment");
//        commissionProfileBean.setProfCode("profCode");
//        commissionProfileBean.setCardProduct("");
//        commissionProfileBean.setCombination("combination");
//        commissionProfileBean.setFlatValue(0.0);
//        commissionProfileBean.setPercentage(0.0);
//        commissionProfileBean.setVolumeId("volumeId");
//        final Queue<CommissionProfileBean> commissionProfileBeans = new LinkedList<>(List.of(commissionProfileBean));
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getAllCommCombination(
//                "commissionProfile", "COMMISSION_TRANSACTION_TABLE", "COMMISSION_SEGMENT_TRANSACTION",
//                "COMMISSION_DEFAULT_TXN")).thenReturn(commissionProfileBeans);
//
//        // Configure MerchantCommissionCalculationRepo.getTransactionForCommission(...).
//        final CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
//        commissionTxnBean.setMerchantcustid("merchantcustid");
//        commissionTxnBean.setCustaccountno("custaccountno");
//        commissionTxnBean.setMeraccountno("meraccountno");
//        commissionTxnBean.setTid("tid");
//        commissionTxnBean.setTransactionamount("0");
//        commissionTxnBean.setMerchantcommssion(0.0);
//        commissionTxnBean.setCurrencytype("currencytype");
//        commissionTxnBean.setCrdr("crdr");
//        commissionTxnBean.setTransactiondate(Date.valueOf(LocalDate.of(2020, 1, 1)));
//        commissionTxnBean.setTransactiontype("transactiontype");
//        commissionTxnBean.setBatchno("batchno");
//        commissionTxnBean.setTransactionid("transactionid");
//        commissionTxnBean.setCardassociation("cardassociation");
//        commissionTxnBean.setCardProduct("cardProduct");
//        commissionTxnBean.setCalculatedMdrPercentage("calculatedMdrPercentage");
//        commissionTxnBean.setCalculatedMdrFlatAmount("calculatedMdrFlatAmount");
//        final ArrayList<CommissionTxnBean> commissionTxnBeans = new ArrayList<>(List.of(commissionTxnBean));
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getTransactionForCommission(
//                "merchantId", "", "", "segment", "COMMISSION_TRANSACTION_WISE", "TRANSACTION_SEGMENT_TXNTYPE",
//                "COMMISSION_DEFAULT_TXN")).thenReturn(commissionTxnBeans);
//
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getCOMISSION_COMBINATION_MIN())
//                .thenReturn("result");
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getCOMISSION_COMBINATION_MAX())
//                .thenReturn("result");
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getCOMISSION_COMBINATION_ADD())
//                .thenReturn("result");
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.insertToEodMerchantComission(
//                "merchantcustid", "custaccountno", "merchantId", "meraccountno", "tid", "0", 0.0, "currencytype",
//                "crdr", Date.valueOf(LocalDate.of(2020, 1, 1)), "transactiontype", "batchno", "transactionid", "",
//                "calMethod", "cardassociation", "", "segment", "cardProduct", "calculatedMdrPercentage",
//                "calculatedMdrFlatAmount")).thenReturn(0);
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getEOD_DONE_STATUS())
//                .thenReturn("EOD_DONE_STATUS");
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.updateEodMerchantTxnEdon(
//                "transactionid", "EOD_DONE_STATUS")).thenReturn(0);
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getCOMMISSION_MCC_WISE()).thenReturn("result");
//        when(merchantCommissionCalculationServiceUnderTest.statusVarList.getCOMMISSION_VOLUME_WISE())
//                .thenReturn("result");
//
//        // Configure MerchantCommissionCalculationRepo.getAllCommCombinationForVolume(...).
//        final CommissionProfileBean commissionProfileBean1 = new CommissionProfileBean();
//        commissionProfileBean1.setCrdr("crdr");
//        commissionProfileBean1.setCardAssociation("cardAssociation");
//        commissionProfileBean1.setBinType("");
//        commissionProfileBean1.setSegment("segment");
//        commissionProfileBean1.setProfCode("profCode");
//        commissionProfileBean1.setCardProduct("");
//        commissionProfileBean1.setCombination("combination");
//        commissionProfileBean1.setFlatValue(0.0);
//        commissionProfileBean1.setPercentage(0.0);
//        commissionProfileBean1.setVolumeId("volumeId");
//        final Queue<CommissionProfileBean> commissionProfileBeans1 = new LinkedList<>(List.of(commissionProfileBean1));
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getAllCommCombinationForVolume(
//                "commissionProfile", "COMMISSION_VOLUME_TABLE", "COMMISSION_SEGMENT_VOLUME",
//                "COMMISSION_DEFAULT_VOLUME")).thenReturn(commissionProfileBeans1);
//
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getVolumeId(0.0))
//                .thenReturn("result");
//
//        // Configure MerchantCommissionCalculationRepo.getCommissionProfile(...).
//        final CommissionProfileBean commissionProfileBean2 = new CommissionProfileBean();
//        commissionProfileBean2.setCrdr("crdr");
//        commissionProfileBean2.setCardAssociation("cardAssociation");
//        commissionProfileBean2.setBinType("");
//        commissionProfileBean2.setSegment("segment");
//        commissionProfileBean2.setProfCode("profCode");
//        commissionProfileBean2.setCardProduct("");
//        commissionProfileBean2.setCombination("combination");
//        commissionProfileBean2.setFlatValue(0.0);
//        commissionProfileBean2.setPercentage(0.0);
//        commissionProfileBean2.setVolumeId("volumeId");
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getCommissionProfile(
//                "commissionProfile", "", "", "COMMISSION_DEFAULT_VOLUME")).thenReturn(commissionProfileBean2);
//
//        // Configure MerchantCommissionCalculationRepo.getTransactionForCommissionVolumeWise(...).
//        final CommissionTxnBean commissionTxnBean1 = new CommissionTxnBean();
//        commissionTxnBean1.setMerchantcustid("merchantcustid");
//        commissionTxnBean1.setCustaccountno("custaccountno");
//        commissionTxnBean1.setMeraccountno("meraccountno");
//        commissionTxnBean1.setTid("tid");
//        commissionTxnBean1.setTransactionamount("0");
//        commissionTxnBean1.setMerchantcommssion(0.0);
//        commissionTxnBean1.setCurrencytype("currencytype");
//        commissionTxnBean1.setCrdr("crdr");
//        commissionTxnBean1.setTransactiondate(Date.valueOf(LocalDate.of(2020, 1, 1)));
//        commissionTxnBean1.setTransactiontype("transactiontype");
//        commissionTxnBean1.setBatchno("batchno");
//        commissionTxnBean1.setTransactionid("transactionid");
//        commissionTxnBean1.setCardassociation("cardassociation");
//        commissionTxnBean1.setCardProduct("cardProduct");
//        commissionTxnBean1.setCalculatedMdrPercentage("calculatedMdrPercentage");
//        commissionTxnBean1.setCalculatedMdrFlatAmount("calculatedMdrFlatAmount");
//        final ArrayList<CommissionTxnBean> commissionTxnBeans1 = new ArrayList<>(List.of(commissionTxnBean1));
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getTransactionForCommissionVolumeWise(
//                "merchantId", "", "", "calMethod", new ArrayList<>(List.of(new CommissionTxnBean()))))
//                .thenReturn(commissionTxnBeans1);
//
//        // Run the test
//        merchantCommissionCalculationServiceUnderTest.calculateMerchantCommission(merchantLocationBean);
//
//        // Verify the results
//        verify(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo).getMerchantDetails(
//                any(CommissionTxnBean.class));
//        verify(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo).insertToEodMerchantComission(
//                "merchantcustid", "custaccountno", "merchantId", "meraccountno", "tid", "0", 0.0, "currencytype",
//                "crdr", Date.valueOf(LocalDate.of(2020, 1, 1)), "transactiontype", "batchno", "transactionid", "",
//                "calMethod", "cardassociation", "", "segment", "cardProduct", "calculatedMdrPercentage",
//                "calculatedMdrFlatAmount");
//        verify(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo).updateEodMerchantTxnEdon(
//                "transactionid", "EOD_DONE_STATUS");
//    }
//
//    @Test
//    void testCalculateMerchantCommission_MerchantCommissionCalculationRepoGetCustomerCommStatusThrowsException() throws Exception {
//        // Setup
//        final MerchantLocationBean merchantLocationBean = new MerchantLocationBean();
//        merchantLocationBean.setMerchantId("merchantId");
//        merchantLocationBean.setRiskProfile("riskProfile");
//        merchantLocationBean.setFeeProfile("feeProfile");
//        merchantLocationBean.setComisionProfile("commissionProfile");
//        merchantLocationBean.setStatementmaintenanceStatus("statementmaintenanceStatus");
//        merchantLocationBean.setMerchantCusNo("merchantCusNo");
//        merchantLocationBean.setMerchantCusName("merchantCusName");
//        merchantLocationBean.setPostalCode("postalCode");
//        merchantLocationBean.setMerchantType("merchantType");
//        merchantLocationBean.setMerchantEmail("merchantEmail");
//        merchantLocationBean.setMerchantCurrency("merchantCurrency");
//        merchantLocationBean.setBankName("bankName");
//        merchantLocationBean.setPaymentMode("paymentMode");
//        merchantLocationBean.setMerchantDes("merchantDes");
//        merchantLocationBean.setMerchantCustomerNo("merchantCustomerNo");
//
//        when(merchantCommissionCalculationServiceUnderTest.commissionCalculationRepo.getCustomerCommStatus(
//                "merchantCustomerNo")).thenThrow(Exception.class);
//
//        // Run the test
//        merchantCommissionCalculationServiceUnderTest.calculateMerchantCommission(merchantLocationBean);
//
//        // Verify the results
//    }
//}
