/**
 * Author : rasintha_j
 * Date : 08/11/2022
 * Time : 08:55 PM
 * Project Name : ecms_eod_engine
 */
package com.epic.cms.service;

import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CheckPaymentForMinimumAmountRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CheckPaymentForMinimumAmountServiceTest {

    private CheckPaymentForMinimumAmountService checkPaymentForMinimumAmountServiceUnderTest;

    @BeforeEach
    void setUp() {
        checkPaymentForMinimumAmountServiceUnderTest = new CheckPaymentForMinimumAmountService();
        checkPaymentForMinimumAmountServiceUnderTest.logManager = mock(LogManager.class);
        checkPaymentForMinimumAmountServiceUnderTest.checkPaymentForMinimumAmountRepo = mock(CheckPaymentForMinimumAmountRepo.class);
        checkPaymentForMinimumAmountServiceUnderTest.status = mock(StatusVarList.class);
        checkPaymentForMinimumAmountServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

//    @Test
//    void testCheckPaymentForMinimumAmount() throws Exception {
//        // Setup
//        final LastStatementSummeryBean lastStatement = new LastStatementSummeryBean();
//        lastStatement.setCardno(new StringBuffer("4380431766518012"));
//        lastStatement.setStatementEndDate(new Date(System.currentTimeMillis()));
//        lastStatement.setMinAmount(10.0);
//        lastStatement.setClosingBalance(0.0);
//        lastStatement.setOpaningBalance(0.0);
//        lastStatement.setDueDate(new Date(System.currentTimeMillis()));
//        lastStatement.setStatementStartDate(new Date(System.currentTimeMillis()));
//        lastStatement.setAccNo("accNo");
//        lastStatement.setNDIA(0);
//        lastStatement.setClosingloyaltypoint(0L);
//
//        final ProcessBean processBean = new ProcessBean();
//        processBean.setProcessId(0);
//        processBean.setProcessDes("processDes");
//        processBean.setCriticalStatus(0);
//        processBean.setRollBackStatus(0);
//        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
//        processBean.setSheduleTime("sheduleTime");
//        processBean.setFrequencyType(0);
//        processBean.setContinuousFrequencyType(0);
//        processBean.setContinuousFrequency(0);
//        processBean.setMultiCycleStatus(0);
//        processBean.setProcessCategoryId(0);
//        processBean.setDependancyStatus(0);
//        processBean.setRunningOnMain(0);
//        processBean.setRunningOnSub(0);
//        processBean.setProcessType(0);
//
//        // Configure
//        String maskCardNo = "438043******8012";
//        Configurations.START_INDEX = 6;
//        Configurations.END_INDEX =12;
//        Configurations.PATTERN_CHAR = "*";
//        Configurations.EOD_DATE = new Date(System.currentTimeMillis());
//
//        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
//            theMock.when(() -> CommonMethods.cardNumberMask(lastStatement.getCardno()))
//                    .thenReturn(maskCardNo);
//            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(lastStatement.getCardno()));
//        }
//
//        when(checkPaymentForMinimumAmountServiceUnderTest.checkPaymentForMinimumAmountRepo.getAccountNoOnCard(any(StringBuffer.class))).thenReturn("accNo");
//        when(checkPaymentForMinimumAmountServiceUnderTest.checkPaymentForMinimumAmountRepo.getPaymentAmount(anyString(), anyInt())).thenReturn(0.0);
//        when(checkPaymentForMinimumAmountServiceUnderTest.checkPaymentForMinimumAmountRepo.getTotalPaymentExceptDueDate(anyString(), anyInt())).thenReturn(0.0);
//        when(checkPaymentForMinimumAmountServiceUnderTest.checkPaymentForMinimumAmountRepo.insertToMinPayTable(any(StringBuffer.class), anyDouble(), anyDouble(), eq(Date.valueOf(LocalDate.of(2020, 1, 1))), eq("accNo"), eq(1), eq(0.0), eq(0.0))).thenReturn(false);
//
//        // Run the test
//        checkPaymentForMinimumAmountServiceUnderTest.CheckPaymentForMinimumAmount(lastStatement, processBean);
//
//        // Verify the results
//        verify(checkPaymentForMinimumAmountServiceUnderTest.checkPaymentForMinimumAmountRepo,times(1)).insertToMinPayTable(any(StringBuffer.class), anyDouble(), anyDouble(), any(Date.class), anyString(), anyInt(), anyDouble(), anyDouble());
//    }
}
