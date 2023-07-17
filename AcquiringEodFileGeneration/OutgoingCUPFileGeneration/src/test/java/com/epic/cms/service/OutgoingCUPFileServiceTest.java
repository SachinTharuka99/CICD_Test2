package com.epic.cms.service;

import com.epic.cms.dao.OutgoingCUPFileDao;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OutgoingCUPFileServiceTest {

    private OutgoingCUPFileService outgoingCUPFileService;

    @BeforeEach
    void setUp() {
        outgoingCUPFileService = new OutgoingCUPFileService();
        outgoingCUPFileService.outgoingCUPFileDao = mock(OutgoingCUPFileDao.class);
    }

    @Test
    void generateOutgoingCUPFile() {
    }

    @Test
    void generateHeader() {
    }

    @Test
    void generateTailer() {
    }

    @Test
    void getTxnCodeTailer() {
    }

    @Test
    void getTransactionRecodeCountTailer() {
    }

    @Test
    void getMAK() {
    }

    @Test
    void getMAC() {
    }

    @Test
    void getBlockbitmapTailer() {
    }

    @Test
    void getGSCSReservedDate() {
    }

    @Test
    void getTxnCodeHeader() {
    }

    @Test
    void getIIN() {
    }

    @Test
    void getVersionTag() {
    }

    @Test
    void getVersionNumber() {
    }

    @Test
    void getBatchDate() {
    }

    @Test
    void getBlockbitmapHeader() {
    }

    @Test
    void addToStatementOutgoingFieldIdentityTable() {
    }

    @Test
    void decimalToHex() {
    }

    @Test
    void binaryToDecimal() {
    }
}