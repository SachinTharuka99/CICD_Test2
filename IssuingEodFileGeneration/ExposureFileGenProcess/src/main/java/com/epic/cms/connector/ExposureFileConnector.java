/**
 * Author : lahiru_p
 * Date : 11/17/2022
 * Time : 10:29 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.bean.ExposureFileBean;
import com.epic.cms.repository.ExposureFileRepo;
import com.epic.cms.service.ExposureFileService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.epic.cms.util.LogManager.*;

@Service
public class ExposureFileConnector extends FileGenProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    ExposureFileRepo exposureFileRepo;

    @Autowired
    ExposureFileService exposureFileService;

    List<ExposureFileBean> exposureFileDetails;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Override
    public void concreteProcess() throws Exception {
        try {
            logInfo.info(logManager.logStartEnd("Exposure File Process"));
            logInfo.info(logManager.logStartEnd("Exposure File Process Started"));

            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_EXPOSURE_FILE;
            CommonMethods.eodDashboardProgressParametersReset();

            //get the exposure details
            exposureFileDetails = exposureFileRepo.getExposureFileDetails();
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = exposureFileDetails.size();

            // SetUp File Path
            fileExtension = ".txt";
            fileDirectory = Configurations.EXPOSURE_FILE_PATH;

            String currentEodId = Integer.toString(Configurations.EOD_ID + 1); // this is to get file sequence number from eodid. eg: 20013100 ->  20013101 -> <001>
            fileName = "DFCCCMSEX".concat(".").concat(new SimpleDateFormat("yyyyMMdd").format(Configurations.EOD_DATE)).concat(ISOUtil.zeropad(currentEodId.substring(currentEodId.length() - 2), 3)).concat(fileExtension);

            filePath = fileDirectory + fileName;
            backUpFilePath = fileDirectory + backUpName + fileName;

            //create directories if not exists
            String backUpFile = fileDirectory + backUpName;
            fileGenerationService.createDirectoriesForFileAndBackUpFile(fileDirectory, backUpFile );

            StringBuilder fileContent = new StringBuilder();
            int recordCount = 0;
            try {
                for (ExposureFileBean bean : exposureFileDetails) {
                    StringBuilder content = new StringBuilder();
                    try {
                        recordCount++;
                        content = exposureFileService.getFileContent(bean, recordCount);
                        fileContent.append(content);
                        Configurations.PROCESS_SUCCESS_COUNT++;
                    } catch (Exception e) {
                        Configurations.PROCESS_FAILD_COUNT++;
                        logError.error("Exception in File generating ", e);
                    }
                }
                fileGenerationService.generateFile( fileContent.toString(), filePath, backUpFilePath);
               logError.error(" Successfully Created Exposure File in: " + filePath);
            } catch (Exception e) {
                logError.error("Exposure File Gen Process Failed", e);
            }
        } catch (Exception e) {
            logError.error("Exposure File Process Failed", e);
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Number of Exposure File Records ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
    }
}
