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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODEngineMainService {


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

    public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    //@Async
    public synchronized void EODEngineMain(String eodID, int categoryId) throws InterruptedException {
        System.out.println("Main Method Started");
        try {
            logManager.logStartEnd("EOD-Engine main service started for EODID:" + eodID, infoLogger);
            Configurations.EOD_ID =Integer.parseInt(eodID);
            Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
            System.out.println("EOD ID :"+Configurations.ERROR_EOD_ID);
            //update eodid to inprogress
            producerRepo.updateEodStatus(Configurations.ERROR_EOD_ID, statusVarList.getINPROGRESS_STATUS());

            List<ProcessBean> processList = new ArrayList<ProcessBean>();
            String uniqueId = generateUniqueId();
            Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = true;
            Configurations.PROCESS_COMPLETE_STATUS = true;

            processList = producerRepo.getProcessListByCategoryId(categoryId);//get process list for this step

            /**if (Configurations.STARTING_EOD_STATUS.equals("INIT")) {//NORMAL EOD

            } else if (Configurations.STARTING_EOD_STATUS.equals("EROR")) {//ERROR EOD
                updatePreviousFailProcesses(Integer.toString(Configurations.ERROR_EOD_ID));
            }*/
            System.out.println("------------->>>>>>>>>> EODEngineMain Thread ID: " + Thread.currentThread().getId());
            this.EODScheduler(processList, uniqueId);

            while (Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS) {
                if (producerRepo.getCompletedProcessCount(uniqueId) == processList.size()) {
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    kafkaMessageUpdator.producerWithNoReturn("true", "processStatus");
                    System.out.println("############# Process step completed");
                }
                Thread.sleep(1000);
            }

            String eodEngineStatus = "";
            //Here the process flow complete status becomes false together with
            //the step flow status if they are successfully completed.
            //if (Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS && Configurations.FLOW_STEP_COMPLETE_STATUS == false) {
                eodEngineStatus = producerRepo.hasErrorforLastEOD() ? statusVarList.getERROR_STATUS() : statusVarList.getSUCCES_STATUS();

            /**
             * check if EOD is in error state.
             */
            producerRepo.updateEodEndStatus(Configurations.ERROR_EOD_ID, eodEngineStatus);
            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                updatePreviousFailProcesses(Integer.toString(Configurations.ERROR_EOD_ID));
            }
            producerRepo.clearEodProcessCountTable();
            System.out.println("EOD is going to be ended.");

            //CreateEodId create = new CreateEodId();
            //Trying to insert a NEW EODID
            //Here if an error occurs, an error EOD id is created.
            boolean isEODInserted = createEodId.insertValuesToEODTable();

            if (isEODInserted) {
                int eodId = createEodId.getCurrentEodId(statusVarList.getINITIAL_STATUS(), statusVarList.getERROR_STATUS());

                //Configurations.EOD_ID is also set.
                System.out.println("next EOD ID: " + String.valueOf(eodId));
                //System.out.println("next EOD date simulation: " + locDate);
                //System.out.println("Scheduler Status: " + Configurations.EOD_SHEDULER);
                /**
                 * Clear all Summary data variables.
                 */
                clearAllStatusSummaries();

                //send eod engine complete message to File Gen Engine
                kafkaMessageUpdator.producerWithNoReturn(eodID, "eodEngineStatus");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logManager.logError(ex.toString(), ex, errorLogger);
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

    public void EODScheduler(List<ProcessBean> processList, String uniqueId)
            throws Exception {
        String includedProcess = "";
        try {
            for (ProcessBean process : processList) {
                includedProcess = includedProcess + process.getProcessId() + " , ";
            }
            producerRepo.insertToEODProcessCount(uniqueId, processList.size(), includedProcess);
            System.out.println("------------->>>>>>>>>> EODScheduler Thread ID: " + Thread.currentThread().getId());
            for (int j = 0; j < processList.size(); j++) {
                //check whether no soft stop request.
                if (!Configurations.EOD_ENGINE_SOFT_STOP) {
                    Configurations.PROCESS_COMPLETE_STATUS = false;
                    Configurations.IS_PROCESS_COMPLETELY_FAILED = false;
                    Configurations.PROCESS_STEP_ID = processList.get(j).getStepId();

                    System.out.println("------------->>>>>>>>>> EODScheduler inside for loop Thread ID: " + Thread.currentThread().getId());
                    boolean future = kafkaMessageUpdator.producerWithReturn(uniqueId,
                            processList.get(j).getKafkaTopic());

                    //wait until process finished.
                    while (true) {
                        //Check whether msg push to consumer service is complete & the process complete from their end.
                        if (future && Configurations.PROCESS_COMPLETE_STATUS && !Configurations.IS_PROCESS_COMPLETELY_FAILED) {
                            LogManager.processStartEndStyle(processList.get(j).getProcessDes() + " completed - ");
                            break;
                        } else if (future && Configurations.PROCESS_COMPLETE_STATUS && Configurations.IS_PROCESS_COMPLETELY_FAILED) {
                            throw new EODEngineCompletelyFailedException(processList.get(j).getProcessDes() + " Process completely failed.");
                        }

                        Thread.sleep(1000);
                    }
                    updateEodEngineDashboardProcessProgress();
                } else {
                    throw new EODEngineHoldException("EOD Engine going to be hold.");
                }
            }
        } catch (EODEngineHoldException ex) {
            throw ex;
        } catch (EODEngineCompletelyFailedException ex) {
            throw ex;
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
                if (Configurations.PROCESS_SUCCESS_COUNT != 0 && Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS != 0) {
                    progress = ((Configurations.PROCESS_SUCCESS_COUNT * 100 / Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));

                    Configurations.PROCESS_PROGRESS = String.valueOf(progress) + "%";

                } else if (Configurations.PROCESS_SUCCESS_COUNT == 0 && Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS != 0) {
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
                logManager.logError(e, errorLogger);
            }
    }
}
