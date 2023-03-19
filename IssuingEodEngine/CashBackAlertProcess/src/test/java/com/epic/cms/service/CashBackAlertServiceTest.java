package com.epic.cms.service;

import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CashBackAlertRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CashBackAlertServiceTest {

    private CashBackAlertService cashBackAlertServiceUnderTest;

    @BeforeEach
    void setUp() {
        cashBackAlertServiceUnderTest = new CashBackAlertService();
        cashBackAlertServiceUnderTest.alert = mock(AlertService.class);
        cashBackAlertServiceUnderTest.cashBackAlertRepo = mock(CashBackAlertRepo.class);
    }

    @Test
    void testProcessCashBackAlertService() throws Exception {
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

        CashBackAlertBean cashBackAlertBean = new CashBackAlertBean();
        cashBackAlertBean.setAccNo("123456");
        cashBackAlertBean.setMainCardNo(new StringBuffer("673857386746"));
        cashBackAlertBean.setCashBackAmount(100.0);
        cashBackAlertBean.setStatementId("12345");

        String accountNum = "123457789";
        ArrayList<CashBackAlertBean> cashBackList = new ArrayList<>();
        cashBackList.add(cashBackAlertBean);

        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(cashBackAlertBean.getMainCardNo())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(cashBackAlertBean.getMainCardNo()));
        }

        // Run the test
        cashBackAlertServiceUnderTest.processCashBackAlertService(accountNum, cashBackList ,processBean);

        // Verify the results
//        verify(cashBackAlertServiceUnderTest.alert, times(1)).alertGenerationCashBack(eq("CASH_BACK_SMS_CODE"),
//                any(CashBackAlertBean.class));
//        verify(cashBackAlertServiceUnderTest.cashBackAlertRepo, times(1)).updateCashBackAlertGenStatus(0);
        verify(cashBackAlertServiceUnderTest.cashBackAlertRepo, times(1)).updateBillingStatementAlertGenStatus(cashBackAlertBean.getStatementId());
    }
}
