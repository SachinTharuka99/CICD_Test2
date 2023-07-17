package com.epic.cms.service;

import com.epic.cms.model.bean.CardBillingInfoBean;
import com.epic.cms.model.bean.EomCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EOMInterestRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EOMInterestServiceTest {

    private EOMInterestService eomInterestServiceUnderTest;
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        eomInterestServiceUnderTest = new EOMInterestService();
        eomInterestServiceUnderTest.logManager = mock(LogManager.class);
        eomInterestServiceUnderTest.status = mock(StatusVarList.class);
        eomInterestServiceUnderTest.commonRepo = mock(CommonRepo.class);
        eomInterestServiceUnderTest.eomInterestRepo = mock(EOMInterestRepo.class);
    }

    @Test
    @DisplayName("Test EOM Interest Calculation Process")
    void testEOMInterestCalculation() throws Exception {
        // Setup
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

        EomCardBean eomCardBean = new EomCardBean();
        eomCardBean.setCardNo(new StringBuffer("4380437588012"));
        eomCardBean.setAccNo("211100031650");
        eomCardBean.setAccStatus("NP");
        eomCardBean.setInterestRate(0.0);
        eomCardBean.setInterestPeriod(0);

        String cardStatus = "CACL";
        final ArrayList<Date> dates = new ArrayList<>(List.of(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()));
        final ArrayList<EomCardBean> accountList = new ArrayList<>(List.of(eomCardBean));

        final CardBillingInfoBean cardBillingInfoBean = new CardBillingInfoBean();
        cardBillingInfoBean.setStartEodId(0);
        cardBillingInfoBean.setEndEodId(0);
        cardBillingInfoBean.setStatementStartDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        cardBillingInfoBean.setStatementEndDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        cardBillingInfoBean.setThisBillingClosingBalance(0.0);
        cardBillingInfoBean.setThisBillingOpeningBalance(0.0);
        cardBillingInfoBean.setDueDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        cardBillingInfoBean.setMinPayDue(0.0);

        //ArrayList<Date> lastBillingDates = new ArrayList<>("08-OCT-2022");

        String maskCardNo = "4380437588012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(eomCardBean.getCardNo())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(eomCardBean.getCardNo()));
        }

        when(eomInterestServiceUnderTest.eomInterestRepo.CheckForCardIncrementStatus(any(StringBuffer.class))).thenReturn("NP");
        when(eomInterestServiceUnderTest.status.getCARD_CLOSED_STATUS()).thenReturn("CACL");

        when(eomInterestServiceUnderTest.eomInterestRepo.clearEomInterest(any(StringBuffer.class))).thenReturn(0);

        // Configure EOMInterestRepo.getLastTwoBillingDatesOnAccount(...).
        when(eomInterestServiceUnderTest.eomInterestRepo.getLastTwoBillingDatesOnAccount("211100031650")).thenReturn(dates);

        // Configure EOMInterestRepo.getLastTwoBillingDatesAndEodIdOnAccount(...).
        when(eomInterestServiceUnderTest.eomInterestRepo.getLastTwoBillingDatesAndEodIdOnAccount("211100031650")).thenReturn(cardBillingInfoBean);

        when(eomInterestServiceUnderTest.eomInterestRepo.getEOMInterest(any(EomCardBean.class),
                any(CardBillingInfoBean.class), eq(0))).thenReturn(new ArrayList<>(List.of(0.0)));
        when(eomInterestServiceUnderTest.status.getEOD_PENDING_STATUS()).thenReturn("EOD_PENDING_STATUS");
        when(eomInterestServiceUnderTest.eomInterestRepo.insertIntoEomInterest(any(StringBuffer.class), eq("211100031650"),
                eq(0.0), eq(0.0), eq(0), eq("EOD_PENDING_STATUS"))).thenReturn(0);
        when(eomInterestServiceUnderTest.eomInterestRepo.insertIntoEodGLAccount(eq(0),
                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()), any(StringBuffer.class),
                eq("TXN_TYPE_INTEREST_INCOME"), eq(0.0), eq("DEBIT"), eq("payType"))).thenReturn(0);

        // Run the test
        eomInterestServiceUnderTest.EOMInterestCalculation(processBean, eomCardBean,faileCardCount);

        // Verify the results
        verify(eomInterestServiceUnderTest.eomInterestRepo,times(1)).clearEomInterest(eomCardBean.getCardNo());
        verify(eomInterestServiceUnderTest.eomInterestRepo,times(1)).getLastTwoBillingDatesOnAccount(eomCardBean.getAccNo());
        verify(eomInterestServiceUnderTest.eomInterestRepo,times(1)).getLastTwoBillingDatesAndEodIdOnAccount(eomCardBean.getAccNo());
        verify(eomInterestServiceUnderTest.eomInterestRepo,times(1)).getEOMInterest(eomCardBean, cardBillingInfoBean, dates.size());
    }
}
