package com.epic.cms.service;
//
//import com.epic.cms.dao.MerchantGLSummaryFileDao;
//import com.epic.cms.model.model.GlAccountBean;
//import com.epic.cms.util.LogManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.Calendar;
//import java.util.GregorianCalendar;
//
//import static org.mockito.Mockito.*;
//
//class MerchantGLSummaryFileServiceTest {
//
//    private MerchantGLSummaryFileService merchantGLSummaryFileServiceUnderTest;
//
//    @BeforeEach
//    void setUp() {
//        merchantGLSummaryFileServiceUnderTest = new MerchantGLSummaryFileService();
//        merchantGLSummaryFileServiceUnderTest.merchantGLSummaryFileDao = mock(MerchantGLSummaryFileDao.class);
//        merchantGLSummaryFileServiceUnderTest.logManager = mock(LogManager.class);
//        merchantGLSummaryFileServiceUnderTest.taskExecutor = mock(ThreadPoolTaskExecutor.class);
//    }
//
//    @Test
//    void testCommissionGlFile() throws Exception {
//        // Setup
//        final GlAccountBean glaccountBean = new GlAccountBean();
//        glaccountBean.setCardNo(new StringBuffer("value"));
//        glaccountBean.setMerchantID("merchantID");
//        glaccountBean.setAccNo("accNo");
//        glaccountBean.setGlType("glType");
//        glaccountBean.setGlAmount("glAmount");
//        glaccountBean.setAmount(0.0);
//        glaccountBean.setFuelSurchargeAmount(0.0);
//        glaccountBean.setCrDr("crDr");
//        glaccountBean.setGlDate("glDate");
//        glaccountBean.setKey("key");
//        glaccountBean.setId(0);
//        glaccountBean.setPaymentType("paymentType");
//
//        when(merchantGLSummaryFileServiceUnderTest.merchantGLSummaryFileDao.insertIntoEodMerchantGLAccount(0,
//                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), "merchantID", "glType", "glAmount",
//                "crDr")).thenReturn(0);
//        when(merchantGLSummaryFileServiceUnderTest.merchantGLSummaryFileDao.updateCommissions("key", 1)).thenReturn(0);
//
//        // Run the test
//        merchantGLSummaryFileServiceUnderTest.commissionGlFile(glaccountBean);
//
//        // Verify the results
////        verify(merchantGLSummaryFileServiceUnderTest.merchantGLSummaryFileDao).insertIntoEodMerchantGLAccount(0,
////                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), "merchantID", "glType", "glAmount", "crDr");
////        verify(merchantGLSummaryFileServiceUnderTest.merchantGLSummaryFileDao).updateCommissions("key", 1);
//    }
//}
