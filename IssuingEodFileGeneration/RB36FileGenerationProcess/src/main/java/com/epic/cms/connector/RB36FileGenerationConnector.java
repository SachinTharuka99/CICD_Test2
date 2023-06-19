/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 10:22 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.EodOuputFileBean;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.GlBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.RB36FileGenerationRepo;
import com.epic.cms.service.RB36FileGenerationService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.epic.cms.util.LogManager.*;

@Service
public class RB36FileGenerationConnector extends FileGenProcessBuilder {

    @Autowired
    RB36FileGenerationRepo rb36FileGenerationRepo;

    @Autowired
    RB36FileGenerationService rb36FileGenerationService;

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    int noofRecords = 0;

    @Override
    public void concreteProcess() throws Exception {
        fileGenerationModel = new FileGenerationModel();
        HashMap<String, ArrayList<GlAccountBean>> hmap;
        ArrayList<StringBuffer> npCards;
        HashMap<String, GlBean> glAccountsDetail;

        fileExtension = ".txt";
        fileDirectory = Configurations.RB36_FILE_PATH;

        filePath = fileDirectory + fileName + fileExtension;
        try {
            //setup file path
            SimpleDateFormat sd = new SimpleDateFormat("yyyy");
            String year = sd.format(new Date());
            String today1 = String.valueOf(year) + Integer.toString(Configurations.EOD_ID).substring(2, 6);
            String fieldDelimeter = Configurations.OUTPUTFILE_FIELD_DELIMETER;
            String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
            int seq = Integer.parseInt(eodSeq) + 1;
            String sequence = String.format("%03d", seq);
            fileName = Configurations.RB36_FILE_PREFIX + today1 + "." + sequence;

            //create directories if not exists
            String backUpFile =Configurations.RB36_FILE_PATH + backUpName;
            fileGenerationService.createDirectoriesForFileAndBackUpFile(Configurations.RB36_FILE_PATH, backUpFile );

            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_RB36_FILE_CREATION);

            //Create File Path
            filePath = fileDirectory + fileName + fileExtension;
            backUpFilePath = fileDirectory + backUpName + fileName + fileExtension;

            if (processBean != null) {
                try {
                    logInfo.info(logManager.logStartEnd("RB36 File Generation Process"));
                    Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_RB36_FILE_CREATION;
                    CommonMethods.eodDashboardProgressParametersReset();
                    logInfo.info(logManager.logStartEnd("RB36 File Generation Successfully Started"));
                    //commonRepo.insertToEodProcessSumery(Configurations.PROCESS_RB36_FILE_CREATION);

                    //Get NP card set
                    npCards = rb36FileGenerationRepo.getNPCard();

                    //get the gl entries for the gl file
                    hmap = rb36FileGenerationRepo.getPaymentDataFromEODGl();
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = hmap.size();

                    //get active gl account details from GLACCOUNT table
                    glAccountsDetail = rb36FileGenerationRepo.getGLAccData();

                    //get content of file
                    fileGenerationModel = rb36FileGenerationService.getFileContent(npCards, hmap, glAccountsDetail, fieldDelimeter);

                    if (fileGenerationModel.getFileContent().length() > 0) {
                        StringBuilder header = new StringBuilder();
                        header.append(fileName.concat(".txt")); //FILE_NAME
                        header.append(fieldDelimeter);
                        header.append(CommonMethods.validateLength(Integer.toString(fileGenerationModel.getHeaderDebitCount()), 4)); //NO_OF_DR
                        header.append(fieldDelimeter);
                        header.append(CommonMethods.validateCurrencyLength(fileGenerationModel.getHeaderDebitBig().toString(), 30)); //DR_TOT_VAL_LCY
                        header.append(fieldDelimeter);
                        header.append(CommonMethods.validateLength(Integer.toString(fileGenerationModel.getHeaderCreditCount()), 4)); //NO_OF_CR
                        header.append(fieldDelimeter);
                        header.append(CommonMethods.validateCurrencyLength(fileGenerationModel.getHeaderCreditBig().toString(), 30)); //CR_TOT_VAL_LCY
                        header.append(fieldDelimeter);
                        header.append(CommonMethods.validateLength("Y", 1)); //BATCH_REJ
                        header.append(fieldDelimeter);
                        header.append(CommonMethods.validateLength("-1", 50)); //CHK_SUM
                        header.append(System.lineSeparator());

                        fileGenerationModel.setFileHeader(header);
                        fileGenerationModel.setFinalFile(fileGenerationModel.getFileHeader().append(fileGenerationModel.getFileContent()));

                        fileGenerationService.generateFile(fileGenerationModel.getFinalFile().toString(), filePath, backUpFilePath);

                        toDeleteStatus = false;

                    } else {
                        logError.error("Empty line in body. Hence No header section in RB36");
                    }
                    //Update picked record status to EDON
                    for (int key : fileGenerationModel.getTxnIdList()) {
                        noofRecords++;
                        rb36FileGenerationRepo.updateEodGLAccount(key);
                    }
                    logInfo.info("RB36 File Process Successfully Completed ");

                    EodOuputFileBean eodoutputfilebean = new EodOuputFileBean();
                    eodoutputfilebean.setFileName(fileName + ".txt");
                    eodoutputfilebean.setNoOfRecords(noofRecords);

                    if (!toDeleteStatus) {
                        commonRepo.insertOutputFiles(eodoutputfilebean, "RB36");
                    }

                    if (Configurations.PROCESS_FAILD_COUNT == 0) {
                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getSUCCES_STATUS(), Configurations.PROCESS_RB36_FILE_CREATION, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                    } else {
                        commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getERROR_STATUS(), Configurations.PROCESS_RB36_FILE_CREATION, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                    }

                } catch (Exception e) {
                    logError.error("Error while writing RB36 file.Exit from the process. Exception:---> ", e);
                }
            }
        } catch (Exception e) {
            logError.error("RB36FileGeneration Process Failed", e);
            commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getERROR_STATUS(), Configurations.PROCESS_RB36_FILE_CREATION,Configurations.PROCESS_SUCCESS_COUNT,Configurations.PROCESS_FAILD_COUNT,CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
            if (processBean.getCriticalStatus() == 1) {
                Configurations.COMMIT_STATUS = false;
                Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                Configurations.MAIN_EOD_STATUS = false;
            }
        }finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Process Name", processBean.getProcessDes());
        summery.put("File Name", fileName);
        summery.put("No of records ", noofRecords);
        summery.put("Header Debit Count ", fileGenerationModel.getHeaderDebitCount());
        summery.put("Header Credit Count ", fileGenerationModel.getHeaderCreditCount());
        summery.put("Created Date ", Configurations.EOD_DATE.toString());
    }
}
