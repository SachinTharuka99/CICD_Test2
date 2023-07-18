package com.epic.cms.service;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.OnlineToBackendTxnRepo;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OnlineToBackendTxnServiceTest {
    int capacity = 200000;
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<Integer>(capacity);
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue <Integer>(capacity);
    private OnlineToBackendTxnService onlineToBackendTxnServiceUnderTest;

    @BeforeEach
    void setUp() {
        onlineToBackendTxnServiceUnderTest = new OnlineToBackendTxnService();
        onlineToBackendTxnServiceUnderTest.logManager = mock(LogManager.class);
        onlineToBackendTxnServiceUnderTest.onlineToBackendTxnRepo = mock(OnlineToBackendTxnRepo.class);
    }

    @Test
    void testOnlineToBackend() throws Exception {
        // Setup
        when(onlineToBackendTxnServiceUnderTest.onlineToBackendTxnRepo.callStoredProcedureForTxnSync()).thenReturn(new int[]{0});

        // Run the test
        onlineToBackendTxnServiceUnderTest.OnlineToBackend();

    }
}
