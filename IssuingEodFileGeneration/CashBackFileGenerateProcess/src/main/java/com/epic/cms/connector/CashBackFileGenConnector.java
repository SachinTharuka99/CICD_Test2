/**
 * Author : lahiru_p
 * Date : 11/29/2022
 * Time : 11:35 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.EodOuputFileBean;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CashBackFileGenRepo;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.service.CashBackFileGenService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.epic.cms.util.CommonMethods.validate;
import static com.epic.cms.util.LogManager.*;

@Service
public class CashBackFileGenConnector extends FileGenProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;

    @Autowired
    CashBackFileGenRepo cashBackFileGenRepo;

    @Autowired
    CashBackFileGenService cashBackFileGenService;

    String filePathF1 = null, filePathF2 = null;
    int recordCount = 0;
    BigDecimal totalAmountBig = BigDecimal.valueOf(0.0);

    @Override
    public void concreteProcess() throws Exception {
        String processHeader = "CASHBACK FILE GENERATION";
        String fileNameF1 = null, fileNameF2 = null;
        String backUpFilePathF1 = null, backUpFilePathF2 = null;
        ArrayList<GlAccountBean> cashBackList = null;
        int count;
        boolean toDeleteStatus = true;
        ArrayList<Integer> txnId = new ArrayList<Integer>();

        fileGenerationModel = new FileGenerationModel();
        FileGenerationModel fileGenerationModel2 = new FileGenerationModel();
        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION);

            if (processBean != null) {
                logManager.logHeader(processHeader, infoLoggerEFGE);

                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION;
                CommonMethods.eodDashboardProgressParametersReset();

                try {
                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
                    SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy MMM");
                    SimpleDateFormat sdf6 = new SimpleDateFormat("yyMMM");
                    SimpleDateFormat sdf7 = new SimpleDateFormat("MMM");

                    String debitAccount;
                    int noofBatches = 0;
                    Date nextWorkingDay = commonFileGenProcessRepo.getNextWorkingDay(new Date());
                    String today3 = sdf4.format(nextWorkingDay);
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy");
                    String year = sd.format(new Date());
                    String today1 = year + Integer.toString(Configurations.EOD_ID).substring(2, 6);
                    String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
                    int seq = Integer.parseInt(eodSeq) + 1;
                    String sequence = String.format("%03d", seq);

                    fileExtension = ".csv";

                    //first file name
                    fileNameF1 = Configurations.CASHBACK_FILE_PREFIX_F1 + today1 + "." + sequence;
                    //second file name
                    fileNameF2 = Configurations.CASHBACK_FILE_PREFIX_F2 + today1 + "." + sequence;

                    //create root directories if not exists
                    String backUpFile = Configurations.CASHBACK_FILE_PATH + backUpName;
                    fileGenerationService.createDirectoriesForFileAndBackUpFile(Configurations.CASHBACK_FILE_PATH, backUpFile);

                    ////////////////////Create File Path///////////////////////
                    //first file
                    filePathF1 = Configurations.CASHBACK_FILE_PATH + fileNameF1 + fileExtension;
                    backUpFilePathF1 = Configurations.CASHBACK_FILE_PATH + backUpName + fileNameF1 + fileExtension;

                    //second file
                    filePathF2 = Configurations.CASHBACK_FILE_PATH + fileNameF2 + fileExtension;
                    backUpFilePathF2 = Configurations.CASHBACK_FILE_PATH + backUpName + fileNameF2 + fileExtension;

                    StringBuilder sbHeader = new StringBuilder();
                    StringBuilder content = new StringBuilder();

                    StringBuilder sbHeader2 = new StringBuilder();
                    StringBuilder content2 = new StringBuilder();

                    //Select * from cashbackredeem exp table
                    cashBackList = cashBackFileGenRepo.getCahsBackRedeemList();
                    debitAccount = cashBackFileGenRepo.getCashBackDebitAccount();

                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cashBackList.size();

                    if (debitAccount != null) {
                        BigDecimal headerCreditBig = new BigDecimal("0.0");
                        int headerCreditCount = 0;

                        for (GlAccountBean glAccountBean : cashBackList) {
                            txnId.add(glAccountBean.getId());
                            BigDecimal cashBackRedeem = new BigDecimal(glAccountBean.getGlAmount());

                            if (cashBackRedeem.compareTo(BigDecimal.ZERO) > 0 && glAccountBean.getAccNo() != null) {
                                recordCount++;

                                totalAmountBig = totalAmountBig.add(cashBackRedeem);
                                noofBatches++;

                                headerCreditBig = headerCreditBig.add(cashBackRedeem);
                                headerCreditCount++;

                                String seqNo = "99" + Integer.toString(Configurations.EOD_ID) + validate(Integer.toString(noofBatches), 6, '0');

                                content = cashBackFileGenService.addFirstFileContent(glAccountBean, cashBackRedeem, seqNo, today3, sdf5, nextWorkingDay);

                                content2 = cashBackFileGenService.addSecondFileContent(glAccountBean, debitAccount, cashBackRedeem, sdf6, sdf7, recordCount);
                            }
                            Configurations.PROCESS_SUCCESS_COUNT++;
                        }

                        if (headerCreditBig.compareTo(BigDecimal.ZERO) > 0 || headerCreditCount > 0) {
                            sbHeader = cashBackFileGenService.addFirstFileHeader(debitAccount, fileNameF1, sdf5, today3, headerCreditBig, headerCreditCount);

                            sbHeader2 = cashBackFileGenService.addSecondFileHeader(today3, sdf6);


                            fileGenerationModel.setFinalFile(sbHeader.append(content));
                            fileGenerationModel2.setFinalFile(sbHeader2.append(content2));

                            //write first file
                            fileGenerationService.generateFile(fileGenerationModel.getFinalFile().toString(), filePathF1, backUpFilePathF1);

                            //write second file
                            fileGenerationService.generateFile(fileGenerationModel2.getFinalFile().toString(), filePathF2, backUpFilePathF2);

                            toDeleteStatus = false;
                        } else {
                            logManager.logInfo("Empty line in body. Hence No header section.", infoLoggerEFGE);
                            logManager.logError("Empty line in body. Hence No header section.", errorLoggerEFGE);
                        }

                        EodOuputFileBean eodoutputfilebean2 = new EodOuputFileBean();
                        eodoutputfilebean2.setFileName(fileNameF2 + ".csv");
                        eodoutputfilebean2.setNoOfRecords(recordCount);

                        //insert second file to EODOUTPUTFIELS table
                        commonRepo.insertOutputFiles(eodoutputfilebean2, "CASHBACK");

                        for (int key : txnId) {
                            cashBackFileGenRepo.updateCashBackRedeemExp(key);
                        }
                    }
                } catch (Exception e) {
                    logManager.logError("Error while writing Cash back file--->", e, errorLoggerEFGE);
                } finally {
                    if (toDeleteStatus) {
                        //delete first file
                        fileGenerationService.deleteExistFile(filePathF1);

                        //delete second file
                        fileGenerationService.deleteExistFile(filePathF2);
                    }
                }

                Configurations.PROCESS_FAILD_COUNT = (Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - Configurations.PROCESS_SUCCESS_COUNT);
                if (Configurations.PROCESS_FAILD_COUNT == 0) {
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getSUCCES_STATUS(), Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                } else {
                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getERROR_STATUS(), Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                }
            }
        } catch (Exception e) {
            try {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                logManager.logError(processHeader + " process failed", e, errorLoggerEFGE);
                commonRepo.updateEodProcessSummery(Configurations.EOD_ID, statusVarList.getERROR_STATUS(), Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));

                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }
            } catch (Exception ex) {
                logManager.logError("Exception", ex, errorLoggerEFGE);
            }
        } finally {
            logManager.logSummery(summery, infoLoggerEFGE);
            if (cashBackList != null && cashBackList.size() != 0) {
                //nullify cashBackList
                for (GlAccountBean glAccountBean : cashBackList) {
                    CommonMethods.clearStringBuffer(glAccountBean.getCardNo());
                }
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("First File name : ", filePathF1);
        summery.put("Second File name : ", filePathF2);
        summery.put("No Of Records  : ", recordCount);
        summery.put("Hash Total  : ", totalAmountBig.toString());
    }
}
