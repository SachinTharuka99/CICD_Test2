package com.epic.cms.connector;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.AutoSettlementRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.AutoSettlementService;
import com.epic.cms.service.FileGenerationService;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.*;

class AutoSettlementConnectorTest {

    private AutoSettlementConnector autoSettlementConnectorUnderTest;

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
        autoSettlementConnectorUnderTest = new AutoSettlementConnector();
        autoSettlementConnectorUnderTest.logManager = mock(LogManager.class);
        autoSettlementConnectorUnderTest.statusVarList = mock(StatusVarList.class);
        autoSettlementConnectorUnderTest.autoSettlementService = mock(AutoSettlementService.class);
        autoSettlementConnectorUnderTest.autoSettlementRepo = mock(AutoSettlementRepo.class);
        autoSettlementConnectorUnderTest.commonRepo = mock(CommonRepo.class);
        autoSettlementConnectorUnderTest.fileGenerationService = mock(FileGenerationService.class);

        Configurations.EOD_ID =22100700;
        Configurations.ERROR_EOD_ID =22100700;
        Configurations.AUTOSETTLEMENT_FILE_PREFIX="AUTO";
        Configurations.EOD_DATE = new Date();
    }

    @Test
    void testConcreteProcess() throws Exception {
        // Setup

        Configurations.AUTO_SETTLEMENT_PROCESS = 100;
        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(100);
        processBean.setProcessDes("processDes");
        when(autoSettlementConnectorUnderTest.commonRepo.getProcessDetails(eq(Configurations.AUTO_SETTLEMENT_PROCESS))).thenReturn(processBean);
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");


        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generatePartialAutoSettlementFile("fileDirectory",
                "fileName", "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generateAutoSettlementFile("fileDirectory", "fileName",
                "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementService.createFileHeaderForAutoSettlementFile("fileName",
                new BigDecimal("0.00"), 0, "sequence", "fieldDelimeter")).thenReturn(new StringBuilder());
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
        verify(autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
    }

    @Test
    void testConcreteProcess_AutoSettlementRepoUpdateAutoSettlementWithPaymentsThrowsException() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments())
                .thenThrow(Exception.class);
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
    }

    @Test
    void testConcreteProcess_AutoSettlementRepoGetUnsuccessfullStandingInstructionFeeEligibleCardsThrowsException() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        doThrow(Exception.class).when(
                autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
    }

    @Test
    void testConcreteProcess_AutoSettlementRepoGeneratePartialAutoSettlementFileReturnsNoItems() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generatePartialAutoSettlementFile("fileDirectory",
                "fileName", "sequence", "fieldDelimeter")).thenReturn(new String[]{});
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generateAutoSettlementFile("fileDirectory", "fileName",
                "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementService.createFileHeaderForAutoSettlementFile("fileName",
                new BigDecimal("0.00"), 0, "sequence", "fieldDelimeter")).thenReturn(new StringBuilder());
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
        verify(autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
    }

    @Test
    void testConcreteProcess_AutoSettlementRepoGeneratePartialAutoSettlementFileThrowsException() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generatePartialAutoSettlementFile("fileDirectory",
                "fileName", "sequence", "fieldDelimeter")).thenThrow(Exception.class);
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
        verify(autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
    }

    @Test
    void testConcreteProcess_AutoSettlementRepoGenerateAutoSettlementFileReturnsNoItems() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generatePartialAutoSettlementFile("fileDirectory",
                "fileName", "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generateAutoSettlementFile("fileDirectory", "fileName",
                "sequence", "fieldDelimeter")).thenReturn(new String[]{});
        when(autoSettlementConnectorUnderTest.autoSettlementService.createFileHeaderForAutoSettlementFile("fileName",
                new BigDecimal("0.00"), 0, "sequence", "fieldDelimeter")).thenReturn(new StringBuilder());
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
        verify(autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
    }

    @Test
    void testConcreteProcess_AutoSettlementRepoGenerateAutoSettlementFileThrowsException() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generatePartialAutoSettlementFile("fileDirectory",
                "fileName", "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generateAutoSettlementFile("fileDirectory", "fileName",
                "sequence", "fieldDelimeter")).thenThrow(Exception.class);
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
        verify(autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
    }

    @Test
    void testConcreteProcess_AutoSettlementServiceThrowsException() throws Exception {
        // Setup
        when(autoSettlementConnectorUnderTest.logManager.processHeaderStyle("AutoSettlement Process"))
                .thenReturn("result");
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.updateAutoSettlementWithPayments()).thenReturn(0);
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generatePartialAutoSettlementFile("fileDirectory",
                "fileName", "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementRepo.generateAutoSettlementFile("fileDirectory", "fileName",
                "sequence", "fieldDelimeter")).thenReturn(new String[]{"result"});
        when(autoSettlementConnectorUnderTest.autoSettlementService.createFileHeaderForAutoSettlementFile("fileName",
                new BigDecimal("0.00"), 0, "sequence", "fieldDelimeter")).thenThrow(Exception.class);
        when(autoSettlementConnectorUnderTest.logManager.ProcessStartEndStyle(
                "AutoSettlement Process Fails")).thenReturn("result");
        when(autoSettlementConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
        when(autoSettlementConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");
        when(autoSettlementConnectorUnderTest.logManager.processSummeryStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Run the test
        autoSettlementConnectorUnderTest.concreteProcess();

        // Verify the results
        verify(autoSettlementConnectorUnderTest.autoSettlementRepo).getUnsuccessfullStandingInstructionFeeEligibleCards();
    }
}
