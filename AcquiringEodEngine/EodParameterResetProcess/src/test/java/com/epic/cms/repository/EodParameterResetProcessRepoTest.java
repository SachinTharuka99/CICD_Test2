package com.epic.cms.repository;

import com.epic.cms.util.QueryParametersList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class EodParameterResetProcessRepoTest {

    @Mock
    private JdbcTemplate mockOnlineJdbcTemplate;
    @Mock
    private QueryParametersList mockQueryParametersList;

    @InjectMocks
    private EodParameterResetProcessRepo eodParameterResetProcessRepoUnderTest;

    @Test
    void testResetTerminalParameters() throws Exception {
        // Setup

        when(mockOnlineJdbcTemplate.update(any(), any(), any(), any(), any(), any())).thenReturn(1);

        // Run the test
        final int result = eodParameterResetProcessRepoUnderTest.resetTerminalParameters();

        // Verify the results
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testResetMerchantParameters() throws Exception {
        // Setup
        // Configure QueryParametersList.getEodParamResetUpdateResetMerchantParameters(...).
        final String s = "EodParamResetUpdateResetMerchantParameters";
        when(mockQueryParametersList.getEodParamResetUpdateResetMerchantParameters()).thenReturn(s);

        when(mockOnlineJdbcTemplate.update(any(), any(), any(), any(), any(), any())).thenReturn(1);

        // Run the test
        eodParameterResetProcessRepoUnderTest.resetMerchantParameters();

        // Verify the results
        verify(mockOnlineJdbcTemplate).update(any(), any(), any(), any(), any(), any());
    }
}