package com.epic.cms.service;

import com.epic.cms.dao.RecipientPushTxnUpdateDao;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.RecipientPushTxnUpdateRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.epic.cms.util.Configurations;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RecipientPushTxnUpdateServiceTest {

    private RecipientPushTxnUpdateService recipientPushTxnUpdateServiceUnderTest;

    @BeforeEach
    void setUp() {
        recipientPushTxnUpdateServiceUnderTest = new RecipientPushTxnUpdateService();
        recipientPushTxnUpdateServiceUnderTest.commonRepo = mock(CommonRepo.class);
        recipientPushTxnUpdateServiceUnderTest.status = mock(StatusVarList.class);
        recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao = mock(RecipientPushTxnUpdateRepo.class);
        recipientPushTxnUpdateServiceUnderTest.logManager = mock(LogManager.class);
    }

    @Test
    void testRecipientPushTxnUpdate() throws Exception {
        // Setup
        HashMap<String, String> financialStatusList = new HashMap<>();

        EodTransactionBean eodTransactionBean = new EodTransactionBean();

        Configurations.YES_STATUS = "YES";
        Configurations.EOD_PENDING_STATUS = "EDON";
        //status.getPRODUCT_CODE_QR_ALL() = "QAL";

        // Configure
        String maskCardNo = "438043******8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        eodTransactionBean.setAuthCode("AUTHCODE");
        eodTransactionBean.setBatchNo("BATCHNO");
        eodTransactionBean.setCardNo(new StringBuffer("4380431766518012"));
        eodTransactionBean.setCountryNumCode("COUNTRYCODE");
        eodTransactionBean.setCurrencyType("TXNCURRENCY");
        eodTransactionBean.setMid("MID");
        eodTransactionBean.setOnOffStatus(0);
        eodTransactionBean.setPosEntryMode("POSENTRYMODE");
        eodTransactionBean.setRrn("RRN");
        eodTransactionBean.setSequenceNumber("CB_SEQ_NO");
        eodTransactionBean.setSettlementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        eodTransactionBean.setTid("TID");
        eodTransactionBean.setToAccNo("TOACCOUNT");
        eodTransactionBean.setTraceId("TRACENO");
        eodTransactionBean.setTxnAmount("TRANSACTIONAMOUNT");
        eodTransactionBean.setTxnDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        eodTransactionBean.setTxnDescription("CAIC");
        eodTransactionBean.setTxnId("TXNID");
        eodTransactionBean.setTxnType("txnType");
        eodTransactionBean.setMcc("MCC");
        eodTransactionBean.setBillingAmount("BILLINGAMOUNT");
        eodTransactionBean.setRequestFrom("REQUESTFROM");
        eodTransactionBean.setChannelType(1);


        financialStatusList.put("txnType", "YES");

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(eodTransactionBean.getCardNo()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(eodTransactionBean.getCardNo()));
        }

        when(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.getFinancialStatus()).thenReturn(financialStatusList);
        when(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.updateTransactionToEDON("TXNID", new StringBuffer("cardNumber"))).thenReturn(0);
        when(recipientPushTxnUpdateServiceUnderTest.status.getPRODUCT_CODE_QR_ALL()).thenReturn("QAL");
        when(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.getCardProduct(any())).thenReturn("cardproduct");
        when(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.insertIntoEodMerchantTransaction(any(EodTransactionBean.class),  eq("EDON"))).thenReturn(1);
        when(recipientPushTxnUpdateServiceUnderTest.logManager.logDetails(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdate(eodTransactionBean);

        verify(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao, times(1)).getFinancialStatus();
        assertThat(0).isEqualTo(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.updateTransactionToEDON(any(), any()));
        assertThat("cardproduct").isEqualTo(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.getCardProduct(any()));
        assertThat("QAL").isEqualTo(recipientPushTxnUpdateServiceUnderTest.status.getPRODUCT_CODE_QR_ALL());
        assertThat(0).isEqualTo(recipientPushTxnUpdateServiceUnderTest.recipientPushTxnUpdateDao.insertIntoEodMerchantTransaction(any(EodTransactionBean.class), eq("EDON")));


    }



}
