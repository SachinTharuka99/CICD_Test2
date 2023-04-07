/**
 * Author : shehan_m
 * Date : 1/16/2023
 * Time : 2:36 PM
 * Project Name : eod-engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.EODEngineProducerRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.EODEngineCompletelyFailedException;
import com.epic.cms.util.EODEngineHoldException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    @Async
    public synchronized void EODEngineMain(String eodID, int categoryId) throws InterruptedException {
        System.out.println("Main Method Started");
        try {
            LogManager.processStartEndStyle("EOD-Engine main service started for EODID:" + eodID);
            List<ProcessBean> processList = new ArrayList<ProcessBean>();
            String uniqueId = generateUniqueId();
            Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = true;
            Configurations.PROCESS_COMPLETE_STATUS = true;

            if (Configurations.STARTING_EOD_STATUS.equals("INIT")) {//NORMAL EOD
                processList = producerRepo.getProcessListByCategoryId(categoryId);//get process list for this step
            } else if (Configurations.STARTING_EOD_STATUS.equals("EROR")) {//ERROR EOD

            }
            System.out.println("------------->>>>>>>>>> EODEngineMain Thread ID: " + Thread.currentThread().getId());
            this.EODScheduler(processList, uniqueId);

            while (Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS) {
                if (producerRepo.getCompletedProcessCount(uniqueId) == processList.size()) {
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    kafkaMessageUpdator.producerWithNoReturn("true", "processStatus");
                    System.out.println("############# Process step completed");
                }
                Thread.sleep(200);
            }

            producerRepo.clearEodProcessCountTable();
            System.out.println("EOD is going to be ended.");

        } catch (Exception ex) {
            ex.printStackTrace();
            logManager.logError(ex.toString(), ex, errorLogger);
        }

    }

    public void EODScheduler(List<ProcessBean> processList, String uniqueId)
            throws InterruptedException, ExecutionException, EODEngineHoldException, EODEngineCompletelyFailedException {
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
}
