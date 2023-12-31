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
    ATMFileClearingConnector atmFileClearingConnector;
    @Autowired
    PaymentFileClearingConnector paymentFileClearingConnector;
    @Autowired
    VisaBaseIIFileClearingConnector visaBaseIIFileClearingConnector;
    @Autowired
    MasterFileClearingConnector masterFileClearingConnector;

    @KafkaListener(topics = "ATMFileClearing", groupId = "group_ATMFileClearing", containerFactory = "kafkaListenerContainerFactory")
    public void ATMFileClearingConsumer(String fileId) throws Exception {
        System.out.println("Start ATM File Clearing Process");
        atmFileClearingConnector.startProcess(fileId, Configurations.PROCESS_ATM_FILE_VALIDATE);
        System.out.println("Complete ATM File Clearing Process");
    }

    @KafkaListener(topics = "PaymentFileClearing", groupId = "group_PaymentFileClearing")
    public void paymentFileClearingConsumer(String fileId) throws Exception {
        System.out.println("Start Payment File Clearing Process");
        paymentFileClearingConnector.startProcess(fileId, Configurations.PROCESS_PAYMENT_FILE_VALIDATE);
        System.out.println("Complete Payment File Clearing Process");
    }

    @KafkaListener(topics = "VisaFileClearing", groupId = "group_VisaFileClearing")
    public void visaFileClearingConsumer(String fileId) throws Exception {
        System.out.println("Start Visa File Clearing Process");
        visaBaseIIFileClearingConnector.startProcess(fileId, Configurations.PROCESS_ID_VISA_BASEII_CLEARING);
        System.out.println("Complete Visa File Clearing Process");
    }

    @KafkaListener(topics = "MasterFileClearing", groupId = "group_MasterFileClearing")
    public void masterFileClearingConsumer(String fileId) throws Exception {
        System.out.println("Start Visa File Clearing Process");
        masterFileClearingConnector.startProcess(fileId, Configurations.PROCESS_ID_MASTER_CLEARING);
        System.out.println("Complete Visa File Clearing Process");
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
