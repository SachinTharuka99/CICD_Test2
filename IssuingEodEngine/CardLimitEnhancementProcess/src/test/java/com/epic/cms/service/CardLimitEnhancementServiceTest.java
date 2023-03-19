package com.epic.cms.service;

import com.epic.cms.model.bean.BalanceComponentBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.CardLimitEnhancementRepo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardLimitEnhancementServiceTest {

    private CardLimitEnhancementService cardLimitEnhancementServiceUnderTest;
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
        cardLimitEnhancementServiceUnderTest = new CardLimitEnhancementService();
        //cardLimitEnhancementServiceUnderTest.commonRepo = mock(CommonRepo.class);
        cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo = mock(CardLimitEnhancementRepo.class);
        cardLimitEnhancementServiceUnderTest.status = mock(StatusVarList.class);

        String maskCardNo = "value";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(new StringBuffer("value")))
                    .thenReturn(maskCardNo);
        }
    }

//    @Test
//    void testProcessCardLimitEnhancementForCredit() throws Exception {
//        // Setup
//        final BalanceComponentBean balanceComponentBean = new BalanceComponentBean();
//        balanceComponentBean.setCustomerId("customerId");
//        balanceComponentBean.setAccountNumber("accountNumber");
//        balanceComponentBean.setCardNumber(new StringBuffer("4380431766518012"));
//        balanceComponentBean.setCustOtbCredit(0.0);
//        balanceComponentBean.setCustOtbCash(0.0);
//        balanceComponentBean.setCustOtbCreditComp(0.0);
//        balanceComponentBean.setOtbCredit(0.0);
//        balanceComponentBean.setOtbCash(0.0);
//        balanceComponentBean.setCardCategory("CARDMAIN");
//        balanceComponentBean.setIncrementAmount(0.0);
//        balanceComponentBean.setIncrementType("CRI");
//        balanceComponentBean.setIncOrDec("incOrDec");
//        balanceComponentBean.setRequestId("requestId");
//        balanceComponentBean.setStartDate("startDate");
//        balanceComponentBean.setEndDate("endDate");
//
//        final ArrayList<BalanceComponentBean> enhancementList = new ArrayList<>(List.of(balanceComponentBean));
//        final OtbBean bean = new OtbBean();
//        bean.setAccountnumber("accountnumber");
//        bean.setCardnumber(new StringBuffer("4380431766518012"));
//        bean.setCustomerid("customerid");
//        bean.setOtbcredit(0.0);
//        bean.setOtbcash(0.0);
//        bean.setFinacialcharges(0.0);
//        bean.setCumpayment(0.0);
//        bean.setCumcashadvance(0.0);
//        bean.setCumtransactions(0.0);
//        bean.setTmpcredit(0.0);
//        bean.setTmpcash(0.0);
//        bean.setTxnAmount(0.0);
//        bean.setTxntype("txntype");
//        bean.setTxntypedesc("txntypedesc");
//        bean.setPayment(0.0);
//
//        // Configure
//        Configurations.CREDIT_INCREMENT ="CRI";
//        Configurations.CARD_CATEGORY_MAIN ="CARDMAIN";
//
//        when(cardLimitEnhancementServiceUnderTest.status.getCREDIT_LIMIT_ENHANCEMENT_ACTIVE())
//                .thenReturn("CREDIT_LIMIT_ENHANCEMENT_ACTIVE");
//        when(cardLimitEnhancementServiceUnderTest.logManager.processDetailsStyles(
//                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
//
//        // Run the test
//        cardLimitEnhancementServiceUnderTest.processCardLimitEnhancement(enhancementList, bean);
//
//        // Verify the results
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo,times(1)).updateCardCreditLimit(
//                any(StringBuffer.class), anyDouble());
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateOnlineCardCreditLimit(
//                any(StringBuffer.class), eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateAccountCreditLimit(eq("accountnumber"),
//                eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateOnlineAccountCreditLimit(
//                eq("accountnumber"), eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateCustomerCreditLimit(eq("customerid"),
//                eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateOnlineCustomerCreditLimit(
//                eq("customerid"), eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateTempLimitIncrementTable(
//                any(StringBuffer.class), eq("CREDIT_LIMIT_ENHANCEMENT_ACTIVE"), eq("requestId"));
//    }
//
//    @Test
//    void testProcessCardLimitEnhancementForCash() throws Exception {
//        // Setup
//        final BalanceComponentBean balanceComponentBean = new BalanceComponentBean();
//        balanceComponentBean.setCustomerId("customerId");
//        balanceComponentBean.setAccountNumber("accountNumber");
//        balanceComponentBean.setCardNumber(new StringBuffer("4380431766518012"));
//        balanceComponentBean.setCustOtbCredit(0.0);
//        balanceComponentBean.setCustOtbCash(0.0);
//        balanceComponentBean.setCustOtbCreditComp(0.0);
//        balanceComponentBean.setOtbCredit(0.0);
//        balanceComponentBean.setOtbCash(0.0);
//        balanceComponentBean.setCardCategory("CARDMAIN");
//        balanceComponentBean.setIncrementAmount(0.0);
//        balanceComponentBean.setIncrementType("CASHI");
//        balanceComponentBean.setIncOrDec("incOrDec");
//        balanceComponentBean.setRequestId("requestId");
//        balanceComponentBean.setStartDate("startDate");
//        balanceComponentBean.setEndDate("endDate");
//        final ArrayList<BalanceComponentBean> enhancementList = new ArrayList<>(List.of(balanceComponentBean));
//        final OtbBean bean = new OtbBean();
//        bean.setAccountnumber("accountnumber");
//        bean.setCardnumber(new StringBuffer("4380431766518012"));
//        bean.setCustomerid("customerid");
//        bean.setOtbcredit(0.0);
//        bean.setOtbcash(0.0);
//        bean.setFinacialcharges(0.0);
//        bean.setCumpayment(0.0);
//        bean.setCumcashadvance(0.0);
//        bean.setCumtransactions(0.0);
//        bean.setTmpcredit(0.0);
//        bean.setTmpcash(0.0);
//        bean.setTxnAmount(0.0);
//        bean.setTxntype("txntype");
//        bean.setTxntypedesc("txntypedesc");
//        bean.setPayment(0.0);
//
//        // Configure
//        Configurations.CASH_INCREMENT ="CASHI";
//        Configurations.CARD_CATEGORY_MAIN ="CARDMAIN";
//
//        when(cardLimitEnhancementServiceUnderTest.status.getCREDIT_LIMIT_ENHANCEMENT_ACTIVE())
//                .thenReturn("CREDIT_LIMIT_ENHANCEMENT_ACTIVE");
//        when(cardLimitEnhancementServiceUnderTest.logManager.processDetailsStyles(
//                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
//
//        // Run the test
//        cardLimitEnhancementServiceUnderTest.processCardLimitEnhancement(enhancementList, bean);
//
//        // Verify the results
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateCardCashLimit(
//                any(StringBuffer.class), eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateOnlineCardCashLimit(
//                any(StringBuffer.class), eq(0.0));
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateAccountCashLimit("accountnumber",
//                0.0);
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateOnlineAccountCashLimit(
//                "accountnumber", 0.0);
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateCustomerCashLimit("customerid",
//                0.0);
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateOnlineCustomerCashLimit(
//                "customerid", 0.0);
//        verify(cardLimitEnhancementServiceUnderTest.cardLimitEnhancementRepo).updateTempLimitIncrementTable(
//                any(StringBuffer.class), eq("CREDIT_LIMIT_ENHANCEMENT_ACTIVE"), eq("requestId"));
//    }

}
