package com.epic.cms.service;

import com.epic.cms.dao.CardRenewDao;
import com.epic.cms.model.bean.CardRenewBean;
import com.epic.cms.repository.CardRenewRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardRenewServiceTest {

    private CardRenewService cardRenewServiceUnderTest;
    static MockedStatic<CommonMethods> common;

    @BeforeEach
    void setUp() {
        cardRenewServiceUnderTest = new CardRenewService();
        cardRenewServiceUnderTest.cardRenewDao = mock(CardRenewDao.class);
        cardRenewServiceUnderTest.cardRenewRepo = mock(CardRenewRepo.class);
        cardRenewServiceUnderTest.logManager = mock(LogManager.class);
    }
/*
    @Test
    @DisplayName("Test Card Renew Process")
    void testCardRenewProcess() throws Exception {
        Mockito.mockStatic(CommonMethods.class);
        // Setup
        final CardRenewBean CRBean = new CardRenewBean();
        CRBean.setCardNumber(new StringBuffer("4380431766518012"));
        CRBean.setEarlyRenew("EarlyRenew");
        CRBean.setExpirydate("2022");
        CRBean.setCardStatus("CardStatus");
        CRBean.setIsProductChange("2301");
        Configurations.EOD_DATE = new Date();

        when(cardRenewServiceUnderTest.cardRenewDao.getCardValidityPeriod(any(StringBuffer.class))).thenReturn(0);

        // Run the test
        cardRenewServiceUnderTest.cardRenewProcess(CRBean);

        // Verify the results
        verify(cardRenewServiceUnderTest.cardRenewDao).updateCardTable(any(StringBuffer.class), eq("2301"),
                eq("2301"));
        verify(cardRenewServiceUnderTest.cardRenewDao).updateCardRenewTable(any(StringBuffer.class));
        verify(cardRenewServiceUnderTest.cardRenewDao).updateOnlineCardTable(any(StringBuffer.class),
                eq("2301"));
        verify(cardRenewServiceUnderTest.cardRenewDao).updateOnlineCardTable(any(StringBuffer.class),
                eq("2301"));
    }*/
}
