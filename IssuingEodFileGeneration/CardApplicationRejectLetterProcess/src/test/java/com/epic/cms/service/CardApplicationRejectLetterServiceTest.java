package com.epic.cms.service;

import com.epic.cms.repository.CardApplicationRejectLetterRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardApplicationRejectLetterServiceTest {

    private CardApplicationRejectLetterService cardApplicationRejectLetterServiceUnderTest;

    @BeforeEach
    void setUp() {
        cardApplicationRejectLetterServiceUnderTest = new CardApplicationRejectLetterService();
        cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo = mock(
                CardApplicationRejectLetterRepo.class);
        cardApplicationRejectLetterServiceUnderTest.letterService = mock(LetterService.class);
        cardApplicationRejectLetterServiceUnderTest.commonRepo = mock(CommonRepo.class);
        cardApplicationRejectLetterServiceUnderTest.commonFileGenProcessRepo = mock(CommonFileGenProcessRepo.class);
        cardApplicationRejectLetterServiceUnderTest.logManager = mock(LogManager.class);
    }

//    @Test
//    void testProcessCardApplicationRejected() throws Exception {
//
//        String applicatioId = "13258796586";
//
//        String[] fileNameAndPath = new String[10];
//        fileNameAndPath[0]="src/test/resources";
//        fileNameAndPath[1]="src/test/resources";
//        Configurations.APPLICATION_REJECTION_LETTER_CODE = "LT002";
//
//        List<String> cardDetails = new ArrayList<String>();
//        cardDetails.add(0,"value");
//        cardDetails.add(1,"value");
//        cardDetails.add(2,"value");
//
//        // Setup
//        when(cardApplicationRejectLetterServiceUnderTest.commonFileGenProcessRepo.getCardProductCardTypeByApplicationId(
//                "aplicationId")).thenReturn(List.of("value"));
//        when(cardApplicationRejectLetterServiceUnderTest.letterService.genaration(
//                eq("APPLICATION_REJECTION_LETTER_CODE"), eq("aplicationId"), any(StringBuffer.class), eq("cardProduct"),
//                eq("sequenceNo"))).thenReturn(new String[]{"result"});
//        when(cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo.getCardNo(
//                "aplicationId")).thenReturn(new StringBuffer("value"));
//        when(cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo.updateLettergenStatus(
//                any(StringBuffer.class), eq("YES"))).thenReturn(0);
//        when(cardApplicationRejectLetterServiceUnderTest.logManager.processSummeryStyles(
//                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
//
//        // Run the test
//        final String[] result = cardApplicationRejectLetterServiceUnderTest.processCardApplicationReject("aplicationId",
//                0);
//
//        // Verify the results
//        assertThat(result).isEqualTo(new String[]{"result"});
//        verify(cardApplicationRejectLetterServiceUnderTest.commonFileGenProcessRepo).InsertIntoDownloadTable(
//                any(StringBuffer.class), eq("filename"), eq(List.of("value")));
//        verify(cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo).updateLettergenStatus(
//                any(StringBuffer.class), eq("YES"));
//    }

    @Test
    @DisplayName("Test case for Card Application Reject Letter")
    void testProcessCardApplicationReject() throws Exception {
        String[] fileNameAndPath = new String[10];
        fileNameAndPath[0] = "src/test/resources";
        fileNameAndPath[1] = "src/test/resources";
        Configurations.APPLICATION_REJECTION_LETTER_CODE = "LT002";

        List<String> cardDetails = new ArrayList<String>();
        cardDetails.add(0, "value");
        cardDetails.add(1, "value");
        cardDetails.add(2, "value");

        // Setup
        when(cardApplicationRejectLetterServiceUnderTest.commonFileGenProcessRepo.getCardProductCardType(any(StringBuffer.class))).thenReturn(cardDetails);
        when(cardApplicationRejectLetterServiceUnderTest.letterService.genaration(anyString(), anyString(), any(StringBuffer.class), anyString(), anyString())).thenReturn(fileNameAndPath);
        when(cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo.updateLettergenStatus(any(StringBuffer.class), anyString())).thenReturn(0);
        when(cardApplicationRejectLetterServiceUnderTest.commonFileGenProcessRepo.getCardProductCardTypeByApplicationId("010000000022")).thenReturn(cardDetails);
        when(cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo.getCardNo(anyString())).thenReturn(any(StringBuffer.class));

        // Run the test
        final String[] result = cardApplicationRejectLetterServiceUnderTest.processCardApplicationReject("010000000022", 0);

        // Verify the results
        verify(cardApplicationRejectLetterServiceUnderTest.cardApplicationRejectLetterRepo, times(1)).getCardNo(anyString());
    }
}
