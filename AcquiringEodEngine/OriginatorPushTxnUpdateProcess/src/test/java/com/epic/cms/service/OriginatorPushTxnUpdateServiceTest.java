package com.epic.cms.service;

import com.epic.cms.dao.OriginatorPushTxnUpdateDao;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class OriginatorPushTxnUpdateServiceTest {

    private OriginatorPushTxnUpdateService originatorPushTxnUpdateServiceUnderTest;

    @BeforeEach
    void setUp() {
        originatorPushTxnUpdateServiceUnderTest = new OriginatorPushTxnUpdateService();
        originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao = mock(OriginatorPushTxnUpdateDao.class);
        originatorPushTxnUpdateServiceUnderTest.logManager = mock(LogManager.class);
    }

    @Test
    void testOriginatorPushTxnUpdate() throws Exception {
        // Setup
        final EodTransactionBean eodTransactionBean = new EodTransactionBean();
        eodTransactionBean.setAccountNo("accountNo");
        eodTransactionBean.setAuthCode("authCode");
        eodTransactionBean.setBatchNo("batchNo");
        eodTransactionBean.setCardNo(new StringBuffer("value"));
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
        eodTransactionBean.setTxnAmount("forexPercentage");
        eodTransactionBean.setTxnDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        eodTransactionBean.setTxnDescription("txnDescription");
        eodTransactionBean.setTxnId("txnId");
        eodTransactionBean.setTxnType("TTC049");
        eodTransactionBean.setCurrencyType("currencyType");
        eodTransactionBean.setMcc("mcc");
        eodTransactionBean.setBillingAmount("billingAmount");
        eodTransactionBean.setFuelSurchargeAmount("fuelSurchargeAmount");
        eodTransactionBean.setRequestFrom("requestFrom");
        eodTransactionBean.setSecondPartyPan("secondPartyPan");
        eodTransactionBean.setChannelType(0);
        eodTransactionBean.setCardAssociation("cardAssociation");

        Configurations.YES_STATUS = "YES";

        when(originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao.getForexPercentage())
                .thenReturn("result");

        // Configure OriginatorPushTxnUpdateDao.getFinancialStatus(...).
        final HashMap<String, String> stringStringHashMap = new HashMap<>(Map.ofEntries(Map.entry("TTC049", "NO")));
        when(originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao.getFinancialStatus())
                .thenReturn(stringStringHashMap);

        when(originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao.insertToEODTransaction(
                any(StringBuffer.class), eq("accountNo"), eq("mid"), eq("tid"), eq("billingAmount"), eq(0), eq("crDr"),
                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()),
                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()), eq("backendTxnType"), eq("batchNo"),
                eq("txnId"), eq("toAccNo"), eq(0.0), eq("txnDescription"), eq("countryNumCode"), eq(0),
                eq("posEntryMode"), eq("traceId"), eq("authCode"), eq(5), eq("requestFrom"), eq("secondPartyPan"),
                eq("fuelSurchargeAmount"), eq("mcc"), eq("cardAssociation"))).thenReturn(0);
        when(originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao.updateTransactionToEDON(eq("txnId"),
                any(StringBuffer.class))).thenReturn(0);
        when(originatorPushTxnUpdateServiceUnderTest.logManager.logDetails(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdate(eodTransactionBean);

        // Verify the results
//         verify(originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao, times(2)).insertToEODTransaction(any(StringBuffer.class), anyString(), anyString(), anyString(), anyString(),
//                anyInt(), anyString(), any(Date.class), any(Date.class), anyString(), anyString(),anyString(),anyString(),anyDouble(),anyString(),
//                anyString(),anyInt(),anyString(),anyString(),anyString(),anyInt(),anyString(),anyString(),anyString(),anyString(),anyString());
//         verify(originatorPushTxnUpdateServiceUnderTest.originatorPushTxnUpdateDao, times(2)).updateTransactionToEDON(anyString(), any(StringBuffer.class));

    }
}
