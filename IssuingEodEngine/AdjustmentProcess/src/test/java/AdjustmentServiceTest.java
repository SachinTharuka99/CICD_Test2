import com.epic.cms.dao.AdjustmentDao;
import com.epic.cms.model.bean.AdjustmentBean;
import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.AdjustmentService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdjustmentServiceTest {
    private AdjustmentService adjustmentServiceUnderTest;
    private final String PAYMENT_ADJUSTMENT_TYPE = "1";
    private final String FEE_ADJUSTMENT_TYPE = "3";
    private final String INTEREST_ADJUSTMENT_TYPE = "4";
    private final String INSTALLMENT_ADJUSTMENT_TYPE = "6";
    private final String TRANSACTION_ADJUSTMENT_TYPE = "2";
    private final String CASH_ADVANCE_ADJUSTMENT_TYPE = "5";
    static MockedStatic<CommonMethods> common;

    public AtomicInteger ADJUSTMENT_SEQUENCE_NO = new AtomicInteger(0);
    int capacity = 200000;
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue<>(capacity);
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<>(capacity);

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
        adjustmentServiceUnderTest = new AdjustmentService();
        adjustmentServiceUnderTest.logManager = mock(LogManager.class);
        adjustmentServiceUnderTest.commonRepo = mock(CommonRepo.class);
        adjustmentServiceUnderTest.adjustmentDao = mock(AdjustmentDao.class);
    }

    @Test
    @DisplayName("Test Proceed CR Adjustment")
    void testProceedCRAdjustment() throws Exception {
        //setup
        final AdjustmentBean adjustmentBean = new AdjustmentBean();
        adjustmentBean.setId("id");
        adjustmentBean.setAdjustAmount(0.0);
        adjustmentBean.setAdjustDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        adjustmentBean.setAdjustDes("adjustDes");
        adjustmentBean.setTxnType("TXN_TYPE_PAYMENT");
        adjustmentBean.setCrDr("CR");
        adjustmentBean.setCardNumber(new StringBuffer("4567886788778888"));
        adjustmentBean.setAccNo("accNo");
        adjustmentBean.setAdjustType("adjustType");
        adjustmentBean.setCurruncyType("curruncyType");
        adjustmentBean.setPaymentType("CASH");
        adjustmentBean.setTxnId("txnId");
        adjustmentBean.setAdjustTxnType("adjustTxnType");
        adjustmentBean.setSequenceNo("sequenceNo");
        adjustmentBean.setTraceNo("traceNo");

        Configurations.PAYMENT_ADJUSTMENT_TYPE = Integer.parseInt(PAYMENT_ADJUSTMENT_TYPE);
        Configurations.FEE_ADJUSTMENT_TYPE = Integer.parseInt(FEE_ADJUSTMENT_TYPE);
        Configurations.INTEREST_ADJUSTMENT_TYPE = Integer.parseInt(INTEREST_ADJUSTMENT_TYPE);
        Configurations.INSTALLMENT_ADJUSTMENT_TYPE = Integer.parseInt(INSTALLMENT_ADJUSTMENT_TYPE);
        Configurations.TRANSACTION_ADJUSTMENT_TYPE = Integer.parseInt(TRANSACTION_ADJUSTMENT_TYPE);
        Configurations.CASHBACK_ADJUSTMENT_TYPE = Integer.parseInt(CASH_ADVANCE_ADJUSTMENT_TYPE);

        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        common.when(() -> CommonMethods.validate(anyString(), eq(8), eq('0'))).thenReturn("1");
        when(adjustmentServiceUnderTest.commonRepo.getMainCardNumber(any(StringBuffer.class))).thenReturn(adjustmentBean.getCardNumber());
        when(adjustmentServiceUnderTest.adjustmentDao.getCardAssociationFromCardBin(anyString())).thenReturn("1");
        when(adjustmentServiceUnderTest.adjustmentDao.updateAdjustmentStatus(anyString())).thenReturn(1);
        when(adjustmentServiceUnderTest.adjustmentDao.updateTransactionToEDON(anyString())).thenReturn(1);

        //run the test
        adjustmentServiceUnderTest.proceedAdjustment(adjustmentBean, ADJUSTMENT_SEQUENCE_NO, failCount, successCount);

        //verify
        assertEquals(adjustmentBean.getCardNumber(), adjustmentServiceUnderTest.commonRepo.getMainCardNumber(adjustmentBean.getCardNumber()));
        verify(adjustmentServiceUnderTest.adjustmentDao, times(1)).insertToEODPayments(any(PaymentBean.class));
        verify(adjustmentServiceUnderTest.adjustmentDao, times(1)).insertInToEODTransaction(any(AdjustmentBean.class), anyString());
        assertEquals("1", adjustmentServiceUnderTest.adjustmentDao.getCardAssociationFromCardBin("456788"));
        assertEquals(1, adjustmentServiceUnderTest.adjustmentDao.updateAdjustmentStatus(adjustmentBean.getId()));
        assertEquals(1, adjustmentServiceUnderTest.adjustmentDao.updateTransactionToEDON(adjustmentBean.getTxnId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {FEE_ADJUSTMENT_TYPE, INTEREST_ADJUSTMENT_TYPE, INSTALLMENT_ADJUSTMENT_TYPE, TRANSACTION_ADJUSTMENT_TYPE, CASH_ADVANCE_ADJUSTMENT_TYPE})
    @DisplayName("Test Proceed DR Adjustment")
    void testProceedDRAdjustment(String adjustType) throws Exception {
        //setup
        final AdjustmentBean adjustmentBean = new AdjustmentBean();
        adjustmentBean.setId("id");
        adjustmentBean.setAdjustAmount(0.0);
        adjustmentBean.setAdjustDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        adjustmentBean.setAdjustDes("adjustDes");
        adjustmentBean.setTxnType("TXN_TYPE_PAYMENT");
        adjustmentBean.setCrDr("DR");
        adjustmentBean.setCardNumber(new StringBuffer("4567886788778888"));
        adjustmentBean.setAccNo("accNo");
        adjustmentBean.setAdjustType(adjustType);
        adjustmentBean.setCurruncyType("curruncyType");
        adjustmentBean.setPaymentType("CASH");
        adjustmentBean.setTxnId("txnId");
        adjustmentBean.setAdjustTxnType("adjustTxnType");
        adjustmentBean.setSequenceNo("sequenceNo");
        adjustmentBean.setTraceNo("traceNo");

        Configurations.PAYMENT_ADJUSTMENT_TYPE = Integer.parseInt(PAYMENT_ADJUSTMENT_TYPE);
        Configurations.FEE_ADJUSTMENT_TYPE = Integer.parseInt(FEE_ADJUSTMENT_TYPE);
        Configurations.INTEREST_ADJUSTMENT_TYPE = Integer.parseInt(INTEREST_ADJUSTMENT_TYPE);
        Configurations.INSTALLMENT_ADJUSTMENT_TYPE = Integer.parseInt(INSTALLMENT_ADJUSTMENT_TYPE);
        Configurations.TRANSACTION_ADJUSTMENT_TYPE = Integer.parseInt(TRANSACTION_ADJUSTMENT_TYPE);
        Configurations.CASHBACK_ADJUSTMENT_TYPE = Integer.parseInt(CASH_ADVANCE_ADJUSTMENT_TYPE);

        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        common.when(() -> CommonMethods.validate(anyString(), eq(8), eq('0'))).thenReturn("1");
        when(adjustmentServiceUnderTest.adjustmentDao.getCardAssociationFromCardBin(anyString())).thenReturn("1");
        when(adjustmentServiceUnderTest.adjustmentDao.updateAdjustmentStatus(anyString())).thenReturn(1);
        when(adjustmentServiceUnderTest.adjustmentDao.updateTransactionToEDON(anyString())).thenReturn(1);

        //run the test
        adjustmentServiceUnderTest.proceedAdjustment(adjustmentBean, ADJUSTMENT_SEQUENCE_NO, failCount, successCount);

        //verify
        assertEquals("1", adjustmentServiceUnderTest.adjustmentDao.getCardAssociationFromCardBin("456788"));
        verify(adjustmentServiceUnderTest.adjustmentDao, times(1)).insertInToEODTransaction(any(AdjustmentBean.class), anyString());
        assertEquals(1, adjustmentServiceUnderTest.adjustmentDao.updateAdjustmentStatus(adjustmentBean.getId()));
        assertEquals(1, adjustmentServiceUnderTest.adjustmentDao.updateTransactionToEDON(adjustmentBean.getTxnId()));
    }
}