package com.epic.cms.service;

import com.epic.cms.repository.CardApplicationConfirmationLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class CardApplicationConfirmationLetterServiceTest {

    private CardApplicationConfirmationLetterService cardApplicationConfirmationLetterServiceUnderTest;

    @BeforeEach
    void setUp() {
        cardApplicationConfirmationLetterServiceUnderTest = new CardApplicationConfirmationLetterService();
        cardApplicationConfirmationLetterServiceUnderTest.logManager = mock(LogManager.class);
        cardApplicationConfirmationLetterServiceUnderTest.letterService = mock(LetterService.class);
        cardApplicationConfirmationLetterServiceUnderTest.statusVarList = mock(StatusVarList.class);
        cardApplicationConfirmationLetterServiceUnderTest.cardApplicationConfirmationLetterRepo = mock(CardApplicationConfirmationLetterRepo.class);
        cardApplicationConfirmationLetterServiceUnderTest.commonFileGenProcessRepo = mock(CommonFileGenProcessRepo.class);
    }

    @Test
    @DisplayName("Card Application Confirmation Letter Process Test")
    void getConfirmationLetter() throws Exception {

        final StringBuffer confirmCard = new StringBuffer("4862950000698568");
        ArrayList<String> cardDetils = new ArrayList<>();
        cardDetils.add(0,"40VISACREDIT");
        cardDetils.add(1,"40110");
        cardDetils.add(2,"4862950000698568");
        Configurations.APPLICATION_CONFIRMATION_LETTER_CODE = "LT001";

        String[] fileNameAndPath = new String[10];
        fileNameAndPath[1]="src/test/resources";

        String maskCardNo = "4862950000698568";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(confirmCard)).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(confirmCard));
        }

        when(cardApplicationConfirmationLetterServiceUnderTest.commonFileGenProcessRepo.getCardProductCardType(any(StringBuffer.class))).thenReturn(cardDetils);

        when(cardApplicationConfirmationLetterServiceUnderTest.letterService.genaration(eq(Configurations.APPLICATION_CONFIRMATION_LETTER_CODE), anyString(), any(StringBuffer.class), eq(cardDetils.get(1)), anyString())).thenReturn(fileNameAndPath);

        when(cardApplicationConfirmationLetterServiceUnderTest.cardApplicationConfirmationLetterRepo.updateLettergenStatus(any(StringBuffer.class), eq("YES"))).thenReturn(0);

        cardApplicationConfirmationLetterServiceUnderTest.getConfirmationLetter(confirmCard, 0);

        verify(cardApplicationConfirmationLetterServiceUnderTest.commonFileGenProcessRepo,times(1)).InsertIntoDownloadTable(any(StringBuffer.class), anyString(), any());

        verify(cardApplicationConfirmationLetterServiceUnderTest.cardApplicationConfirmationLetterRepo,times(1)).updateLettergenStatus(any(StringBuffer.class), eq("YES"));

    }
}