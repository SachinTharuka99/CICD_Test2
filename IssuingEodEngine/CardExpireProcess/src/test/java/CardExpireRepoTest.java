/**
 * Created By Lahiru Sandaruwan
 * Date : 10/23/2022
 * Time : 10:44 PM
 * Project Name : EODRevamp_Mockito
 * Topic :
 */

import com.epic.cms.repository.CardBlockRepo;
import com.epic.cms.repository.CardExpireRepo;
import com.epic.cms.repository.ConfigurationsRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CardExpireRepoTest {


    @InjectMocks
    private CardExpireRepo cardExpireRepo;

    @InjectMocks
    private ConfigurationsRepo configurationsRepo;

    @InjectMocks
    private CardBlockRepo cardBlockRepo;

    @Mock
    private JdbcTemplate backendJdbcTemplate;

    @Spy
    private JdbcTemplate onlineJdbcTemplate;

    @Mock
    StatusVarList statusList;

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
    @DisplayName("Test Update of card status to Expire")
    public void testUpdateCardStatusToExpire() throws Exception {
        // given - precondition or setup
        StringBuffer cardNo = new StringBuffer("4380431766518012");

        /**
         * when - action or behaviour that we are going test
         * then - verify the result or output using assert statements
         */
        when(backendJdbcTemplate.update(any(), any(), any(), any())).thenReturn(1);
        assertEquals(1, cardExpireRepo.setCardStatusToExpire(cardNo));
    }

    @Test
    @DisplayName("Test Deactivate Card Block")
    public void testDeactivateCardBlock() throws Exception {
        StringBuffer cardNo = new StringBuffer("4380431766518012");
        // Mockito.mockStatic(CommonMethods.class);
        when(backendJdbcTemplate.update(any(), any(), any(), any(), any(), any())).thenReturn(1);
        assertEquals(1, cardBlockRepo.deactivateCardBlock(cardNo));
    }

    @Test
    @DisplayName("Test Insert to Card Block")
    public void testInsertToCardBlock() throws Exception {
        StringBuffer cardNo = new StringBuffer("4380431766518012");
        String cardStatus = "s";
        //Mockito.mockStatic(CommonMethods.class);
        when(backendJdbcTemplate.update(any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(1);
        assertEquals(1, cardExpireRepo.insertToCardBlock(cardNo, cardStatus));
    }

    @Spy
    List<String> myList = new ArrayList<String>();

    @Test
    public void usingSpyAnnotation() {
        myList.add("Hello, This is Test");
        Mockito.verify(myList).add("Hello, This is Test");
        assertEquals(1, myList.size());
    }
}
