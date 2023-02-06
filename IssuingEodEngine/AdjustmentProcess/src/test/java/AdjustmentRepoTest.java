import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.repository.AdjustmentRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdjustmentRepoTest {

    @Mock
    private JdbcTemplate backendJdbcTemplate;

    @Mock
    private Configurations configurations;

    @Spy
    private StatusVarList statusVarList;

    @Spy
    private QueryParametersList queryParametersList;

    @InjectMocks
    private AdjustmentRepo adjustmentRepo;

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
    void testGetAdjustmentList() {
    }

    @Test
    @DisplayName("Test Insert To EODPAYMENT")
    void insertToEODPayments() throws Exception {
        queryParametersList.setAdjustment_insertToEODPayments("query");
        statusVarList.setINITIAL_STATUS("INIT");
        Configurations.EOD_USER="eoduser";

        PaymentBean pb = new PaymentBean();
        pb.setEodid(Configurations.EOD_ID);
        pb.setTraceid("1111");
        pb.setSequencenumber("1111");
        pb.setCardnumber(new StringBuffer("4455667777888"));
        pb.setMaincardno(new StringBuffer("4445654444444"));
        if (pb.getCardnumber().equals(pb.getMaincardno())) {
            pb.setIsprimary("YES");
        } else {
            pb.setIsprimary("NO");
        }
        pb.setAmount(1000.00);
        pb.setPaymenttype("CR");

//        when(backendJdbcTemplate.update(anyString(), anyString(), anyInt(), anyString(), anyString(), anyString(),
//                anyDouble(), anyString(), anyString(), anyDouble(), anyString(), anyString())).thenReturn(2);

        //run the test
        adjustmentRepo.insertToEODPayments(pb);

        //verify the result
        verify(backendJdbcTemplate,times(1)).update(anyString(), anyString(), anyInt(), anyString(), anyString(), anyString(),
                anyDouble(), anyString(), anyString(), anyDouble(), anyString(), anyString());

    }

    @Test
    @DisplayName("Test Get Card Association From Card BIN")
    void getCardAssociationFromCardBin() throws Exception {
        String cardBin = "445677";
        String cardAssociation = "1";
        queryParametersList.setAdjustment_getCardAssociationFromCardBin("query");

        when(backendJdbcTemplate.queryForObject(anyString(), eq(String.class), any())).thenReturn("1");
        assertEquals(cardAssociation, adjustmentRepo.getCardAssociationFromCardBin(cardBin));
    }

    @Test
    @DisplayName("Test Insert to EODTRANSACTION")
    void testInsertInToEODTransaction() throws Exception {

    }

    @Test
    @DisplayName("Test Update Adjustment Status")
    void testUpdateAdjustmentStatus() throws Exception {
        String adjustmentId = "testid";
        Configurations.EOD_DONE_STATUS = "EDON";

        when(backendJdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(1);
        assertEquals(1, adjustmentRepo.updateAdjustmentStatus(adjustmentId));
    }

    @Test
    @DisplayName("Test Update Transaction To EDON")
    void testUpdateTransactionToEDON() throws Exception {
        String txnId = "testid";
        queryParametersList.setAdjustment_updateTransactionToEDON("query");
        Configurations.EOD_DONE_STATUS = "EDON";

        when(backendJdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(1);
        assertEquals(1, adjustmentRepo.updateTransactionToEDON(txnId));

    }
}