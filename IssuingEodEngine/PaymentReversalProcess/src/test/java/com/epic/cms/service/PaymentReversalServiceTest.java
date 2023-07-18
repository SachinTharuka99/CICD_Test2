package com.epic.cms.service;

import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.PaymentReversalRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class PaymentReversalServiceTest {
    int capacity = 200000;
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<Integer>(capacity);
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue <Integer>(capacity);
    private PaymentReversalService paymentReversalServiceUnderTest;
    private StatusVarList statusList;
    private CommonMethods commonMethods;
    @BeforeEach
    void setUp() {
        statusList = new StatusVarList();
        paymentReversalServiceUnderTest = new PaymentReversalService();
        paymentReversalServiceUnderTest.logManager = mock(LogManager.class);
        paymentReversalServiceUnderTest.status = mock(StatusVarList.class);
        paymentReversalServiceUnderTest.commonRepo = mock(CommonRepo.class);
        paymentReversalServiceUnderTest.paymentReversalRepo = mock(PaymentReversalRepo.class);
        commonMethods = new CommonMethods();
    }

    @Test
    @DisplayName("Test case for update payment reversal")
    void setPaymentReversalServiceUnderTest() throws Exception {
        final PaymentBean bean = new PaymentBean();
        bean.setCardnumber(new StringBuffer("438043****8012"));
        bean.setTraceid("123456789");
        when(paymentReversalServiceUnderTest.paymentReversalRepo.updatePaymentsForCashReversals(any(StringBuffer.class), anyString())).thenReturn(1);
        paymentReversalServiceUnderTest.setPaymentReversals(bean,successCount,failCount);
        verify(paymentReversalServiceUnderTest.paymentReversalRepo,times(1)).updatePaymentsForCashReversals(
                any(StringBuffer.class), anyString()
        );
        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(bean.getCardnumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(bean.getCardnumber()));
        }
    }
}
