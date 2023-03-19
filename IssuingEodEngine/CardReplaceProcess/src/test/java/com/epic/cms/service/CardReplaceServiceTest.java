package com.epic.cms.service;

import com.epic.cms.model.bean.CardReplaceBean;
import com.epic.cms.repository.CardReplaceRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardReplaceServiceTest {

    private CardReplaceService cardReplaceServiceUnderTest;

    static MockedStatic<LogManager> common;
    @BeforeAll
    public static void init() {
        common = Mockito.mockStatic(LogManager.class);
    }

    @AfterAll
    public static void close() {
        common.close();
    }

    @BeforeEach
    void setUp() {
        cardReplaceServiceUnderTest = new CardReplaceService();
        cardReplaceServiceUnderTest.cardReplaceRepo = mock(CardReplaceRepo.class);
        cardReplaceServiceUnderTest.status = mock(StatusVarList.class);
    }

    @Test
    void testCardReplace() throws Exception {
        // Setup
        final CardReplaceBean cardReplaceBean = new CardReplaceBean();
        cardReplaceBean.setNewCardNo(new StringBuffer("4380431766518012"));
        cardReplaceBean.setOldCardNo(new StringBuffer("4380431766518012"));
        cardReplaceBean.setStatus("status");

        // Configure
        String maskCardNo = "438043******8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(cardReplaceBean.getOldCardNo()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(cardReplaceBean.getOldCardNo()));
        }
        // Run the test
        cardReplaceServiceUnderTest.cardReplace(cardReplaceBean);

        // Verify the results
        verify(cardReplaceServiceUnderTest.cardReplaceRepo).updateBackendOldCardFromNewCard(any(CardReplaceBean.class));
        verify(cardReplaceServiceUnderTest.cardReplaceRepo).updateCardReplaceStatus(any(StringBuffer.class));
        verify(cardReplaceServiceUnderTest.cardReplaceRepo).updateOnlineOldCardFromNewCard(any(CardReplaceBean.class));
    }

}
