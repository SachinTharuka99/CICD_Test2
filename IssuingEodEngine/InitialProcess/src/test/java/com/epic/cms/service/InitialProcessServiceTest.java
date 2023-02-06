package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InitialProcessRepo;
import com.epic.cms.util.CommonVarList;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InitialProcessServiceTest {

    private InitialProcessService initialProcessServiceUnderTest;

    @BeforeEach
    void setUp() {
        initialProcessServiceUnderTest = new InitialProcessService();
        initialProcessServiceUnderTest.initialProcessRepo = mock(InitialProcessRepo.class);
        initialProcessServiceUnderTest.commonVarList = mock(CommonVarList.class);
        initialProcessServiceUnderTest.logManager = mock(LogManager.class);
        initialProcessServiceUnderTest.status = mock(StatusVarList.class);
        initialProcessServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    void testGetMessage() {
        // Setup
        when(initialProcessServiceUnderTest.commonVarList.getTitle()).thenReturn("result");

        // Run the test
        final String result = initialProcessServiceUnderTest.getMessage();

        // Verify the results
        assertThat(result).isEqualTo("Message from Initial Process:result");
    }

    @Test
    void testStartInitialProcess() throws Exception {
        // Setup
        when(initialProcessServiceUnderTest.logManager.processHeaderStyle("Initial Process")).thenReturn("result");
        Configurations.STARTING_EOD_STATUS ="INIT";
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
        when(initialProcessServiceUnderTest.commonRepo.getProcessDetails(100)).thenReturn(processBean);

        when(initialProcessServiceUnderTest.status.getINITIAL_STATUS()).thenReturn("INIT");
        when(initialProcessServiceUnderTest.initialProcessRepo.swapEodCardBalance()).thenReturn(0);
        when(initialProcessServiceUnderTest.initialProcessRepo.insertIntoOpeningAccBal()).thenReturn(false);

        // Run the test
        initialProcessServiceUnderTest.startInitialProcess();

        // Verify the results
        verify(initialProcessServiceUnderTest.initialProcessRepo).swapEodCardBalance();
        verify(initialProcessServiceUnderTest.initialProcessRepo).setResetCapsLimit("ECMS_ONLINE_CARD");
        verify(initialProcessServiceUnderTest.initialProcessRepo).setResetCapsLimitAccount("ECMS_ONLINE_ACCOUNT");
        verify(initialProcessServiceUnderTest.initialProcessRepo).insertIntoOpeningAccBal();
    }
}
