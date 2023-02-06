package com.epic.cms.service;

import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;
import com.epic.cms.repository.LetterRepo;
import com.epic.cms.util.Configurations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LetterServiceTest {

    @Mock
    private LetterRepo mockLetterRepo;

    @InjectMocks
    private LetterService letterServiceUnderTest;

    static Stream<Arguments> argsProviderTempID() {
        return Stream.of(
//                arguments("def", "5"),
                arguments("CARJ", "0", "486295******8568", "CP001", "009")
        );
    }

    @ParameterizedTest
    @MethodSource({"argsProviderTempID"})
    void testGenaration(String tempID, String appID, StringBuffer cardnumber, String cardProduct, String seqNo) throws Exception {
        //configure
        Configurations.APPLICATION_REJECTION_LETTER_CODE = "CARJ";

        //setup
        when(mockLetterRepo.getParametersInLetterTemplate(anyString(), anyString())).thenReturn(new ArrayList<>(List.of("value")));
        when(mockLetterRepo.getCardTypebyApplicationID(anyString())).thenReturn(anyString());
        when(mockLetterRepo.getCardTypebyCardNumber(new StringBuffer())).thenReturn(anyString());
        when(mockLetterRepo.getLetterFieldDetails(anyString(), tempID)).thenReturn(any(LetterGenerationReferanceTableDetailsBean.class));

        when(mockLetterRepo.getParameterValueForAppLetters(any(String[].class), appID)).thenReturn("result");
        when(mockLetterRepo.getParameterValueForCardLetters(any(String[].class), any(StringBuffer.class))).thenReturn("result");
        when(mockLetterRepo.getTemplateBody(any(String[].class))).thenThrow(Exception.class);

        //run
        assertThatThrownBy(() -> letterServiceUnderTest.genaration(tempID, appID, cardnumber, cardProduct, seqNo)).isInstanceOf(Exception.class);

        //verify
        verify(mockLetterRepo, times(1)).getTemplateBody(any(String[].class));
    }
}
