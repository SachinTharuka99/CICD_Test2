/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:36 PM
 * Project Name : eod-engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.EODEngineProducerRepo;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODEngineMainService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    @Autowired
    @Qualifier("EODEngineProducerRepo")
    EODEngineProducerRepo producerRepo;
    @Autowired
    LogManager logManager;
    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    CreateEodId createEodId;

    //    @Async
    public void startEodEngine(String eodID) throws Exception {
        List<ProcessBean> processList = new ArrayList<>();
        try {
            Configurations.EOD_ID = Integer.parseInt(eodID);
            Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
            //Configurations.Str_EOD_ID = eodID;

            String uniqueId = generateUniqueId();//generate an unique id
            Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = true;
            Configurations.PROCESS_COMPLETE_STATUS = true;

            processList = producerRepo.getProcessListByModule(Configurations.EOD_ENGINE);//load eod engine process list

            if (processList.size() > 0) {
                logInfo.info(logManager.processStartEndStyle("EOD Engine started for EOD ID:" + eodID));

                //update eod status to inprogress & reset step id
                producerRepo.updateEodStatus(Configurations.ERROR_EOD_ID, statusVarList.getINPROGRESS_STATUS());

                kafkaMessageUpdator.producerWithNoReturn(eodID, "loadEodInfo");//reset dashboard eod id

                this.startEODEngineScheduler(processList, uniqueId);//start eod engine scheduler

                while (Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS) {
                    if (producerRepo.getCompletedProcessCount(uniqueId) == processList.size()) {
                        Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                        kafkaMessageUpdator.producerWithNoReturn("true", "processStatus");
                        logManager.logDashboardInfo("Process step completed");
                        System.out.println("############# Process step completed");
                    }
                    Thread.sleep(1000);
                }

                //Here the process flow complete status becomes false together with
                //the step flow status if they are successfully completed.
                //if (Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS && Configurations.FLOW_STEP_COMPLETE_STATUS == false) {
                String eodEngineStatus = producerRepo.hasErrorforLastEOD() ? statusVarList.getERROR_STATUS() : statusVarList.getSUCCES_STATUS();

                /**
                 * check if EOD is in error state.
                 */
                producerRepo.updateEodEndStatus(Configurations.ERROR_EOD_ID, eodEngineStatus);
                if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                    updatePreviousFailProcesses(Integer.toString(Configurations.ERROR_EOD_ID));
                }
                producerRepo.clearEodProcessCountTable();
                System.out.println("EOD is going to be ended.");

                //generate next eod id based on the eod status and insert into eod table
                boolean isEODInserted = createEodId.insertValuesToEODTable();

                if (isEODInserted) {
                    int eodId = createEodId.getCurrentEodId(statusVarList.getINITIAL_STATUS(), statusVarList.getERROR_STATUS());

                    //Configurations.EOD_ID is also set.
                    System.out.println("next EOD ID: " + eodId);
                    logManager.logDashboardInfo("Next EOD ID: "+eodId);
                    //System.out.println("next EOD date simulation: " + locDate);
                    //System.out.println("Scheduler Status: " + Configurations.EOD_SHEDULER);
                    /**
                     * Clear all Summary data variables.
                     */
                    clearAllStatusSummaries();

                    //send eod engine complete message to File Gen Engine
                    if (eodEngineStatus.equals(statusVarList.getSUCCES_STATUS())) {
                        kafkaMessageUpdator.producerWithNoReturn(eodID, "eodEngineStatus");
                    }
                }
            } else {
                throw new EODEngineStartFailException("Cannot be started. There is no any active process to run");
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            logInfo.info(logManager.processStartEndStyle("EOD Engine completed for EOD ID:" + eodID));
        }

    }

    private void clearAllStatusSummaries() {
        Statusts.SUMMARY_FOR_CARDREPLACE = 0;
        Statusts.SUMMARY_FOR_CARDREPLACE_PROCESSED = 0;
        Statusts.SUMMARY_FOR_CARDRRENEW = 0;
        Statusts.SUMMARY_FOR_CARDRRENEW_PROCESSED = 0;
        Statusts.SUMMARY_FOR_CARDS_MINAMOUNT_PAID = 0;
        Statusts.SUMMARY_FOR_CARDS_ON_DUEDATE = 0;
        Statusts.SUMMARY_FOR_CHEQUE_PAYMENTS = 0;
        Statusts.SUMMARY_FOR_FEE_ANNIVERSARY = 0;
        Statusts.SUMMARY_FOR_FEE_ANNIVERSARY_PROCESSED = 0;
        Statusts.SUMMARY_FOR_FEE_CASHADVANCES = 0;
        Statusts.SUMMARY_FOR_FEE_LATEPAYMENTS = 0;
        Statusts.SUMMARY_FOR_FEE_UPDATE = 0;
        Statusts.SUMMARY_FOR_MINPAYMENT_RISK_ADDED = 0;
        Statusts.SUMMARY_FOR_MINPAYMENT_RISK_REMOVED = 0;
        Statusts.SUMMARY_FOR_PAYMENTS = 0;
        Statusts.SUMMARY_FOR_PAYMENTS_PROCESSED = 0;
        Statusts.SUMMARY_FOR_TRANSACTIONS = 0;
        Statusts.SUMMARY_FOR_TRANSACTIONS_PROCESSED = 0;
        Statusts.SUMMARY_FOR_VISA_POSTING = 0;
    }

    public void updatePreviousFailProcesses(String currentEodID) {
        try {
            int result = producerRepo.updatePreviousEODErrorCardDetails(currentEodID);
            result = producerRepo.updatePreviousEODErrorMerchantDetails(currentEodID);
        } catch (Exception e) {

        }
    }

    public void startEODEngineScheduler(List<ProcessBean> processList, String uniqueId)
            throws Exception {
        try {
            String includedProcess = processList.stream()
                    .map(ProcessBean::getProcessId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(" , "));

            producerRepo.insertToEODProcessCount(uniqueId, processList.size(), includedProcess);
//            System.out.println("------------->>>>>>>>>> EOD-Engine scheduler Thread ID: " + Thread.currentThread().getId());
            for (ProcessBean process : processList) {
                if (!Configurations.EOD_ENGINE_SOFT_STOP) {//check whether initiated soft stop request
                    Configurations.PROCESS_COMPLETE_STATUS = false;
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = false;
                    Configurations.PROCESS_STEP_ID = process.getStepId();

//                    System.out.println("------------->>>>>>>>>> EODScheduler inside for loop Thread ID: " + Thread.currentThread().getId());
                    boolean future = kafkaMessageUpdator.producerWithReturn(uniqueId,
                            process.getKafkaTopic());

                    while (true) {//wait until process finished
                        //Check whether msg push to consumer service is complete & the process complete from their end
                        if (future && Configurations.PROCESS_COMPLETE_STATUS && !Configurations.IS_PROCESS_COMPLETELY_FAILED) {
                            logInfo.info(logManager.processStartEndStyle(process.getProcessDes() + " successfully completed."));
                            break;
                        } else if (future && Configurations.PROCESS_COMPLETE_STATUS && Configurations.IS_PROCESS_COMPLETELY_FAILED) {
                            logInfo.info("EOD-Engine completely failed. Process Name: " + process.getProcessDes());
                            //update EOD status to FAIL & fail step ID
                            producerRepo.updateEodStatus(Configurations.ERROR_EOD_ID, "FAIL", process.getStepId());
                            throw new EODEngineCompletelyFailedException(process.getProcessDes() + " completely failed.");
                        }
                        Thread.sleep(1000);
                    }
                    updateEodEngineDashboardProcessProgress();//update process progress
                } else {
                    logInfo.info("EOD-Engine hold. Process Name: " + process.getProcessDes());
                    //update EOD status to HOLD & next step ID
                    producerRepo.updateEodStatus(Configurations.ERROR_EOD_ID, "HOLD", process.getStepId());
                    //set next step ID
                    throw new EODEngineHoldException("EOD Engine going to be hold.");
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    private String generateUniqueId() throws Exception {
        String uniqueId = null;
        try {
            uniqueId = Long.toString(System.currentTimeMillis()) + Math.round(Math.random() * 10) + Math.round(Math.random() * 10);
            System.out.println("@@@@@@@@@@@@ " + uniqueId);
        } catch (Exception e) {
            throw e;
        }
        return uniqueId;
    }

    public void updateEodEngineDashboardProcessProgress() throws Exception {
        int progress = 0;
        List<String> errorProcessList;

        try {
            if (Configurations.successCount.size() != 0 && Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS != 0) {
                progress = ((Configurations.successCount.size() * 100 / Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));

                Configurations.PROCESS_PROGRESS = progress + "%";

            } else if (Configurations.successCount.size() == 0 && Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS != 0) {
                Configurations.PROCESS_PROGRESS = "0%";
            } else {
                Configurations.PROCESS_PROGRESS = "100%";
            }

            producerRepo.updateEodProcessProgress();
            errorProcessList = producerRepo.getErrorProcessIdList();
            if (errorProcessList != null) {
                for (String processId : errorProcessList) {
                    producerRepo.updateProcessProgressForErrorProcess(processId);
                }
            }
            // update success process count | error process count in eod table
            producerRepo.updateEodProcessStateCount();

            //Thread.sleep(5000); // After every 5 seconds update particular process pogress.

        } catch (Exception e) {
            logError.error("Update Eod Engine Dashboard Process Progress Error", e);
        }
    }
}
