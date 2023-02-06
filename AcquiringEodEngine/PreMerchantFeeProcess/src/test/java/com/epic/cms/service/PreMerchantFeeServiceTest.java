package com.epic.cms.service;

import com.epic.cms.dao.PreMerchantFeeDao;
import com.epic.cms.model.bean.MerchantBeanForFee;
import com.epic.cms.model.bean.TerminalBeanForFee;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PreMerchantFeeServiceTest {

    private PreMerchantFeeService preMerchantFeeServiceUnderTest;

    @BeforeEach
    void setUp() {
        preMerchantFeeServiceUnderTest = new PreMerchantFeeService();
        preMerchantFeeServiceUnderTest.status = mock(StatusVarList.class);
        preMerchantFeeServiceUnderTest.logManager = mock(LogManager.class);
        preMerchantFeeServiceUnderTest.preMerchantFeeDao = mock(PreMerchantFeeDao.class);
    }

    @Test
    void testPreMerchantFee() throws Exception {
        // Setup
        final MerchantBeanForFee merchantBean = new MerchantBeanForFee();
        merchantBean.setMerchantId("merchantId");
        merchantBean.setFeeProfile("feeProfile");
        merchantBean.setNextAnniversaryDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        final TerminalBeanForFee terminalBeanForFee = new TerminalBeanForFee();
        terminalBeanForFee.setNextAnniversaryDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextRentalDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setTerminalType(0);
        terminalBeanForFee.setTerminalStatus("terminalStatus");
        terminalBeanForFee.setNextBiMonthlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextQuarterlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextHalfYearlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextWeeklyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantBean.setTerminalList(List.of(terminalBeanForFee));
        merchantBean.setNextBiMonthlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantBean.setNextQuarterlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantBean.setNextHalfYearlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());

        final HashMap<String, List<String>> feeCodeMap = new HashMap<>(
                Map.ofEntries(Map.entry("value", List.of("value"))));
        when(preMerchantFeeServiceUnderTest.preMerchantFeeDao.addMerchantFeeCount("merchantId", "feeCode"))
                .thenReturn(0);
        when(preMerchantFeeServiceUnderTest.status.getTERMINAL_DELETE_STATUS()).thenReturn("result");

        // Run the test
        preMerchantFeeServiceUnderTest.preMerchantFee(merchantBean, feeCodeMap);

        // Verify the results
    }

    @Test
    void testPreMerchantFee_PreMerchantFeeDaoThrowsException() throws Exception {
        // Setup
        final MerchantBeanForFee merchantBean = new MerchantBeanForFee();
        merchantBean.setMerchantId("merchantId");
        merchantBean.setFeeProfile("feeProfile");
        merchantBean.setNextAnniversaryDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        final TerminalBeanForFee terminalBeanForFee = new TerminalBeanForFee();
        terminalBeanForFee.setNextAnniversaryDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextRentalDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setTerminalType(0);
        terminalBeanForFee.setTerminalStatus("terminalStatus");
        terminalBeanForFee.setNextBiMonthlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextQuarterlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextHalfYearlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        terminalBeanForFee.setNextWeeklyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantBean.setTerminalList(List.of(terminalBeanForFee));
        merchantBean.setNextBiMonthlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantBean.setNextQuarterlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantBean.setNextHalfYearlyDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());

        final HashMap<String, List<String>> feeCodeMap = new HashMap<>(
                Map.ofEntries(Map.entry("value", List.of("value"))));
        when(preMerchantFeeServiceUnderTest.preMerchantFeeDao.addMerchantFeeCount("merchantId", "feeCode"))
                .thenThrow(Exception.class);

        // Run the test
        preMerchantFeeServiceUnderTest.preMerchantFee(merchantBean, feeCodeMap);

        // Verify the results
    }
}
