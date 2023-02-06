package com.epic.cms.service;

import com.epic.cms.model.bean.DailyInterestBean;
import com.epic.cms.model.bean.InterestDetailBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.repository.DailyInterestCalculationRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DailyInterestCalculationServiceTest {

    private DailyInterestCalculationService dailyInterestCalculationServiceUnderTest;

    @BeforeEach
    void setUp() {
        dailyInterestCalculationServiceUnderTest = new DailyInterestCalculationService();
        dailyInterestCalculationServiceUnderTest.interestCalculationRepo = mock(DailyInterestCalculationRepo.class);
    }

    @Test
    void testStartDailyInterestCalculation() throws Exception {
        // Setup
        final StatementBean stmtBean = new StatementBean();
        stmtBean.setBillingID("billingID");
        stmtBean.setStatementID("statementID");
        stmtBean.setStartEodID(0);
        stmtBean.setEndEodID(0);
        stmtBean.setAccountNo("accountNo");
        stmtBean.setCardNo(new StringBuffer("4380431766518012"));
        stmtBean.setClosingBalance(0.0);
        stmtBean.setLastBillclosingBalance(0.0);
        stmtBean.setStartingBalance(0.0);
        stmtBean.setMainCardNo(new StringBuffer("4380431766518012"));
        stmtBean.setCardCategory("cardCategory");
        stmtBean.setStatementStartDate(new Date(System.currentTimeMillis()));
        stmtBean.setStatementEndDate(new Date(System.currentTimeMillis()));
        stmtBean.setStatementDueDate(new Date(System.currentTimeMillis()));
        stmtBean.setOldNextBillingDate(new Date(System.currentTimeMillis()));

        // Configure
        String maskCardNo = "value";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(stmtBean.getCardNo()))
                    .thenReturn(maskCardNo);

            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(stmtBean.getCardNo()));
        }

        // Configure
        final InterestDetailBean interestDetailBean = new InterestDetailBean();
        interestDetailBean.setInterest(0.0);
        interestDetailBean.setStatementEndDate(new Date(System.currentTimeMillis()));
        interestDetailBean.setInterestperiod(0.0);
        when(dailyInterestCalculationServiceUnderTest.interestCalculationRepo.getIntProf("accountNo"))
                .thenReturn(interestDetailBean);

        // Configure
        final DailyInterestBean dailyInterestBean = new DailyInterestBean();
        dailyInterestBean.setAmount(0.0);
        dailyInterestBean.setNoOfDays(0);
        final ArrayList<DailyInterestBean> dailyInterestBeanList = new ArrayList<>(List.of(dailyInterestBean));
        when(dailyInterestCalculationServiceUnderTest.interestCalculationRepo.getTxnOrPaymentDetailByAccount(
                anyString(), anyInt(), anyInt(), any(Date.class), anyDouble(),
                any(Date.class),any(Date.class), anyInt())).thenReturn(dailyInterestBeanList);

        when(dailyInterestCalculationServiceUnderTest.interestCalculationRepo.updateEodInterest(
                any(StatementBean.class), eq(0.0), eq(0.0))).thenReturn(0);


        // Run the test
        dailyInterestCalculationServiceUnderTest.startDailyInterestCalculation(stmtBean);

        // Verify the results
        verify(dailyInterestCalculationServiceUnderTest.interestCalculationRepo, times(1)).updateEodInterest(
                any(StatementBean.class), eq(0.0), eq(0.0));
    }

    @Test
    void testCalculateInterest() throws Exception {
        // Setup
        final InterestDetailBean interestDetailBean = new InterestDetailBean();
        interestDetailBean.setInterest(0.0);
        interestDetailBean.setStatementEndDate(new Date(System.currentTimeMillis()));
        interestDetailBean.setInterestperiod(0.0);

        // Run the test
        final double result = dailyInterestCalculationServiceUnderTest.calculateInterest(0.0, 0, interestDetailBean);

        // Verify the results
        assertThat(result).isEqualTo(0.0, within(0.0001));
    }

}
