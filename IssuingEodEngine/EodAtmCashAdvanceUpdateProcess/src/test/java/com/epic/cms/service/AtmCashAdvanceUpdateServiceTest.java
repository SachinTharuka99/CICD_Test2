package com.epic.cms.service;

import com.epic.cms.repository.AtmCashAdvanceUpdateRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AtmCashAdvanceUpdateServiceTest {

    private AtmCashAdvanceUpdateService atmCashAdvanceUpdateServiceUnderTest;

    @BeforeEach
    void setUp() {
        atmCashAdvanceUpdateServiceUnderTest = new AtmCashAdvanceUpdateService();
        atmCashAdvanceUpdateServiceUnderTest.atmCashAdvanceUpdateRepo = mock(AtmCashAdvanceUpdateRepo.class);
        atmCashAdvanceUpdateServiceUnderTest.logManager = mock(LogManager.class);
        atmCashAdvanceUpdateServiceUnderTest.status = mock(StatusVarList.class);
        atmCashAdvanceUpdateServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    void testStartEodAtmCashAdvanceUpdate() throws Exception {
        // Setup
        int[] txnCounts = new int[]{0,0,0};
        Configurations.EOD_DATE = new Date();

        when(atmCashAdvanceUpdateServiceUnderTest.logManager.processHeaderStyle(
                "ATM Cash Advance Update Process ")).thenReturn("result");
        when(atmCashAdvanceUpdateServiceUnderTest.atmCashAdvanceUpdateRepo.callStoredProcedureForCashAdvUpdate())
                .thenReturn(new int[]{0,0,0});

        // Run the test
        atmCashAdvanceUpdateServiceUnderTest.startEodAtmCashAdvanceUpdate();

        // Verify the results
        assertThat(txnCounts).isEqualTo(atmCashAdvanceUpdateServiceUnderTest.atmCashAdvanceUpdateRepo.callStoredProcedureForCashAdvUpdate());
    }
}
