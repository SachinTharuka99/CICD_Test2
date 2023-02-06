package com.epic.cms.repository;

import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyInterestCalculationRepoTest {

    @Mock
    private StatusVarList mockStatusList;
    @Mock
    private JdbcTemplate mockBackendJdbcTemplate;

    @InjectMocks
    private DailyInterestCalculationRepo dailyInterestCalculationRepoUnderTest;

    @Test
    void testGetLatestStatementAccountList() throws Exception {
        // Setup
        when(mockStatusList.getCARD_CLOSED_STATUS()).thenReturn("CARD_CLOSED_STATUS");

        // Configure JdbcTemplate.query(...).
        final StatementBean statementBean = new StatementBean();
        statementBean.setBillingID("billingID");
        statementBean.setStatementID("statementID");
        statementBean.setStartEodID(0);
        statementBean.setEndEodID(0);
        statementBean.setAccountNo("accountNo");
        statementBean.setCardNo(new StringBuffer("value"));
        statementBean.setClosingBalance(0.0);
        statementBean.setLastBillclosingBalance(0.0);
        statementBean.setStartingBalance(0.0);
        statementBean.setMainCardNo(new StringBuffer("value"));
        statementBean.setCardCategory("cardCategory");
        statementBean.setStatementStartDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        statementBean.setStatementEndDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        statementBean.setStatementDueDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        statementBean.setOldNextBillingDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        final ArrayList<StatementBean> statementBeans = new ArrayList<>(List.of(statementBean));
        when(mockBackendJdbcTemplate.query(
                eq("SELECT BS.ACCOUNTNO, BS.MAINCARDNO, BS.CARDNO, BS.STATEMENTSTARTDATE,  BS.STATEMENTENDDATE, BS.THISBILLOPERNINGBALANCE, BS.THISBILLCLOSINGBALANCE, BS.DUEDATE, BS.STARTEODID, BS.ENDEODID  FROM BILLINGLASTSTATEMENTSUMMARY BLS INNER JOIN  BILLINGSTATEMENT BS ON BLS.STATEMENTID=BS.STATEMENTID  INNER JOIN CARD C ON BS.CARDNO=C.CARDNUMBER WHERE C.CARDSTATUS NOT IN(?)"),
                any(RowMapperResultSetExtractor.class), eq("CARD_CLOSED_STATUS"))).thenReturn(statementBeans);

        // Run the test
        final ArrayList<StatementBean> result = dailyInterestCalculationRepoUnderTest.getLatestStatementAccountList();

        // Verify the results
        assertThat(result).isEqualTo(statementBeans);
    }

    @Test
    void testGetLatestStatementAccountList_JdbcTemplateReturnsNull() throws Exception {
        // Setup
        when(mockStatusList.getCARD_CLOSED_STATUS()).thenReturn("CARD_CLOSED_STATUS");
        when(mockBackendJdbcTemplate.query(
                eq("SELECT BS.ACCOUNTNO, BS.MAINCARDNO, BS.CARDNO, BS.STATEMENTSTARTDATE,  BS.STATEMENTENDDATE, BS.THISBILLOPERNINGBALANCE, BS.THISBILLCLOSINGBALANCE, BS.DUEDATE, BS.STARTEODID, BS.ENDEODID  FROM BILLINGLASTSTATEMENTSUMMARY BLS INNER JOIN  BILLINGSTATEMENT BS ON BLS.STATEMENTID=BS.STATEMENTID  INNER JOIN CARD C ON BS.CARDNO=C.CARDNUMBER WHERE C.CARDSTATUS NOT IN(?)"),
                any(RowMapperResultSetExtractor.class), eq("CARD_CLOSED_STATUS"))).thenReturn(null);

        // Run the test
        final ArrayList<StatementBean> result = dailyInterestCalculationRepoUnderTest.getLatestStatementAccountList();

        // Verify the results
    }
/*

    @Test
    void testGetIntProf() throws Exception {
        // Setup
        // Configure JdbcTemplate.queryForObject(...).
        final InterestDetailBean interestDetailBean = new InterestDetailBean();
        interestDetailBean.setInterest(0.0);
        interestDetailBean.setStatementEndDate(Date.valueOf(LocalDate.of(2020, 1, 1)));
        interestDetailBean.setInterestperiod(0.0);
        when(mockBackendJdbcTemplate.queryForObject(
                eq("SELECT IP.INTERESTRATE, IP.INTERESTPERIODVALUE FROM CARDACCOUNT CA INNER JOIN INTERESTPROFILE IP ON IP.INTERESTPROFILECODE = CA.INTERESTPROFILECODE WHERE CA.ACCOUNTNO= ?"),
                any(RowMapper.class))).thenReturn(interestDetailBean);

        // Run the test
        final InterestDetailBean result = dailyInterestCalculationRepoUnderTest.getIntProf("accountNo");

        // Verify the results
        assertThat(result).isEqualTo(interestDetailBean);
    }

    @Test
    void testGetIntProf_JdbcTemplateReturnsNull() throws Exception {
        // Setup
        when(mockBackendJdbcTemplate.queryForObject(
                eq("SELECT IP.INTERESTRATE, IP.INTERESTPERIODVALUE FROM CARDACCOUNT CA INNER JOIN INTERESTPROFILE IP ON IP.INTERESTPROFILECODE = CA.INTERESTPROFILECODE WHERE CA.ACCOUNTNO= ?"),
                any(RowMapper.class))).thenReturn(null);

        // Run the test
        final InterestDetailBean result = dailyInterestCalculationRepoUnderTest.getIntProf("accountNo");

        // Verify the results
    }
*/

}
