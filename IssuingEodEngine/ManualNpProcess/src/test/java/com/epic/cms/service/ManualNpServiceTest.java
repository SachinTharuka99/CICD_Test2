package com.epic.cms.service;

import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.repository.ManualNpRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManualNpServiceTest {

    private ManualNpService manualNpServiceUnderTest;
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        manualNpServiceUnderTest = new ManualNpService();
        manualNpServiceUnderTest.logManager = mock(LogManager.class);
        manualNpServiceUnderTest.status = mock(StatusVarList.class);
        manualNpServiceUnderTest.manualNpRepo = mock(ManualNpRepo.class);
    }

    @Test
    @DisplayName("Test case for ManualNP(ArrayList<String>) when acc status 'NP'")
    void testManualNpClassification() throws Exception {
        // Setup
        final ArrayList<StringBuffer> accDetails = new ArrayList<>(List.of(new StringBuffer("4862950000698568"),
                new StringBuffer("212140052145"),
                new StringBuffer("NP"),
                new StringBuffer("5")
                ));

        // Configure ManualNpRepo.setDelinquentAccountDetails(...).
        final DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        delinquentAccountBean.setAccNo("4862950000698568");
        delinquentAccountBean.setAccStatus("NP");
        delinquentAccountBean.setNDIA(0);
        delinquentAccountBean.setMIA(0);
        delinquentAccountBean.setLastStatementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setDelinqstatus("NP");
        delinquentAccountBean.setCardNumber(new StringBuffer("4862950000698568"));
        delinquentAccountBean.setDueDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setAssignStatus("NP");
        delinquentAccountBean.setSupervisor("assignee");
        delinquentAccountBean.setAssignee("assignee");
        delinquentAccountBean.setRiskClass("riskClass");
        delinquentAccountBean.setDueAmount("dueAmount");
        delinquentAccountBean.setNpInterest(0.0);
        delinquentAccountBean.setNpOutstanding(0.0);
        delinquentAccountBean.setAccruedInterest(0.0);
        delinquentAccountBean.setAccruedFees(0.0);
        delinquentAccountBean.setAccruedOverLimit(0.0);
        delinquentAccountBean.setAccruedlatePay(0.0);
        delinquentAccountBean.setNpDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setRemainDue(0.0);

        StringBuffer cNo = new StringBuffer("4862950000698568");
        String acc = "4862950000698568";

        String remark = "Account has Auto Non Performing to Manual Non Performing by the manual.";

        String maskCardNo = "4862950000698568";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(delinquentAccountBean.getCardNumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(delinquentAccountBean.getCardNumber()));
        }

        when(manualNpServiceUnderTest.status.getACCOUNT_NON_PERFORMING_STATUS()).thenReturn("NP");
        //setup
        when(manualNpServiceUnderTest.manualNpRepo.updateNpStatusCardAccount(anyString(), anyInt())).thenReturn(0);
        when(manualNpServiceUnderTest.manualNpRepo.insertIntoDelinquentHistory(any(),anyString(),anyString())).thenReturn(0);

        // Run the test
        manualNpServiceUnderTest.manualNpClassification(accDetails,  faileCardCount);

        //verify
        verify(manualNpServiceUnderTest.manualNpRepo, times(1)).updateNpStatusCardAccount(anyString(), anyInt());
        verify(manualNpServiceUnderTest.manualNpRepo, times(1)).insertIntoDelinquentHistory(
                any(StringBuffer.class), anyString(), anyString());
    }
}
