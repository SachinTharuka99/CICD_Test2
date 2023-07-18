/**
 * Author : rasintha_j
 * Date : 7/13/2023
 * Time : 9:59 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileProcessingProcessBuilder;
import com.epic.cms.dao.DCFFileClearingDao;
import com.epic.cms.model.bean.PaymentFileDataBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.DCFFileClearingService;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.epic.cms.util.Configurations;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DCFFileClearingConnector extends FileProcessingProcessBuilder {

    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    DCFFileClearingDao dcfFileReadDao;
    @Autowired
    StatusVarList status;
    @Autowired
    QueryParametersList queryParametersList;
    @Autowired
    @Qualifier("ThreadPool_ATMFileValidator")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    DCFFileClearingService dcfFileClearingService;
    public AtomicInteger failedFileNameCount = new AtomicInteger(0);
    public AtomicInteger failedFileCount = new AtomicInteger(0);

    @Override
    public void concreteProcess(String fileId) throws Exception {
        ArrayList<PaymentFileDataBean> fileList = new ArrayList<>();
        ArrayList<String> nameFieldList = new ArrayList<>();
        String filepath = "";
        String isFileNameValid = "";
        try {
            if ("LINUX".equals(Configurations.SERVER_RUN_PLATFORM)) {
                filepath = commonRepo.getLinuxFilePath(Configurations.FILE_CODE_DCF);
            } else if ("WINDOWS".equals(Configurations.SERVER_RUN_PLATFORM)) {
                filepath = commonRepo.getWindowsFilePath(Configurations.FILE_CODE_DCF);
            }

            nameFieldList = dcfFileReadDao.getNameFields(Configurations.FILE_CODE_DCF);

            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_DCF_FILE_READ;
            CommonMethods.eodDashboardProgressParametersReset();

            fileList = dcfFileReadDao.getDCFFileList();

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = fileList.size();

            for (PaymentFileDataBean fileBean : fileList) {

                isFileNameValid = dcfFileClearingService.fileNameValidation(nameFieldList, fileBean.getFilename());

                dcfFileClearingService.fileProcess(fileBean,isFileNameValid,filepath,failedFileNameCount,failedFileCount);
            }

            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logError.error("Exception", e);
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("DCF File Success Count", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - failedFileCount.get());
        summery.put("DCF File failed Count", failedFileCount.get());
    }
}
