package com.epic.cms.connector;

import com.epic.cms.dao.MasterCardT67FileReadDao;
import com.epic.cms.model.bean.FileDetailsBean;
import com.epic.cms.model.bean.FilePathBean;
import com.epic.cms.service.MasterCardT67FileReadService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class MasterCardT67FileReadConnectorTest {

    private MasterCardT67FileReadConnector masterCardT67FileReadConnectorUnderTest;

    @BeforeEach
    void setUp() {
        masterCardT67FileReadConnectorUnderTest = new MasterCardT67FileReadConnector();
        masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadService = mock(MasterCardT67FileReadService.class);
        masterCardT67FileReadConnectorUnderTest.logManager = mock(LogManager.class);
        masterCardT67FileReadConnectorUnderTest.taskExecutor = mock(ThreadPoolTaskExecutor.class);
        masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao = mock(MasterCardT67FileReadDao.class);
    }

    @Test
    void testConcreteProcess() throws Exception {
        // Setup
        // Configure MasterCardT67FileReadDao.loadFilePaths(...).
        final FilePathBean filePathBean = new FilePathBean();
        filePathBean.setPath_master_file_windows("path_master_file_windows");
        filePathBean.setPath_master_file_linux("path_master_file_linux");
        filePathBean.setPath_backup_windows("path_backup_windows");
        filePathBean.setPath_backup_linux("path_backup_linux");

        Configurations.PATH_MASTER_FILE = "gfdhdhgh";

        when(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao.loadFilePaths()).thenReturn(filePathBean);

        when(masterCardT67FileReadConnectorUnderTest.taskExecutor.getActiveCount()).thenReturn(0);
        when(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao.isFilesAvailable("INIT"))
                .thenReturn(false);

        // Configure MasterCardT67FileReadDao.getFileDetails(...).
        final FileDetailsBean fileDetailsBean = new FileDetailsBean();
        fileDetailsBean.setFileName("fileName");
        fileDetailsBean.setFileId("fileId");
        fileDetailsBean.setTransactionType("transactionType");
        final ArrayList<FileDetailsBean> fileDetailsBeans = new ArrayList<>(List.of(fileDetailsBean));
        when(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao.getFileDetails("INIT"))
                .thenReturn(fileDetailsBeans);

        when(masterCardT67FileReadConnectorUnderTest.logManager.processStartEndStyle("print")).thenReturn("result");
        when(masterCardT67FileReadConnectorUnderTest.logManager.logHeader("Error : File does not exist"))
                .thenReturn("result");
        when(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao.truncateEodMasterIP0040T1Data())
                .thenReturn(0);
        when(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao.truncateEodMasterIP0075T1Data())
                .thenReturn(0);
        when(masterCardT67FileReadConnectorUnderTest.logManager.logSummery(
                Map.ofEntries(Map.entry("value", "value")))).thenReturn("result");
        when(masterCardT67FileReadConnectorUnderTest.logManager.logStartEnd(
                "Master Card T67 File Reading process completed")).thenReturn("result");

        // Run the test
        masterCardT67FileReadConnectorUnderTest.concreteProcess("fileId");

        // Verify the results
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadService).FileAvailabilityCheck(
                new File("filename.txt"), 0);
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao).updateFileStartTime("fileId");
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao).updateFileStatus("fileId", "FREAD");
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao).truncateEodMasterIP0040T1Data();
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao).truncateEodMasterIP0075T1Data();
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadService).IP0040T1UnpackThread(
                new ArrayList<>(List.of("value")), 0, 0, 0, "fileId");
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadService).IP0075T1UnpackThread(
                new ArrayList<>(List.of("value")), 0, 0, 0, "fileId");
        verify(masterCardT67FileReadConnectorUnderTest.masterCardT67FileReadDao).updateFileStatistics("fileId", "FCOMP",
                "transactionCount");
    }
}
