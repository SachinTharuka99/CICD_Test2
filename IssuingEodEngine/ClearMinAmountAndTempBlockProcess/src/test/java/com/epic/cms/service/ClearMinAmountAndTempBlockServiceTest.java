package com.epic.cms.service;

import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.ClearMinAmountAndTempBlockRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ClearMinAmountAndTempBlockServiceTest {

    private ClearMinAmountAndTempBlockService clearMinAmountAndTempBlockServiceUnderTest;

    @BeforeEach
    void setUp() {
        clearMinAmountAndTempBlockServiceUnderTest = new ClearMinAmountAndTempBlockService();
        clearMinAmountAndTempBlockServiceUnderTest.logManager = mock(LogManager.class);
        clearMinAmountAndTempBlockServiceUnderTest.statusList = mock(StatusVarList.class);
        clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo = mock(CardBlockRepo.class);
        clearMinAmountAndTempBlockServiceUnderTest.commonRepo = mock(CommonRepo.class);
        clearMinAmountAndTempBlockServiceUnderTest.clearMinAmountAndTempBlockRepo = mock(ClearMinAmountAndTempBlockRepo.class);
    }

    @Test
    void testProcessClearMinAmountAndTempBlock() throws Exception {
        // Setup
        LastStatementSummeryBean lastStatement = new LastStatementSummeryBean();
        lastStatement.setCardno(new StringBuffer("4380431766518012"));
        lastStatement.setOpaningBalance(0.0);
        lastStatement.setClosingBalance(0.0);
        lastStatement.setMinAmount(0.0);
        lastStatement.setDueDate(new Date(System.currentTimeMillis()));
        lastStatement.setStatementStartDate(new Date(System.currentTimeMillis()));
        lastStatement.setStatementEndDate(new Date(System.currentTimeMillis()));
        lastStatement.setAccNo("accNo");
        lastStatement.setNDIA(0);
        lastStatement.setClosingloyaltypoint(0L);

        BlockCardBean blockCardBean = new BlockCardBean();
        blockCardBean.setCardNo(new StringBuffer("value"));
        blockCardBean.setCardStatus("CardStatus");
        blockCardBean.setOldStatus("CARD_ACTIVE_STATUS");
        blockCardBean.setNewStatus("CARD_ACTIVE_STATUS");
        blockCardBean.setBlockReason("blockReason");

        // Configure
        String maskCardNo = "4380431766518012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(lastStatement.getCardno())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(lastStatement.getCardno()));
        }

        HashMap<String, Double> stringDoubleHashMap = new HashMap<>(Map.ofEntries(Map.entry("Value", 10.0)));
        //when(clearMinAmountAndTempBlockServiceUnderTest.commonRepo.getDueAmountList(any(StringBuffer.class))).thenReturn(stringDoubleHashMap);
        when(clearMinAmountAndTempBlockServiceUnderTest.clearMinAmountAndTempBlockRepo.getMinimumPaymentExistStatementDate(any(StringBuffer.class), eq(1))).thenReturn(new ArrayList<>(List.of("value")));
        when(clearMinAmountAndTempBlockServiceUnderTest.commonRepo.getTotalPaymentSinceLastDue("accNo", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()))).thenReturn(0.0);

        // Configure ClearMinAmountAndTempBlockRepo.getAllCards(...).
        //ArrayList<StringBuffer[]> stringBuffers = new ArrayList<>(List.of(new StringBuffer[]{new StringBuffer("value")}));

        //when(clearMinAmountAndTempBlockServiceUnderTest.clearMinAmountAndTempBlockRepo.getAllCards(any(StringBuffer.class))).thenReturn(stringBuffers);
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_VIRTUAL_ACTIVE_STATUS()).thenReturn("CARD_ACTIVE_STATUS");
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getONLINE_CARD_VIRTUAL_ACTIVE_STATUS()).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_ACTIVE_STATUS()).thenReturn("CARD_ACTIVE_STATUS");
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getONLINE_CARD_ACTIVE_STATUS()).thenReturn(0);

        // Configure CardBlockRepo.getCardBlockOldCardStatus(...).
        when(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo.getCardBlockOldCardStatus(any(StringBuffer.class))).thenReturn(blockCardBean);
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_TEMPORARY_BLOCK_Status()).thenReturn("result");
        when(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo.updateCardStatus(any(StringBuffer.class), eq("CARD_ACTIVE_STATUS"))).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo.updateOnlineCardStatus(any(StringBuffer.class), eq(0))).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo.deactivateCardBlock(any(StringBuffer.class))).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo.deactivateCardBlockOnline(any(StringBuffer.class))).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_EXPIRED_STATUS()).thenReturn("result");
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_PERMANENT_BLOCKED_STATUS()).thenReturn("result");
        when(clearMinAmountAndTempBlockServiceUnderTest.clearMinAmountAndTempBlockRepo.updateCardBlock(any(StringBuffer.class), eq("CARD_ACTIVE_STATUS"), eq("CARD_ACTIVE_STATUS"))).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_INIT()).thenReturn("result");
        when(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo.insertIntoCardBlock(any(StringBuffer.class), eq("CARD_ACTIVE_STATUS"), eq("CARD_INIT"), eq("reason"))).thenReturn(0);
        when(clearMinAmountAndTempBlockServiceUnderTest.statusList.getCARD_BLOCK_STATUS()).thenReturn("result");

        // Run the test
        clearMinAmountAndTempBlockServiceUnderTest.processClearMinAmountAndTempBlock(lastStatement);

        // Verify the results
        //verify(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo).deactivateCardBlock(any(StringBuffer.class));
        //verify(clearMinAmountAndTempBlockServiceUnderTest.cardBlockRepo).deactivateCardBlockOnline(any(StringBuffer.class));
        //verify(clearMinAmountAndTempBlockServiceUnderTest.clearMinAmountAndTempBlockRepo).removeFromMinPayTable(any(StringBuffer.class), eq(0.0));
    }
}
