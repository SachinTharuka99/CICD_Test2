package com.epic.cms.service;

import com.epic.cms.model.bean.LimitIncrementBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.IncrementLimitExpireRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IncrementLimitExpireServiceTest {

    private IncrementLimitExpireService incrementLimitExpireServiceUnderTest;
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        incrementLimitExpireServiceUnderTest = new IncrementLimitExpireService();
        incrementLimitExpireServiceUnderTest.status = mock(StatusVarList.class);
        incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo = mock(IncrementLimitExpireRepo.class);
        incrementLimitExpireServiceUnderTest.logManager = mock(LogManager.class);
    }

    @Test
    void testProcessCreditLimitExpire() throws Exception {
        // Setup
        final LimitIncrementBean limitIncrementBean = new LimitIncrementBean();
        limitIncrementBean.setCustomerid("customerid");
        limitIncrementBean.setAccountnumber("438043****8012");
        limitIncrementBean.setCardNumber(new StringBuffer("438043****8012"));
        limitIncrementBean.setCardcategorycode("1");
        limitIncrementBean.setCustotbcredit(0.0);
        limitIncrementBean.setCustotbcash(0.0);
        limitIncrementBean.setAccotbcredit(0.0);
        limitIncrementBean.setAccotbcash(0.0);
        limitIncrementBean.setOtbcredit(0.0);
        limitIncrementBean.setOtbcash(0.0);
        limitIncrementBean.setIncrementAmount("incrementAmount");
        limitIncrementBean.setIncrementType("1");
        limitIncrementBean.setIncordec("incordec");
        limitIncrementBean.setRequestid("requestid");

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

        Configurations.CREDIT_INCREMENT ="1";
        Configurations.CARD_CATEGORY_MAIN = "1";
        Configurations.CARD_CATEGORY_ESTABLISHMENT = "1";
        Configurations.CARD_CATEGORY_FD = "1";
        Configurations.CARD_CATEGORY_AFFINITY = "1";
        Configurations.CARD_CATEGORY_CO_BRANDED = "1";
        Configurations.CASH_INCREMENT = "1";

        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(limitIncrementBean.getCardNumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(limitIncrementBean.getCardNumber()));
        }

        when(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo.expireCreditLimit(
                any(LimitIncrementBean.class))).thenReturn(0);
        when(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo.expireOnlineCreditLimit(
                any(LimitIncrementBean.class))).thenReturn(0);
        when(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo.expireCashLimit(
                any(LimitIncrementBean.class))).thenReturn(0);
        when(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo.expireOnlineCashLimit(
                any(LimitIncrementBean.class))).thenReturn(0);
        when(incrementLimitExpireServiceUnderTest.status.getCREDIT_LIMIT_ENHANCEMENT_EXPIRED())
                .thenReturn("CREDIT_LIMIT_ENHANCEMENT_EXPIRED");
        when(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo.updateTempLimitIncrementTable(
                any(StringBuffer.class), eq("CREDIT_LIMIT_ENHANCEMENT_EXPIRED"), eq("requestid"), eq(0))).thenReturn(0);



        // Run the test
        incrementLimitExpireServiceUnderTest.processCreditLimitExpire(limitIncrementBean, processBean, 0,
                "processHeader",  faileCardCount);

        // Verify the results
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).limitExpireOnAccount(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).limitOnlineExpireOnAccount(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).limitExpireOnCustomer(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).limitOnlineExpireOnCustomer(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).cashLimitExpireOnAccount(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).cashLimitOnlineExpireOnAccount(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).cashLimitExpireOnCustomer(
                any(LimitIncrementBean.class));
        verify(incrementLimitExpireServiceUnderTest.incrementLimitExpireRepo).cashLimitOnlineExpireOnCustomer(
                any(LimitIncrementBean.class));
    }
}
