import com.epic.cms.dao.FeePostDao;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.service.FeePostService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FeePostServiceTest {
    int capacity = 200000;
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<Integer>(capacity);
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue<Integer>(capacity);
    private FeePostService feePostServiceUnderTest;
    static MockedStatic<CommonMethods> common;
    @BeforeAll
    public static void init() {
        System.out.println("--before all--");
        common = Mockito.mockStatic(CommonMethods.class);
    }

    @AfterAll
    public static void close() {
        common.close();
    }

    @BeforeEach
    void setUp() {
        feePostServiceUnderTest = new FeePostService();
        feePostServiceUnderTest.feePostDao = mock(FeePostDao.class);
        feePostServiceUnderTest.status = mock(StatusVarList.class);
        feePostServiceUnderTest.logManager = mock(LogManager.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Test Proceed Fee Post")
    void testProceedFeePost(int noOfFees) throws Exception {

        // Setup
        final OtbBean bean = new OtbBean();
        bean.setCustomerid("customerid");
        bean.setAccountnumber("accountnumber");

        // Configure FeePostDao.getFeeAmount(...).
        //1st fee
        final OtbBean otbBean1 = new OtbBean();
        otbBean1.setCardnumber(new StringBuffer("value"));
        otbBean1.setOtbcredit(250.0);
        otbBean1.setTmpcredit(250.0);
        //2nd fee
        final OtbBean otbBean2 = new OtbBean();
        otbBean2.setCardnumber(new StringBuffer("value"));
        otbBean2.setOtbcredit(350.0);
        otbBean2.setTmpcredit(350.0);

        int noOfCardTableCalls = 0;
        List<OtbBean> feeList = null;
        if (noOfFees == 1) {
            feeList = List.of(otbBean1);
            noOfCardTableCalls = 1;
        } else if (noOfFees == 2) {
            feeList = List.of(otbBean1, otbBean2);
            noOfCardTableCalls = 2;
        }

        Configurations.EOD_USER = "eoduser";

        when(feePostServiceUnderTest.feePostDao.getFeeAmount(anyString())).thenReturn(feeList);
        when(feePostServiceUnderTest.feePostDao.updateCardOtb(any())).thenReturn(1);
        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");

        // Run the test
        feePostServiceUnderTest.proceedFeePost(bean,successCount,failCount);

        // Verify the results
        assertEquals(1, feePostServiceUnderTest.feePostDao.updateCardOtb(any(OtbBean.class)));
        verify(feePostServiceUnderTest.feePostDao,times(noOfCardTableCalls)).updateCardOtb(any(OtbBean.class));
        verify(feePostServiceUnderTest.feePostDao, times(noOfCardTableCalls)).updateEODCARDBALANCEByFee(any(OtbBean.class));
        verify(feePostServiceUnderTest.feePostDao, times(noOfCardTableCalls)).updateOnlineCardOtb(any(OtbBean.class));

        verify(feePostServiceUnderTest.feePostDao, times(1)).updateAccountOtb(any(OtbBean.class));
        verify(feePostServiceUnderTest.feePostDao, times(1)).updateEODCARDFEE(bean.getAccountnumber());
        verify(feePostServiceUnderTest.feePostDao, times(1)).updateEOMINTEREST(bean.getAccountnumber());
        verify(feePostServiceUnderTest.feePostDao, times(1)).updateOnlineAccountOtb(any(OtbBean.class));
        verify(feePostServiceUnderTest.feePostDao, times(1)).updateCustomerOtb(any(OtbBean.class));
        verify(feePostServiceUnderTest.feePostDao, times(1)).updateOnlineCustomerOtb(any(OtbBean.class));

    }
}