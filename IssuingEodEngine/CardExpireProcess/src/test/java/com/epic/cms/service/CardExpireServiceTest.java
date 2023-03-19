package com.epic.cms.service;

import com.epic.cms.model.bean.CardBean;
import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CardExpireRepo;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardExpireServiceTest {

    private CardExpireService cardExpireServiceUnderTest;
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
        cardExpireServiceUnderTest = new CardExpireService();
        cardExpireServiceUnderTest.cardBlockRepo = mock(CardBlockRepo.class);
        cardExpireServiceUnderTest.statusList = mock(StatusVarList.class);
        cardExpireServiceUnderTest.cardExpireRepo = mock(CardExpireRepo.class);
    }

    @Test
    void testProcessCardExpire() throws Exception {
        // Setup
        final CardBean cardBean = new CardBean();
        cardBean.setBillingID("billingID");
        cardBean.setCardnumber(new StringBuffer("4380431766518012"));
        cardBean.setAccountno("accountno");
        cardBean.setApplicationId("applicationId");
        cardBean.setCustomerid("customerid");
        cardBean.setMainCardNo(new StringBuffer("4380431766518012"));
        cardBean.setCardtype("cardtype");
        cardBean.setCardProduct("cardProduct");
        cardBean.setExpiryDate("expiryDate");
        cardBean.setNameOnCard("nameOnCard");
        cardBean.setCardStatus("cardStatus");
        cardBean.setAccStatus("accStatus");
        cardBean.setCreditLimit(0.0);
        cardBean.setOtbCash(0.0);
        cardBean.setOtbCredit(0.0);

        // Configure
        String maskCardNo = "438043******8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(cardBean.getCardnumber()))
                    .thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(cardBean.getCardnumber()));
        }

        when(cardExpireServiceUnderTest.cardExpireRepo.setCardStatusToExpire(any(StringBuffer.class))).thenReturn(0);
        when(cardExpireServiceUnderTest.cardBlockRepo.deactivateCardBlock(any(StringBuffer.class))).thenReturn(0);
        when(cardExpireServiceUnderTest.cardExpireRepo.insertToCardBlock(any(StringBuffer.class),
                eq("cardStatus"))).thenReturn(0);
        when(cardExpireServiceUnderTest.statusList.getONLINE_CARD_EXPIRED_STATUS()).thenReturn(0);
        when(cardExpireServiceUnderTest.cardBlockRepo.insertToOnlineCardBlock(any(StringBuffer.class),
                eq(0))).thenReturn(0);
        when(cardExpireServiceUnderTest.statusList.getCARD_EXPIRED_STATUS()).thenReturn("result");
        // Run the test
        cardExpireServiceUnderTest.processCardExpire(cardBean);

        // Verify the results
        verify(cardExpireServiceUnderTest.cardExpireRepo).setCardStatusToExpire(any(StringBuffer.class));
        verify(cardExpireServiceUnderTest.cardExpireRepo).setOnlineCardStatusToExpire(any(StringBuffer.class));
        verify(cardExpireServiceUnderTest.cardBlockRepo).deactivateCardBlock(any(StringBuffer.class));
        verify(cardExpireServiceUnderTest.cardExpireRepo).insertToCardBlock(any(StringBuffer.class), eq("cardStatus"));
        verify(cardExpireServiceUnderTest.cardBlockRepo).insertToOnlineCardBlock(any(StringBuffer.class), eq(0));
    }
}
