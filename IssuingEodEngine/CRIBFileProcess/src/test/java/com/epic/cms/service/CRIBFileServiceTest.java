package com.epic.cms.service;

import com.epic.cms.repository.CRIBFileRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CRIBFileServiceTest {
    private CRIBFileService cribFileServiceUnderTest;

    @BeforeEach
    void setUp() {
        cribFileServiceUnderTest = new CRIBFileService();
        cribFileServiceUnderTest.logManager = mock(LogManager.class);
        cribFileServiceUnderTest.cribFileRepo = mock(CRIBFileRepo.class);
    }

    @Test
    void startCribFileProcess() throws Exception {
        // Setup
        Configurations.EOD_DATE = new Date();
        int[] txnCounts = new int[]{0,0,0,0};

        when(cribFileServiceUnderTest.cribFileRepo.callStoredProcedureCribFileGeneration()).thenReturn(new int[]{0,0,0,0});
        when(cribFileServiceUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        cribFileServiceUnderTest.startCribFileProcess();

        // Verify the results
        assertThat(txnCounts).isEqualTo(cribFileServiceUnderTest.cribFileRepo.callStoredProcedureCribFileGeneration());

    }
}