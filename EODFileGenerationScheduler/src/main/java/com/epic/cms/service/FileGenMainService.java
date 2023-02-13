/**
 * Author : lahiru_p
 * Date : 1/24/2023
 * Time : 2:01 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.common.ProcessBuilder;
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
import java.util.HashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;


@Service
public class FileGenMainService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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

    public void startEODFileGenEngine(int categoryId, int issuingOrAcquiring, int eodId) {
        System.out.println("Main Method Started");
        try {
            //get EOD-ID
            Configurations.EOD_ID = eodId;
            CreateEodId createDate = new CreateEodId();
            Configurations.EOD_DATE = createDate.getDateFromEODID(Configurations.EOD_ID);

            infoLogger.info(logManager.processStartEndStyle("EOD-File-Generation Engine main service started for EOD-ID:" + Configurations.EOD_ID));
            List<ProcessBean> processList = new ArrayList<ProcessBean>();
            //get process list by process category id , issuing or acquiring and file-gen category

            processList = producerRepo.getProcessListByFileGenCategoryId(categoryId, issuingOrAcquiring);//get process list for this step

            this.EODScheduler(processList);

        } catch (Exception e) {
            errorLogger.error("EOD File Generation Process Failed ", e);
        }
    }

    private void EODScheduler(List<ProcessBean> processList) throws Exception {
        try {
            System.out.println("Start EOD File Generation Processes");
            loadProcessConnectorList();
            for (ProcessBean processBean : processList) {
                processThreadService.startProcessByProcessId(processBean.getProcessId());
            }
            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            System.out.println("EOD File Generation process completed..");
        } catch (Exception e) {
            throw e;
        }
    }

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



    public void loadProcessConnectorList() throws Exception {
        try {
            HashMap<Integer, Object> connectorList =new HashMap<>();
            connectorList.put(Configurations.AUTO_SETTLEMENT_PROCESS, autoSettlementConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE, cardApplicationConfirmationLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT, cardApplicationRejectLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CARDRENEW_LETTER, cardRenewLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CARD_REPLACE, cardReplaceLetterConnector);
            connectorList.put(Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION, cashBackFileGenConnector);
            connectorList.put(Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS, collectionAndRecoveryLetterConnector);
            connectorList.put(Configurations.PROCESS_EXPOSURE_FILE, exposureFileConnector);
            connectorList.put(Configurations.PROCESS_ID_GL_FILE_CREATION, glSummaryFileConnector);
            connectorList.put(Configurations.PROCESS_RB36_FILE_CREATION, rb36FileGenerationConnector);
            Configurations.processConnectorList=connectorList;

        }catch (Exception e){
            throw e;
        }
    }
}
