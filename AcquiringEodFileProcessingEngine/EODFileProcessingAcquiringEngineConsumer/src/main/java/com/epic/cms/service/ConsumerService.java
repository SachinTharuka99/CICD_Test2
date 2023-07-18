package com.epic.cms.service;


import com.epic.cms.connector.*;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConsumerService {

    @Autowired
    MasterCardT67FileReadConnector masterCardT67FileReadConnector;

    @Autowired
    DCFFileClearingConnector dcfFileClearingConnector;

    @KafkaListener(topics = "MasterCardT67", groupId = "group_MasterCardT67", containerFactory = "kafkaListenerContainerFactory")
    public void MasterCardT67ClearingConsumer(String fileId) throws Exception {
        System.out.println("Start MasterCardT67 File Clearing Process");
        masterCardT67FileReadConnector.startProcess(fileId, Configurations.PROCESS_MASTER_CARD_T67_FILE_READ);
        System.out.println("Complete MasterCardT67 File Clearing Process");
    }

    @KafkaListener(topics = "DCFFileClearing", groupId = "group_DCFFileClearing", containerFactory = "kafkaListenerContainerFactory")
    public void DCFFileClearingConsumer(String fileId) throws Exception {
        System.out.println("Start DCF File Clearing Process");
        dcfFileClearingConnector.startProcess(fileId, Configurations.PROCESS_DCF_FILE_READ);
        System.out.println("Complete DCF File Clearing Process");
    }

    @KafkaListener(topics = "eodIdUpdator", groupId = "group_eodIdUpdator")
    public void eodIdUpdator(String eodId) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Configurations.EOD_ID = Integer.parseInt(eodId);
        Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
        Configurations.STARTING_EOD_STATUS = "INIT";
        Configurations.EOD_DATE = getDateFromEODID(Configurations.EOD_ID);
        Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);
    }

    public Date getDateFromEODID(int eodId) {
        Date parsedDate = null;
        String streodID = "";
        try {
            if (eodId > 10000000) {
                streodID = eodId + "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                String eodIDsubs = streodID.substring(0, streodID.length() - 2);
                parsedDate = sdf.parse(eodIDsubs);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            return parsedDate;
        }
    }
}
