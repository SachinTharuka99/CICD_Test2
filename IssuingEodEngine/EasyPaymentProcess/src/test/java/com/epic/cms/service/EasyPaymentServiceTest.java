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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class EasyPaymentServiceTest {
    private EasyPaymentService easyPaymentServiceUnderTest;
    private EasyPaymentService spyEasyPaymentServiceUnderTest;
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        easyPaymentServiceUnderTest = new EasyPaymentService();
        easyPaymentServiceUnderTest.logManager = mock(LogManager.class);
        easyPaymentServiceUnderTest.commonRepo = mock(CommonRepo.class);
        easyPaymentServiceUnderTest.statusList = mock(StatusVarList.class);
        easyPaymentServiceUnderTest.installmentPaymentRepo = mock(InstallmentPaymentRepo.class);
        spyEasyPaymentServiceUnderTest = spy(EasyPaymentService.class);
    }

    static MockedStatic<CommonMethods> common;

    @BeforeAll
    public static void init() {
        common = Mockito.mockStatic(CommonMethods.class);
    }

    @AfterAll
    public static void close() {
        common.close();
    }

    @Test
    void test_accelerateEasyPaymentRequestForNpAccount() throws Exception {
        // Setup
        when(easyPaymentServiceUnderTest.statusList.getYES_STATUS_1()).thenReturn(0);
        when(easyPaymentServiceUnderTest.statusList.getCOMMON_REQUEST_ACCEPTED()).thenReturn("COMMON_REQUEST_ACCEPTED");

        // Configure InstallmentPaymentRepo.getManualNpRequestDetails(...).
        final ManualNpRequestBean manualNpRequestBean = new ManualNpRequestBean();
        manualNpRequestBean.setAccNumber("accNumber");
        manualNpRequestBean.setAccStatus("accStatus");
        manualNpRequestBean.setCardNumber(new StringBuffer("value"));
        manualNpRequestBean.setRequestId(0);
        manualNpRequestBean.setNdia(0);
        final List<ManualNpRequestBean> manualNpRequestBeans = List.of(manualNpRequestBean);
        when(easyPaymentServiceUnderTest.installmentPaymentRepo.getManualNpRequestDetails(0,
                "COMMON_REQUEST_ACCEPTED")).thenReturn(manualNpRequestBeans);

        when(easyPaymentServiceUnderTest.installmentPaymentRepo.updateEasyPaymentRequestToAccelerate("accNumber",
                "EASYPAYMENTREQUEST")).thenReturn(0);

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
        final List<DelinquentAccountBean> delinquentAccountBeans = List.of(delinquentAccountBean);

        String[] newRiskClass = new String[3];
        newRiskClass[0] = "1";
        newRiskClass[1] = "1";
        newRiskClass[2] = "1";


        when(easyPaymentServiceUnderTest.installmentPaymentRepo.getDelinquentAccounts())
                .thenReturn(delinquentAccountBeans);

        when(easyPaymentServiceUnderTest.installmentPaymentRepo.checkForPayment("accNO",
                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(0.0);
        when(easyPaymentServiceUnderTest.installmentPaymentRepo.getRiskClassOnNdia(anyInt()))
                .thenReturn(newRiskClass);
        when(easyPaymentServiceUnderTest.installmentPaymentRepo.getNPRiskClass()).thenReturn("riskClass");
        when(easyPaymentServiceUnderTest.installmentPaymentRepo.getNDIAOnRiskClass("riskClass"))
                .thenReturn(new String[]{"value","1"});
        when(easyPaymentServiceUnderTest.installmentPaymentRepo.checkLeastMinimumPayment("accNO")).thenReturn(0.0);

        // Run the test
        easyPaymentServiceUnderTest.accelerateEasyPaymentRequestForNpAccount();

        // Verify the results
        verify(easyPaymentServiceUnderTest.installmentPaymentRepo).updateEasyPaymentRequestToAccelerate("accNumber",
                "EASYPAYMENTREQUEST");
    }

    final String subTestCase1 = "Test FirstInstallment - Easy Payment Process for installment (Running Status != 1) and AccelerateStatus = 'YES' and FeeType ='FEE'";
    final String subTestCase2 = "Test FirstInstallment - Easy Payment Process for installment (Running Status != 1) and AccelerateStatus = 'NO'";
    final String subTestCase3 = "Test Installment - Easy Payment Process for installment (Running Status == 1) and AccelerateStatus = 'YES'";

    @ParameterizedTest
    @ValueSource(strings = {subTestCase1, subTestCase2, subTestCase3})
    @DisplayName("Test Easy Payment Process")
    void test_startBalanceTransferProcess(String subTestCase) throws Exception {
        // Setup
        Configurations.EOD_DATE = new Date();
        Configurations.EOD_ID = 2200782;
        Configurations.CREDIT ="CREDIT";

        final InstallmentBean easyPaymentBean = new InstallmentBean();
        easyPaymentBean.setCurrentCount(0);
        easyPaymentBean.setCardNumber(new StringBuffer("4380431766518012"));
        easyPaymentBean.setTxnID("txnID");
        easyPaymentBean.setTxnAmount("0.0");
        easyPaymentBean.setTotalFEeAmount("0.0");
        easyPaymentBean.setStatus("RQAC");
        easyPaymentBean.setInstalmentAmount("0.0");
        easyPaymentBean.setCurruncyCode("curruncyCode");
        easyPaymentBean.setInterestRate("0.0");
        easyPaymentBean.setAccNo("accNo");
        easyPaymentBean.setRemainingCount(0);
        easyPaymentBean.setNxtTxnDate("nxtTxnDate");
        easyPaymentBean.setRunningStatus(0);
        easyPaymentBean.setTxnDescription("txnDescription");
        easyPaymentBean.setDuration(0);
        easyPaymentBean.setFeeApplyFirstMonth("NO");
        easyPaymentBean.setFeeType("feeType");
        easyPaymentBean.setAccelarateStatus("accelarateStatus");
        easyPaymentBean.setTraceNumber("traceNumber");

        if(subTestCase.equals(subTestCase1)){
            easyPaymentBean.setRunningStatus(0);
            easyPaymentBean.setAccelarateStatus("YES");
            easyPaymentBean.setFeeType("FEE");
        } else if (subTestCase.equals(subTestCase2)) {
            easyPaymentBean.setRunningStatus(0);
            easyPaymentBean.setAccelarateStatus("NO");
            easyPaymentBean.setFeeType("INT");
        }else if (subTestCase.equals(subTestCase3)) {
            easyPaymentBean.setRunningStatus(1);
            easyPaymentBean.setAccelarateStatus("YES");
            easyPaymentBean.setFeeType("FEE_Type");
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

        when(easyPaymentServiceUnderTest.commonRepo.getCardAssociationFromCardBin(anyString()))
                .thenReturn("cardAssociation");
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");

        when(easyPaymentServiceUnderTest.commonRepo.insertIntoEodGLAccount(anyInt(),any(Date.class), any(StringBuffer.class),
                any(), anyDouble(), any(), any())).thenReturn(0);

        when(easyPaymentServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionWithoutGL(
                any(StringBuffer.class), anyString(), anyDouble(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), any(), anyInt(),
                anyString())).thenReturn(1);

        when(easyPaymentServiceUnderTest.commonRepo.insertInToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                anyString(), any(), any(),anyString())).thenReturn(1);

        doReturn(0.0).when(spyEasyPaymentServiceUnderTest).calculateUpfrontfalseFeePortion(anyDouble(),anyInt(),anyInt());
        doReturn(new String[]{"0.0","0.0"}).when(spyEasyPaymentServiceUnderTest).calculateFirstInstallmentAmountAndFee(easyPaymentBean);

        when(easyPaymentServiceUnderTest.installmentPaymentRepo.getEodtxnDescription(anyString()))
                .thenReturn("txnDescription");

        if(easyPaymentBean.getRunningStatus() != 1){

            when(easyPaymentServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(
                    any(InstallmentBean.class), eq("EASYPAYMENTREQUEST"))).thenReturn(1);
            when(easyPaymentServiceUnderTest.installmentPaymentRepo.updateFeeToEDONInTransactionTable(
                    any(StringBuffer.class), eq("traceNumber"), any())).thenReturn(1);

        }
        if(easyPaymentBean.getRunningStatus() == 1){
            when(easyPaymentServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTable(any(InstallmentBean.class),
                    eq("EASYPAYMENTREQUEST"))).thenReturn(1);
        }

        // Run the test
        easyPaymentServiceUnderTest.startEasyPaymentProcess(easyPaymentBean, processBean,faileCardCount);

        // Verify the results
        if(easyPaymentBean.getRunningStatus() != 1){

            verify(easyPaymentServiceUnderTest.installmentPaymentRepo).updateEasyPaymentTableWithFirstInstallment(
                    any(InstallmentBean.class), eq("EASYPAYMENTREQUEST"));
            verify(easyPaymentServiceUnderTest.installmentPaymentRepo).updateFeeToEDONInTransactionTable(
                    any(StringBuffer.class), eq("traceNumber"), any());

            verify(easyPaymentServiceUnderTest.commonRepo,times(2)).insertIntoEodGLAccount(anyInt(),any(Date.class), any(StringBuffer.class),
                    any(), anyDouble(), any(), any());

            verify(easyPaymentServiceUnderTest.commonRepo, times(1)).insertInToEODTransaction(any(StringBuffer.class), anyString(),
                    anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                    anyString(), any(), any(),anyString());
        }
        if(easyPaymentBean.getRunningStatus() == 1){
            verify(easyPaymentServiceUnderTest.installmentPaymentRepo).updateEasyPaymentTable(
                    any(InstallmentBean.class), eq("EASYPAYMENTREQUEST"));

            verify(easyPaymentServiceUnderTest.commonRepo,times(1)).insertIntoEodGLAccount(anyInt(),any(Date.class), any(StringBuffer.class),
                    any(), anyDouble(), any(), any());

            verify(easyPaymentServiceUnderTest.commonRepo).insertInToEODTransaction(any(StringBuffer.class), anyString(),
                    anyString(), anyString(), anyString(), anyString(), any(), anyString(),
                    anyString(), any(), any(),anyString());
        }

        verify(easyPaymentServiceUnderTest.installmentPaymentRepo).insertInToEODTransactionWithoutGL(any(StringBuffer.class), anyString(), anyDouble(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), any(), anyInt(),
                anyString());
    }
}