package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EodParameterResetProcessRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class EodParameterResetProcessServiceTest {

    private EodParameterResetProcessService eodParameterResetProcessServiceUnderTest;

    @BeforeEach
    void setUp() {
        eodParameterResetProcessServiceUnderTest = new EodParameterResetProcessService();
        eodParameterResetProcessServiceUnderTest.eodParameterResetProcessRepo = mock(
                EodParameterResetProcessRepo.class);
        eodParameterResetProcessServiceUnderTest.logManager = mock(LogManager.class);
        eodParameterResetProcessServiceUnderTest.status = mock(StatusVarList.class);
        eodParameterResetProcessServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    void testStartEodParameterResetProcess() throws Exception {
        // Setup
        when(eodParameterResetProcessServiceUnderTest.logManager.processHeaderStyle(
                "EOD Parameter Reset Process")).thenReturn("result");
        Configurations.STARTING_EOD_STATUS = "INIT";

        // Configure CommonRepo.getProcessDetails(...).
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
        when(eodParameterResetProcessServiceUnderTest.commonRepo.getProcessDetails(0)).thenReturn(processBean);

        when(eodParameterResetProcessServiceUnderTest.status.getINITIAL_STATUS()).thenReturn("INIT");
        when(eodParameterResetProcessServiceUnderTest.eodParameterResetProcessRepo.resetTerminalParameters())
                .thenReturn(0);
        when(eodParameterResetProcessServiceUnderTest.eodParameterResetProcessRepo.resetMerchantParameters())
                .thenReturn(0);
        // Run the test
        eodParameterResetProcessServiceUnderTest.startEodParameterResetProcess();

        // Verify the results
        verify(eodParameterResetProcessServiceUnderTest.commonRepo).insertToEodProcessSumery(0);
        verify(eodParameterResetProcessServiceUnderTest.eodParameterResetProcessRepo,times(1)).resetMerchantParameters();
        verify(eodParameterResetProcessServiceUnderTest.eodParameterResetProcessRepo).resetTerminalParameters();
    }
}
