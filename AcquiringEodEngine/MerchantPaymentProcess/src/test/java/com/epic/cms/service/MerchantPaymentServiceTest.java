package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantPaymentRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MerchantPaymentServiceTest {

    private MerchantPaymentService merchantPaymentServiceUnderTest;

    @BeforeEach
    void setUp() {
        merchantPaymentServiceUnderTest = new MerchantPaymentService();
        merchantPaymentServiceUnderTest.logManager = mock(LogManager.class);
        merchantPaymentServiceUnderTest.status = mock(StatusVarList.class);
        merchantPaymentServiceUnderTest.commonRepo = mock(CommonRepo.class);
        merchantPaymentServiceUnderTest.merchantPaymentRepo = mock(MerchantPaymentRepo.class);
    }

    @Test
    void testStartMerchantPayment() throws Exception {
        // Setup
        // Configure CommonRepo.getProcessDetails(...).
        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setStepId(0);
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

        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
        Configurations.PROCESS_SUCCESS_COUNT = 0;
        Configurations.PROCESS_FAILD_COUNT = 0;

        // Setup
        int[] txnCounts = new int[]{0,0,0};
        Configurations.EOD_DATE = new Date();

        when(merchantPaymentServiceUnderTest.logManager.processHeaderStyle(
                "Merchant Payment Process process ")).thenReturn("result");

        when(merchantPaymentServiceUnderTest.commonRepo.getProcessDetails(0)).thenReturn(processBean);

        when(merchantPaymentServiceUnderTest.merchantPaymentRepo.callStoredProcedureForEodMerchantPayment())
                .thenReturn(txnCounts);

        // Run the test
        merchantPaymentServiceUnderTest.startMerchantPayment();

        // Verify the results
        assertThat(txnCounts).isEqualTo(merchantPaymentServiceUnderTest.merchantPaymentRepo.callStoredProcedureForEodMerchantPayment());
    }
}
