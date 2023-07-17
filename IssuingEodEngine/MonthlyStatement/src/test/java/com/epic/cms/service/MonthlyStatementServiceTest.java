package com.epic.cms.service;

import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.repository.MonthlyStatementRepo;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MonthlyStatementServiceTest {

    private MonthlyStatementService monthlyStatementServiceUnderTest;

    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        monthlyStatementServiceUnderTest = new MonthlyStatementService();
        monthlyStatementServiceUnderTest.logManager = mock(LogManager.class);
        monthlyStatementServiceUnderTest.status = mock(StatusVarList.class);
        monthlyStatementServiceUnderTest.monthlyStatementRepo = mock(MonthlyStatementRepo.class);
    }

    @Test
    void testMonthlyStatement() throws Exception {
        // Setup
        final CardBean cardBean = new CardBean();
        cardBean.setBillingID("1");
        cardBean.setCardnumber(new StringBuffer("4862950000698568"));
        cardBean.setAccountno("4862950000698568");
        cardBean.setApplicationId("applicationId");
        cardBean.setCustomerid("customerid");
        cardBean.setMainCardNo(new StringBuffer("4862950000698568"));
        cardBean.setCardtype("cardtype");
        cardBean.setCardProduct("cardProduct");
        cardBean.setExpiryDate("expiryDate");
        cardBean.setNameOnCard("nameOnCard");
        cardBean.setCardStatus("YES");
        cardBean.setAccStatus("NP");
        cardBean.setCreditLimit(0.0);
        cardBean.setOtbCash(0.0);
        cardBean.setOtbCredit(0.0);
        final ArrayList<CardBean> accDetails = new ArrayList<>(List.of(cardBean));

        // Configure MonthlyStatementRepo.CheckBillingCycleChangeRequest(...).
        final StatementBean statementBean = new StatementBean();
        statementBean.setBillingID("1");
        statementBean.setStatementID("statementID");
        statementBean.setStartEodID(0);
        statementBean.setEndEodID(0);
        statementBean.setAccountNo("4862950000698568");
        statementBean.setCardNo(new StringBuffer("4862950000698568"));
        statementBean.setClosingBalance(0.0);
        statementBean.setLastBillclosingBalance(0.0);
        statementBean.setStartingBalance(0.0);
        statementBean.setMainCardNo(new StringBuffer("4862950000698568"));
        statementBean.setCardCategory("cardCategory");
        statementBean.setStatementStartDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        statementBean.setStatementEndDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        statementBean.setStatementDueDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        statementBean.setOldNextBillingDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());

        when(monthlyStatementServiceUnderTest.monthlyStatementRepo.CheckBillingCycleChangeRequest("4862950000698568"))
                .thenReturn(statementBean);

        // Run the test
        monthlyStatementServiceUnderTest.monthlyStatement("4862950000698568", accDetails, faileCardCount);

        // Verify the results
        verify(monthlyStatementServiceUnderTest.monthlyStatementRepo, times(1)).UpdateStatementDeatils(
                eq(accDetails), any(StatementBean.class), eq("4862950000698568"));
    }

}
