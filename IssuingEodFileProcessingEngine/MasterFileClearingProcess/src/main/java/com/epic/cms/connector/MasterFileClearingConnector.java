/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:33 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileProcessingProcessBuilder;
import com.epic.cms.dao.MasterFileClearingDao;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.service.MasterFileClearingService;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterFileClearingConnector extends FileProcessingProcessBuilder {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    MasterFileClearingService masterFileClearingService;
    @Autowired
    MasterFileClearingDao masterFileClearingDao;
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;

    @Override
    public void concreteProcess(String fileId) {
        System.out.println("Class Name:MasterFileClearingConnector,File ID:" + fileId + ",Current Thread:" + Thread.currentThread().getName());
        FileBean fileBean;
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MASTER_CLEARING;
            //reset eod dashboard parameters
            CommonMethods.eodDashboardProgressParametersReset();

            //get Master file info
            fileBean = this.getMasterFileInfo(fileId);
            if (fileBean != null) {
                //load file paths
                masterFileClearingDao.loadFilePaths();

                if ("LINUX".equals(Configurations.SERVER_RUN_PLATFORM)) {
                    Configurations.PATH_MASTER_FILE = Configurations.PATH_MASTER_FILE_LINUX;
                    Configurations.PATH_BACKUP = Configurations.PATH_BACKUP_LINUX;
                }
                if ("WINDOWS".equals(Configurations.SERVER_RUN_PLATFORM)) {
                    Configurations.PATH_MASTER_FILE = Configurations.PATH_MASTER_FILE_WINDOWS;
                    Configurations.PATH_BACKUP = Configurations.PATH_BACKUP_WINDOWS;
                }
                Configurations.PATH_ROOT = Configurations.PATH_MASTER_FILE;

                //process file
                masterFileClearingService.processFile(fileBean);
            } else {
                //file cannot proceed due to invalid status
                logError.error("Cannot read, Master file " + fileId + " is not in the initial status");
            }

        } catch (Exception ex) {
            logError.error("Master File clearing process failed for file " + fileId, ex);
            //update file status to FEROR
            try {
                masterFileClearingDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_ERROR);
            } catch (Exception e) {
                logError.error("", e);
            }
        }
    }

    @Override
    public void addSummaries() {

    }

    private synchronized FileBean getMasterFileInfo(String fileId) throws Exception {
        FileBean fileBean;
        try {
            fileBean = masterFileClearingDao.getMasterFileInfo(fileId);
            if (fileBean.getFileId() != null) {
                //file status is INIT,so it can proceed.
                //update file status to intermediate status FPROS
                //masterFileClearingDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_PROS);
            } else {
                fileBean = null;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return fileBean;
    }
}
