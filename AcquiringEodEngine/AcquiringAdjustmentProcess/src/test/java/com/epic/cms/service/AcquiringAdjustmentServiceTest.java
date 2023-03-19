//package com.epic.cms.service;
//
//import com.epic.cms.dao.AcquiringAdjustmentDao;
//import com.epic.cms.model.bean.*;
//import com.epic.cms.util.LogManager;
//import com.epic.cms.util.StatusVarList;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.Calendar;
//import java.util.GregorianCalendar;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//class AcquiringAdjustmentServiceTest {
//
//    private AcquiringAdjustmentService acquiringAdjustmentServiceUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        acquiringAdjustmentServiceUnderTest = new AcquiringAdjustmentService();
//        acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao = mock(AcquiringAdjustmentDao.class);
//        acquiringAdjustmentServiceUnderTest.logManager = mock(LogManager.class);
//        acquiringAdjustmentServiceUnderTest.status = mock(StatusVarList.class);
//    }
//
//    @Test
//    void testAcquringAdjustment() throws Exception {
//        // Setup
//        final AcqAdjustmentBean acqAdjustmentBean = new AcqAdjustmentBean();
//        acqAdjustmentBean.setAdjustAmount("adjustAmount");
//        acqAdjustmentBean.setAdjustDate(Date.valueOf(LocalDate.of(2020, 1, 1)));
//        acqAdjustmentBean.setAdjustDes("adjustDes");
//        acqAdjustmentBean.setTxnType("feeCode");
//        acqAdjustmentBean.setCrDr("crdr");
//        acqAdjustmentBean.setMerchantId("merchantId");
//        acqAdjustmentBean.setCardOrMerchant("cardOrMerchant");
//        acqAdjustmentBean.setAdjustType("adjustType");
//        acqAdjustmentBean.setCurruncyType("currencytype");
//        acqAdjustmentBean.setId("id");
//        acqAdjustmentBean.setTxnId("txnId");
//        acqAdjustmentBean.setCardNumber(new StringBuffer("value"));
//        acqAdjustmentBean.setDescription("description");
//        acqAdjustmentBean.setMcc("mcc");
//        acqAdjustmentBean.setCardAssociation("cardAssociation");
//
//        // Configure AcquiringAdjustmentDao.getMerchanDetails(...).
//        final MerchantDetailsBean merchantDetailsBean = new MerchantDetailsBean();
//        merchantDetailsBean.setMid("merchantId");
//        merchantDetailsBean.setMerchantCustomerId("merchantCusId");
//        merchantDetailsBean.setMerchantAccountNo("accountNo");
//        merchantDetailsBean.setMerchantCusAccNo("custaccountno");
//        merchantDetailsBean.setMerchantCountry("countryNumCode");
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.getMerchanDetails(
//                any(MerchantDetailsBean.class))).thenReturn(merchantDetailsBean);
//
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertToEodMerchantPayment(
//                any(MerchantPayBean.class), eq("ACQ_ADJUSTMENT_TYPE_PAYMENT"))).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertToEodMerchantComission("merchantCusId",
//                "custaccountno", "merchantId", "accountNo", "tid", "0", "merchantComission", "currencytype", "crdr",
//                Date.valueOf(LocalDate.of(2020, 1, 1)), "transactiontype", "batchno", "transactionid", "binStatus",
//                "calMethod", "VISA_ASSOCIATION", "cardProduct", "segment", "cardProduct", 0)).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.getOnUsStatus("txnId")).thenReturn(false);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertReversalTxnIntoTxnTable("txnId",
//                "transactionid")).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertReversalTxnIntoMerchantTxnTable("txnId",
//                "transactionid")).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertReversalCommission("txnId",
//                "transactionid")).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.getBinType("sixDigitBin",
//                "eightDigitBin")).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.getAccountNoOnCard(
//                any(StringBuffer.class))).thenReturn("accountNo");
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.getCardAssociationFromBinRange(
//                "cardNumber")).thenReturn("cardAssociation");
//        when(acquiringAdjustmentServiceUnderTest.status.getONUS_STATUS()).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertToEODTransaction(any(StringBuffer.class),
//                eq("accountNo"), eq("merchantId"), eq("tId"), eq("adjustAmount"), eq(0), eq("crdr"),
//                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()),
//                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()), eq("TXN_TYPE_REFUND"), eq("batchNo"),
//                eq("transactionid"), eq("toAccNo"), eq(0.0), eq("description"), eq("COUNTRY_CODE_SRILANKA"), eq(0),
//                eq("poStringsEntryMode"), eq("traceId"), eq("authCode"), eq(0), eq("cardAssociation"))).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertReversalTxnIntoTxnTable(
//                any(AcqAdjustmentBean.class), eq("transactionid"), eq(0))).thenReturn(0);
//        when(acquiringAdjustmentServiceUnderTest.status.getPRODUCT_CODE_CUP_ALL()).thenReturn("cardProduct");
//        when(acquiringAdjustmentServiceUnderTest.status.getEOD_DONE_STATUS()).thenReturn("EOD_DONE_STATUS");
//        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.insertIntoEodMerchantTransaction(
//                any(EodTransactionBean.class), eq("EOD_DONE_STATUS"))).thenReturn(0);
////        when(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao.updateAdjustmentToEdon("id",
////                "transactionid")).thenReturn(1);
//
//        // Run the test
//        acquiringAdjustmentServiceUnderTest.acquringAdjustment(acqAdjustmentBean);
//
//        // Verify the results
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertToEodMerchantPayment(
////                any(MerchantPayBean.class), eq("ACQ_ADJUSTMENT_TYPE_PAYMENT"));
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertToEodMerchantComission("merchantCusId",
////                "custaccountno", "merchantId", "accountNo", "tid", "0", "merchantComission", "currencytype", "crdr",
////                Date.valueOf(LocalDate.of(2020, 1, 1)), "transactiontype", "batchno", "transactionid", "binStatus",
////                "calMethod", "VISA_ASSOCIATION", "cardProduct", "segment", "cardProduct", 0);
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertToEODMerchantFee(
////                any(MerchantFeeBean.class), eq("adjustAmount"),
////                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()));
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertReversalTxnIntoTxnTable("txnId",
////                "transactionid");
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertReversalTxnIntoMerchantTxnTable(
////                "txnId", "transactionid");
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertReversalCommission("txnId",
////                "transactionid");
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).getPaymentAmount(eq("txnId"),
////                any());
//////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertToEODTransaction(
////                any(StringBuffer.class), eq("accountNo"), eq("merchantId"), eq("tId"), eq("adjustAmount"), eq(0),
////                eq("crdr"), eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()),
////                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()), eq("TXN_TYPE_REFUND"), eq("batchNo"),
////                eq("transactionid"), eq("toAccNo"), eq(0.0), eq("description"), eq("COUNTRY_CODE_SRILANKA"), eq(0),
////                eq("poStringsEntryMode"), eq("traceId"), eq("authCode"), eq(0), eq("cardAssociation"));
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertReversalTxnIntoTxnTable(
////                any(AcqAdjustmentBean.class), eq("transactionid"), eq(0));
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).insertIntoEodMerchantTransaction(
////                any(EodTransactionBean.class), eq("EOD_DONE_STATUS"));
////        verify(acquiringAdjustmentServiceUnderTest.acquiringAdjustmentDao).updateAdjustmentToEdon("id",
////                "transactionid");
//    }
//
//}
