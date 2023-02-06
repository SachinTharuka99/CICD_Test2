import com.epic.cms.dao.MasterFileClearingDao;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.MasterFieldsDataBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.MasterExtractElementService;
import com.epic.cms.service.MasterFileClearingService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;

import static org.mockito.Mockito.*;

/**
 * Author :
 * Date : 2/4/2023
 * Time : 12:51 AM
 * Project Name : ecms_eod_file_processing_engine
 */

public class MasterFileClearingServiceTest {
    private MasterFileClearingService masterFileReadServiceUnderTest;

    @BeforeEach
    void setUp() {
        masterFileReadServiceUnderTest = new MasterFileClearingService();
        masterFileReadServiceUnderTest.logManager = mock(LogManager.class);
        masterFileReadServiceUnderTest.masterFileClearingDao = mock(MasterFileClearingDao.class);
        masterFileReadServiceUnderTest.status = mock(StatusVarList.class);
        masterFileReadServiceUnderTest.commonRepo = mock(CommonRepo.class);
        masterFileReadServiceUnderTest.masterExtractElementService = spy(MasterExtractElementService.class);
        masterFileReadServiceUnderTest.masterExtractElementService.masterFileClearingDao = mock(MasterFileClearingDao.class);
    }

    final String subTestCase1 = "Test Success Path";
    final String subTestCase2 = "Test File Not Exist";
    final String subTestCase3 = "Test Invalid Card";

    @ParameterizedTest
    @ValueSource(strings = {subTestCase1, subTestCase2, subTestCase3})
    @DisplayName("Test Read Master File")
    void testReadMasterFile(String subTestCase) throws Exception {
        // Setup
        Configurations.PATH_MASTER_FILE = "src/test/resources";
        Configurations.FIRST_PRESENTMENT_MTI = "1240";
        Configurations.INCOMMING_IPM_FILE_ENCODING_FORMAT = 1;//ASCII
        Configurations.EOD_USER = "eodUser";
        StringBuffer cardNumber = new StringBuffer("cardNumber");

        final FileBean fileDetailsBean = new FileBean();
        if (!subTestCase.equals(subTestCase2)) {
            fileDetailsBean.setFileName("MAS_220209_005_MA.IPM");
        } else {// Test File Not Exist (subTestCase2)
            fileDetailsBean.setFileName("testFile");
        }
        fileDetailsBean.setFileId("fileId");

        when(masterFileReadServiceUnderTest.logManager.processHeaderStyle(anyString())).thenReturn("formatted header");
        if (!subTestCase.equals(subTestCase3)) {
            when(masterFileReadServiceUnderTest.commonRepo.checkForValidCard(any(StringBuffer.class))).thenReturn(true);
        } else {// Test Invalid Card (subTestCase3)
            when(masterFileReadServiceUnderTest.commonRepo.checkForValidCard(any(StringBuffer.class))).thenReturn(false);
        }
        when(masterFileReadServiceUnderTest.masterFileClearingDao.insertExceptionalTransactionData(anyString(),//if not a valid card
                anyString(), eq(""), any(StringBuffer.class), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), eq(""), anyString(),
                any(Date.class), anyString(), anyString(), eq(""),
                anyString(), anyString(), anyString(), anyString(), eq(""),
                eq(""), eq(""), anyString(), eq(""), eq("MASTER"))).thenReturn(1);
        when(masterFileReadServiceUnderTest.masterFileClearingDao.loadMasterTransactionCount(anyString())).thenReturn(1000);

        // Run the test
        masterFileReadServiceUnderTest.processFile(fileDetailsBean);

        // Verify the result
        if (!subTestCase.equals(subTestCase2)) {
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(1)).updateFileStatus("fileId", "FREAD");
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(3)).insertFileDetailsIntoEODMasterInputRowData(anyString(), anyString(), anyString());
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(3)).insertFileDetailsIntoEODMasterFieldIdentity(any(MasterFieldsDataBean.class));
            verify(masterFileReadServiceUnderTest.commonRepo, times(3)).checkForValidCard(any(StringBuffer.class));
            if (subTestCase.equals(subTestCase1)) {
                verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(3)).insertFileDetailsIntoEODMasterTransaction(any(MasterFieldsDataBean.class));
            } else if (subTestCase.equals(subTestCase3)) {// Test Invalid Card (subTestCase3)
                verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(3)).insertExceptionalTransactionData(anyString(),//if not a valid card
                        anyString(), eq(""), any(StringBuffer.class), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), eq(""), anyString(),
                        any(Date.class), anyString(), anyString(), eq(""),
                        anyString(), anyString(), anyString(), anyString(), eq(""),
                        eq(""), eq(""), anyString(), eq(""), eq("MASTER"));
            }
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(1)).updateFileRecordCount(anyString(), anyString());
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(1)).updateFileStatus("fileId", "FCOMP");
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(1)).updateFileTxnCount(anyString(), anyString());
        } else {// Test File Not Exist (subTestCase2)
            verify(masterFileReadServiceUnderTest.masterFileClearingDao, times(1)).updateFileStatus("fileId", "FEROR");
        }
    }
}
