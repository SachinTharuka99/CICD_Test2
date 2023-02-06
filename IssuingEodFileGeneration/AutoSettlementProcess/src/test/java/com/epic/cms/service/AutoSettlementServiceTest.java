package com.epic.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutoSettlementServiceTest {

    private AutoSettlementService autoSettlementServiceUnderTest;

    @BeforeEach
    void setUp() {
        autoSettlementServiceUnderTest = new AutoSettlementService();
    }

    @Test
    void testCreateFileHeaderForAutoSettlementFile() throws Exception {
        assertThat(
                autoSettlementServiceUnderTest.createFileHeaderForAutoSettlementFile("fileName", new BigDecimal("0.00"),
                        0, "sequence", "fieldDelimeter")).isEqualTo(new StringBuilder());
        assertThatThrownBy(() -> autoSettlementServiceUnderTest.createFileHeaderForAutoSettlementFile("fileName",
                new BigDecimal("0.00"), 0, "sequence", "fieldDelimeter")).isInstanceOf(Exception.class);
    }
}
