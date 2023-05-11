package com.epic.cms.service;

import com.epic.cms.repository.ConfigurationsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ConfigurationServiceTest {

    private ConfigurationService configurationServiceUnderTest;

    @BeforeEach
    void setUp() {
        configurationServiceUnderTest = new ConfigurationService();
        configurationServiceUnderTest.configurationsRepo = mock(ConfigurationsRepo.class);
    }

    @Test
    void testSetConfigurations() throws Exception {
        // Setup
        // Run the test
        configurationServiceUnderTest.setConfigurations();

        // Verify the results
        verify(configurationServiceUnderTest.configurationsRepo).setConfigurations();
    }

    @Test
    void testSetConfigurations_ThrowsException() throws Exception {
        // Setup
        doThrow(ArrayIndexOutOfBoundsException.class).when(configurationServiceUnderTest.configurationsRepo).setConfigurations();
        // Run the test
        assertThatThrownBy(() -> configurationServiceUnderTest.setConfigurations()).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void testLoadTxnTypeConfigurations() throws Exception {
        // Setup
        // Run the test
        configurationServiceUnderTest.loadTxnTypeConfigurations();

        // Verify the results
        verify(configurationServiceUnderTest.configurationsRepo).loadTxnTypeConfigurations();
    }

    @Test
    void testLoadTxnTypeConfigurations_ConfigurationsRepoThrowsException() throws Exception {
        // Setup
        doThrow(Exception.class).when(configurationServiceUnderTest.configurationsRepo).loadTxnTypeConfigurations();

        // Run the test
        assertThatThrownBy(() -> configurationServiceUnderTest.loadTxnTypeConfigurations()).isInstanceOf(Exception.class);
    }

    @Test
    void testLoadFilePath() throws Exception {
        // Setup
        // Run the test
        configurationServiceUnderTest.loadFilePath();

        // Verify the results
        verify(configurationServiceUnderTest.configurationsRepo).loadFilePath();
    }

    @Test
    void testLoadFilePath_ConfigurationsRepoThrowsException() throws Exception {
        // Setup
        doThrow(Exception.class).when(configurationServiceUnderTest.configurationsRepo).loadFilePath();

        // Run the test
        assertThatThrownBy(() -> configurationServiceUnderTest.loadFilePath()).isInstanceOf(Exception.class);
    }
}
