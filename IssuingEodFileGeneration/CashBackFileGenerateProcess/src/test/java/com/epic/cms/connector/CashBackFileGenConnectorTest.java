//package com.epic.cms.connector;
//
//import com.epic.cms.model.bean.GlAccountBean;
//import com.epic.cms.repository.CashBackFileGenRepo;
//import com.epic.cms.repository.CommonFileGenProcessRepo;
//import com.epic.cms.service.CashBackFileGenService;
//import com.epic.cms.util.LogManager;
//import com.epic.cms.util.StatusVarList;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//class CashBackFileGenConnectorTest {
//
//    private CashBackFileGenConnector cashBackFileGenConnectorUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        cashBackFileGenConnectorUnderTest = new CashBackFileGenConnector();
//        cashBackFileGenConnectorUnderTest.logManager = mock(LogManager.class);
//        cashBackFileGenConnectorUnderTest.statusVarList = mock(StatusVarList.class);
//        cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo = mock(CommonFileGenProcessRepo.class);
//        cashBackFileGenConnectorUnderTest.cashBackFileGenRepo = mock(CashBackFileGenRepo.class);
//        cashBackFileGenConnectorUnderTest.cashBackFileGenService = mock(CashBackFileGenService.class);
//    }
//
//    @Test
//    void testConcreteProcess() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//
//        // Configure CommonFileGenProcessRepo.getNextWorkingDay(...).
//        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);
//
//        // Configure CashBackFileGenRepo.getCahsBackRedeemList(...).
//        final GlAccountBean glAccountBean = new GlAccountBean();
//        glAccountBean.setCardNo(new StringBuffer("value"));
//        glAccountBean.setMerchantID("merchantID");
//        glAccountBean.setAccNo("accNo");
//        glAccountBean.setGlType("glType");
//        glAccountBean.setGlAmount("0.0");
//        glAccountBean.setAmount(0.0);
//        glAccountBean.setFuelSurchargeAmount(0.0);
//        glAccountBean.setCrDr("crDr");
//        glAccountBean.setGlDate("glDate");
//        glAccountBean.setKey("key");
//        glAccountBean.setId(0);
//        glAccountBean.setPaymentType("paymentType");
//        final ArrayList<GlAccountBean> glAccountBeans = new ArrayList<>(List.of(glAccountBean));
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCahsBackRedeemList()).thenReturn(glAccountBeans);
//
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCashBackDebitAccount())
//                .thenReturn("debitAccount");
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileContent(any(GlAccountBean.class),
//                eq(new BigDecimal("0.00")), eq("seqNo"), eq("today3"),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileContent(any(GlAccountBean.class),
//                eq("debitAccount"), eq(new BigDecimal("0.00")),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)), eq(0))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileHeader("debitAccount", "fileNameF1",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US), "today3", new BigDecimal("0.00"),
//                0)).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileHeader("today3",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.updateCashBackRedeemExp(0)).thenReturn(0);
//        when(cashBackFileGenConnectorUnderTest.logManager.processSummeryStyles(
//                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//        verify(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo).updateCashBackRedeemExp(0);
//    }
//
//    @Test
//    void testConcreteProcess_CommonFileGenProcessRepoThrowsException() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenThrow(Exception.class);
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//    }
//
//    @Test
//    void testConcreteProcess_CashBackFileGenRepoGetCahsBackRedeemListReturnsNoItems() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//
//        // Configure CommonFileGenProcessRepo.getNextWorkingDay(...).
//        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);
//
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCahsBackRedeemList())
//                .thenReturn(new ArrayList<>());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCashBackDebitAccount())
//                .thenReturn("debitAccount");
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileContent(any(GlAccountBean.class),
//                eq(new BigDecimal("0.00")), eq("seqNo"), eq("today3"),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileContent(any(GlAccountBean.class),
//                eq("debitAccount"), eq(new BigDecimal("0.00")),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)), eq(0))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileHeader("debitAccount", "fileNameF1",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US), "today3", new BigDecimal("0.00"),
//                0)).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileHeader("today3",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.updateCashBackRedeemExp(0)).thenReturn(0);
//        when(cashBackFileGenConnectorUnderTest.logManager.processSummeryStyles(
//                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//        verify(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo).updateCashBackRedeemExp(0);
//    }
//
//    @Test
//    void testConcreteProcess_CashBackFileGenRepoGetCahsBackRedeemListThrowsException() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//
//        // Configure CommonFileGenProcessRepo.getNextWorkingDay(...).
//        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);
//
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCahsBackRedeemList()).thenThrow(Exception.class);
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//    }
//
//    @Test
//    void testConcreteProcess_CashBackFileGenRepoGetCashBackDebitAccountReturnsNull() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//
//        // Configure CommonFileGenProcessRepo.getNextWorkingDay(...).
//        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);
//
//        // Configure CashBackFileGenRepo.getCahsBackRedeemList(...).
//        final GlAccountBean glAccountBean = new GlAccountBean();
//        glAccountBean.setCardNo(new StringBuffer("value"));
//        glAccountBean.setMerchantID("merchantID");
//        glAccountBean.setAccNo("accNo");
//        glAccountBean.setGlType("glType");
//        glAccountBean.setGlAmount("0.0");
//        glAccountBean.setAmount(0.0);
//        glAccountBean.setFuelSurchargeAmount(0.0);
//        glAccountBean.setCrDr("crDr");
//        glAccountBean.setGlDate("glDate");
//        glAccountBean.setKey("key");
//        glAccountBean.setId(0);
//        glAccountBean.setPaymentType("paymentType");
//        final ArrayList<GlAccountBean> glAccountBeans = new ArrayList<>(List.of(glAccountBean));
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCahsBackRedeemList()).thenReturn(glAccountBeans);
//
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCashBackDebitAccount()).thenReturn(null);
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileContent(any(GlAccountBean.class),
//                eq(new BigDecimal("0.00")), eq("seqNo"), eq("today3"),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileContent(any(GlAccountBean.class),
//                eq("debitAccount"), eq(new BigDecimal("0.00")),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)), eq(0))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileHeader("debitAccount", "fileNameF1",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US), "today3", new BigDecimal("0.00"),
//                0)).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileHeader("today3",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.updateCashBackRedeemExp(0)).thenReturn(0);
//        when(cashBackFileGenConnectorUnderTest.logManager.processSummeryStyles(
//                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//        verify(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo).updateCashBackRedeemExp(0);
//    }
//
//    @Test
//    void testConcreteProcess_CashBackFileGenRepoGetCashBackDebitAccountThrowsException() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//
//        // Configure CommonFileGenProcessRepo.getNextWorkingDay(...).
//        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);
//
//        // Configure CashBackFileGenRepo.getCahsBackRedeemList(...).
//        final GlAccountBean glAccountBean = new GlAccountBean();
//        glAccountBean.setCardNo(new StringBuffer("value"));
//        glAccountBean.setMerchantID("merchantID");
//        glAccountBean.setAccNo("accNo");
//        glAccountBean.setGlType("glType");
//        glAccountBean.setGlAmount("0.0");
//        glAccountBean.setAmount(0.0);
//        glAccountBean.setFuelSurchargeAmount(0.0);
//        glAccountBean.setCrDr("crDr");
//        glAccountBean.setGlDate("glDate");
//        glAccountBean.setKey("key");
//        glAccountBean.setId(0);
//        glAccountBean.setPaymentType("paymentType");
//        final ArrayList<GlAccountBean> glAccountBeans = new ArrayList<>(List.of(glAccountBean));
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCahsBackRedeemList()).thenReturn(glAccountBeans);
//
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCashBackDebitAccount())
//                .thenThrow(Exception.class);
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//    }
//
//    @Test
//    void testConcreteProcess_CashBackFileGenRepoUpdateCashBackRedeemExpThrowsException() throws Exception {
//        // Setup
//        when(cashBackFileGenConnectorUnderTest.logManager.processHeaderStyle("CASHBACK FILE GENERATION"))
//                .thenReturn("result");
//
//        // Configure CommonFileGenProcessRepo.getNextWorkingDay(...).
//        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
//        when(cashBackFileGenConnectorUnderTest.commonFileGenProcessRepo.getNextWorkingDay(
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);
//
//        // Configure CashBackFileGenRepo.getCahsBackRedeemList(...).
//        final GlAccountBean glAccountBean = new GlAccountBean();
//        glAccountBean.setCardNo(new StringBuffer("value"));
//        glAccountBean.setMerchantID("merchantID");
//        glAccountBean.setAccNo("accNo");
//        glAccountBean.setGlType("glType");
//        glAccountBean.setGlAmount("0.0");
//        glAccountBean.setAmount(0.0);
//        glAccountBean.setFuelSurchargeAmount(0.0);
//        glAccountBean.setCrDr("crDr");
//        glAccountBean.setGlDate("glDate");
//        glAccountBean.setKey("key");
//        glAccountBean.setId(0);
//        glAccountBean.setPaymentType("paymentType");
//        final ArrayList<GlAccountBean> glAccountBeans = new ArrayList<>(List.of(glAccountBean));
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCahsBackRedeemList()).thenReturn(glAccountBeans);
//
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.getCashBackDebitAccount())
//                .thenReturn("debitAccount");
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileContent(any(GlAccountBean.class),
//                eq(new BigDecimal("0.00")), eq("seqNo"), eq("today3"),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileContent(any(GlAccountBean.class),
//                eq("debitAccount"), eq(new BigDecimal("0.00")),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)),
//                eq(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)), eq(0))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addFirstFileHeader("debitAccount", "fileNameF1",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US), "today3", new BigDecimal("0.00"),
//                0)).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenService.addSecondFileHeader("today3",
//                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US))).thenReturn(new StringBuilder());
//        when(cashBackFileGenConnectorUnderTest.cashBackFileGenRepo.updateCashBackRedeemExp(0))
//                .thenThrow(Exception.class);
//        when(cashBackFileGenConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
//
//        // Run the test
//        cashBackFileGenConnectorUnderTest.concreteProcess();
//
//        // Verify the results
//    }
//}
