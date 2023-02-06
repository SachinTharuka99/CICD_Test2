package com.epic.cms.repository;

import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialProcessRepoTest {
    @InjectMocks
    private InitialProcessRepo initialProcessRepo;
    @Mock
    private JdbcTemplate mockBackendJdbcTemplate;
    @Mock
    private JdbcTemplate mockOnlineJdbcTemplate;
    @Mock
    private QueryParametersList mockQueryParametersList;
    @Mock
    private StatusVarList mockStatusList;

    @Mock
    private ProcessBeanRowMapper processBeanRowMapper;

    @Test
    void testSwapEodCardBalance() throws Exception {
        // Setup
        lenient().when(mockBackendJdbcTemplate.update(eq("sql"))).thenReturn(0);
        // Run the test
        int result = initialProcessRepo.swapEodCardBalance();

        // Verify the results
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testInsertIntoOpeningAccBal() throws Exception {
        // Setup
        when(mockQueryParametersList.getInitialUpdateInsertIntoAccountBalance())
                .thenReturn("InitialUpdateInsertIntoAccountBalance");
        lenient().when(mockBackendJdbcTemplate.update("InitialUpdateInsertIntoAccountBalance")).thenReturn(0);

        // Run the test
        final boolean result = initialProcessRepo.insertIntoOpeningAccBal();

        // Verify the results
        assertThat(result).isFalse();
    }
/**
    @Test
    void testSetResetCapsLimit() {
        // Setup
        when(mockQueryParametersList.getInitialUpdateSetResetCapsLimit()).thenReturn("result");
        when(mockStatusList.getONLINE_PINTRYEXCEED_STATUS()).thenReturn(0);
        when(mockOnlineJdbcTemplate.update(any(), any(),any(),any(),any(),any(),any())).thenReturn(0);

        // Run the test
        initialProcessRepo.setResetCapsLimit("tableName");

        // Verify the results
        verify(mockOnlineJdbcTemplate).update(any(),any(),any(),any(),any(),any(),any());
    }


    @Test
    void testGetProcessDetails() {
        // Setup
        when(mockQueryParametersList.getCommonSelectGetProcessDetails()).thenReturn("CommonSelectGetProcessDetails");

        // Configure JdbcTemplate.queryForObject(...).
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
        when(mockBackendJdbcTemplate.queryForObject(any(),
                eq(processBeanRowMapper), any())).thenReturn(processBean);

        // Run the test
        final ProcessBean result = initialProcessRepo.getProcessDetails(0);

        // Verify the results
        assertThat(result).isEqualTo(processBean);
    }
*/
}
