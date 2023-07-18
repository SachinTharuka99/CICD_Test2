/**
 * Author : rasintha_j
 * Date : 02/11/2022
 * Time : 13:15 PM
 * Project Name : ecms_eod_engine
 */
package com.epic.cms.service;

import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.StampDutyBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.StampDutyFeeRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StampDutyFeeServiceTest {

    private StampDutyFeeService stampDutyFeeServiceUnderTest;
    int capacity = 200000;
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<Integer>(capacity);
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue <Integer>(capacity);

    @BeforeEach
    void setUp() {
        stampDutyFeeServiceUnderTest = new StampDutyFeeService();
        stampDutyFeeServiceUnderTest.logManager = mock(LogManager.class);
        stampDutyFeeServiceUnderTest.stampDutyFeeRepo = mock(StampDutyFeeRepo.class);
        stampDutyFeeServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }
    static MockedStatic<CommonMethods> common;

    @BeforeAll
    public static void init() {
        common = Mockito.mockStatic(CommonMethods.class);
    }

    @AfterAll
    public static void close() {
        common.close();
    }
    @Test
    void testStampDutyFee() throws Exception {
        Configurations.EOD_DATE = new Date(System.currentTimeMillis());
        // Setup
        final StampDutyBean stampDutyBean = new StampDutyBean();
        stampDutyBean.setAccountNumber("accNumber");
        stampDutyBean.setCardNumber(new StringBuffer("4380431766518012"));
        stampDutyBean.setPersentage(10.0);
        stampDutyBean.setCurrencycode(0);
        stampDutyBean.setForiegnTxnAmount(10.0);

        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        common.when(() -> CommonMethods.calcStampDutyFee(anyDouble(),anyDouble())).thenReturn(10.0);

        final ArrayList<StampDutyBean> stampDutyBeans = new ArrayList<>(List.of(stampDutyBean));
        when(stampDutyFeeServiceUnderTest.stampDutyFeeRepo.getStatementCardList(anyString())).thenReturn(stampDutyBeans);

        // Configure StampDutyFeeRepo.getOldCardNumbers(...).
        final ArrayList<StringBuffer> stringBuffers = new ArrayList<>(List.of(new StringBuffer("value")));
        when(stampDutyFeeServiceUnderTest.stampDutyFeeRepo.getOldCardNumbers(any(StringBuffer.class))).thenReturn(stringBuffers);
        when(stampDutyFeeServiceUnderTest.stampDutyFeeRepo.getTotalForeignTxns(anyString(), anyInt())).thenReturn(0.0);

        // Run the test
        stampDutyFeeServiceUnderTest.StampDutyFee(stampDutyBean, successCount,failCount);

        // Verify the results
        verify(stampDutyFeeServiceUnderTest.stampDutyFeeRepo,times(1)).insertToEODcardFee(any(CardFeeBean.class), anyDouble(), any(Date.class), any());
    }
}
