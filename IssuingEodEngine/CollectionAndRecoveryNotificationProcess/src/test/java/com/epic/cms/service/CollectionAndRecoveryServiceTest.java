package com.epic.cms.service;

import com.epic.cms.model.bean.CollectionAndRecoveryBean;
import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CollectionAndRecoveryRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CollectionAndRecoveryServiceTest {
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    private CollectionAndRecoveryService collectionAndRecoveryServiceUnderTest;
    static MockedStatic<LogManager> common;
    @BeforeAll
    public static void init() {
        common = Mockito.mockStatic(LogManager.class);
    }

    @AfterAll
    public static void close() {
        common.close();
    }
    @BeforeEach
    void setUp() {
        collectionAndRecoveryServiceUnderTest = new CollectionAndRecoveryService();
        collectionAndRecoveryServiceUnderTest.logManager = mock(LogManager.class);
        collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo = mock(CollectionAndRecoveryRepo.class);
        collectionAndRecoveryServiceUnderTest.statusList = mock(StatusVarList.class);
        collectionAndRecoveryServiceUnderTest.commonRepo = mock(CommonRepo.class);

        // Configure
        String maskCardNo = "value";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(new StringBuffer("value")))
                    .thenReturn(maskCardNo);
        }
    }

    @Test
    void testProcessX_DATES_BEFORE_FIRST_DUE_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo.CheckForTriggerPoint(
                any(StringBuffer.class))).thenReturn(false);
        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processX_DATES_BEFORE_FIRST_DUE_DATE(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).addCardToTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }


    @Test
    void testProcessX_DATES_AFTER_FIRST_DUE_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processX_DATES_AFTER_FIRST_DUE_DATE(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }


    @Test
    void testProcessON_THE_2ND_STATEMENT_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processON_THE_2ND_STATEMENT_DATE(collectionAndRecoveryBean, processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }

    @Test
    void testProcessX_DATES_AFTER_SECOND_STATEMENT() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processX_DATES_AFTER_SECOND_STATEMENT(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }

    @Test
    void testProcessIMMEDIATELY_AFTER_THE_2ND_DUE_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        // Configure CommonRepo.setDelinquentAccountDetails(...).
        final DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        delinquentAccountBean.setAccNo("accNo");
        delinquentAccountBean.setCif("cif");
        delinquentAccountBean.setNameOnCard("nameOnCard");
        delinquentAccountBean.setNameInFull("nameInFull");
        delinquentAccountBean.setIdType("idType");
        delinquentAccountBean.setIdNumber("idNumber");
        delinquentAccountBean.setAccStatus("accStatus");
        delinquentAccountBean.setCardCategory("cardCategory");
        delinquentAccountBean.setNDIA(0);
        delinquentAccountBean.setMIA(0);
        delinquentAccountBean.setLastStatementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setContactNo("contactNo");
        delinquentAccountBean.setEmail("email");
        delinquentAccountBean.setAddress("address");
        delinquentAccountBean.setDelinqstatus("delinqstatus");
        when(collectionAndRecoveryServiceUnderTest.commonRepo.setDelinquentAccountDetails(
                any(StringBuffer.class))).thenReturn(delinquentAccountBean);

        when(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo.addDetailsToCardLetterNotifyTable(
                any(StringBuffer.class), eq("nameInFull"), eq("accNo"), eq("contactNo"), eq("email"), eq("address"),
                eq(0.0), eq("dueDate"), eq("lastTriger"))).thenReturn(0);
        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processIMMEDIATELY_AFTER_THE_2ND_DUE_DATE(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }


    @Test
    void testProcessON_THE_3RD_STATEMENT_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processON_THE_3RD_STATEMENT_DATE(collectionAndRecoveryBean, processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }

    @Test
    void testProcessIMMEDIATELY_AFTER_THE_3RD_DUE_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processIMMEDIATELY_AFTER_THE_3RD_DUE_DATE(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }

    @Test
    void testProcessON_THE_4TH_STATEMENT_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processON_THE_4TH_STATEMENT_DATE(collectionAndRecoveryBean, processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }


    @Test
    void testProcessX_DAYS_AFTER_THE_4TH_STATEMENT_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        // Configure CommonRepo.setDelinquentAccountDetails(...).
        final DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        delinquentAccountBean.setAccNo("accNo");
        delinquentAccountBean.setCif("cif");
        delinquentAccountBean.setNameOnCard("nameOnCard");
        delinquentAccountBean.setNameInFull("nameInFull");
        delinquentAccountBean.setIdType("idType");
        delinquentAccountBean.setIdNumber("idNumber");
        delinquentAccountBean.setAccStatus("accStatus");
        delinquentAccountBean.setCardCategory("cardCategory");
        delinquentAccountBean.setNDIA(0);
        delinquentAccountBean.setMIA(0);
        delinquentAccountBean.setLastStatementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setContactNo("contactNo");
        delinquentAccountBean.setEmail("email");
        delinquentAccountBean.setAddress("address");
        delinquentAccountBean.setDelinqstatus("delinqstatus");
        when(collectionAndRecoveryServiceUnderTest.commonRepo.setDelinquentAccountDetails(
                any(StringBuffer.class))).thenReturn(delinquentAccountBean);

        when(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo.addDetailsToCardLetterNotifyTable(
                any(StringBuffer.class), eq("nameInFull"), eq("accNo"), eq("contactNo"), eq("email"), eq("address"),
                eq(0.0), eq("dueDate"), eq("lastTriger"))).thenReturn(0);
        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processX_DAYS_AFTER_THE_4TH_STATEMENT_DATE(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }

    @Test
    void testProcessWITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processWITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER(
                collectionAndRecoveryBean, processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }

    @Test
    void testProcessIMMEDIATELY_AFTER_THE_4TH_DUE_DATE() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("4380431766518012"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);

        when(collectionAndRecoveryServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        collectionAndRecoveryServiceUnderTest.processIMMEDIATELY_AFTER_THE_4TH_DUE_DATE(collectionAndRecoveryBean,
                processBean,faileCardCount);

        // Verify the results
        verify(collectionAndRecoveryServiceUnderTest.collectionAndRecoveryRepo).updateTriggerCards(
                any(CollectionAndRecoveryBean.class));
    }
}
