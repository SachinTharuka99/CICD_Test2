package com.epic.cms.service;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EodPaymentUpdateRepo;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EodPaymentUpdateServiceTest {

    private EodPaymentUpdateService eodPaymentUpdateServiceUnderTest;

    @BeforeEach
    void setUp() {
        eodPaymentUpdateServiceUnderTest = new EodPaymentUpdateService();
        eodPaymentUpdateServiceUnderTest.logManager = mock(LogManager.class);
        eodPaymentUpdateServiceUnderTest.status = mock(StatusVarList.class);
        eodPaymentUpdateServiceUnderTest.commonRepo = mock(CommonRepo.class);
        eodPaymentUpdateServiceUnderTest.eodPaymentUpdateRepo = mock(EodPaymentUpdateRepo.class);
    }

    @Test
    void testStartEODPaymentUpdate() throws Exception {
        // Setup
        when(eodPaymentUpdateServiceUnderTest.eodPaymentUpdateRepo.callStoredProcedureForEodPaymentUpdate())
                .thenReturn(new int[]{0,0,0,0});

        // Run the test
        eodPaymentUpdateServiceUnderTest.startEODPaymentUpdate();

        // Verify the results
    }

}
