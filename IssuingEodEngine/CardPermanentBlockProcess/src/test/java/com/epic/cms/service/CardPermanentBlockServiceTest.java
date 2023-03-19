/**
 * Author : rasintha_j
 * Date : 07/11/2022
 * Time : 12:55 PM
 * Project Name : ecms_eod_engine
 */
package com.epic.cms.service;

import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardPermanentBlockServiceTest {

    private CardPermanentBlockService cardPermanentBlockServiceUnderTest;

    @BeforeEach
    void setUp() {
        cardPermanentBlockServiceUnderTest = new CardPermanentBlockService();
        cardPermanentBlockServiceUnderTest.statusList = mock(StatusVarList.class);
        cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo = mock(CardBlockRepo.class);
        cardPermanentBlockServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    @DisplayName("Test case for Card Permanent Block For Equal Current Status")
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
        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(blockCardBean.getCardNo()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(blockCardBean.getCardNo()));
        }

        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.updateCardTableForBlock(any(StringBuffer.class), eq("CARD_PERMANENT_BLOCKED_STATUS"))).thenReturn("newStatus");
        when(cardPermanentBlockServiceUnderTest.statusList.getCARD_PERMANENT_BLOCKED_STATUS()).thenReturn("CARD_PERMANENT_BLOCKED_STATUS");
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.deactivateCardBlock(any(StringBuffer.class))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.insertIntoCardBlock(any(StringBuffer.class), eq("CARD_PERMANENT_BLOCKED_STATUS"), eq("newStatus"), eq("reason"))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.updateMinimumPaymentTable(any(StringBuffer.class), eq("CARD_PERMANENT_BLOCKED_STATUS"))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.statusList.getCARD_INIT()).thenReturn("result");
        when(cardPermanentBlockServiceUnderTest.statusList.getCARD_BLOCK_STATUS()).thenReturn("result");
        when(cardPermanentBlockServiceUnderTest.statusList.getCARD_EXPIRED_STATUS()).thenReturn("result");

        // Run the test
        cardPermanentBlockServiceUnderTest.processCardPermanentBlock(blockCardBean, processBean);

        // Verify the results
        verify(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo,times(1)).deactivateCardBlock(any(StringBuffer.class));
        verify(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo,times(1)).updateMinimumPaymentTable(any(StringBuffer.class), anyString());

    }

    @Test
    @DisplayName("Test case for Card Permanent Block For Not Equal Current Status")
    void testProcessCardPermanentBlockForNotEquelCurrentStatus() throws Exception {
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
        String maskCardNo = "438043****8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(blockCardBean.getCardNo()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(blockCardBean.getCardNo()));
        }

        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.updateCardTableForBlock(any(StringBuffer.class), eq("CARD_PERMANENT_BLOCKED_STATUS"))).thenReturn("newStatus");
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.deactivateCardBlock(any(StringBuffer.class))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.updateOnlineCardStatus(any(StringBuffer.class), eq(0))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.deactivateCardBlockOnline(any(StringBuffer.class))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.insertIntoCardBlock(any(StringBuffer.class), eq("CARD_PERMANENT_BLOCKED_STATUS"), eq("newStatus"), eq("reason"))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.insertToOnlineCardBlock(any(StringBuffer.class), eq(0))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.statusList.getCARD_PERMANENT_BLOCKED_STATUS()).thenReturn("CARD_PERMANENT_BLOCKED_STATUS");
        when(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo.updateMinimumPaymentTable(any(StringBuffer.class), eq("CARD_PERMANENT_BLOCKED_STATUS"))).thenReturn(0);
        when(cardPermanentBlockServiceUnderTest.statusList.getONLINE_CARD_PERMANENTLY_BLOCKED_STATUS()).thenReturn(0);

        // Run the test
        cardPermanentBlockServiceUnderTest.processCardPermanentBlock(blockCardBean, processBean);

        // Verify the results
        verify(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo,times(1)).deactivateCardBlock(any(StringBuffer.class));
        verify(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo,times(1)).deactivateCardBlockOnline(any(StringBuffer.class));
        verify(cardPermanentBlockServiceUnderTest.cardPermanentBlockRepo,times(1)).updateMinimumPaymentTable(any(StringBuffer.class), anyString());
    }
}
