package com.epic.cms.service;

import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.repository.AcqTxnUpdateRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AcqTxnUpdateServiceTest {

    private AcqTxnUpdateService acqTxnUpdateServiceUnderTest;
    private AcqTxnUpdateService spyAcqTxnUpdateServiceUnderTest;

    @BeforeEach
    void setUp() {
        acqTxnUpdateServiceUnderTest = new AcqTxnUpdateService();
        acqTxnUpdateServiceUnderTest.logManager = mock(LogManager.class);
        acqTxnUpdateServiceUnderTest.status = mock(StatusVarList.class);
        acqTxnUpdateServiceUnderTest.commonRepo = mock(CommonRepo.class);
        acqTxnUpdateServiceUnderTest.acqTxnUpdateRepo = mock(AcqTxnUpdateRepo.class);
        spyAcqTxnUpdateServiceUnderTest = spy(AcqTxnUpdateService.class);
    }

    static MockedStatic<CommonMethods> common;
    static MockedStatic<LogManager> commonLog;

    @BeforeAll
    public static void init() {
        common = Mockito.mockStatic(CommonMethods.class);
        commonLog = Mockito.mockStatic(LogManager.class);
    }

    @AfterAll
    public static void close() {
        common.close();
        commonLog.close();
    }

    @Test
    @DisplayName("Test Acquiring Transaction Update Process")
    void processAcqTxnUpdate() throws Exception{
        // Setup
        final EodTransactionBean eodTransactionBean = new EodTransactionBean();
        eodTransactionBean.setAccountNo("accountNo");
        eodTransactionBean.setAuthCode("authCode");
        eodTransactionBean.setBatchNo("batchNo");
        eodTransactionBean.setCardNo(new StringBuffer("4380431766518012"));
        eodTransactionBean.setCountryNumCode("countryNumCode");
        eodTransactionBean.setCrDr("crDr");
        eodTransactionBean.setForexMarkupAmount("forexMarkupAmount");
        eodTransactionBean.setMid("mid");
        eodTransactionBean.setOnOffStatus(0);
        eodTransactionBean.setPosEntryMode("posEntryMode");
        eodTransactionBean.setSettlementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        eodTransactionBean.setTid("tid");
        eodTransactionBean.setToAccNo("toAccNo");
        eodTransactionBean.setTraceId("traceId");
        eodTransactionBean.setTxnAmount("0.0");
        eodTransactionBean.setTxnDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        eodTransactionBean.setTxnDescription("txnDescription");
        eodTransactionBean.setTxnId("txnId");
        eodTransactionBean.setTxnType("value");
        eodTransactionBean.setCurrencyType("0");
        eodTransactionBean.setMcc("mcc");
        eodTransactionBean.setBin("bin");
        eodTransactionBean.setFuelSurchargeAmount("0.0");
        eodTransactionBean.setRequestFrom("requestFrom");
        eodTransactionBean.setSecondPartyPan("secondPartyPan");
        eodTransactionBean.setChannelType(0);
        eodTransactionBean.setCardAssociation("cardAssociation");
        eodTransactionBean.setCardProduct("cardProduct");
        eodTransactionBean.setListenerType("listenerType");

        Configurations.YES_STATUS = "YES";
        Configurations.BASE_CURRENCY ="Base";
        Configurations.DEBIT ="DEBIT";

        final HashMap<String, String> visaTxnFields = new HashMap<>(Map.ofEntries(Map.entry("value", "YES")));
        when(acqTxnUpdateServiceUnderTest.status.getLISTENER_TYPE_IPG()).thenReturn(0);
        when(acqTxnUpdateServiceUnderTest.status.getPRODUCT_CODE_IPG_VISA()).thenReturn("cardProduct");
        when(acqTxnUpdateServiceUnderTest.status.getPRODUCT_CODE_IPG_MASTER()).thenReturn("cardProduct");
        when(acqTxnUpdateServiceUnderTest.status.getPRODUCT_CODE_CUP_ALL()).thenReturn("cardProduct");
        when(acqTxnUpdateServiceUnderTest.status.getONUS_STATUS()).thenReturn(1);
        when(acqTxnUpdateServiceUnderTest.status.getEOD_PENDING_STATUS()).thenReturn("EOD_PENDING_STATUS");

        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        doReturn(true).when(spyAcqTxnUpdateServiceUnderTest).isFinancialStatusYes(eodTransactionBean.getTxnType(), visaTxnFields);

        when(acqTxnUpdateServiceUnderTest.commonRepo.insertToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(),any(Date.class),any(Date.class), anyString(),anyString(),
                anyString(), any(), anyDouble(), any(), anyString(), anyInt(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), any())).thenReturn(1);

        when(acqTxnUpdateServiceUnderTest.commonRepo.insertIntoEodMerchantTransaction(any(EodTransactionBean.class),
                eq("EOD_PENDING_STATUS"))).thenReturn(1);
        when(acqTxnUpdateServiceUnderTest.commonRepo.updateTransactionToEDON(anyString(),
                any(StringBuffer.class))).thenReturn(1);
        when(acqTxnUpdateServiceUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        acqTxnUpdateServiceUnderTest.processAcqTxnUpdate(1, eodTransactionBean, visaTxnFields, List.of("value"));

        // Verify the results
        verify(acqTxnUpdateServiceUnderTest.commonRepo).insertToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), anyString(), anyInt(), anyString(),any(Date.class),any(Date.class), anyString(),anyString(),
                anyString(), any(), anyDouble(), any(), anyString(), anyInt(),
                anyString(), anyString(), anyString(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), any());

        verify(acqTxnUpdateServiceUnderTest.commonRepo).updateTransactionToEDON(anyString(),
                any(StringBuffer.class));

    }

    @Test
    void isFinancialStatusYes() throws Exception{

    }
}