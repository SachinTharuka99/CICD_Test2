import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.InstallmentBean;
import com.epic.cms.model.bean.ManualNpRequestBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.InstallmentPaymentRepo;
import com.epic.cms.service.LoanOnCardService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoanOnCardServiceTest {
    final static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    final static String condition1 = "no_payment_on_current_day";
    final static String condition2 = "payment_on_current_day_less_than_min_payment";
    private LoanOnCardService loanOnCardServiceUnderTest;
    static MockedStatic<CommonMethods> common;
    static String EOD_ID = "22110400";
    public AtomicInteger faileCardCount = new AtomicInteger(0);

    @BeforeAll
    public static void init() throws Exception {
        common = Mockito.mockStatic(CommonMethods.class);
        Configurations.EOD_ID = Integer.parseInt(EOD_ID);
        Configurations.EOD_DATE = sdf.parse(EOD_ID.substring(0, EOD_ID.length() - 2));
    }

    @AfterAll
    public static void close() {
        common.close();
    }

    @BeforeEach
    void setUp() {
        loanOnCardServiceUnderTest = new LoanOnCardService();
        loanOnCardServiceUnderTest.installmentPaymentRepo = mock(InstallmentPaymentRepo.class);
        loanOnCardServiceUnderTest.commonRepo = mock(CommonRepo.class);
        loanOnCardServiceUnderTest.status = mock(StatusVarList.class);
        loanOnCardServiceUnderTest.logManager = mock(LogManager.class);

    }

    @Test
    @DisplayName("Test Accelerate LOC Request For Manual NP Account")
    void testAccelerateLOCRequestForManualNPAccount() throws Exception {
        // Setup
        when(loanOnCardServiceUnderTest.status.getYES_STATUS_1()).thenReturn(1);
        when(loanOnCardServiceUnderTest.status.getCOMMON_REQUEST_ACCEPTED()).thenReturn("RQAC");

        // Configure InstallmentPaymentRepo.getManualNpRequestDetails(...).
        final ManualNpRequestBean manualNpRequestBean = new ManualNpRequestBean();
        manualNpRequestBean.setAccNumber("accNumber");
        manualNpRequestBean.setAccStatus("accStatus");
        manualNpRequestBean.setCardNumber(new StringBuffer("value"));
        manualNpRequestBean.setRequestId(0);
        manualNpRequestBean.setNdia(0);

        final List<ManualNpRequestBean> manualNpList = new ArrayList<>();// manual NP list
        manualNpList.add(manualNpRequestBean);

        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getManualNpRequestDetails(eq(1),
                eq("RQAC"))).thenReturn(manualNpList);
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(anyString(), eq("LOANONCARDREQUEST"))).thenReturn(1);

        //set delenquent account list to 0
        final List<DelinquentAccountBean> delinquentAccountBeanList = new ArrayList<>();
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getDelinquentAccounts())
                .thenReturn(delinquentAccountBeanList);

        // Run the test
        loanOnCardServiceUnderTest.accelerateLOCRequestForNpAccount();

        // Verify the results
        verify(loanOnCardServiceUnderTest.installmentPaymentRepo, times(1)).updateEasyPaymentRequestToAccelerate(anyString(),
                eq("LOANONCARDREQUEST"));
    }

    @ParameterizedTest
    @ValueSource(strings = {condition1, condition2})
    @DisplayName("Test Accelerate LOC Request For Automatic NP Account")
    void testAccelerateLOCRequestForAutoNpAccount(String condition) throws Exception {
        // Setup
        String npRiskClass = "9";// bucket 9
        when(loanOnCardServiceUnderTest.status.getYES_STATUS_1()).thenReturn(1);
        when(loanOnCardServiceUnderTest.status.getCOMMON_REQUEST_ACCEPTED()).thenReturn("RQAC");
        double payment = 0.00;

        if (condition.equals(condition1)) {
            //do nothing
        } else if (condition.equals(condition2)) {
            payment = 1500.0;
        }

        //set manual NP request list to 0
        final List<ManualNpRequestBean> manualNpList = new ArrayList<>();// manual NP list
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getManualNpRequestDetails(eq(1),
                eq("RQAC"))).thenReturn(manualNpList);

        // Configure InstallmentPaymentRepo.getDelinquentAccounts(...).
        final DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
        delinquentAccountBean.setAccNo("accNumber");
        delinquentAccountBean.setAccStatus("accStatus");
        delinquentAccountBean.setCif("cif");
        delinquentAccountBean.setNameOnCard("nameOnCard");
        delinquentAccountBean.setNameInFull("nameInFull");
        delinquentAccountBean.setIdType("idType");
        delinquentAccountBean.setIdNumber("idNumber");
        delinquentAccountBean.setCardCategory("cardCategory");
        delinquentAccountBean.setNDIA(89);
        delinquentAccountBean.setMIA(0);
        delinquentAccountBean.setLastStatementDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        delinquentAccountBean.setContactNo("contactNo");
        delinquentAccountBean.setEmail("email");
        delinquentAccountBean.setAddress("address");
        delinquentAccountBean.setRiskClass("riskClass");
        final List<DelinquentAccountBean> delinquentAccountBeanList = List.of(delinquentAccountBean);//delinquent account list

        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getDelinquentAccounts())
                .thenReturn(delinquentAccountBeanList);

        when(loanOnCardServiceUnderTest.installmentPaymentRepo.checkForPayment(anyString(), any(Date.class))).thenReturn(payment);
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getRiskClassOnNdia(anyInt()))// NDIA
                .thenReturn(new String[]{"90", npRiskClass, "120"});//MIN NDIA, risk class, MIN NDIA
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getNPRiskClass()).thenReturn(npRiskClass);// NP risk class from common config
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.getNDIAOnRiskClass(npRiskClass))// NDIA of NP risk class
                .thenReturn(new String[]{npRiskClass, "90", "120"});//risk class, MIN NDIA, MAX NDIA

        when(loanOnCardServiceUnderTest.installmentPaymentRepo.checkLeastMinimumPayment(anyString())).thenReturn(1750.0);// set min payment to LKR 1750.0

        when(loanOnCardServiceUnderTest.installmentPaymentRepo.updateEasyPaymentRequestToAccelerate(anyString(), eq("LOANONCARDREQUEST"))).thenReturn(1);

        // Run the test
        loanOnCardServiceUnderTest.accelerateLOCRequestForNpAccount();

        // Verify the results
        verify(loanOnCardServiceUnderTest.installmentPaymentRepo, times(1)).updateEasyPaymentRequestToAccelerate(anyString(),
                eq("LOANONCARDREQUEST"));

    }

    @Test
    @DisplayName("Test Start LOC Process For First Installment Cycle")
    void testStartLOCProcess_firstInstallmentCycle() throws Exception {
        // Setup
        Configurations.TXN_TYPE_SALE = "TTC001";
        Configurations.TXN_TYPE_INSTALLMENT = "TTC025";// easy payment
        Configurations.TXN_TYPE_LOAN_ON_CARD = "TTC034";//loan on card
        Configurations.TXN_TYPE_FEE_INSTALLMENT = "TTC036";// fee installment
        Configurations.TXN_TYPE_REVERSAL_INSTALLMENT = "TTC039";// reversal installment
        Configurations.TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE = "UNERICUF";
        Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE = "FIUF";
        Configurations.TXN_TYPE_UNEARNED_INCOME = "UNERIC";
        Configurations.DEBIT = "DR";
        Configurations.CREDIT = "CR";

        StringBuffer cardNumber = new StringBuffer("4566789999999999");
        final InstallmentBean installmentBean = new InstallmentBean();
        installmentBean.setCurrentCount(0);
        installmentBean.setCardNumber(cardNumber);
        installmentBean.setTxnID("txnID");
        installmentBean.setTxnAmount("10000.0");
        installmentBean.setTotalFEeAmount("0.0");
        installmentBean.setStatus("RQAC");
        installmentBean.setInstalmentAmount("0.0");
        installmentBean.setCurruncyCode("144");
        installmentBean.setInterestRate("0.0");
        installmentBean.setAccNo("accNo");
        installmentBean.setRemainingCount(10);
        installmentBean.setRunningStatus(0);// first cycle
        installmentBean.setTxnDescription("Loan On Card");
        installmentBean.setDuration(12);
        installmentBean.setFeeApplyFirstMonth("NO");// fee apply in first month
        installmentBean.setFeeType("FEE");// FEE,INT
        installmentBean.setAccelarateStatus("NO");// accelaratate status
        installmentBean.setTraceNumber("traceNumber");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(1);

        when(loanOnCardServiceUnderTest.commonRepo.getCardAssociationFromCardBin(anyString())).thenReturn("1");// VISA
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456678******9999");
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionOnlyVisaFalse(
                any(StringBuffer.class), anyString(), anyDouble(), anyString(), eq("TEST"), eq("TEST"),
                eq("TTC001"), anyString(), anyString(), eq("DR"), eq(1),
                anyString())).thenReturn(1);
        when(loanOnCardServiceUnderTest.commonRepo.insertInToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), eq("TEST"), eq("TEST"), eq("TTC039"), anyString(),
                eq("Loan On Card Transaction-Reversal"), eq("CR"), eq(null),
                anyString())).thenReturn(1);
        when(loanOnCardServiceUnderTest.commonRepo.insertIntoEodGLAccount(anyInt(),
                any(Date.class), any(StringBuffer.class),
                anyString(), anyDouble(), eq("CR"), eq(null))).thenReturn(1);
        when(loanOnCardServiceUnderTest.commonRepo.insertInToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), eq("TEST"), eq("TEST"), eq("TTC025"), anyString(),
                anyString(), eq("DR"), eq(null),
                anyString())).thenReturn(1);
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionWithoutGL(
                any(StringBuffer.class), anyString(), anyDouble(), anyString(), eq("TEST"), eq("TEST"),
                eq("TTC036"), anyString(), anyString(), eq("DR"), eq(1),
                anyString())).thenReturn(1);
        when(loanOnCardServiceUnderTest.commonRepo.insertIntoEodGLAccount(anyInt(),
                any(Date.class), any(StringBuffer.class),
                anyString(), anyDouble(), eq("DR"), eq(null))).thenReturn(1);
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(any(InstallmentBean.class), eq("LOANONCARDREQUEST"))).thenReturn(1);
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.updateFeeToEDONInTransactionTable(any(StringBuffer.class), anyString(), eq("TTC034"))).thenReturn(1);

        // Run the test
        loanOnCardServiceUnderTest.startLOCProcess(installmentBean, processBean, faileCardCount);

        // Verify the results
        assertEquals(1, loanOnCardServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionOnlyVisaFalse(installmentBean.getCardNumber(), installmentBean.getAccNo(), Double.parseDouble(installmentBean.getTxnAmount()), installmentBean.getCurruncyCode(), "TEST", "TEST",
                "TTC001", installmentBean.getTxnID(), "Loan On Card Transaction", "DR", 1,
                "1"));
        assertEquals(1, loanOnCardServiceUnderTest.commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(),
                installmentBean.getTxnAmount(), installmentBean.getCurruncyCode(), "TEST", "TEST", "TTC039", installmentBean.getTxnID(),
                "Loan On Card Transaction-Reversal", "CR", null, "1"));
        assertEquals(1, loanOnCardServiceUnderTest.commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID,
                Configurations.EOD_DATE, installmentBean.getCardNumber(),
                "gtType", Double.parseDouble(installmentBean.getTotalFEeAmount()), "CR", null));
        assertEquals(1, loanOnCardServiceUnderTest.commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(),
                installmentBean.getTxnAmount(), installmentBean.getCurruncyCode(), "TEST", "TEST", "TTC025", installmentBean.getTxnID(),
                "txnDes", "DR", null, "1"));
        assertEquals(1, loanOnCardServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), 0.0, installmentBean.getCurruncyCode(), "TEST", "TEST",
                "TTC036", installmentBean.getTxnID(), "txnDes", "DR", 1,
                "1"));
        assertEquals(1, loanOnCardServiceUnderTest.commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID,
                Configurations.EOD_DATE, installmentBean.getCardNumber(),
                "gtType", Double.parseDouble(installmentBean.getTotalFEeAmount()), "DR", null));
        assertEquals(1, loanOnCardServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTableWithFirstInstallment(installmentBean, "LOANONCARDREQUEST"));
        assertEquals(1, loanOnCardServiceUnderTest.installmentPaymentRepo.updateFeeToEDONInTransactionTable(installmentBean.getCardNumber(), installmentBean.getTraceNumber(), "TTC034"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"YES", "NO"})
    @DisplayName("Test Start LOC Process For Already Running Plan")
    void testStartLOCProcess_alreadyRunningPlan(String feeApplyOnFirstMonth) throws Exception {
        // Setup
        Configurations.TXN_TYPE_SALE = "TTC001";
        Configurations.TXN_TYPE_INSTALLMENT = "TTC025";// easy payment
        Configurations.TXN_TYPE_LOAN_ON_CARD = "TTC034";//loan on card
        Configurations.TXN_TYPE_FEE_INSTALLMENT = "TTC036";// fee installment
        Configurations.TXN_TYPE_REVERSAL_INSTALLMENT = "TTC039";// reversal installment
        Configurations.TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE = "UNERICUF";
        Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE = "FIUF";
        Configurations.TXN_TYPE_UNEARNED_INCOME = "UNERIC";
        Configurations.DEBIT = "DR";
        Configurations.CREDIT = "CR";

        StringBuffer cardNumber = new StringBuffer("4566789999999999");
        final InstallmentBean installmentBean = new InstallmentBean();
        installmentBean.setCurrentCount(0);
        installmentBean.setCardNumber(cardNumber);
        installmentBean.setTxnID("txnID");
        installmentBean.setTxnAmount("10000.0");
        installmentBean.setTotalFEeAmount("0.0");
        installmentBean.setStatus("RQAC");
        installmentBean.setInstalmentAmount("0.0");
        installmentBean.setCurruncyCode("144");
        installmentBean.setInterestRate("0.0");
        installmentBean.setAccNo("accNo");
        installmentBean.setRemainingCount(10);
        installmentBean.setRunningStatus(1);// already running
        installmentBean.setTxnDescription("Loan On Card");
        installmentBean.setDuration(12);
        installmentBean.setFeeApplyFirstMonth(feeApplyOnFirstMonth);// fee apply in first month
        installmentBean.setFeeType("FEE");// FEE,INT
        installmentBean.setAccelarateStatus("NO");// accelaratate status
        installmentBean.setTraceNumber("traceNumber");

        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(1);

        when(loanOnCardServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionWithoutGL(
                any(StringBuffer.class), anyString(), anyDouble(), anyString(), eq("TEST"), eq("TEST"),
                eq("TTC036"), anyString(), anyString(), eq("DR"), eq(1),
                anyString())).thenReturn(1);
        when(loanOnCardServiceUnderTest.commonRepo.insertIntoEodGLAccount(anyInt(),
                any(Date.class), any(StringBuffer.class),
                anyString(), anyDouble(), eq("DR"), eq(null))).thenReturn(1);
        when(loanOnCardServiceUnderTest.commonRepo.insertInToEODTransaction(any(StringBuffer.class), anyString(),
                anyString(), anyString(), eq("TEST"), eq("TEST"), eq("TTC025"), anyString(),
                anyString(), eq("DR"), eq(null),
                anyString())).thenReturn(1);
        when(loanOnCardServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTable(any(InstallmentBean.class), eq("LOANONCARDREQUEST"))).thenReturn(1);

        // Run the test
        loanOnCardServiceUnderTest.startLOCProcess(installmentBean, processBean,faileCardCount);

        // Verify the result
        if (feeApplyOnFirstMonth.equals("NO")) {
            assertEquals(1, loanOnCardServiceUnderTest.installmentPaymentRepo.insertInToEODTransactionWithoutGL(installmentBean.getCardNumber(), installmentBean.getAccNo(), 0.0, installmentBean.getCurruncyCode(), "TEST", "TEST",
                    "TTC036", installmentBean.getTxnID(), "txnDes", "DR", 1,
                    "1"));
        }
        assertEquals(1, loanOnCardServiceUnderTest.commonRepo.insertIntoEodGLAccount(Configurations.EOD_ID,
                Configurations.EOD_DATE, installmentBean.getCardNumber(),
                "gtType", Double.parseDouble(installmentBean.getTotalFEeAmount()), "DR", null));
        assertEquals(1, loanOnCardServiceUnderTest.commonRepo.insertInToEODTransaction(installmentBean.getCardNumber(), installmentBean.getAccNo(),
                installmentBean.getTxnAmount(), installmentBean.getCurruncyCode(), "TEST", "TEST", "TTC025", installmentBean.getTxnID(),
                "txnDes", "DR", null, "1"));
        assertEquals(1, loanOnCardServiceUnderTest.installmentPaymentRepo.updateEasyPaymentTable(installmentBean, "LOANONCARDREQUEST"));
    }

}