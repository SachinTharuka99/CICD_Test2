/**
 * Author : rasintha_j
 * Date : 08/11/2022
 * Time : 12:55 PM
 * Project Name : ecms_eod_engine
 */
package com.epic.cms.service;

import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardTemporaryBlockServiceTest {

    private CardTemporaryBlockService cardTemporaryBlockServiceUnderTest;

    public AtomicInteger faileCardCount = new AtomicInteger(0);


    @BeforeEach
    void setUp() {
        cardTemporaryBlockServiceUnderTest = new CardTemporaryBlockService();
        cardTemporaryBlockServiceUnderTest.statusList = mock(StatusVarList.class);
        cardTemporaryBlockServiceUnderTest.logManager = mock(LogManager.class);
        cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo = mock(CardBlockRepo.class);
    }

    @Test
    void testProcessCardPermanentBlockForCurrentStatus() throws Exception {
        // Setup
        final BlockCardBean blockCardBean = new BlockCardBean();
        blockCardBean.setCardNo(new StringBuffer("4380431766518012"));
        blockCardBean.setCardStatus("CardStatus");
        blockCardBean.setOldStatus("oldStatus");
        blockCardBean.setNewStatus("newStatus");
        blockCardBean.setBlockReason("blockReason");

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

        // Configure
        String maskCardNo = "438043******8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(blockCardBean.getCardNo()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(blockCardBean.getCardNo()));
        }

        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.updateCardTableForBlock(any(StringBuffer.class), eq("CARD_TEMPORARY_BLOCK_Status"))).thenReturn("newStatus");
        when(cardTemporaryBlockServiceUnderTest.statusList.getCARD_INIT()).thenReturn("result");
        when(cardTemporaryBlockServiceUnderTest.statusList.getCARD_BLOCK_STATUS()).thenReturn("result");
        when(cardTemporaryBlockServiceUnderTest.statusList.getCARD_TEMPORARY_BLOCK_Status()).thenReturn("CARD_TEMPORARY_BLOCK_Status");
        when(cardTemporaryBlockServiceUnderTest.statusList.getCARD_EXPIRED_STATUS()).thenReturn("result");
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.deactivateCardBlock(any(StringBuffer.class))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.insertIntoCardBlock(any(StringBuffer.class), eq("CARD_TEMPORARY_BLOCK_Status"), eq("newStatus"), eq("reason"))).thenReturn(0);

        // Run the test
        cardTemporaryBlockServiceUnderTest.processCardTemporaryBlock(blockCardBean, processBean, faileCardCount);

        // Verify the results
        verify(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo,times(1)).deactivateCardBlock(any(StringBuffer.class));
        verify(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo,times(1)).updateMinimumPaymentTable(any(StringBuffer.class), anyString());

    }

    @Test
    void testProcessCardPermanentBlockForNullCurrentStatus() throws Exception {
        // Setup
        final BlockCardBean blockCardBean = new BlockCardBean();
        blockCardBean.setCardNo(new StringBuffer("4380431766518012"));
        blockCardBean.setCardStatus("CardStatus");
        blockCardBean.setOldStatus("oldStatus");
        blockCardBean.setNewStatus("newStatus");
        blockCardBean.setBlockReason("blockReason");

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

        // Configure
        String maskCardNo = "438043******8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(blockCardBean.getCardNo()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(blockCardBean.getCardNo()));
        }

        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.updateCardTableForBlock(any(StringBuffer.class), eq("CARD_TEMPORARY_BLOCK_Status"))).thenReturn("newStatus");
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.deactivateCardBlock(any(StringBuffer.class))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.updateOnlineCardStatus(any(StringBuffer.class), eq(0))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.statusList.getONLINE_CARD_TEMPORARILY_BLOCKED_STATUS()).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.deactivateCardBlockOnline(any(StringBuffer.class))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.insertIntoCardBlock(any(StringBuffer.class), eq("CARD_TEMPORARY_BLOCK_Status"), eq("newStatus"), eq("reason"))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.statusList.getCARD_TEMPORARY_BLOCK_Status()).thenReturn("CARD_TEMPORARY_BLOCK_Status");
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.insertToOnlineCardBlock(any(StringBuffer.class), eq(0))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.statusList.getONLINE_CARD_PERMANENTLY_BLOCKED_STATUS()).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.statusList.getONLINE_CARD_TEMPORARY_BLOCK()).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo.updateMinimumPaymentTable(any(StringBuffer.class), eq("CARD_TEMPORARY_BLOCK_Status"))).thenReturn(0);
        when(cardTemporaryBlockServiceUnderTest.statusList.getCARD_TEMPORARY_BLOCK_Status()).thenReturn("CARD_TEMPORARY_BLOCK_Status");

        // Run the test
        cardTemporaryBlockServiceUnderTest.processCardTemporaryBlock(blockCardBean, processBean, faileCardCount);

        // Verify the results
        verify(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo,times(1)).deactivateCardBlock(any(StringBuffer.class));
        verify(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo,times(1)).deactivateCardBlockOnline(any(StringBuffer.class));
        verify(cardTemporaryBlockServiceUnderTest.cardTemporaryBlockRepo,times(1)).updateMinimumPaymentTable(any(StringBuffer.class), anyString());
    }
}
