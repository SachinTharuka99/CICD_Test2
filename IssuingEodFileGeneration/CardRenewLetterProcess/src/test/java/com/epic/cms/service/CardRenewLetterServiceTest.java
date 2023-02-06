package com.epic.cms.service;

import com.epic.cms.repository.CardRenewLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardRenewLetterServiceTest {

    private CardRenewLetterService cardRenewLetterServiceUnderTest;

    static MockedStatic<CommonMethods> common;

    @BeforeAll
    public static void init() {
        common = Mockito.mockStatic(CommonMethods.class);
    }

    @AfterAll
    public static void close() {
        common.close();
    }

    @BeforeEach
    void setUp() {
        cardRenewLetterServiceUnderTest = new CardRenewLetterService();
        cardRenewLetterServiceUnderTest.commonFileGenProcessRepo = mock(CommonFileGenProcessRepo.class);
        cardRenewLetterServiceUnderTest.cardRenewLetterRepo = mock(CardRenewLetterRepo.class);
        cardRenewLetterServiceUnderTest.letterService = mock(LetterService.class);

        Configurations.EOD_ID =22100700;
        Configurations.ERROR_EOD_ID =22100700;
        Configurations.CARD_RENEWAL_LETTER_CODE="CARD";
        Configurations.EOD_DATE = new Date();
    }

    @Test
    void testStartCardRenewLetterProcess() throws Exception {
        // Setup
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");

        List<String> cardDetails = new ArrayList<>();
        cardDetails.add(0,"value");
        cardDetails.add(1,"value");
        cardDetails.add(2,"1");

        final StringBuffer cardNo = new StringBuffer("value");
        when(cardRenewLetterServiceUnderTest.commonFileGenProcessRepo.getCardProductCardType(
                any(StringBuffer.class))).thenReturn(cardDetails);
        when(cardRenewLetterServiceUnderTest.letterService.genaration(anyString(), anyString(),
                any(StringBuffer.class), anyString(), anyString())).thenReturn(new String[]{"result","result"});
        when(cardRenewLetterServiceUnderTest.cardRenewLetterRepo.updateLettergenStatusInCardRenew(
                any(StringBuffer.class), eq("YES"))).thenReturn(0);

        // Run the test
        final String[] result = cardRenewLetterServiceUnderTest.startCardRenewLetterProcess(cardNo, 0);

        // Verify the results
        assertThat(result).isEqualTo(new String[]{"result","result"});
    }
}
