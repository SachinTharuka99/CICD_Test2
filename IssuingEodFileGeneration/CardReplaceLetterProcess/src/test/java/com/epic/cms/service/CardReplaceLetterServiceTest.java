package com.epic.cms.service;

import com.epic.cms.repository.CardReplaceLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CardReplaceLetterServiceTest {

    private CardReplaceLetterService cardReplaceLetterServiceUnderTest;

    @BeforeEach
    void setUp() {
        cardReplaceLetterServiceUnderTest = new CardReplaceLetterService();
        cardReplaceLetterServiceUnderTest.logManager = mock(LogManager.class);
        cardReplaceLetterServiceUnderTest.statusVarList = mock(StatusVarList.class);
        cardReplaceLetterServiceUnderTest.letterService = mock(LetterService.class);
        cardReplaceLetterServiceUnderTest.cardReplaceLetterRepo = mock(CardReplaceLetterRepo.class);
        cardReplaceLetterServiceUnderTest.commonFileGenProcessRepo = mock(CommonFileGenProcessRepo.class);
    }

    @Test
    @DisplayName("for replace cards except product change cards")
    void testReplaceCardExceptProductChangeCard() throws Exception {
        // Setup
        final StringBuffer replaceCard = new StringBuffer("4862950000698568");
        List<String> cardDetils = new ArrayList<>();
        cardDetils.add(0,"40VISACREDIT");
        cardDetils.add(1,"40110");
        cardDetils.add(2,"4862950000698568");

        Configurations.CARD_REPLACEMENT_LETTER_CODE = "LT006";

        String[] fileNameAndPath = new String[10];
        fileNameAndPath[1]="src/test/resources";

        String maskCardNo = "4862950000698568";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(replaceCard)).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(replaceCard));
        }
        when(cardReplaceLetterServiceUnderTest.commonFileGenProcessRepo.getCardProductCardType(any(StringBuffer.class))).thenReturn(cardDetils);

        when(cardReplaceLetterServiceUnderTest.letterService.genaration(eq(Configurations.CARD_REPLACEMENT_LETTER_CODE), anyString(), any(StringBuffer.class), eq(cardDetils.get(1)), anyString())).thenReturn(fileNameAndPath);

        when(cardReplaceLetterServiceUnderTest.cardReplaceLetterRepo.updateLettergenStatusInCardReplace(any(StringBuffer.class), eq("YES"))).thenReturn(0);

        // Run the test
        cardReplaceLetterServiceUnderTest.replaceCardExceptProductChangeCard(replaceCard, 0);

        // Verify the results
        verify(cardReplaceLetterServiceUnderTest.commonFileGenProcessRepo,times(1)).InsertIntoDownloadTable(any(StringBuffer.class), anyString(), any());

        verify(cardReplaceLetterServiceUnderTest.cardReplaceLetterRepo,times(1)).updateLettergenStatusInCardReplace(any(StringBuffer.class), eq("YES"));
    }

    @Test
    @DisplayName("for product change cards")
    void testForProductChangeCards() throws Exception {
        // Setup
        final StringBuffer productChangeCard = new StringBuffer("4862950000698568");

        final StringBuffer replaceCard = new StringBuffer("4862950000698568");

        List<String> cardDetils = new ArrayList<>();
        cardDetils.add(0,"40VISACREDIT");
        cardDetils.add(1,"40110");
        cardDetils.add(2,"4862950000698568");

        Configurations.PRODUCT_CHANGE_LETTER_CODE = "LT007";

        String[] fileNameAndPath = new String[10];
        fileNameAndPath[1]="src/test/resources";

        String maskCardNo = "4862950000698568";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(replaceCard)).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(replaceCard));
        }

        when(cardReplaceLetterServiceUnderTest.cardReplaceLetterRepo.getCardProductCardTypeForProductChangeCards(any(StringBuffer.class))).thenReturn(cardDetils);

        when(cardReplaceLetterServiceUnderTest.letterService.genaration(eq(Configurations.PRODUCT_CHANGE_LETTER_CODE), anyString(), any(StringBuffer.class), eq(cardDetils.get(1)), anyString())).thenReturn(fileNameAndPath);

        when(cardReplaceLetterServiceUnderTest.cardReplaceLetterRepo.updateLettergenStatusInProductChange(any(StringBuffer.class), eq("YES"))).thenReturn(0);

        // Run the test
        cardReplaceLetterServiceUnderTest.forProductChangeCards(productChangeCard, 0);

        // Verify the results
        verify(cardReplaceLetterServiceUnderTest.commonFileGenProcessRepo,times(1)).InsertIntoDownloadTable(any(StringBuffer.class), anyString(), any());

        verify(cardReplaceLetterServiceUnderTest.cardReplaceLetterRepo,times(1)).updateLettergenStatusInProductChange(any(StringBuffer.class), eq("YES"));
    }

}
