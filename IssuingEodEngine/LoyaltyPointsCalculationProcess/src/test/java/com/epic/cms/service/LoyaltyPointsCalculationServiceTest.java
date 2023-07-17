package com.epic.cms.service;

import com.epic.cms.model.bean.LoyaltyBean;
import com.epic.cms.repository.LoyaltyPointsCalculationRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoyaltyPointsCalculationServiceTest {

    private LoyaltyPointsCalculationService loyaltyPointsCalculationServiceUnderTest;
    private CommonMethods commonMethods;
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        loyaltyPointsCalculationServiceUnderTest = new LoyaltyPointsCalculationService();
        loyaltyPointsCalculationServiceUnderTest.statusList = mock(StatusVarList.class);
        loyaltyPointsCalculationServiceUnderTest.logManager = mock(LogManager.class);
        loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo = mock(
                LoyaltyPointsCalculationRepo.class);
        commonMethods = new CommonMethods();
    }

    @Test
    @DisplayName("Test Loyalty points calculation process")
    void testCalculateLoyaltyPoints() throws Exception {
        // Setup
        final LoyaltyBean loyaltyBean = new LoyaltyBean();
        loyaltyBean.setOpeningLoyaltyPoints(5.0);
        loyaltyBean.setEarnLoyaltyPoints(4.0);
        loyaltyBean.setAvailableLoyaltyPoints(3.0);
        loyaltyBean.setAdjustLoyaltyPoints(2.0);
        loyaltyBean.setRedeemLoyaltyPoints(10.0);
        loyaltyBean.setClosingLoyaltyPoints(9.0);
        loyaltyBean.setPurchase(0.0);
        loyaltyBean.setCardNo(new StringBuffer("4380433458012"));
        loyaltyBean.setAccNo("2380433458012");
        loyaltyBean.setStatementId("100");
        loyaltyBean.setStmtStartEodID(2);
        loyaltyBean.setStmtEndEodID(2);
        loyaltyBean.setStmtStartDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        loyaltyBean.setStmtEndDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());

        ArrayList<Integer> requestID = new ArrayList<>();
        requestID.add(1);
        requestID.add(2);
        requestID.add(3);
        requestID.add(4);
        Configurations.EOD_DONE_STATUS = "done";

        StringBuffer cardNo= new StringBuffer("4380433458012");

        String maskCardNo = "4380433458012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(loyaltyBean.getCardNo())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(loyaltyBean.getCardNo()));
        }

        when(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo.getLastStmtClosingLoyalty(
                any(StringBuffer.class), anyString())).thenReturn(0.0);
        when(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo.getThisMonthPurchases("2380433458012", 0,
                0)).thenReturn(0.0);
        when(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo.getThisMonthRedeem(
                any(StringBuffer.class), eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()),
                eq(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime()),
                eq(requestID))).thenReturn(0.0);
        when(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo.getAdjustLoyalty("2380433458012",
                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(),
                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(0.0);

        // Run the test
        loyaltyPointsCalculationServiceUnderTest.calculateLoyaltyPoints(loyaltyBean, faileCardCount);

        // Verify the results
        verify(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo,times(1)).getLastStmtClosingLoyalty(
                loyaltyBean.getCardNo(), loyaltyBean.getStatementId());
        verify(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo,times(1)).getThisMonthPurchases(
                loyaltyBean.getAccNo(), loyaltyBean.getStmtEndEodID(), loyaltyBean.getStmtStartEodID());
//        verify(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo, times(1)).getThisMonthRedeem(
//                loyaltyBean.getCardNo(), loyaltyBean.getStmtEndDate(), loyaltyBean.getStmtStartDate(), requestID);
//        verify(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo,times(1)).getAdjustLoyalty(
//                loyaltyBean.getAccNo(), any(Date.class), any(Date.class));
        verify(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo,times(1)).updateBillingStatment(
                any(LoyaltyBean.class));
//        verify(loyaltyPointsCalculationServiceUnderTest.loyaltyPointsCalculationRepo,times(1)).updateLoyaltyRedeemRequest(
//                requestID, "EOD_DONE_STATUS");

    }

}
