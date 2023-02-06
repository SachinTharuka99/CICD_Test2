package com.epic.cms.service;

import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OverLimitFeeServiceTest {

    private OverLimitFeeService overLimitFeeServiceUnderTest;

    @BeforeEach
    void setUp() {
        overLimitFeeServiceUnderTest = new OverLimitFeeService();
        overLimitFeeServiceUnderTest.logManager = mock(LogManager.class);
        overLimitFeeServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    @DisplayName("Test Over Limit Fee Process")
    void testAddOverLimitFee() throws Exception {
        // Setup
        String accountNum = "438043****8012";
        StringBuffer cardNumber = new StringBuffer("438043****8012");

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

        final PaymentBean bean = new PaymentBean();
        bean.setCardnumber(new StringBuffer("438043****8012"));
        bean.setTraceid("123456789");
        Configurations.OVER_LIMIT_FEE = "FEC006";

        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(bean.getCardnumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(bean.getCardnumber()));
        }

        when(overLimitFeeServiceUnderTest.commonRepo.addCardFeeCount(any(StringBuffer.class), eq("OVER_LIMIT_FEE"),
                eq(0))).thenReturn(0);

        // Run the test
        overLimitFeeServiceUnderTest.addOverLimitFee(accountNum, cardNumber, processBean, "processHeader");

        // Verify the results
        verify(overLimitFeeServiceUnderTest.commonRepo,times(1)).addCardFeeCount(cardNumber, Configurations.OVER_LIMIT_FEE,
                0);
    }
}
