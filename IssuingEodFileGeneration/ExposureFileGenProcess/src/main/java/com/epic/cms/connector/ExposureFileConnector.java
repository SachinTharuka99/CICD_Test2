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
import org.jpos.iso.ISOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

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

    @Override
    public void concreteProcess() throws Exception {
        try {
            infoLogger.info(logManager.processHeaderStyle("Exposure File Process"));
            infoLogger.info(logManager.processHeaderStyle("Exposure File Process Started"));

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
                        errorLogger.error("Exception in File generating ", e);
                    }
                }
                fileGenerationService.generateFile( fileContent.toString(), filePath, backUpFilePath);
                infoLogger.info(" Successfully Created Exposure File in: " + filePath);
            } catch (Exception e) {
                errorLogger.error("Exposure File Gen Process Failed", e);
            }
        } catch (Exception e) {
            errorLogger.error("Exposure File Process Failed", e);
        }
    }
}
