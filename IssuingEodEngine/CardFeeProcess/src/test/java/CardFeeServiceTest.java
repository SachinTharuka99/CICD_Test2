import com.epic.cms.dao.CardFeeDao;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.service.CardFeeService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CardFeeServiceTest {
    SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
    private CardFeeService cardFeeServiceUnderTest;
    static MockedStatic<CommonMethods> common;
    public AtomicInteger faileCardCount = new AtomicInteger(0);

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
        System.out.println("--before each--");
        cardFeeServiceUnderTest = new CardFeeService();
        cardFeeServiceUnderTest.status = mock(StatusVarList.class);
        cardFeeServiceUnderTest.cardFeeDao = mock(CardFeeDao.class);
        cardFeeServiceUnderTest.logManager = mock(LogManager.class);
    }

    @Test
    @DisplayName("Test Card Fee Calculate")
    void testCardFeeCalculate() throws Exception {
        //setup
        final CardFeeBean cardBean = new CardFeeBean();
        cardBean.setCardNumber(new StringBuffer("value"));
        cardBean.setAccNumber("accNumber");
        cardBean.setAccStatus("NP");//NP Acccount
        String LATE_PAYMENT_FEE = "FEC004";
        cardBean.setFeeCode(LATE_PAYMENT_FEE);
        cardBean.setFeeCount(0);
        cardBean.setCurrCode(0);
        cardBean.setCrOrDr("CR");
        cardBean.setFlatFee(0.0);
        cardBean.setMinAmount(0.0);
        cardBean.setMaxAmount(0.0);
        cardBean.setPercentageAmount(0.0);
        cardBean.setCombination("combination");
        cardBean.setCashAmount(0.0);
        cardBean.setNextAnniversaryDate("nextAnniversaryDate");
        cardBean.setOtbCredit(0.0);

        final CardFeeBean cardFeeBean = new CardFeeBean();
        cardFeeBean.setCardNumber(new StringBuffer("value"));
        cardFeeBean.setAccNumber("accNumber");
        cardFeeBean.setAccStatus("NP");//NP Account
        cardFeeBean.setFeeCode(LATE_PAYMENT_FEE);
        cardFeeBean.setFeeCount(0);
        cardFeeBean.setCurrCode(0);
        cardFeeBean.setCrOrDr("CR");
        cardFeeBean.setFlatFee(0.0);
        cardFeeBean.setMinAmount(0.0);
        cardFeeBean.setMaxAmount(0.0);
        cardFeeBean.setPercentageAmount(0.0);
        cardFeeBean.setCombination("combination");
        cardFeeBean.setCashAmount(0.0);
        cardFeeBean.setNextAnniversaryDate("nextAnniversaryDate");
        cardFeeBean.setOtbCredit(0.0);

        String EOD_ID = "22110400";
        Configurations.EOD_DATE = sdf.parse(EOD_ID.substring(0, EOD_ID.length() - 2));
        Configurations.LATE_PAYMENT_FEE = LATE_PAYMENT_FEE;
        String OVER_LIMIT_FEE = "FEC006";
        Configurations.OVER_LIMIT_FEE = OVER_LIMIT_FEE;
        cardFeeServiceUnderTest.status.setACCOUNT_NON_PERFORMING_STATUS("NP");

        java.sql.Date nextBillingDate = new java.sql.Date(Configurations.EOD_DATE.getTime());

        when(cardFeeServiceUnderTest.cardFeeDao.getCardFeeCountForCard(any(StringBuffer.class), anyString(), anyString())).thenReturn(cardFeeBean);
        common.when(() -> CommonMethods.getAmountFromCombination(anyDouble(), anyDouble(), anyString())).thenReturn(0.0);
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");
        common.when(() -> CommonMethods.getSqldate(any(Date.class))).thenReturn(new java.sql.Date(Configurations.EOD_DATE.getTime()));
        when(cardFeeServiceUnderTest.cardFeeDao.getNextBillingDateForCard(any(StringBuffer.class))).thenReturn(nextBillingDate);//next billing date equal to billing date
        when(cardFeeServiceUnderTest.cardFeeDao.updateDELINQUENTACCOUNTNpDetails(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString())).thenReturn(1);
        //when(cardFeeServiceUnderTest.cardFeeDao.updateDELINQUENTACCOUNTNpDetails(eq(0.0), anyDouble(), eq(0.0), eq(0.0), anyString())).thenReturn(1);
        //when(cardFeeServiceUnderTest.cardFeeDao.updateDELINQUENTACCOUNTNpDetails(eq(0.0), eq(0.0), eq(0.0), anyDouble(), anyString())).thenReturn(1);


        // Run the test
        cardFeeServiceUnderTest.cardFeeCalculate(cardBean, faileCardCount);

        // Verify the results
        assertEquals(cardFeeBean, cardFeeServiceUnderTest.cardFeeDao.getCardFeeCountForCard(cardFeeBean.getCardNumber(), cardBean.getAccNumber(), cardBean.getFeeCode()));
        //if (feeCode.equals(LATE_PAYMENT_FEE)) {
        assertEquals(nextBillingDate, cardFeeServiceUnderTest.cardFeeDao.getNextBillingDateForCard(cardBean.getCardNumber()));
        verify(cardFeeServiceUnderTest.cardFeeDao, times(1)).insertToEODCardFee(eq(cardFeeBean), eq(0.0), eq(nextBillingDate));
        verify(cardFeeServiceUnderTest.cardFeeDao, times(1)).updateCardFeeCount(cardFeeBean);
        assertEquals(1, cardFeeServiceUnderTest.cardFeeDao.updateDELINQUENTACCOUNTNpDetails(0.0, 0.0, 0.0, 0.0, cardBean.getAccNumber()));
        //}
    }

}