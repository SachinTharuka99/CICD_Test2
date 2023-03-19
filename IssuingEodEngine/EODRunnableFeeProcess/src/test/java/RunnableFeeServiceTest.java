import com.epic.cms.dao.CommonDao;
import com.epic.cms.dao.RunnableFeeDao;
import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.CashAdvanceBean;
import com.epic.cms.model.bean.LastStmtSummeryBean;
import com.epic.cms.service.RunnableFeeService;
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
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RunnableFeeServiceTest {
    final static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    private RunnableFeeService runnableFeeServiceUnderTest;
    static MockedStatic<CommonMethods> common;
    static String EOD_ID = "22110400";

    @BeforeAll
    public static void init() throws Exception {
        common = Mockito.mockStatic(CommonMethods.class);
        Configurations.EOD_DATE = sdf.parse(EOD_ID.substring(0, EOD_ID.length() - 2));
    }

    @AfterAll
    public static void close() {
        common.close();
    }

    @BeforeEach
    void setup() {
        runnableFeeServiceUnderTest = new RunnableFeeService();
        runnableFeeServiceUnderTest.runnableFeeDao = mock(RunnableFeeDao.class);
        runnableFeeServiceUnderTest.commonDao = mock(CommonDao.class);
        runnableFeeServiceUnderTest.status = mock(StatusVarList.class);
    }

    @Test
    @DisplayName("Test Add Annual Fees")
    void testAddAnnualFee() throws Exception {
        // Setup
        java.sql.Date nextAnniversaryDate = new java.sql.Date(Configurations.EOD_DATE.getTime());
        StringBuffer cardNumber = new StringBuffer("4456676666666666");
        Configurations.CARD_CATEGORY_ESTABLISHMENT = "E";
        Configurations.ANNUAL_FEE_FOR_NP_ACCOUNTS = 1;// 0 - Not Apply,1 - Apply
        Configurations.ANNUAL_FEE = "FEC002";// annual fee

        final CardBean cardBean = new CardBean();
        cardBean.setCardnumber(cardNumber);
        cardBean.setMainCardNo(cardNumber);
        cardBean.setCardStatus("CACT");// card active
        cardBean.setAccStatus("ACT");// account active
        cardBean.setNextAnniversaryDate(nextAnniversaryDate);
        cardBean.setCardCategory("M");// card category main

        when(runnableFeeServiceUnderTest.status.getYES_STATUS_1()).thenReturn(1);
        when(runnableFeeServiceUnderTest.status.getNO_STATUS_0()).thenReturn(0);
        when(runnableFeeServiceUnderTest.status.getCARD_REPLACED_STATUS()).thenReturn("CARP");
        when(runnableFeeServiceUnderTest.status.getCARD_PRODUCT_CHANGE_STATUS()).thenReturn("CAPC");
        when(runnableFeeServiceUnderTest.status.getDEACTIVE_STATUS()).thenReturn("DEA");// deactive
        when(runnableFeeServiceUnderTest.status.getACCOUNT_NON_PERFORMING_STATUS()).thenReturn("NP");// NP

        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        when(runnableFeeServiceUnderTest.runnableFeeDao.addCardFeeCount(any(StringBuffer.class), eq("FEC002"), anyDouble())).thenReturn(1);

        //configure cash advances as empty
        List<CashAdvanceBean> cashAdvances = new ArrayList<>();
        when(runnableFeeServiceUnderTest.runnableFeeDao.findCashAdvances(any(StringBuffer.class))).thenReturn(cashAdvances);

        //configure late payments as empty
        when(runnableFeeServiceUnderTest.runnableFeeDao.getLastStatementSummaryInfor(any(StringBuffer.class))).thenReturn(null);

        // Run the test
        runnableFeeServiceUnderTest.addRunnableFees(cardBean);

        // Verify the results
        verify(runnableFeeServiceUnderTest.runnableFeeDao).checkFeeExistForCard(any(StringBuffer.class), anyString());
        assertEquals(1, runnableFeeServiceUnderTest.runnableFeeDao.addCardFeeCount(cardBean.getCardnumber(), "FEC002", 0.0));
        verify(runnableFeeServiceUnderTest.runnableFeeDao).updateNextAnniversaryDate(any(StringBuffer.class));
    }

    @Test
    @DisplayName("Test Add Cash Advance Fee")
    void testAddCashAdvanceFee() throws Exception {
        // Setup
        StringBuffer cardNumber = new StringBuffer("4456676666666666");
        Configurations.CASH_ADVANCE_FEE = "FEC007";// cash advance fee
        Configurations.LATE_PAYMENT_FEE = "FEC004";// late payment fee

        final CardBean cardBean = new CardBean();
        cardBean.setCardnumber(cardNumber);

        // Configure RunnableFeeDao.findCashAdvances(...).
        final CashAdvanceBean cashAdvanceBean = new CashAdvanceBean();
        cashAdvanceBean.setCardNumber(cardNumber);
        cashAdvanceBean.setAccountNo("accountNo");
        cashAdvanceBean.setTotalCashAdvanceAmount(0.0);
        cashAdvanceBean.setTransactionDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        cashAdvanceBean.setTxnid("txnId");

        final List<CashAdvanceBean> cashAdvanceBeans = List.of(cashAdvanceBean);

        when(runnableFeeServiceUnderTest.runnableFeeDao.findCashAdvances(any(StringBuffer.class)))
                .thenReturn(cashAdvanceBeans);

        // Configure RunnableFeeDao.getCardFeeProfileForCard(...).
        final CardFeeBean cardFeeBean = new CardFeeBean();
        cardFeeBean.setCurrCode(144);
        cardFeeBean.setCrOrDr("CR");
        cardFeeBean.setCombination("combination");
        cardFeeBean.setFeeCode("FEC007");
        cardFeeBean.setTxnId("txnId");
        cardFeeBean.setCardNumber(cardNumber);

        when(runnableFeeServiceUnderTest.runnableFeeDao.getCardFeeProfileForCard(any(StringBuffer.class),
                eq("FEC007"))).thenReturn(cardFeeBean);

        common.when(() -> CommonMethods.getAmountFromCombination(anyDouble(), anyDouble(), anyString())).thenReturn(100.0);
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        common.when(() -> CommonMethods.getSqldate(any(Date.class))).thenReturn(new java.sql.Date(Configurations.EOD_DATE.getTime()));
        when(runnableFeeServiceUnderTest.runnableFeeDao.checkDuplicateCashAdvances(any(StringBuffer.class), anyString(), anyString())).thenReturn(false);

        //configure late payments as empty
        when(runnableFeeServiceUnderTest.runnableFeeDao.getLastStatementSummaryInfor(any(StringBuffer.class))).thenReturn(null);

        // Run the test
        runnableFeeServiceUnderTest.addRunnableFees(cardBean);

        // Verify the results
        verify(runnableFeeServiceUnderTest.runnableFeeDao, times(1)).insertToEODcardFee(any(CardFeeBean.class), anyDouble(), any(java.sql.Date.class));

    }

    @Test
    @DisplayName("Test Add Late Payment Fee")
    void testAddLatePaymentFee() throws Exception {
        // Setup
        StringBuffer cardNumber = new StringBuffer("4456676666666666");
        Configurations.LATE_PAYMENT_FEE = "FEC004";// late payment fee

        final CardBean cardBean = new CardBean();
        cardBean.setCardnumber(cardNumber);

        // Configure RunnableFeeDao.getLastStatementSummaryInfor(...).
        final LastStmtSummeryBean lastStmtSummeryBean = new LastStmtSummeryBean();
        lastStmtSummeryBean.setOpaningBalance(0.0);
        lastStmtSummeryBean.setClosingBalance(15000.0);
        lastStmtSummeryBean.setMinAmount(2500.0);
        lastStmtSummeryBean.setDueDate(java.sql.Date.valueOf(LocalDate.of(2022, 10, 15)));
        lastStmtSummeryBean.setStatementStartDate(java.sql.Date.valueOf(LocalDate.of(2022, 9, 1)));
        lastStmtSummeryBean.setStatementEndDate(java.sql.Date.valueOf(LocalDate.of(2022, 10, 1)));
        lastStmtSummeryBean.setClosingLoyaltyPoint(0L);
        lastStmtSummeryBean.setNDIA(0);

        when(runnableFeeServiceUnderTest.runnableFeeDao.getLastStatementSummaryInfor(
                any(StringBuffer.class))).thenReturn(lastStmtSummeryBean);

        when(runnableFeeServiceUnderTest.runnableFeeDao.getNextBillingDateForCard(any(StringBuffer.class))).thenReturn(new java.sql.Date(Configurations.EOD_DATE.getTime()));// equal to eod date

        common.when(() -> CommonMethods.getDate(any(Date.class))).thenReturn("221104");
        when(runnableFeeServiceUnderTest.runnableFeeDao.getAccountNoOnCard(any(StringBuffer.class))).thenReturn("accNo");
        when(runnableFeeServiceUnderTest.runnableFeeDao.getTotalPayment(anyString(), anyInt(), anyInt())).thenReturn(1500.00);
        when(runnableFeeServiceUnderTest.runnableFeeDao.addCardFeeCount(any(StringBuffer.class), eq("FEC004"), eq(0.0))).thenReturn(1);

        //configure cash advances as empty
        List<CashAdvanceBean> cashAdvances = new ArrayList<>();
        when(runnableFeeServiceUnderTest.runnableFeeDao.findCashAdvances(any(StringBuffer.class))).thenReturn(cashAdvances);

        // Run the test
        runnableFeeServiceUnderTest.addRunnableFees(cardBean);

        // Verify the results
        assertEquals(1, runnableFeeServiceUnderTest.runnableFeeDao.addCardFeeCount(cardBean.getCardnumber(), "FEC004", 0.0));

    }

    @ParameterizedTest
    @ValueSource(strings = {"FEC007", "FEC004"})
    @DisplayName("Test Insert To EODCARDFEE")
    void testInsertToEODCARDFee(String feeType) throws Exception {
        // Setup
        final StringBuffer cardNumber = new StringBuffer("4456676666666666");
        final LinkedHashMap details = new LinkedHashMap<>(Map.ofEntries());
        final String txnId = "txnId";
        final String accountNo = "accountNo";
        final double cashAmount = 5000.0;
        Configurations.CASH_ADVANCE_FEE = "FEC007";// cash advance fee
        Configurations.LATE_PAYMENT_FEE = "FEC004";// late payment fee


        // Configure RunnableFeeDao.getCardFeeProfileForCard(...).
        final CardFeeBean cardFeeBean = new CardFeeBean();
        cardFeeBean.setCurrCode(144);
        cardFeeBean.setCrOrDr("CR");// CR or DR
        cardFeeBean.setFlatFee(100.0);
        cardFeeBean.setMinAmount(0.0);
        cardFeeBean.setMaxAmount(10000.0);
        cardFeeBean.setPercentageAmount(5.0);
        cardFeeBean.setCombination("MAX");// MIN,MAX,CMB
        cardFeeBean.setFeeCode(feeType);
        cardFeeBean.setCardNumber(cardNumber);

        when(runnableFeeServiceUnderTest.runnableFeeDao.getCardFeeProfileForCard(any(StringBuffer.class),
                eq(feeType))).thenReturn(cardFeeBean);

        common.when(() -> CommonMethods.getAmountFromCombination(anyDouble(), anyDouble(), anyString())).thenReturn(250.0);//250.0,100.0,MAX
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("445667******6666");
        common.when(() -> CommonMethods.getSqldate(any(Date.class))).thenReturn(new java.sql.Date(Configurations.EOD_DATE.getTime()));
        when(runnableFeeServiceUnderTest.runnableFeeDao.getNextBillingDateForCard(any(StringBuffer.class))).thenReturn(new java.sql.Date(Configurations.EOD_DATE.getTime()));

        // Run the test
        runnableFeeServiceUnderTest.insertToEODCARDFee(cardNumber, txnId, accountNo, feeType, cashAmount, details);

        // Verify the results
        verify(runnableFeeServiceUnderTest.runnableFeeDao, times(1)).insertToEODcardFee(any(CardFeeBean.class), anyDouble(), any(java.sql.Date.class));
        if (feeType.equals(Configurations.LATE_PAYMENT_FEE)) {
            verify(runnableFeeServiceUnderTest.runnableFeeDao, times(1)).updateCardFeeCount(any(CardFeeBean.class));
        } else if (feeType.equals(Configurations.CASH_ADVANCE_FEE)) {
            verify(runnableFeeServiceUnderTest.runnableFeeDao).checkDuplicateCashAdvances(any(StringBuffer.class), anyString(), anyString());
        }

    }

}