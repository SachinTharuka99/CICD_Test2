package com.epic.cms.connector;

import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.GLSummaryFileRepo;
import com.epic.cms.service.FileGenerationService;
import com.epic.cms.service.GLSummaryFileService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GLSummaryFileConnectorTest {

    private GLSummaryFileConnector glSummaryFileConnectorUnderTest;

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
        glSummaryFileConnectorUnderTest = new GLSummaryFileConnector();
        glSummaryFileConnectorUnderTest.logManager = mock(LogManager.class);
        glSummaryFileConnectorUnderTest.statusVarList = mock(StatusVarList.class);
        glSummaryFileConnectorUnderTest.glSummaryFileService = mock(GLSummaryFileService.class);
        glSummaryFileConnectorUnderTest.glSummaryFileRepo = mock(GLSummaryFileRepo.class);
        glSummaryFileConnectorUnderTest.commonRepo = mock(CommonRepo.class);
        glSummaryFileConnectorUnderTest.fileGenerationService = mock(FileGenerationService.class);

        Configurations.EOD_ID =22100700;
        Configurations.ERROR_EOD_ID =22100700;
        Configurations.GL_SUMMARY_FILE_PREFIX="GL";
        Configurations.EOD_DATE = new Date();

    }

    @Test
    void testConcreteProcess() throws Exception {
        // Setup
        when(glSummaryFileConnectorUnderTest.logManager.processHeaderStyle("GL File Generation Process"))
                .thenReturn("result");
//        when(glSummaryFileConnectorUnderTest.logManager.processStartEndStyle(
//                "GL File Generation Successfully Started")).thenReturn("result");

        Configurations.PROCESS_ID_GL_FILE_CREATION = 100;
        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(100);
        processBean.setProcessDes("processDes");
        when(glSummaryFileConnectorUnderTest.commonRepo.getProcessDetails(eq(Configurations.PROCESS_ID_GL_FILE_CREATION))).thenReturn(processBean);
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");

        // Configure GLSummaryFileRepo.getCashbackDataToEODGL(...).
        final GlAccountBean glAccountBean = new GlAccountBean();
        glAccountBean.setCardNo(new StringBuffer("value"));
        glAccountBean.setMerchantID("merchantID");
        glAccountBean.setAccNo("accNo");
        glAccountBean.setGlType("glType");
        glAccountBean.setGlAmount("glAmount");
        glAccountBean.setAmount(0.0);
        glAccountBean.setFuelSurchargeAmount(0.0);
        glAccountBean.setCrDr("crDr");
        glAccountBean.setGlDate("glDate");
        glAccountBean.setKey("key");
        glAccountBean.setId(0);
        glAccountBean.setPaymentType("CASH_PAYMENT");
        final ArrayList<GlAccountBean> glAccountBeans = new ArrayList<>(List.of(glAccountBean));
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.getCashbackDataToEODGL()).thenReturn(glAccountBeans);

        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.insertIntoEodGLAccount(anyInt(),
                any(Date.class), any(StringBuffer.class), anyString(),
                anyDouble(), anyString(), anyString())).thenReturn(1);
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.updateCashback(0, 1)).thenReturn(1);
        when(glSummaryFileConnectorUnderTest.logManager.processDetailsStyles(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");

        // Configure GLSummaryFileRepo.getCashbackExpAndRedeemDataToEODGL(...).
        final GlAccountBean glAccountBean1 = new GlAccountBean();
        glAccountBean1.setCardNo(new StringBuffer("value"));
        glAccountBean1.setMerchantID("merchantID");
        glAccountBean1.setAccNo("accNo");
        glAccountBean1.setGlType("glType");
        glAccountBean1.setGlAmount("glAmount");
        glAccountBean1.setAmount(0.0);
        glAccountBean1.setFuelSurchargeAmount(0.0);
        glAccountBean1.setCrDr("crDr");
        glAccountBean1.setGlDate("glDate");
        glAccountBean1.setKey("key");
        glAccountBean1.setId(0);
        glAccountBean1.setPaymentType("CASH_PAYMENT");
        final ArrayList<GlAccountBean> glAccountBeans1 = new ArrayList<>(List.of(glAccountBean1));
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.getCashbackExpAndRedeemDataToEODGL())
                .thenReturn(glAccountBeans1);

        when(glSummaryFileConnectorUnderTest.statusVarList.getCASH_PAYMENT()).thenReturn("CASH_PAYMENT");
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.updateCashbackExpAndRedeem(0, 1)).thenReturn(0);

        // Configure GLSummaryFileRepo.getAdjustmentDataToEODGL(...).
        final GlAccountBean glAccountBean2 = new GlAccountBean();
        glAccountBean2.setCardNo(new StringBuffer("value"));
        glAccountBean2.setMerchantID("merchantID");
        glAccountBean2.setAccNo("accNo");
        glAccountBean2.setGlType("glType");
        glAccountBean2.setGlAmount("glAmount");
        glAccountBean2.setAmount(0.0);
        glAccountBean2.setFuelSurchargeAmount(0.0);
        glAccountBean2.setCrDr("crDr");
        glAccountBean2.setGlDate("glDate");
        glAccountBean2.setKey("key");
        glAccountBean2.setId(0);
        glAccountBean2.setPaymentType("CASH_PAYMENT");
        final ArrayList<GlAccountBean> glAccountBeans2 = new ArrayList<>(List.of(glAccountBean2));
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.getAdjustmentDataToEODGL()).thenReturn(glAccountBeans2);

        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.updateAdjusment("key", 1)).thenReturn(0);

        // Configure GLSummaryFileRepo.getFeeDataToEODGL(...).
        final GlAccountBean glAccountBean3 = new GlAccountBean();
        glAccountBean3.setCardNo(new StringBuffer("value"));
        glAccountBean3.setMerchantID("merchantID");
        glAccountBean3.setAccNo("accNo");
        glAccountBean3.setGlType("glType");
        glAccountBean3.setGlAmount("glAmount");
        glAccountBean3.setAmount(0.0);
        glAccountBean3.setFuelSurchargeAmount(0.0);
        glAccountBean3.setCrDr("crDr");
        glAccountBean3.setGlDate("glDate");
        glAccountBean3.setKey("key");
        glAccountBean3.setId(0);
        glAccountBean3.setPaymentType("CASH_PAYMENT");
        final ArrayList<GlAccountBean> glAccountBeans3 = new ArrayList<>(List.of(glAccountBean3));
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.getFeeDataToEODGL()).thenReturn(glAccountBeans3);

        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.updateFeeTable("key", 1)).thenReturn(0);

        // Configure GLSummaryFileRepo.getEODTxnDataToGL(...).
        final GlAccountBean glAccountBean4 = new GlAccountBean();
        glAccountBean4.setCardNo(new StringBuffer("value"));
        glAccountBean4.setMerchantID("merchantID");
        glAccountBean4.setAccNo("accNo");
        glAccountBean4.setGlType("glType");
        glAccountBean4.setGlAmount("glAmount");
        glAccountBean4.setAmount(0.0);
        glAccountBean4.setFuelSurchargeAmount(0.0);
        glAccountBean4.setCrDr("crDr");
        glAccountBean4.setGlDate("glDate");
        glAccountBean4.setKey("key");
        glAccountBean4.setId(0);
        glAccountBean4.setPaymentType("CASH_PAYMENT");
        final ArrayList<GlAccountBean> glAccountBeans4 = new ArrayList<>(List.of(glAccountBean4));
        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.getEODTxnDataToGL()).thenReturn(glAccountBeans4);

        // Configure GLSummaryFileService.createGLFile(...).
        final FileGenerationModel fileGenerationModel = new FileGenerationModel();
        fileGenerationModel.setFinalFile(new StringBuilder());
        fileGenerationModel.setFileHeader(new StringBuilder());
        fileGenerationModel.setFileContent(new StringBuilder());
        fileGenerationModel.setTxnIdList(new ArrayList<>(List.of(0)));
        fileGenerationModel.setHeaderCreditBig(new BigDecimal("0.00"));
        fileGenerationModel.setHeaderDebitBig(new BigDecimal("0.00"));
        fileGenerationModel.setHeaderCreditCount(0);
        fileGenerationModel.setHeaderDebitCount(0);
        fileGenerationModel.setTotalFileTxnCount(0);
        fileGenerationModel.setStatus(false);
        fileGenerationModel.setDeleteStatus(false);
        when(glSummaryFileConnectorUnderTest.glSummaryFileService.createGLFile(anyString(), anyString(),
                anyString(), any(ProcessBean.class))).thenReturn(fileGenerationModel);

        when(glSummaryFileConnectorUnderTest.glSummaryFileRepo.updateEodGLAccount(0)).thenReturn(0);
        when(glSummaryFileConnectorUnderTest.statusVarList.getSUCCES_STATUS()).thenReturn("SUCCES_STATUS");
        when(glSummaryFileConnectorUnderTest.statusVarList.getERROR_STATUS()).thenReturn("result");

        // Run the test
        glSummaryFileConnectorUnderTest.concreteProcess();

        // Verify the results
        verify( glSummaryFileConnectorUnderTest.glSummaryFileRepo,times(5)).insertIntoEodGLAccount(anyInt(),
                any(Date.class), any(StringBuffer.class), anyString(),
                anyDouble(), anyString(), anyString());
        verify(glSummaryFileConnectorUnderTest.glSummaryFileRepo).updateCashback(0, 1);
        verify(glSummaryFileConnectorUnderTest.glSummaryFileRepo).updateCashbackExpAndRedeem(0, 1);
        verify(glSummaryFileConnectorUnderTest.glSummaryFileRepo).updateAdjusment("key", 1);
        verify(glSummaryFileConnectorUnderTest.glSummaryFileRepo).updateFeeTable("key", 1);
        verify(glSummaryFileConnectorUnderTest.glSummaryFileRepo).updateEODTxn("key", 1);
    }
}
