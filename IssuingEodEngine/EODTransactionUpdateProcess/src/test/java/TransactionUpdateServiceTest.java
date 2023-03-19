import com.epic.cms.repository.TransactionUpdateRepo;
import com.epic.cms.service.TransactionUpdateService;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.Mockito.*;

class TransactionUpdateServiceTest {
    private TransactionUpdateService transactionUpdateServiceUnderTest;

    @BeforeEach
    void setUp() {
        transactionUpdateServiceUnderTest = new TransactionUpdateService();
        transactionUpdateServiceUnderTest.status = mock(StatusVarList.class);
        transactionUpdateServiceUnderTest.transactionUpdateRepo = mock(TransactionUpdateRepo.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"VISA", "MASTER"})
    @DisplayName("Test Transaction Update")
    void transactionUpdate(String cardAssociation) throws Exception {
        // Run the test
        transactionUpdateServiceUnderTest.transactionUpdate(cardAssociation);

        // Verify the result
        if (cardAssociation.equals("VISA")) {
            verify(transactionUpdateServiceUnderTest.transactionUpdateRepo, times(1)).callStoredProcedureForVisaTxnUpdate();
        } else if (cardAssociation.equals("MASTER")) {
            verify(transactionUpdateServiceUnderTest.transactionUpdateRepo, times(1)).callStoredProcedureForMasterTxnUpdate();
        }

    }
}