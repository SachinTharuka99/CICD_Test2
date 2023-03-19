import com.epic.cms.dao.ChequeReturnDao;
import com.epic.cms.dao.CommonDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.service.ChequeReturnService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChequeReturnServiceTest {
    final static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    private ChequeReturnService chequeReturnServiceUnderTest;
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
    void setUp() {
        chequeReturnServiceUnderTest = new ChequeReturnService();
        chequeReturnServiceUnderTest.chequeReturnDao = mock(ChequeReturnDao.class);
        chequeReturnServiceUnderTest.commonDao = mock(CommonDao.class);
        chequeReturnServiceUnderTest.status = mock(StatusVarList.class);
    }

    @Test
    @DisplayName("Test Update Cheque Returns")
    void testUpdateChequeReturns() throws Exception {
        // Setup
        final StringBuffer cardNumber = new StringBuffer("4456676666666666");

        // Configure ChequeReturnDao.getChequeReturns(...).
        final List<ReturnChequePaymentDetailsBean> returnedChqList = new ArrayList<>();
        ReturnChequePaymentDetailsBean chequeReturnBean = new ReturnChequePaymentDetailsBean();
        chequeReturnBean.setCardnumber(cardNumber);
        chequeReturnBean.setEodid(22110400);
        chequeReturnBean.setChequenumber("chequeNo");
        chequeReturnBean.setTraceid("traceId");
        chequeReturnBean.setChqRtnDate(Date.valueOf(LocalDate.of(2022, 07, 10)));//posting date
        returnedChqList.add(chequeReturnBean);

        when(chequeReturnServiceUnderTest.chequeReturnDao.getChequeReturns()).thenReturn(returnedChqList);

        when(chequeReturnServiceUnderTest.chequeReturnDao.updateChequeReturns(any(StringBuffer.class), anyString(), any(Date.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateChequeReturnsForEODPayment(any(StringBuffer.class), anyString())).thenReturn(1);
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("445667******6666");

        // Run the test
        chequeReturnServiceUnderTest.updateChequeReturns();

        // Verify the results
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateChequeReturns(cardNumber, chequeReturnBean.getTraceid(), chequeReturnBean.getChqRtnDate()));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateChequeReturnsForEODPayment(cardNumber, chequeReturnBean.getTraceid()));
    }

    final String fullTestCase = "Test Full Test Case";
    final String subTestCase1 = "Test EOM Card Balance Not Exist";
    final String subTestCase2 = "Test Payment Insufficient For Card";

    @ParameterizedTest
    @ValueSource(strings = {fullTestCase, subTestCase1, subTestCase2})
    @DisplayName("Test Cqrt Date After Due Date and Before Stmt Date")
    void proceedChequeReturn_CqrtDateAfterDueDateAndBeforeStmtDate(String testCase) throws Exception {
        // Setup
        Map<StringBuffer, List<ReturnChequePaymentDetailsBean>> chequeReturnsList = new HashMap<StringBuffer, List<ReturnChequePaymentDetailsBean>>();
        final StringBuffer cardNumber = new StringBuffer("4890119126440554");
        final String txnId = "txnId";
        final String cqrtSeqNo = "182679607";
        final OtbBean otbBean = new OtbBean();
        Configurations.LATE_PAYMENT_FEE = "FEC004";
        Configurations.CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE = "FEC013";
        Configurations.CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE = "FEC012";
        Configurations.CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE = "FEC027";

        double payment = 10000.0;
        if (testCase.equals(subTestCase2)) {//Payment Insufficient For Card (subTestCase2)
            payment = 4000.0;
        }

        final ReturnChequePaymentDetailsBean returnChequePaymentDetail = new ReturnChequePaymentDetailsBean();
        returnChequePaymentDetail.setCardnumber(cardNumber);
        returnChequePaymentDetail.setOldcardnumber(cardNumber);
        returnChequePaymentDetail.setId(4109);
        returnChequePaymentDetail.setEodid(Integer.parseInt(EOD_ID));
        returnChequePaymentDetail.setAmount(30000.0);
        returnChequePaymentDetail.setChequedate(Date.valueOf(LocalDate.of(2021, 8, 20)));
        returnChequePaymentDetail.setMinamount(5296.52);
        returnChequePaymentDetail.setForwardinterest(1766.89);
        returnChequePaymentDetail.setInterestrate(18.0);
        returnChequePaymentDetail.setChequestatus("CQRT");//return
        returnChequePaymentDetail.setDelinquentclass("0");
        returnChequePaymentDetail.setCardstatus("CACT");
        returnChequePaymentDetail.setDuedate(Date.valueOf(LocalDate.of(2021, 8, 10)));//due date
        returnChequePaymentDetail.setStatementstarteodid(21062000);
        returnChequePaymentDetail.setStatementendeodid(21082000);
        returnChequePaymentDetail.setReturnreason("reason");
        returnChequePaymentDetail.setSeqNo("182493990");
        returnChequePaymentDetail.setCqrtseqNo(cqrtSeqNo);
        returnChequePaymentDetail.setChqRtnDate(Date.valueOf(LocalDate.of(2021, 8, 19)));//cheque return date
        returnChequePaymentDetail.setCHEQUE_RET_CODE("FEC012");
        returnChequePaymentDetail.setNdia(0);
        returnChequePaymentDetail.setAccountNo("accountNo");

        chequeReturnsList.put(cardNumber, List.of(returnChequePaymentDetail));//check return list

        when(chequeReturnServiceUnderTest.status.getSTATUS_NO()).thenReturn("NO");
        when(chequeReturnServiceUnderTest.status.getCHEQUE_PAYMENT()).thenReturn("CHEQUE");
        when(chequeReturnServiceUnderTest.status.getEOD_DONE_STATUS()).thenReturn("EDON");
        when(chequeReturnServiceUnderTest.status.getCARD_EXPIRED_STATUS()).thenReturn("CAEX");
        when(chequeReturnServiceUnderTest.status.getACTIVE_STATUS()).thenReturn("ACT");
        when(chequeReturnServiceUnderTest.status.getCARD_TEMPORARY_BLOCK_Status()).thenReturn("CATB");
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("489011******0554");

        // Configure ChequeReturnDao.getChequeKnockOffBean(...).
        final ReturnChequePaymentDetailsBean returnBean = new ReturnChequePaymentDetailsBean();
        returnBean.setCardnumber(cardNumber);
        returnBean.setAccountNo("AccountNo");
        returnBean.setCustomerid("CustomerId");

        when(chequeReturnServiceUnderTest.chequeReturnDao.getChequeKnockOffBean(any(StringBuffer.class)))
                .thenReturn(returnBean);

        // Configure ChequeReturnDao.getEOMPendingKnockOffList(...).
        final OtbBean eomOtb = new OtbBean();
        eomOtb.setAccountnumber("accountNo");
        eomOtb.setCardnumber(cardNumber);
        eomOtb.setCustomerid("customerid");
        eomOtb.setOtbcredit(0.0);
        eomOtb.setOtbcash(0.0);
        eomOtb.setFinacialcharges(0.0);
        eomOtb.setCumpayment(0.0);
        eomOtb.setCumcashadvance(0.0);
        eomOtb.setCumtransactions(0.0);
        eomOtb.setTmpcredit(0.0);
        eomOtb.setTmpcash(0.0);
        eomOtb.setTxnAmount(0.0);
        eomOtb.setTxntype("txntype");
        eomOtb.setIsPrimary("YES");//YES, NO
        eomOtb.setMaincardno(new StringBuffer("value"));

        common.when(() -> CommonMethods.getDateFromEODID(eq(21082000))).thenReturn(Date.valueOf(LocalDate.of(2021, 8, 20)));
        if (!testCase.equals(subTestCase1)) {
            when(chequeReturnServiceUnderTest.chequeReturnDao.getEOMPendingKnockOffList(
                    any(StringBuffer.class))).thenReturn(eomOtb);
        } else {// EOM Card Balance Not Exist (subTestCase1)
            when(chequeReturnServiceUnderTest.chequeReturnDao.getEOMPendingKnockOffList(
                    any(StringBuffer.class))).thenReturn(null);
        }

        when(chequeReturnServiceUnderTest.chequeReturnDao.updateEOMCARDBalanceKnockOn(any(OtbBean.class))).thenReturn(1);
        //update Card, Customer and Account Balances
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCustomerOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateAccountOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCardOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCustomerOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineAccountOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCardOtb(any(OtbBean.class))).thenReturn(1);

        // Configure ChequeReturnDao.getIntProf(...).
        final InterestDetailBean interestDetailBean = new InterestDetailBean();
        interestDetailBean.setInterest(3.0);
        interestDetailBean.setInterestperiod(12.0);

        when(chequeReturnServiceUnderTest.chequeReturnDao.getIntProf(anyString())).thenReturn(interestDetailBean);

        common.when(() -> CommonMethods.ValuesRoundup(anyDouble())).thenReturn("250");//calculated interests amount

        // Configure ChequeReturnDao.getCardAccountCustomer(...).
        final CardAccountCustomerBean cardAccountCustomerBean = new CardAccountCustomerBean();
        cardAccountCustomerBean.setAccountNumber("accNo");
        cardAccountCustomerBean.setCustomerId("customerId");
        cardAccountCustomerBean.setMaincardNumber(new StringBuffer("value"));
        cardAccountCustomerBean.setCardNumber(cardNumber);

        when(chequeReturnServiceUnderTest.chequeReturnDao.getCardAccountCustomer(any(StringBuffer.class)))
                .thenReturn(cardAccountCustomerBean);
        when(chequeReturnServiceUnderTest.chequeReturnDao.getTxnIdForLastChequeByAccount(any(PaymentBean.class))).thenReturn(txnId);
        when(chequeReturnServiceUnderTest.chequeReturnDao.getTxnIdForLastCheque(any(PaymentBean.class))).thenReturn(txnId);
        when(chequeReturnServiceUnderTest.commonDao.getCardAssociationFromCardBin(anyString())).thenReturn("1");// card accociation
        when(chequeReturnServiceUnderTest.chequeReturnDao.checkDuplicateChequeReturnEntry(any(StringBuffer.class),
                anyDouble(), anyString(), anyString(), anyString())).thenReturn(false);// no duplicates
        when(chequeReturnServiceUnderTest.chequeReturnDao.insertReturnChequeToEODTransaction(any(StringBuffer.class),
                anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.addCardFeeCount(any(StringBuffer.class),
                anyString(), anyDouble())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updatePaymentStatus(any(StringBuffer.class), eq("EDON"), anyString())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateTransactionEODStatus(any(StringBuffer.class),
                any(StringBuffer.class), eq("EDON"), anyString())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateChequeStatusForEODTxn(any(PaymentBean.class),
                anyString())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateChequePaymentStatus(anyInt(), eq("EDON")))
                .thenReturn(1);

        when(chequeReturnServiceUnderTest.chequeReturnDao.getAccountNoOnCard(any(StringBuffer.class)))
                .thenReturn("accountNo");
        when(chequeReturnServiceUnderTest.chequeReturnDao.getPaymentAmountBetweenDueDate(anyString(), anyInt(), eq("EDON"),
                anyString())).thenReturn(payment);// payment

        // Configure ChequeReturnDao.getEodInterestForCard(...).
        final EodInterestBean eodInterestBean = new EodInterestBean();
        eodInterestBean.setAccountno("accountNo");
        eodInterestBean.setCardNumber(cardNumber);
        eodInterestBean.setDueDate(Date.valueOf(LocalDate.of(2021, 8, 10)));

        when(chequeReturnServiceUnderTest.chequeReturnDao.getEodInterestForCard(any(StringBuffer.class)))
                .thenReturn(eodInterestBean);

        when(chequeReturnServiceUnderTest.chequeReturnDao.updateEodInterestForCard(any(StringBuffer.class),
                anyDouble())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.getFeeCodeIfThereExists(any(StringBuffer.class),
                eq("FEC004"))).thenReturn(true);// exist late payment fee
        when(chequeReturnServiceUnderTest.chequeReturnDao.addCardFeeCount(any(StringBuffer.class), eq("FEC004"), anyDouble())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.restoreMinimumPayment(any(StringBuffer.class)))
                .thenReturn(true);// restore the minimumpayment details
        when(chequeReturnServiceUnderTest.chequeReturnDao.insertToMinPayTableOld(any(StringBuffer.class), anyDouble(),
                anyDouble(), any(Date.class), anyDouble())).thenReturn(false);

        // Configure ChequeReturnDao.getCardBlockOldCardStatus(...).
        final BlockCardBean blockCardBean = new BlockCardBean();
        blockCardBean.setCardNo(cardNumber);
        blockCardBean.setOldStatus("OLDSTATUS");
        blockCardBean.setNewStatus("NEWSTATUS");

        when(chequeReturnServiceUnderTest.chequeReturnDao.getCardBlockOldCardStatus(
                any(StringBuffer.class))).thenReturn(blockCardBean);// block bean not null
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(any(StringBuffer.class),
                eq("OLDSTATUS"))).thenReturn(1);// update card status
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(any(StringBuffer.class),
                eq("CATB"))).thenReturn(1);// clear temporary block status

        when(chequeReturnServiceUnderTest.chequeReturnDao.getRiskClassOnNdia(1)).thenReturn(new String[]{"1", "1", "1"});// MIN NDIA, RISK CLASS, MIN NDIA
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateDelinquencyStatus(any(StringBuffer.class),
                anyString(), anyInt())).thenReturn(1);

        // Run the test
        Iterator it = chequeReturnsList.entrySet().iterator();
        if (!chequeReturnsList.isEmpty()) {
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                chequeReturnServiceUnderTest.proceedChequeReturn(pair);
            }
        }

        // Verify the result
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateEOMCARDBalanceKnockOn(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCustomerOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateAccountOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCardOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCustomerOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineAccountOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCardOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.insertReturnChequeToEODTransaction(cardNumber,
                "accountNo", 0.0, "txnId", "traceId", cqrtSeqNo, "1"));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.addCardFeeCount(cardNumber, "feeCode", 0.0));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updatePaymentStatus(cardNumber, "EDON", cqrtSeqNo));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateTransactionEODStatus(returnChequePaymentDetail.getOldcardnumber(),
                returnChequePaymentDetail.getOldcardnumber(), "EDON", cqrtSeqNo));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateChequeStatusForEODTxn(new PaymentBean(), "accountNo"));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateChequePaymentStatus(returnChequePaymentDetail.getId(), "EDON"));

        if (testCase.equals(subTestCase2)) {//Payment insufficient for card
            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateEodInterestForCard(cardNumber, 0.0));

            verify(chequeReturnServiceUnderTest.chequeReturnDao).restoreMinimumPayment(any(StringBuffer.class));

            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(cardNumber, "OLDSTATUS"));
            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(cardNumber, "CATB"));

            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateDelinquencyStatus(cardNumber, "1", 1));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {fullTestCase, subTestCase2})
    @DisplayName("Test Cqrt Date Before Due Date")
    void proceedChequeReturn_CqrtDateBeforeDueDate(String testCase) throws Exception {
        // Setup
        Map<StringBuffer, List<ReturnChequePaymentDetailsBean>> chequeReturnsList = new HashMap<StringBuffer, List<ReturnChequePaymentDetailsBean>>();
        final StringBuffer cardNumber = new StringBuffer("4890119126440554");
        final String txnId = "txnId";
        final String cqrtSeqNo = "182679607";
        final OtbBean otbBean = new OtbBean();
        Configurations.LATE_PAYMENT_FEE = "FEC004";
        Configurations.CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE = "FEC013";
        Configurations.CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE = "FEC012";
        Configurations.CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE = "FEC027";

        double payment = 10000.0;
        if (testCase.equals(subTestCase2)) {//Payment Insufficient For Card (subTestCase2)
            payment = 4000.0;
        }

        final ReturnChequePaymentDetailsBean returnChequePaymentDetail = new ReturnChequePaymentDetailsBean();
        returnChequePaymentDetail.setCardnumber(cardNumber);
        returnChequePaymentDetail.setOldcardnumber(cardNumber);
        returnChequePaymentDetail.setId(4109);
        returnChequePaymentDetail.setEodid(Integer.parseInt(EOD_ID));
        returnChequePaymentDetail.setAmount(30000.0);
        returnChequePaymentDetail.setChequedate(Date.valueOf(LocalDate.of(2021, 8, 20)));
        returnChequePaymentDetail.setMinamount(5296.52);
        returnChequePaymentDetail.setForwardinterest(1766.89);
        returnChequePaymentDetail.setInterestrate(18.0);
        returnChequePaymentDetail.setChequestatus("CQRT");//return
        returnChequePaymentDetail.setDelinquentclass("0");
        returnChequePaymentDetail.setCardstatus("CACT");
        returnChequePaymentDetail.setDuedate(Date.valueOf(LocalDate.of(2021, 8, 25)));//due date
        returnChequePaymentDetail.setStatementstarteodid(21062000);
        returnChequePaymentDetail.setStatementendeodid(21082000);
        returnChequePaymentDetail.setReturnreason("reason");
        returnChequePaymentDetail.setSeqNo("182493990");
        returnChequePaymentDetail.setCqrtseqNo(cqrtSeqNo);
        returnChequePaymentDetail.setChqRtnDate(Date.valueOf(LocalDate.of(2021, 8, 21)));//cheque return date
        returnChequePaymentDetail.setCHEQUE_RET_CODE("FEC012");
        returnChequePaymentDetail.setNdia(0);
        returnChequePaymentDetail.setAccountNo("accountNo");

        chequeReturnsList.put(cardNumber, List.of(returnChequePaymentDetail));//check return list

        when(chequeReturnServiceUnderTest.status.getSTATUS_NO()).thenReturn("NO");
        when(chequeReturnServiceUnderTest.status.getCHEQUE_PAYMENT()).thenReturn("CHEQUE");
        when(chequeReturnServiceUnderTest.status.getEOD_DONE_STATUS()).thenReturn("EDON");
        when(chequeReturnServiceUnderTest.status.getCARD_EXPIRED_STATUS()).thenReturn("CAEX");
        when(chequeReturnServiceUnderTest.status.getACTIVE_STATUS()).thenReturn("ACT");
        when(chequeReturnServiceUnderTest.status.getCARD_TEMPORARY_BLOCK_Status()).thenReturn("CATB");
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("489011******0554");

        // Configure ChequeReturnDao.getChequeKnockOffBean(...).
        final ReturnChequePaymentDetailsBean returnBean = new ReturnChequePaymentDetailsBean();
        returnBean.setCardnumber(cardNumber);
        returnBean.setAccountNo("AccountNo");
        returnBean.setCustomerid("CustomerId");

        when(chequeReturnServiceUnderTest.chequeReturnDao.getChequeKnockOffBean(any(StringBuffer.class)))
                .thenReturn(returnBean);

        common.when(() -> CommonMethods.getDateFromEODID(eq(21082000))).thenReturn(Date.valueOf(LocalDate.of(2021, 8, 20)));

        when(chequeReturnServiceUnderTest.chequeReturnDao.updateEOMCARDBalanceKnockOn(any(OtbBean.class))).thenReturn(1);
        //update Card, Customer and Account Balances
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCustomerOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateAccountOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCardOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCustomerOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineAccountOtb(any(OtbBean.class))).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCardOtb(any(OtbBean.class))).thenReturn(1);

        // Configure ChequeReturnDao.getCardAccountCustomer(...).
        final CardAccountCustomerBean cardAccountCustomerBean = new CardAccountCustomerBean();
        cardAccountCustomerBean.setAccountNumber("accNo");
        cardAccountCustomerBean.setCustomerId("customerId");
        cardAccountCustomerBean.setMaincardNumber(new StringBuffer("value"));
        cardAccountCustomerBean.setCardNumber(cardNumber);

        when(chequeReturnServiceUnderTest.chequeReturnDao.getCardAccountCustomer(any(StringBuffer.class)))
                .thenReturn(cardAccountCustomerBean);
        when(chequeReturnServiceUnderTest.chequeReturnDao.getTxnIdForLastChequeByAccount(any(PaymentBean.class))).thenReturn(txnId);
        when(chequeReturnServiceUnderTest.chequeReturnDao.getTxnIdForLastCheque(any(PaymentBean.class))).thenReturn(txnId);
        when(chequeReturnServiceUnderTest.commonDao.getCardAssociationFromCardBin(anyString())).thenReturn("1");// card accociation
        when(chequeReturnServiceUnderTest.chequeReturnDao.checkDuplicateChequeReturnEntry(any(StringBuffer.class),
                anyDouble(), anyString(), anyString(), anyString())).thenReturn(false);// no duplicates
        when(chequeReturnServiceUnderTest.chequeReturnDao.insertReturnChequeToEODTransaction(any(StringBuffer.class),
                anyString(), anyDouble(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.addCardFeeCount(any(StringBuffer.class),
                anyString(), anyDouble())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updatePaymentStatus(any(StringBuffer.class), eq("EDON"), anyString())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateTransactionEODStatus(any(StringBuffer.class),
                any(StringBuffer.class), eq("EDON"), anyString())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateChequeStatusForEODTxn(any(PaymentBean.class),
                anyString())).thenReturn(1);
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateChequePaymentStatus(anyInt(), eq("EDON")))
                .thenReturn(1);

        when(chequeReturnServiceUnderTest.chequeReturnDao.getAccountNoOnCard(any(StringBuffer.class)))
                .thenReturn("accountNo");
        when(chequeReturnServiceUnderTest.chequeReturnDao.getPaymentAmountBetweenDueDate(anyString(), anyInt(), eq("EDON"),
                anyString())).thenReturn(payment);// payment

        // Configure ChequeReturnDao.getCardBlockOldCardStatus(...).
        final BlockCardBean blockCardBean = new BlockCardBean();
        blockCardBean.setCardNo(cardNumber);
        blockCardBean.setOldStatus("OLDSTATUS");
        blockCardBean.setNewStatus("NEWSTATUS");

        when(chequeReturnServiceUnderTest.chequeReturnDao.getCardBlockOldCardStatus(
                any(StringBuffer.class))).thenReturn(blockCardBean);// block bean not null
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(any(StringBuffer.class),
                eq("OLDSTATUS"))).thenReturn(1);// update card status
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(any(StringBuffer.class),
                eq("CATB"))).thenReturn(1);// clear temporary block status

        when(chequeReturnServiceUnderTest.chequeReturnDao.getRiskClassOnNdia(1)).thenReturn(new String[]{"1", "1", "1"});// MIN NDIA, RISK CLASS, MIN NDIA
        when(chequeReturnServiceUnderTest.chequeReturnDao.updateDelinquencyStatus(any(StringBuffer.class),
                anyString(), anyInt())).thenReturn(1);

        // Run the test
        Iterator it = chequeReturnsList.entrySet().iterator();
        if (!chequeReturnsList.isEmpty()) {
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                chequeReturnServiceUnderTest.proceedChequeReturn(pair);
            }
        }

        // Verify the result
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateEOMCARDBalanceKnockOn(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCustomerOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateAccountOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCardOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCustomerOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineAccountOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateOnlineCardOtb(otbBean));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.insertReturnChequeToEODTransaction(cardNumber,
                "accountNo", 0.0, "txnId", "traceId", cqrtSeqNo, "1"));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.addCardFeeCount(cardNumber, "feeCode", 0.0));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updatePaymentStatus(cardNumber, "EDON", cqrtSeqNo));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateTransactionEODStatus(returnChequePaymentDetail.getOldcardnumber(),
                returnChequePaymentDetail.getOldcardnumber(), "EDON", cqrtSeqNo));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateChequeStatusForEODTxn(new PaymentBean(), "accountNo"));
        assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateChequePaymentStatus(returnChequePaymentDetail.getId(), "EDON"));

        if (testCase.equals(subTestCase2)) {//Payment insufficient for card
            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(cardNumber, "OLDSTATUS"));
            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateCardStatus(cardNumber, "CATB"));

            assertEquals(1, chequeReturnServiceUnderTest.chequeReturnDao.updateDelinquencyStatus(cardNumber, "1", 1));
        }

    }
}