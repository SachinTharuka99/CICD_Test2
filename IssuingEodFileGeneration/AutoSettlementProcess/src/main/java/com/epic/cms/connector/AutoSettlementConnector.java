/**
 * Author : lahiru_p
 * Date : 11/28/2022
 * Time : 10:02 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.EodOuputFileBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.AutoSettlementRepo;
import com.epic.cms.service.AutoSettlementService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * *******************************************************************************
 * <p>
 * This method have 3 main steps.
 * ->Step 1 - update payments
 * ->Step 2 - genarate partial file
 * ->Step 3 - genarate Auto settlement file
 * <p>
 * STEP-1
 * Considering Txn table select all standing instruction payments.
 * Check those payments with remaining_amount in AutoSattlement table.
 * Calculate new remaing amount(new rem_amt = currnt rem_amt - payment) and update
 * that amount in autosettlemet table.
 * If the new remainingAmount =0 then,
 * Update RunningStatus as completed,
 * Update processingCount as 0,
 * <p>
 * STEP-2
 * Considering remaining amounts and processingCount,Genarate partial auto settlement file.
 * Genarate this Considering file only,
 * runningState = 1 (already in running state, that means this Standing
 * Instruction also sent in previous(1 or 2 days before) Full Autosettlement file)
 * Remaining amount >0
 * processingCount >0
 * <p>
 * STEP-3
 * Considering due dates (due date = eod date+1) Genarate Auto settlement file
 * Changing runningstatus = 0,2 -> 1 (onhold state or complete state change as running status)
 * <p>
 * <p>
 * ----NOTE----
 * This steps shoud call in given order.
 * IF AutoSettlemet file genarate before the partial file
 * Same Standing instruction will be include boths files
 * <p>
 * *******************************************************************************
 */
@Service
public class AutoSettlementConnector extends FileGenProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    AutoSettlementService autoSettlementService;

    @Autowired
    AutoSettlementRepo autoSettlementRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Override
    public void concreteProcess() throws Exception {
        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.AUTO_SETTLEMENT_PROCESS);

            if (processBean != null) {
                String[] partialFileContent = new String[3];
                String[] fullFileContent = new String[3];
                boolean toDeleteStatus = true;

                Configurations.RUNNING_PROCESS_ID = Configurations.AUTO_SETTLEMENT_PROCESS;
                CommonMethods.eodDashboardProgressParametersReset();

                /** SetUp File Path*/
                fileExtension = ".txt";
                fileDirectory = Configurations.AUTO_SETTLEMENT_FILE_PATH;

                SimpleDateFormat sd = new SimpleDateFormat("yyyy");
                String year = sd.format(new Date());
                String today1 = String.valueOf(year) + Integer.toString(Configurations.EOD_ID).substring(2, 6);
                String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
                int seq = Integer.parseInt(eodSeq) + 1;
                String sequence = String.format("%03d", seq);

                fileName = Configurations.AUTOSETTLEMENT_FILE_PREFIX + today1 + "." + sequence + fileExtension;

                filePath = fileDirectory + fileName;
                backUpFilePath = fileDirectory + backUpName + fileName;

                /**create directories if not exists*/
                String backUpFile = fileDirectory + backUpName;
                fileGenerationService.createDirectoriesForFileAndBackUpFile(fileDirectory, backUpFile);

                /**update auto settlement payments*/
                int receivedPaymentCount = autoSettlementRepo.updateAutoSettlementWithPayments();

                /** apply unsuccessfullStanding instruction fee*/
                autoSettlementRepo.getUnsuccessfullStandingInstructionFeeEligibleCards();

                /**considering received payments, genarate Partial file*/
                partialFileContent = autoSettlementRepo.generatePartialAutoSettlementFile(fileDirectory, fileName, sequence, fieldDelimeter);

                /**Generate new Auto Settlement file, considering due date*/
                fullFileContent = autoSettlementRepo.generateAutoSettlementFile(fileDirectory, fileName, sequence, fieldDelimeter);

                summery.put("Process Name", "AutoSettlement");

                BigDecimal headerTotalAmount = BigDecimal.valueOf(0.0);
                int totalFileTxnCount = 0;
                StringBuilder sbHeader = new StringBuilder();
                try {
                    if (fullFileContent[0] != null || partialFileContent[0] != null) {

                        if (fullFileContent[0] != null) {
                            totalFileTxnCount += Integer.parseInt(fullFileContent[1]);
                            headerTotalAmount = headerTotalAmount.add(new BigDecimal(fullFileContent[2]));
                        }
                        if (partialFileContent[0] != null) {
                            totalFileTxnCount += Integer.parseInt(partialFileContent[1]);
                            headerTotalAmount = headerTotalAmount.add(new BigDecimal(partialFileContent[2]));
                        }
                        sbHeader = autoSettlementService.createFileHeaderForAutoSettlementFile(fileName, headerTotalAmount, totalFileTxnCount, sequence, fieldDelimeter);
                        fileGenerationModel = new FileGenerationModel();
                        fileGenerationModel.setFileHeader(sbHeader);

                        if (fullFileContent[0] != null) {
                            fileGenerationModel.setFinalFile(fileGenerationModel.getFileHeader().append(fullFileContent[0]));
                            fileGenerationService.generateFile(fileGenerationModel.getFinalFile().toString(), filePath, backUpFilePath);
                            summery.put("Autosettlement Full File Genrated", "YES");
                        }

                        if (partialFileContent[0] != null) {
                            fileGenerationModel.setFinalFile(fileGenerationModel.getFileHeader().append(partialFileContent[0]));
                            fileGenerationService.generateFile(fileGenerationModel.getFinalFile().toString(), filePath, backUpFilePath);
                            summery.put("Autosettlement Partial File Genrated", "YES");
                        }

                        /** insert to eod output file table*/
                        EodOuputFileBean eodOutPutFileBean = new EodOuputFileBean();
                        eodOutPutFileBean.setFileName(fileName);
                        eodOutPutFileBean.setNoOfRecords(totalFileTxnCount);
                        commonRepo.insertOutputFiles(eodOutPutFileBean, "AUTOSETTLEMENT");
                        toDeleteStatus = false;

                    }
                    Configurations.PROCESS_SUCCESS_COUNT++;

                } catch (Exception e) {
                    Configurations.PROCESS_FAILD_COUNT++;
                    logInfo.info(logManager.logStartEnd("AutoSettlement Process Fails"));
                } finally {
                    try {
                        if (toDeleteStatus) {
                            fileGenerationService.deleteExistFile(filePath);
                        }
                    } catch (Exception ee) {
                        System.out.println("Error " + ee);
                    }
                }
                summery.put("Process Status", "Success");
            }

        } catch (Exception ex) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            summery.put("Process Status", "Error");
            logError.error("Auto Settlement Letter Process Failed", ex);
            try {
                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }
            } catch (Exception e2) {
                logError.error("Exception ", e2);
            }
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("Process Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Process Failed Count ", Configurations.PROCESS_FAILD_COUNT);
    }
}
