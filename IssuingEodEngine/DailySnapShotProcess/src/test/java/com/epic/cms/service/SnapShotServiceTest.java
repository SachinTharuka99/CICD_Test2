package com.epic.cms.service;

import com.epic.cms.repository.SnapShotRepo;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SnapShotServiceTest {

    private SnapShotService snapShotServiceUnderTest;

    @BeforeEach
    void setUp() {
        snapShotServiceUnderTest = new SnapShotService();
        snapShotServiceUnderTest.logManager = mock(LogManager.class);
        snapShotServiceUnderTest.snapShotRepo = mock(SnapShotRepo.class);
    }

    @Test
    void testStartDailySnapShotProcess() throws Exception {
        // Setup
        when(snapShotServiceUnderTest.logManager.processHeaderStyle("Snapshot Process ")).thenReturn("result");
        when(snapShotServiceUnderTest.snapShotRepo.checkEodComplete()).thenReturn(0);

        // Run the test
        snapShotServiceUnderTest.startDailySnapShotProcess();

        // Verify the results
        verify(snapShotServiceUnderTest.snapShotRepo, times(1)).updateSnapShotTableOfCards();
        verify(snapShotServiceUnderTest.snapShotRepo, times(1)).updateSnapShotTableOfAccounts();
        verify(snapShotServiceUnderTest.snapShotRepo, times(1)).updateOnlineSnapShotTableOfCards();
        verify(snapShotServiceUnderTest.snapShotRepo, times(1)).updateOnlineSnapShotTableOfAccounts();
    }
}
