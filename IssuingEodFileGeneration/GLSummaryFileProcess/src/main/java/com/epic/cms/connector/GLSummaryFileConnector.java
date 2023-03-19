/**
 * Author : lahiru_p
 * Date : 11/30/2022
 * Time : 8:34 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.EodOuputFileBean;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.GLSummaryFileRepo;
import com.epic.cms.service.GLSummaryFileService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.*;

@Service
public class GLSummaryFileConnector extends FileGenProcessBuilder {

    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    GLSummaryFileService glSummaryFileService;
    @Autowired
    GLSummaryFileRepo glSummaryFileRepo;

    int count;
    int noofRecords;
    LinkedHashMap accDetails = new LinkedHashMap();
    ArrayList<GlAccountBean> list = null;

    @Override
    public void concreteProcess() throws Exception {
        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_GL_FILE_CREATION);

            if (processBean != null) {

                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_GL_FILE_CREATION;
                CommonMethods.eodDashboardProgressParametersReset();

                try {
                    /**Create cashback GL file*/
                    try {
                        list = glSummaryFileRepo.getCashbackDataToEODGL();
                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                        accDetails.put("Gl data for", "Adjustment");

                        for (GlAccountBean glaccountBean : list) {
                            try {
                                accDetails.put("Card Number", CommonMethods.cardNumberMask(glaccountBean.getCardNo()));
                                accDetails.put("GL type", glaccountBean.getGlType());
                                accDetails.put("CRDR", glaccountBean.getCrDr());
                                accDetails.put("Amount", glaccountBean.getAmount());
                                accDetails.put("ID", glaccountBean.getKey());

                                /**insert to EODGL table*/
                                glSummaryFileRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getCardNo(), glaccountBean.getGlType(), glaccountBean.getAmount(), glaccountBean.getCrDr(), glaccountBean.getPaymentType());
                                glSummaryFileRepo.updateCashback(glaccountBean.getId(), 1);

                                Configurations.PROCESS_SUCCESS_COUNT++;

                            } catch (Exception e) {
                                Configurations.PROCESS_FAILD_COUNT++;
                                //errorLogger.error("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey(), e);
                                logManager.logError("Sync fail to EOD GL Account Table for Primary ID ", errorLoggerEFGE);
                                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
                            }

                            logManager.logDetails(accDetails, infoLoggerEFGE);
                            accDetails.clear();
                        }

                        accDetails.put("Data Retrieval Status for CashBack", "Passed");

                    } catch (Exception e) {
                        accDetails.put("Data Retrieval Status for CashBack", "Failed");
                    }

                    logManager.logDetails(accDetails, infoLoggerEFGE);
                    accDetails.clear();
                    list.clear();

                    /**Create cashback redeemed and expired GL file*/
                    try {

                        list = glSummaryFileRepo.getCashbackExpAndRedeemDataToEODGL();
                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                        accDetails.put("Gl data for", "Adjustment");

                        for (GlAccountBean glaccountBean : list) {
                            try {
                                accDetails.put("Card Number", CommonMethods.cardNumberMask(glaccountBean.getCardNo()));
                                accDetails.put("GL type", glaccountBean.getGlType());
                                accDetails.put("CRDR", glaccountBean.getCrDr());
                                accDetails.put("Amount", glaccountBean.getAmount());
                                accDetails.put("ID", glaccountBean.getKey());

                                if (glaccountBean.getGlType().equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || glaccountBean.getGlType().equalsIgnoreCase(Configurations.TXN_TYPE_DEBIT_PAYMENT)) {
                                    glaccountBean.setPaymentType(statusVarList.getCASH_PAYMENT());
                                }

                                /**insert to EODGL table*/
                                glSummaryFileRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getCardNo(), glaccountBean.getGlType(), glaccountBean.getAmount(), glaccountBean.getCrDr(), glaccountBean.getPaymentType());
                                glSummaryFileRepo.updateCashbackExpAndRedeem(glaccountBean.getId(), 1);

                                Configurations.PROCESS_SUCCESS_COUNT++;
                            } catch (Exception e) {
                                Configurations.PROCESS_FAILD_COUNT++;

                                logManager.logError("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), errorLoggerEFGE);
                                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
                            }

                            logManager.logDetails(accDetails, infoLoggerEFGE);
                            accDetails.clear();
                        }

                        accDetails.put("Data Retrieval Status for CashBackRedeem and Expire", "Passed");

                    } catch (Exception e) {

                        accDetails.put("Data Retrieval Status for CashBackRedeem and Expire", "Failed");
                    }
                    logManager.logDetails(accDetails, infoLoggerEFGE);
                    accDetails.clear();
                    list.clear();

                    /**Create Adjustment GL file*/
                    try {

                        list = glSummaryFileRepo.getAdjustmentDataToEODGL();
                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                        accDetails.put("Gl data for", "Adjustment");

                        for (GlAccountBean glaccountBean : list) {
                            try {
                                accDetails.put("Card Number", CommonMethods.cardNumberMask(glaccountBean.getCardNo()));
                                accDetails.put("GL type", glaccountBean.getGlType());
                                accDetails.put("CRDR", glaccountBean.getCrDr());
                                accDetails.put("Amount", glaccountBean.getAmount());
                                accDetails.put("ID", glaccountBean.getKey());

                                if (glaccountBean.getGlType().equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || glaccountBean.getGlType().equalsIgnoreCase(Configurations.TXN_TYPE_DEBIT_PAYMENT)) {
                                    glaccountBean.setPaymentType(statusVarList.getCASH_PAYMENT());
                                }

                                /**insert to EODGL table*/
                                glSummaryFileRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getCardNo(), glaccountBean.getGlType(), glaccountBean.getAmount(), glaccountBean.getCrDr(), glaccountBean.getPaymentType());
                                glSummaryFileRepo.updateAdjusment(glaccountBean.getKey(), 1);

                                Configurations.PROCESS_SUCCESS_COUNT++;
                            } catch (Exception e) {
                                Configurations.PROCESS_FAILD_COUNT++;

                                logManager.logError("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), errorLoggerEFGE);
                                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
                            }
                            logManager.logDetails(accDetails, infoLoggerEFGE);
                            accDetails.clear();
                        }
                        accDetails.put("Data Retrieval Status for Adjustment", "Passed");

                    } catch (Exception e) {

                        accDetails.put("Data Retrieval Status for Adjustment", "Failed");
                    }

                    logManager.logDetails(accDetails, infoLoggerEFGE);
                    accDetails.clear();
                    list.clear();

                    /**Create Fee GL file*/
                    try {

                        list = glSummaryFileRepo.getFeeDataToEODGL();
                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                        accDetails.put("Gl data for", "Fee Details");

                        for (GlAccountBean glaccountBean : list) {
                            try {
                                accDetails.put("Card Number", CommonMethods.cardNumberMask(glaccountBean.getCardNo()));
                                accDetails.put("GL type", glaccountBean.getGlType());
                                accDetails.put("CRDR", glaccountBean.getCrDr());
                                accDetails.put("Amount", glaccountBean.getAmount());
                                accDetails.put("ID", glaccountBean.getKey());

                                /**insert to EODGL table*/
                                glSummaryFileRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getCardNo(), glaccountBean.getGlType(), glaccountBean.getAmount(), glaccountBean.getCrDr(), glaccountBean.getPaymentType());
                                glSummaryFileRepo.updateFeeTable(glaccountBean.getKey(), 1);

                                Configurations.PROCESS_SUCCESS_COUNT++;
                            } catch (Exception e) {
                                Configurations.PROCESS_FAILD_COUNT++;

                                logManager.logError("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), errorLoggerEFGE);
                                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
                            }
                            logManager.logDetails(accDetails, infoLoggerEFGE);
                            accDetails.clear();
                        }

                        accDetails.put("Data Retrieval Status for Fee data", "Passed");

                    } catch (Exception e) {

                        accDetails.put("Data Retrieval Status for Fee data", "Failed");
                    }
                    logManager.logDetails(accDetails, infoLoggerEFGE);
                    accDetails.clear();
                    list.clear();

                    /**Create EODTXN table GL file*/
                    try {

                        list = glSummaryFileRepo.getEODTxnDataToGL();
                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                        accDetails.put("Gl data for", "EOD Transaction");

                        for (GlAccountBean glaccountBean : list) {
                            try {

                                accDetails.put("Card Number", CommonMethods.cardNumberMask(glaccountBean.getCardNo()));
                                accDetails.put("GL type", glaccountBean.getGlType());
                                accDetails.put("CRDR", glaccountBean.getCrDr());
                                accDetails.put("Amount", glaccountBean.getAmount());
                                accDetails.put("ID", glaccountBean.getKey());

                                /**Insert to EODGL table*/
                                glSummaryFileRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getCardNo(), glaccountBean.getGlType(), glaccountBean.getAmount(), glaccountBean.getCrDr(), glaccountBean.getPaymentType());
                                glSummaryFileRepo.updateEODTxn(glaccountBean.getKey(), 1);

                                Configurations.PROCESS_SUCCESS_COUNT++;
                            } catch (Exception e) {
                                Configurations.PROCESS_FAILD_COUNT++;

                                logManager.logError("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), errorLoggerEFGE);
                                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
                            }
                            logManager.logDetails(accDetails, infoLoggerEFGE);
                            accDetails.clear();
                        }
                        accDetails.put("Data Retrieval Status for EOD Transaction Data", "Passed");
                    } catch (Exception e) {

                        accDetails.put("Data Retrieval Status for EOD Transaction Data", "Failed");
                    }
                    logManager.logDetails(accDetails, infoLoggerEFGE);
                    accDetails.clear();
                    list.clear();

                    fileGenerationModel = new FileGenerationModel();

                    fileExtension = ".txt";
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy");
                    String year = sd.format(new Date());
                    String today1 = String.valueOf(year) + Integer.toString(Configurations.EOD_ID).substring(2, 6);

                    String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
                    int seq = Integer.parseInt(eodSeq) + 1;
                    String sequence = String.format("%03d", seq);

                    fileName = Configurations.GL_SUMMARY_FILE_PREFIX + today1 + "." + sequence;

                    /**create directories if not exists*/
                    String backUpFile = Configurations.GLFILE_FILE_PATH + backUpName;
                    fileGenerationService.createDirectoriesForFileAndBackUpFile(Configurations.GLFILE_FILE_PATH, backUpFile);

                    /**create file path*/
                    filePath = Configurations.GLFILE_FILE_PATH + fileName + fileExtension;
                    backUpFilePath = Configurations.GLFILE_FILE_PATH + backUpName + fileName + fileExtension;


                    fileGenerationModel = glSummaryFileService.createGLFile(fileName, filePath, backUpFilePath, processBean);
                    if (fileGenerationModel.getStatus()) {
                        for (int key : fileGenerationModel.getTxnIdList()) {
                            count = glSummaryFileRepo.updateEodGLAccount(key);
                        }
                        if (count == fileGenerationModel.getTxnIdList().size()) {
                            logManager.logInfo("GL File Process Successfully Completed ", infoLoggerEFGE);
                        }

                        if (!fileGenerationModel.getDeleteStatus()) {
                            /**insert file details into the eodoutputfile table ...*/
                            EodOuputFileBean eodoutputfilebean = new EodOuputFileBean();
                            eodoutputfilebean.setFileName(fileName + fileExtension);
                            eodoutputfilebean.setNoOfRecords(noofRecords);

                            commonRepo.insertOutputFiles(eodoutputfilebean, "GL");
                        }
                    }

                } catch (Exception e) {
                    try {
                        logManager.logInfo("GL File Process Failed ", infoLoggerEFGE);

                        if (processBean.getCriticalStatus() == 1) {
                            Configurations.COMMIT_STATUS = false;
                            Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                            Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                            Configurations.MAIN_EOD_STATUS = false;
                        }
                    } catch (Exception e2) {
                        logManager.logError("Exception in GL File Process", e2, errorLoggerEFGE);
                    }
                } finally {
                    if (list != null) {
                        for (GlAccountBean glaccountBean : list) {
                            CommonMethods.clearStringBuffer(glaccountBean.getCardNo());
                        }
                        list.clear();
                    }
                }
            }
        } catch (Exception e) {
            logManager.logError("Exception in GL File Process", e, errorLoggerEFGE);
        } finally {
            logManager.logSummery(summery, infoLoggerEFGE);
        }
    }

    @Override
    public void addSummaries() {

    }
}
