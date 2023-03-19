package com.epic.cms.service;

import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.InstallmentBean;
import com.epic.cms.model.bean.ManualNpRequestBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InstallmentPaymentRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BalanceTransferServiceTest {

    private BalanceTransferService balanceTransferServiceUnderTest;
    private BalanceTransferService spyBalanceTransferServiceUnderTest;

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
        balanceTransferServiceUnderTest = new BalanceTransferService();
        balanceTransferServiceUnderTest.commonRepo = mock(CommonRepo.class);
        balanceTransferServiceUnderTest.logManager = mock(LogManager.class);
        balanceTransferServiceUnderTest.statusList = mock(StatusVarList.class);
        balanceTransferServiceUnderTest.installmentPaymentRepo = mock(InstallmentPaymentRepo.class);
        spyBalanceTransferServiceUnderTest = spy(BalanceTransferService.class);
    }

    @Test
    void testAccelerateBalanceTransferRequestForNpAccount() throws Exception {
        // Setup
        when(balanceTransferServiceUnderTest.statusList.getYES_STATUS_1()).thenReturn(0);
        when(balanceTransferServiceUnderTest.statusList.getCOMMON_REQUEST_ACCEPTED())
                .thenReturn("COMMON_REQUEST_ACCEPTED");

        // Configure InstallmentPaymentRepo.getManualNpRequestDetails(...).
        final ManualNpRequestBean manualNpRequestBean = new ManualNpRequestBean();
        manualNpRequestBean.setAccNumber("accNumber");
        manualNpRequestBean.setAccStatus("accStatus");
        manualNpRequestBean.setCardNumber(new StringBuffer("value"));
        manualNpRequestBean.setRequestId(0);
        manualNpRequestBean.setNdia(0);
        final List<ManualNpRequestBean> manualNpRequestBeans = List.of(manualNpRequestBean);
        when(balanceTransferServiceUnderTest.installmentPaymentRepo.getManualNpRequestDetails(0,
                "COMMON_REQUEST_ACCEPTED")).thenReturn(manualNpRequestBeans);

        when(balanceTransferServiceUnderTest.installmentPaymentRepo.updateEasyPaymentRequestToAccelerate("accNumber",
                "BALANCETRASFERREQUEST")).thenReturn(0);

        // Configure InstallmentPaymentRepo.getDelinquentAccounts(...).
        final DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        delinquentAccountBean.setAccNo("accNO");
        delinquentAccountBean.setCif("cif");
        delinquentAccountBean.setNameOnCard("nameOnCard");
        delinquentAccountBean.setNameInFull("nameInFull");
        delinquentAccountBean.setIdType("idType");
        delinquentAccountBean.setIdNumber("idNumber");
        delinquentAccountBean.setAccStatus("accStatus");
        delinquentAccountBean.setCardCategory("cardCategory");
        delinquentAccountBean.setNDIA(0);
        delinquentAccountBean.setMIA(0);
        delinquentAccountBean.setLastStatementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setContactNo("contactNo");
        delinquentAccountBean.setEmail("email");
        delinquentAccountBean.setAddress("address");
        delinquentAccountBean.setRiskClass("riskClass");

        String[] newRiskClass = new String[3];
        newRiskClass[0] = "1";
        newRiskClass[1] = "1";
        newRiskClass[2] = "1";

        final List<DelinquentAccountBean> delinquentAccountBeans = List.of(delinquentAccountBean);
        when(balanceTransferServiceUnderTest.installmentPaymentRepo.getDelinquentAccounts())
                .thenReturn(delinquentAccountBeans);

        when(balanceTransferServiceUnderTest.installmentPaymentRepo.checkForPayment("accNO",
                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(0.0);
        when(balanceTransferServiceUnderTest.installmentPaymentRepo.getRiskClassOnNdia(delinquentAccountBean.getNDIA() + 1))
                .thenReturn(newRiskClass);
        when(balanceTransferServiceUnderTest.installmentPaymentRepo.getNPRiskClass()).thenReturn("riskClass");
        when(balanceTransferServiceUnderTest.installmentPaymentRepo.getNDIAOnRiskClass("riskClass"))
                .thenReturn(new String[]{"value","1"});
        when(balanceTransferServiceUnderTest.installmentPaymentRepo.checkLeastMinimumPayment("accNO")).thenReturn(0.0);

        // Run the test
        balanceTransferServiceUnderTest.accelerateBalanceTransferRequestForNpAccount();

        // Verify the results
        verify(balanceTransferServiceUnderTest.installmentPaymentRepo).updateEasyPaymentRequestToAccelerate("accNumber",
                "BALANCETRASFERREQUEST");
    }

    final String subTestCase1 = "Test FirstInstallment - Balance Transfer Process for installment (Running Status != 1) and AccelerateStatus = 'YES' and FeeType ='FEE'";
    final String subTestCase2 = "Test FirstInstallment - Balance Transfer Process for installment (Running Status != 1) and AccelerateStatus = 'NO'";
    final String subTestCase3 = "Test Installment - Balance Transfer Process for installment (Running Status == 1) and AccelerateStatus = 'YES'";

    @ParameterizedTest
    @ValueSource(strings = {subTestCase1, subTestCase2, subTestCase3})
    @DisplayName("Test Balance Transfer Process")
    void testStartBalanceTransferProcess(String subTestCase) throws Exception {
        // Setup
        Configurations.EOD_DATE = new Date();
        Configurations.EOD_ID = 2200782;
        Configurations.TXN_TYPE_SALE = "TXN_SALE";
        Configurations.TXN_TYPE_REVERSAL_INSTALLMENT = "TXN_REVERSAL_INSTALLMENT";
        Configurations.DEBIT ="DEBIT";
        Configurations.CREDIT ="CREDIT";

        final InstallmentBean installmentBean = new InstallmentBean();
        installmentBean.setCurrentCount(0);
        installmentBean.setCardNumber(new StringBuffer("4380431766518012"));
        installmentBean.setTxnID("txnID");
        installmentBean.setTxnAmount("0.0");
        installmentBean.setTotalFEeAmount("0.0");
        installmentBean.setStatus("RQAC");
        installmentBean.setInstalmentAmount("0.0");
        installmentBean.setCurruncyCode("curruncyCode");
        installmentBean.setInterestRate("0.0");
        installmentBean.setAccNo("accNo");
        installmentBean.setRemainingCount(0);
        installmentBean.setNxtTxnDate("nxtTxnDate");
        installmentBean.setTxnDescription("Balance Transfer");
        installmentBean.setDuration(0);
        installmentBean.setFeeApplyFirstMonth("NO");
        installmentBean.setTraceNumber("traceNumber");

        if(subTestCase.equals(subTestCase1)){
            installmentBean.setRunningStatus(0);
            installmentBean.setAccelarateStatus("YES");
            installmentBean.setFeeType("FEE");
        } else if (subTestCase.equals(subTestCase2)) {
            installmentBean.setRunningStatus(0);
            installmentBean.setAccelarateStatus("NO");
            installmentBean.setFeeType("INT");
        }else if (subTestCase.equals(subTestCase3)) {
            installmentBean.setRunningStatus(1);
            installmentBean.setAccelarateStatus("YES");
            installmentBean.setFeeType("FEE_Type");
        }

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);


        when(balanceTransferServiceUnderTest.commonRepo.getCardAssociationFromCardBin(anyString()))
                .thenReturn("cardAssociation");
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");

        when(balanceTransferServiceUnderTest.commonRepo.insertIntoEodGLAccount(anyInt(),any(Date.class), any(StringBuffer.class),
                any(), anyDouble(), any(), any())).thenReturn(0);

        when(balanceTransferServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionWithoutGL(
                any(StringBuffer.class), anyString(), anyDouble(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), any(), anyInt(),
                anyString())).thenReturn(1);

        when(balanceTransferServiceUnderTest.commonRepo.insertInToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                anyString(), anyString(), any(),anyString())).thenReturn(1);

        doReturn(0.0).when(spyBalanceTransferServiceUnderTest).calculateUpfrontfalseFeePortion(anyDouble(),anyInt(),anyInt());
        doReturn(new String[]{"0.0","0.0"}).when(spyBalanceTransferServiceUnderTest).calculateFirstInstallment(installmentBean);

        if(installmentBean.getRunningStatus() != 1){
            when(balanceTransferServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionOnlyVisaFalse(
                    any(StringBuffer.class), anyString(), anyDouble(), anyString(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), anyString(), anyInt(),
                    anyString())).thenReturn(0);

            when(balanceTransferServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(
                    any(InstallmentBean.class), eq("BALANCETRASFERREQUEST"))).thenReturn(1);
            when(balanceTransferServiceUnderTest.installmentPaymentRepo.updateFeeToEDONInTransactionTable(
                    any(StringBuffer.class), eq("traceNumber"), any())).thenReturn(1);

        }
        if(installmentBean.getRunningStatus() == 1){
            when(balanceTransferServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTable(any(InstallmentBean.class),
                    eq("BALANCETRASFERREQUEST"))).thenReturn(1);
        }

        // Run the test
        balanceTransferServiceUnderTest.startBalanceTransferProcess(installmentBean, processBean);

        // Verify the results
        if(installmentBean.getRunningStatus() != 1){
            verify(balanceTransferServiceUnderTest.installmentPaymentRepo).insertInToEODTransactionOnlyVisaFalse(any(StringBuffer.class),
                    anyString(), anyDouble(), anyString(), anyString(), anyString(),anyString(), anyString(), anyString(), anyString(), anyInt(),
                    anyString());

            verify(balanceTransferServiceUnderTest.installmentPaymentRepo).updateEasyPaymentTableWithFirstInstallment(
                    any(InstallmentBean.class), eq("BALANCETRASFERREQUEST"));
            verify(balanceTransferServiceUnderTest.installmentPaymentRepo).updateFeeToEDONInTransactionTable(
                    any(StringBuffer.class), eq("traceNumber"), any());

            verify(balanceTransferServiceUnderTest.commonRepo,times(2)).insertIntoEodGLAccount(anyInt(),any(Date.class), any(StringBuffer.class),
                    any(), anyDouble(), any(), any());

            verify(balanceTransferServiceUnderTest.commonRepo, times(2)).insertInToEODTransaction(any(StringBuffer.class), anyString(),
                    anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                    anyString(), anyString(), any(),anyString());
        }
        if(installmentBean.getRunningStatus() == 1){
            verify(balanceTransferServiceUnderTest.installmentPaymentRepo).updateEasyPaymentTable(
                    any(InstallmentBean.class), eq("BALANCETRASFERREQUEST"));

            verify(balanceTransferServiceUnderTest.commonRepo,times(1)).insertIntoEodGLAccount(anyInt(),any(Date.class), any(StringBuffer.class),
                    any(), anyDouble(), any(), any());

            verify(balanceTransferServiceUnderTest.commonRepo).insertInToEODTransaction(any(StringBuffer.class), anyString(),
                    anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                    anyString(), anyString(), any(),anyString());
        }

        verify(balanceTransferServiceUnderTest.installmentPaymentRepo).insertInToEODTransactionWithoutGL(any(StringBuffer.class), anyString(), anyDouble(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), any(), anyInt(),
                anyString());


    }
}
