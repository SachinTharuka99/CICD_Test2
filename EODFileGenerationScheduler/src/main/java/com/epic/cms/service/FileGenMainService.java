/**
 * Author : lahiru_p
 * Date : 1/24/2023
 * Time : 2:01 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.connector.*;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.EODFileGenEngineProducerRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;


@Service
public class FileGenMainService {

    @Autowired
    LogManager logManager;

    @Autowired
    EODFileGenEngineProducerRepo producerRepo;

    @Autowired
    ProcessThreadService processThreadService;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    ExposureFileConnector exposureFileConnector;

    @Autowired
    CardApplicationConfirmationLetterConnector cardApplicationConfirmationLetterConnector;

    @Autowired
    AutoSettlementConnector autoSettlementConnector;

    @Autowired
    CardApplicationRejectLetterConnector cardApplicationRejectLetterConnector;

    @Autowired
    CardRenewLetterConnector cardRenewLetterConnector;

    @Autowired
    CardReplaceLetterConnector cardReplaceLetterConnector;

    @Autowired
    CashBackFileGenConnector cashBackFileGenConnector;

    @Autowired
    CollectionAndRecoveryLetterConnector collectionAndRecoveryLetterConnector;

    @Autowired
    GLSummaryFileConnector glSummaryFileConnector;

    @Autowired
    RB36FileGenerationConnector rb36FileGenerationConnector;

    @Autowired
    CreateEodId createEodId;

    public void startEODFileGenEngine(int categoryId, int issuingOrAcquiring, int eodId) throws Exception {
        System.out.println("Main Method Started");
        try {
            //get EOD-ID
            Configurations.EOD_ID = eodId;
            Configurations.ERROR_EOD_ID = Configurations.EOD_ID;

            Configurations.EOD_DATE = createEodId.getDateFromEODID(Configurations.EOD_ID);

            logManager.logInfo("EOD-File-Generation Engine main service started for EOD-ID:" + Configurations.EOD_ID, infoLogger);
            List<ProcessBean> processList = new ArrayList<ProcessBean>();

            //update file generation eod status to In Progress
            producerRepo.updateEodFileGenStatus(Configurations.EOD_ID, statusVarList.getINPROGRESS_STATUS());

            String uniqueId = generateUniqueId();
            //get process list by process category id , issuing or acquiring and file-gen category

            processList = producerRepo.getProcessListByFileGenCategoryId(categoryId, issuingOrAcquiring);//get process list for this step

            this.EODScheduler(processList, uniqueId);

        } catch (Exception e) {
            logManager.logError("EOD File Generation Process Failed ", e, errorLogger);
            //update file generation eod status to In Progress
            producerRepo.updateEodFileGenStatus(Configurations.EOD_ID, statusVarList.getERROR_STATUS());
        }
    }

    private void EODScheduler(List<ProcessBean> processList, String uniqueId) throws Exception {
        String includedProcess = "";
        try {
            System.out.println("Start EOD File Generation Processes");
            for (ProcessBean process : processList) {
                includedProcess = includedProcess + process.getProcessId() + " , ";
            }
            producerRepo.insertToEODProcessCount(uniqueId, processList.size(), includedProcess);

            loadProcessConnectorList();
            for (ProcessBean processBean : processList) {
                processThreadService.startProcessByProcessId(processBean.getProcessId(), uniqueId);
            }
            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                updateEodFileGenDashboardProcessProgress(uniqueId);
                if (producerRepo.getCompletedProcessCount(uniqueId) == processList.size()) {
                    System.out.println("############# Process step completed");
                    //update file gen status
                }
                Thread.sleep(1000);
            }

            String eodFileGenStatus = "";
            //Here the process flow complete status becomes false together with
            //the step flow status if they are successfully completed.
            //if (Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS && Configurations.FLOW_STEP_COMPLETE_STATUS == false) {
            eodFileGenStatus = producerRepo.hasErrorforLastEOD() ? statusVarList.getERROR_STATUS() : statusVarList.getSUCCES_STATUS();

            /**
             * check if EOD is in error state.
             */
            producerRepo.updateEodFileGenStatus(Configurations.EOD_ID, eodFileGenStatus);
            producerRepo.clearEodProcessCountTable();


            System.out.println("EOD File Generation process completed..");
        } catch (Exception e) {
            throw e;
        }
    }

    private void updateEodFileGenDashboardProcessProgress(String uniqueId) throws Exception {
        String processIdList = "";
        processIdList = producerRepo.getProcessIdByUniqueId(uniqueId);

        String str[] = processIdList.split(" ,");

        int progress = 0;
        List<String> errorProcessList;

        for (String s : str) {
            if (!s.trim().equals("")) {
                int processID = Integer.parseInt(s);
                try {
                    if (processID == Configurations.AUTO_SETTLEMENT_PROCESS) {
                        if (Configurations.AUTO_SETTLEMENT_PROCESS_SUCCESS_COUNT != 0 && Configurations.AUTO_SETTLEMENT_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.AUTO_SETTLEMENT_PROCESS_SUCCESS_COUNT * 100 / Configurations.AUTO_SETTLEMENT_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.AUTO_SETTLEMENT_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.AUTO_SETTLEMENT_PROCESS_SUCCESS_COUNT == 0 && Configurations.AUTO_SETTLEMENT_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.AUTO_SETTLEMENT_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.AUTO_SETTLEMENT_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.AUTO_SETTLEMENT_PROCESS_SUCCESS_COUNT,
                                (Configurations.AUTO_SETTLEMENT_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.AUTO_SETTLEMENT_PROCESS_SUCCESS_COUNT),
                                Configurations.AUTO_SETTLEMENT_PROCESS_PROGRESS,
                                Configurations.AUTO_SETTLEMENT_PROCESS);
                    }
                    if (processID == Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE) {
                        if (Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_SUCCESS_COUNT != 0 && Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_SUCCESS_COUNT * 100 / Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_SUCCESS_COUNT == 0 && Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_SUCCESS_COUNT,
                                (Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_SUCCESS_COUNT),
                                Configurations.CARD_APP_CONFIRM_LETTER_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE);
                    }
                    if (processID == Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT) {
                        if (Configurations.CARD_APP_REJECT_LETTER_PROCESS_SUCCESS_COUNT != 0 && Configurations.CARD_APP_REJECT_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.CARD_APP_REJECT_LETTER_PROCESS_SUCCESS_COUNT * 100 / Configurations.CARD_APP_REJECT_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.CARD_APP_REJECT_LETTER_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.CARD_APP_REJECT_LETTER_PROCESS_SUCCESS_COUNT == 0 && Configurations.CARD_APP_REJECT_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.CARD_APP_REJECT_LETTER_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.CARD_APP_REJECT_LETTER_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.CARD_APP_REJECT_LETTER_PROCESS_SUCCESS_COUNT,
                                (Configurations.CARD_APP_REJECT_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.CARD_APP_REJECT_LETTER_PROCESS_SUCCESS_COUNT),
                                Configurations.CARD_APP_REJECT_LETTER_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
                    }
                    if (processID == Configurations.PROCESS_ID_CARDRENEW_LETTER) {
                        if (Configurations.CARD_RENEW_LETTER_PROCESS_SUCCESS_COUNT != 0 && Configurations.CARD_RENEW_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.CARD_RENEW_LETTER_PROCESS_SUCCESS_COUNT * 100 / Configurations.CARD_RENEW_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.CARD_RENEW_LETTER_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.CARD_RENEW_LETTER_PROCESS_SUCCESS_COUNT == 0 && Configurations.CARD_RENEW_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.CARD_RENEW_LETTER_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.CARD_RENEW_LETTER_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.CARD_RENEW_LETTER_PROCESS_SUCCESS_COUNT,
                                (Configurations.CARD_RENEW_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.CARD_RENEW_LETTER_PROCESS_SUCCESS_COUNT),
                                Configurations.CARD_RENEW_LETTER_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_CARDRENEW_LETTER);
                    }
                    if (processID == Configurations.PROCESS_ID_CARDREPLACE_LETTER) {
                        if (Configurations.CARDREPLACE_LETTER_PROCESS_SUCCESS_COUNT != 0 && Configurations.CARDREPLACE_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.CARDREPLACE_LETTER_PROCESS_SUCCESS_COUNT * 100 / Configurations.CARDREPLACE_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.CARDREPLACE_LETTER_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.CARDREPLACE_LETTER_PROCESS_SUCCESS_COUNT == 0 && Configurations.CARDREPLACE_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.CARDREPLACE_LETTER_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.CARDREPLACE_LETTER_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.CARDREPLACE_LETTER_PROCESS_SUCCESS_COUNT,
                                (Configurations.CARDREPLACE_LETTER_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.CARDREPLACE_LETTER_PROCESS_SUCCESS_COUNT),
                                Configurations.CARDREPLACE_LETTER_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_CARDREPLACE_LETTER);
                    }
                    if (processID == Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION) {
                        if (Configurations.CASH_BACK_FILE_PROCESS_SUCCESS_COUNT != 0 && Configurations.CASH_BACK_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.CASH_BACK_FILE_PROCESS_SUCCESS_COUNT * 100 / Configurations.CASH_BACK_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.CASH_BACK_FILE_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.CASH_BACK_FILE_PROCESS_SUCCESS_COUNT == 0 && Configurations.CASH_BACK_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.CASH_BACK_FILE_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.CASH_BACK_FILE_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.CASH_BACK_FILE_PROCESS_SUCCESS_COUNT,
                                (Configurations.CASH_BACK_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.CASH_BACK_FILE_PROCESS_SUCCESS_COUNT),
                                Configurations.CASH_BACK_FILE_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION);
                    }
                    if (processID == Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS) {
                        if (Configurations.COLLECTION_AND_RECOVERY_PROCESS_SUCCESS_COUNT != 0 && Configurations.COLLECTION_AND_RECOVERY_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.COLLECTION_AND_RECOVERY_PROCESS_SUCCESS_COUNT * 100 / Configurations.COLLECTION_AND_RECOVERY_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.COLLECTION_AND_RECOVERY_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.COLLECTION_AND_RECOVERY_PROCESS_SUCCESS_COUNT == 0 && Configurations.COLLECTION_AND_RECOVERY_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.COLLECTION_AND_RECOVERY_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.COLLECTION_AND_RECOVERY_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.COLLECTION_AND_RECOVERY_PROCESS_SUCCESS_COUNT,
                                (Configurations.COLLECTION_AND_RECOVERY_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.COLLECTION_AND_RECOVERY_PROCESS_SUCCESS_COUNT),
                                Configurations.COLLECTION_AND_RECOVERY_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS);
                    }
                    if (processID == Configurations.PROCESS_EXPOSURE_FILE) {
                        if (Configurations.EXPOSURE_FILE_PROCESS_SUCCESS_COUNT != 0 && Configurations.EXPOSURE_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.EXPOSURE_FILE_PROCESS_SUCCESS_COUNT * 100 / Configurations.EXPOSURE_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.EXPOSURE_FILE_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.EXPOSURE_FILE_PROCESS_SUCCESS_COUNT == 0 && Configurations.EXPOSURE_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.EXPOSURE_FILE_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.EXPOSURE_FILE_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.EXPOSURE_FILE_PROCESS_SUCCESS_COUNT,
                                (Configurations.EXPOSURE_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.EXPOSURE_FILE_PROCESS_SUCCESS_COUNT),
                                Configurations.EXPOSURE_FILE_PROCESS_PROGRESS,
                                Configurations.PROCESS_EXPOSURE_FILE);
                    }
                    if (processID == Configurations.PROCESS_ID_GL_FILE_CREATION) {
                        if (Configurations.GL_SUMMARY_FILE_PROCESS_SUCCESS_COUNT != 0 && Configurations.GL_SUMMARY_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.GL_SUMMARY_FILE_PROCESS_SUCCESS_COUNT * 100 / Configurations.GL_SUMMARY_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.GL_SUMMARY_FILE_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.GL_SUMMARY_FILE_PROCESS_SUCCESS_COUNT == 0 && Configurations.GL_SUMMARY_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.GL_SUMMARY_FILE_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.GL_SUMMARY_FILE_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.GL_SUMMARY_FILE_PROCESS_SUCCESS_COUNT,
                                (Configurations.GL_SUMMARY_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.GL_SUMMARY_FILE_PROCESS_SUCCESS_COUNT),
                                Configurations.GL_SUMMARY_FILE_PROCESS_PROGRESS,
                                Configurations.PROCESS_ID_GL_FILE_CREATION);
                    }
                    if (processID == Configurations.PROCESS_RB36_FILE_CREATION) {
                        if (Configurations.RB36_FILE_PROCESS_SUCCESS_COUNT != 0 && Configurations.RB36_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            progress = ((Configurations.RB36_FILE_PROCESS_SUCCESS_COUNT * 100 / Configurations.RB36_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS));

                            Configurations.RB36_FILE_PROCESS_PROGRESS = String.valueOf(progress) + "%";

                        } else if (Configurations.RB36_FILE_PROCESS_SUCCESS_COUNT == 0 && Configurations.RB36_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS != 0) {
                            Configurations.RB36_FILE_PROCESS_PROGRESS = "0%";
                        } else {
                            Configurations.RB36_FILE_PROCESS_PROGRESS = "100%";
                        }
                        //update Dashboard Process Progress
                        producerRepo.updateEodProcessProgress(Configurations.RB36_FILE_PROCESS_SUCCESS_COUNT,
                                (Configurations.RB36_FILE_PROCESS_TOTAL_NO_OF_TRANSACTIONS - Configurations.RB36_FILE_PROCESS_SUCCESS_COUNT),
                                Configurations.RB36_FILE_PROCESS_PROGRESS,
                                Configurations.PROCESS_RB36_FILE_CREATION);
                    }

                    errorProcessList = producerRepo.getErrorProcessIdList();
                    if (errorProcessList != null) {
                        for (String processId : errorProcessList) {
                            producerRepo.updateProcessProgressForErrorProcess(processId);
                        }
                    }
                    // update success process count | error process count in eod table
                    producerRepo.updateEodProcessStateCount();

                } catch (Exception e) {
                    logManager.logError(e, errorLogger);
                }
            }
        }
    }

    public void loadProcessConnectorList() {
        try {
            HashMap<Integer, Object> connectorList = new HashMap<>();
            connectorList.put(Configurations.AUTO_SETTLEMENT_PROCESS, autoSettlementConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE, cardApplicationConfirmationLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT, cardApplicationRejectLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDRENEW_LETTER, cardRenewLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDREPLACE_LETTER, cardReplaceLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION, cashBackFileGenConnector);
            connectorList.put(Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS, collectionAndRecoveryLetterConnector);
            connectorList.put(Configurations.PROCESS_EXPOSURE_FILE, exposureFileConnector);
            connectorList.put(Configurations.PROCESS_ID_GL_FILE_CREATION, glSummaryFileConnector);
            connectorList.put(Configurations.PROCESS_RB36_FILE_CREATION, rb36FileGenerationConnector);
            Configurations.processConnectorList = connectorList;

        } catch (Exception e) {
            throw e;
        }
    }

    private String generateUniqueId() {
        String uniqueId = null;
        try {
            uniqueId = Long.toString(System.currentTimeMillis()) + Math.round(Math.random() * 10) + Math.round(Math.random() * 10);
            System.out.println("@@@@@@@@@@@@ " + uniqueId);
        } catch (Exception e) {
            throw e;
        }
        return uniqueId;
    }
}
