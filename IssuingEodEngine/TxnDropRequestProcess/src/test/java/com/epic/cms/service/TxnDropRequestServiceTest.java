package com.epic.cms.service;

import com.epic.cms.model.bean.DropRequestBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.TxnDropRequestRepo;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TxnDropRequestServiceTest {

    private TxnDropRequestService txnDropRequestServiceUnderTest;
    int capacity = 200000;
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<Integer>(capacity);
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue <Integer>(capacity);

    @BeforeEach
    void setUp() {
        txnDropRequestServiceUnderTest = new TxnDropRequestService();
        txnDropRequestServiceUnderTest.txnDropRequestRepo = mock(TxnDropRequestRepo.class);
        txnDropRequestServiceUnderTest.statusList = mock(StatusVarList.class);
        txnDropRequestServiceUnderTest.logManager = mock(LogManager.class);
    }

    @Test
    @DisplayName("Test transaction drop request  Process")
    void testProcessTxnDropRequest() throws Exception {
        // Setup
        final DropRequestBean bean = new DropRequestBean();
        bean.setCardNumber(new StringBuffer("438043****8012"));
        bean.setTxnId("txnId");

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

        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(bean.getCardNumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(bean.getCardNumber()));
        }

        when(txnDropRequestServiceUnderTest.txnDropRequestRepo.getTransactionReverseStatus("txnId")).thenReturn(false);

        // Run the test
        txnDropRequestServiceUnderTest.processTxnDropRequest(bean, processBean,successCount,failCount);

        // Verify the results
        verify(txnDropRequestServiceUnderTest.txnDropRequestRepo,times(1)).addTxnDropRequest(eq("txnId"),
                any(StringBuffer.class));
    }
}
