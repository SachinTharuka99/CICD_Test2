import com.epic.cms.model.bean.RecATMFileIptRowDataBean;
import com.epic.cms.repository.ATMFileClearingRepo;
import com.epic.cms.service.ATMFileClearingService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Author :
 * Date : 2/4/2023
 * Time : 1:04 AM
 * Project Name : ecms_eod_file_processing_engine
 */

public class ATMFileClearingServiceTest {
    private ATMFileClearingService atmFileClearingServiceUnderTest;
    static MockedStatic<CommonMethods> common;
    private final String testCase1 = "valid record";
    private final String testCase2 = "invalid record";
    private final String testCase3 = "invalid card";

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
        atmFileClearingServiceUnderTest = new ATMFileClearingService();
        atmFileClearingServiceUnderTest.atmFileClearingRepo = mock(ATMFileClearingRepo.class);
        atmFileClearingServiceUnderTest.status = mock(StatusVarList.class);
        atmFileClearingServiceUnderTest.logManager = mock(LogManager.class);

    }

    @ParameterizedTest
    @ValueSource(strings = {testCase1, testCase2, testCase3})
    void testValidateFile(String testCase) throws Exception {
        //setup
        Hashtable<String, String[]> paymentFieldValidationsTable = new Hashtable<String, String[]>();
        paymentFieldValidationsTable.put("1", new String[]{"PV003"});
        paymentFieldValidationsTable.put("2", new String[]{"PV003"});
        paymentFieldValidationsTable.put("3", new String[]{"PV003"});
        paymentFieldValidationsTable.put("4", new String[]{"PV001"});
        paymentFieldValidationsTable.put("5", new String[]{"PV000"});
        paymentFieldValidationsTable.put("6", new String[]{"PV001"});
        paymentFieldValidationsTable.put("7", new String[]{"PV001"});
        paymentFieldValidationsTable.put("8", new String[]{"PV001"});
        paymentFieldValidationsTable.put("9", new String[]{"PV001"});
        paymentFieldValidationsTable.put("10", new String[]{"PV000"});
        paymentFieldValidationsTable.put("11", new String[]{"PV006"});
        paymentFieldValidationsTable.put("12", new String[]{"PV001"});

        Configurations.USER = "EOD";
        Configurations.ATM_VALIDATION_HASH_TABLE = paymentFieldValidationsTable;
        String fileId = "fileId";

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);
        final AtomicInteger invalidCount = new AtomicInteger(0);

        final RecATMFileIptRowDataBean paymentFileBean = new RecATMFileIptRowDataBean();
        paymentFileBean.setFileid(fileId);
        paymentFileBean.setFilename("filename");
        paymentFileBean.setLinenumber(new BigDecimal("1"));
        if (testCase.equals(testCase1) || testCase.equals(testCase3)) {//valid record
            paymentFileBean.setLinecontent("WDL\tNORM\tVICR\t0\t21-AUG-2022 06:15:35 PM\t000679\t000593668231\t483559\t4835592695561067\t4\t2000\t144");
        } else if (testCase.equals(testCase2)) {//invalid record
            paymentFileBean.setLinecontent("WDL\tNORM\tVICR\t0\t21-AUG-2022 06:15:35 PM\t000679\t000593668231\t483559\t4835592695561067\t4\t2000\tLKR");
        }

        common.when(() -> CommonMethods.cardNumberMask(any(StringBuffer.class))).thenReturn("456788******8888");

        when(atmFileClearingServiceUnderTest.atmFileClearingRepo.getErrorDesc(anyString())).thenReturn("validationDesc");
        when(atmFileClearingServiceUnderTest.atmFileClearingRepo.getATMFieldDesc(anyString())).thenReturn("fieldDesc");
        when(atmFileClearingServiceUnderTest.atmFileClearingRepo.insertToRECATMFILEINVALID(anyString(),
                any(BigDecimal.class), anyString())).thenReturn(1);

        if (!testCase.equals(testCase3)) {
            when(atmFileClearingServiceUnderTest.atmFileClearingRepo.checkForValidCard(any(StringBuffer.class)))
                    .thenReturn(true);
        } else {
            when(atmFileClearingServiceUnderTest.atmFileClearingRepo.checkForValidCard(any(StringBuffer.class)))
                    .thenReturn(false);
        }
        when(atmFileClearingServiceUnderTest.atmFileClearingRepo.insertToATMTRANSACTION(anyString(), anyString(),
                any(String[].class))).thenReturn(1);
        when(atmFileClearingServiceUnderTest.atmFileClearingRepo.insertExceptionalTransactionData(anyString(), anyString(),
                eq(""), any(StringBuffer.class), eq(""), eq(""), eq(""), eq(""), anyString(), eq(""), eq(""),
                eq(Configurations.USER), any(Date.class), anyString(),
                anyString(), eq("YES"), eq(""), eq(""), eq(""), eq(""), eq(""), eq(""), anyString(),
                eq(""), eq(""), eq("ATM"), eq("Card No Invalid"))).thenReturn(1);
        when(atmFileClearingServiceUnderTest.atmFileClearingRepo.updateRawAtm(anyString(),
                any(BigDecimal.class))).thenReturn(1);

        // Run the test
        atmFileClearingServiceUnderTest.validateFile(fileId, paymentFileBean);

        // Verify the results
        if (testCase.equals(testCase1)) {
            verify(atmFileClearingServiceUnderTest.atmFileClearingRepo, times(1)).insertToATMTRANSACTION(eq(fileId),
                    anyString(), any(String[].class));
            verify(atmFileClearingServiceUnderTest.atmFileClearingRepo, times(1)).updateRawAtm(fileId, paymentFileBean.getLinenumber());
        } else if (testCase.equals(testCase2)) {
            verify(atmFileClearingServiceUnderTest.atmFileClearingRepo, times(1)).insertToRECATMFILEINVALID(eq(fileId), eq(paymentFileBean.getLinenumber()), anyString());
        } else if (testCase.equals(testCase3)) {
            verify(atmFileClearingServiceUnderTest.atmFileClearingRepo, times(1)).insertExceptionalTransactionData(eq(fileId), anyString(), eq(""), any(StringBuffer.class), eq(""), eq(""), eq(""), eq(""), anyString(), eq(""), eq(""), eq(Configurations.USER), any(Date.class), anyString(), anyString(), eq("YES"), eq(""), eq(""), eq(""), eq(""), eq(""), eq(""), anyString(), eq(""), eq(""), eq("ATM"), eq("Card No Invalid"));
            verify(atmFileClearingServiceUnderTest.atmFileClearingRepo, times(1)).updateRawAtm(fileId, paymentFileBean.getLinenumber());
        }
    }
}
