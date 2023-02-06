package com.epic.cms.repository;

import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcqTxnUpdateRepoTest {

    @Mock
    private JdbcTemplate mockBackendJdbcTemplate;

    @Mock
    QueryParametersList query;

    @Mock
    StatusVarList status;

    @InjectMocks
    AcqTxnUpdateRepo mockAcqTxnUpdateRepo;

    @Test
    void getForexPercentage() throws Exception {
        // given - precondition or setup
      final String forexRate = "1";

        /**
         * when - action or behaviour that we are going test
         * then - verify the result or output using assert statements
         */
        when(mockBackendJdbcTemplate.queryForObject(any(),eq(String.class))).thenReturn(forexRate);
        assertEquals(forexRate, mockAcqTxnUpdateRepo.getForexPercentage());
    }

    @Test
    void getFuelSurchargeRatePercentage() throws Exception {
        // given - precondition or setup
        final String fuelSurchargeRate = "10";

        /**
         * when - action or behaviour that we are going test
         * then - verify the result or output using assert statements
         */
        when(mockBackendJdbcTemplate.queryForObject(any(),eq(String.class))).thenReturn(fuelSurchargeRate);
        assertEquals(fuelSurchargeRate, mockAcqTxnUpdateRepo.getForexPercentage());
    }

    @Test
    void getFuelMccList() throws Exception {
        // Setup
        when(query.getAcqTxnUpdate_getFuelMccList()).thenReturn("AcqTxnUpdate_getFuelMccList");
        when(mockBackendJdbcTemplate.queryForList("AcqTxnUpdate_getFuelMccList", String.class))
                .thenReturn(List.of("value"));

        // Run the test
        final List<String> result = mockAcqTxnUpdateRepo.getFuelMccList();

        // Verify the results
        assertThat(result).isEqualTo(List.of("value"));
    }

    @Test
    void getFinancialStatus() throws Exception {
        // Setup
        when(query.getAcqTxnUpdate_getFinancialStatus()).thenReturn("AcqTxnUpdate_getFinancialStatus");

        // Configure JdbcTemplate.query(...).
        final HashMap<String, String> stringStringHashMap = new HashMap<>(Map.ofEntries(Map.entry("value", "value")));
        when(mockBackendJdbcTemplate.query(eq("AcqTxnUpdate_getFinancialStatus"),
                any(ResultSetExtractor.class))).thenReturn(stringStringHashMap);

        // Run the test
        HashMap<String, String> result= mockAcqTxnUpdateRepo.getFinancialStatus();

        // Verify the results
        assertThat(result).isEqualTo(stringStringHashMap);
        //verify(mockBackendJdbcTemplate).query(eq("AcqTxnUpdate_getFinancialStatus"), any(ResultSetExtractor.class));

    }

    @Test
    void testGetFuelMccList_JdbcTemplateReturnsNoItems() throws Exception {
        // Setup
        when(query.getAcqTxnUpdate_getFuelMccList()).thenReturn("AcqTxnUpdate_getFuelMccList");
        when(mockBackendJdbcTemplate.queryForList("AcqTxnUpdate_getFuelMccList", String.class))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<String> result = mockAcqTxnUpdateRepo.getFuelMccList();

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }
}