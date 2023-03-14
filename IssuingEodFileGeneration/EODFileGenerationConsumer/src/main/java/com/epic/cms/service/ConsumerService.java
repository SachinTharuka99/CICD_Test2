package com.epic.cms.service;


import com.epic.cms.connector.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConsumerService {

    @Autowired
    OutgoingIPMFileGenConnector outgoingIPMFileGenConnector;

    @Autowired
    RB36FileGenerationConnector rb36FileGenerationConnector;

    @Autowired
    ExposureFileConnector exposureFileGenerationConnector;

    @Autowired
    CardRenewLetterConnector cardRenewLetterConnector;

    @Autowired
    CollectionAndRecoveryLetterConnector collectionAndRecoveryLetterConnector;

    @Autowired
    AutoSettlementConnector autoSettlementConnector;

    @Autowired
    CashBackFileGenConnector  cashBackFileGenConnector;

    @Autowired
    CardApplicationConfirmationLetterConnector cardApplicationConfirmationLetterConnector;

    @Autowired
    CardReplaceLetterConnector cardReplaceLetterConnector;

    @Autowired
    CardApplicationRejectLetterConnector cardApplicationRejectLetterConnector;

    @Autowired
    GLSummaryFileConnector glSummaryFileConnector;

    @KafkaListener(topics = "GlSummeryFile", groupId = "group_GlSummeryFile")
    public void GLSummeryFileConsumer(String msg) throws Exception {
        System.out.println("Start GL Summery File Gen Process");
        //glSummaryFileConnector.startProcess();
        System.out.println("Complete GL Summery File Gen Process");
    }

    @KafkaListener(topics = "OutgoingIPMFileGen", groupId = "group_OutgoingIPMFileGen")
    public void OutgoingIPMFileGenConsumer(String msg) throws Exception {
        System.out.println("Start Outgoing IPM File Gen Process");
        //outgoingIPMFileGenConnector.startProcess();
        System.out.println("Complete Outgoing IPM File Gen Process");
    }

    @KafkaListener(topics = "rb36File", groupId = "group_rb36File")
    public void rb36FileGeneration(String msg) throws Exception {
        System.out.println("Start RB36 File Gen Process");
        ///rb36FileGenerationConnector.startProcess();
        System.out.println("Complete RB36 File Gen Process");
    }

    @KafkaListener(topics = "exposureFile", groupId = "group_exposureFile")
    public void exposureFileGeneration(String msg) throws Exception {
        System.out.println("Start Exposure File Gen Process");
        //exposureFileGenerationConnector.startProcess();
        System.out.println("Complete Exposure File Gen Process");
    }

    @KafkaListener(topics = "cardRenewLetter", groupId = "group_cardRenewLetter")
    public void cardRenewLetterGeneration(String msg) throws Exception {
        System.out.println("Start Card Renew Letter Gen Process");
        //cardRenewLetterConnector.startProcess();
        System.out.println("Complete Card Renew Letter Gen Process");
    }

    @KafkaListener(topics = "collectionAndRecoveryLetter", groupId = "group_collectionAndRecoveryLetter")
    public void collectionAndRecoveryLetterGeneration(String msg) throws Exception {
        System.out.println("Start collection And Recovery Letter Gen Process");
        //collectionAndRecoveryLetterConnector.startProcess();
        System.out.println("Complete collection And Recovery Letter Gen Process");
    }
    @KafkaListener(topics = "cardApplicationConfirmLetter", groupId = "group_cardApplicationConfirmLetter")
    public void cardApplicationConfirmLetter(String msg) throws Exception {
        System.out.println("Start Card Application Confirmation Letter Process");
        //cardApplicationConfirmationLetterConnector.startProcess();
        System.out.println("Complete Card Application Confirmation Letter Process");
    }

    @KafkaListener(topics = "cardReplaceLetter", groupId = "group_cardReplaceLetter")
    public void cardReplaceLetter(String msg) throws Exception {
        System.out.println("Start Card Replace Letter Process");
        //cardReplaceLetterConnector.startProcess();
        System.out.println("Complete Card Replace Letter Process");
    }

    @KafkaListener(topics = "cardApplicationRejectLetter", groupId = "group_cardApplicationRejectLetter")
    public void cardApplicationRejectLetterConsumer(String msg) throws Exception {
        System.out.println("Start Collection And Recovery Notification Process");
        //cardApplicationRejectLetterConnector.startProcess();
        System.out.println("Complete Collection And Recovery Notification Process");
    }

    @KafkaListener(topics = "autoSettlement", groupId = "group_autoSettlement")
    public void autoSettlementFileGeneration(String msg) throws Exception {
        System.out.println("Start Auto Settlement File Gen Process");
        //autoSettlementConnector.startProcess();
        System.out.println("Complete Auto Settlement File Gen Process");
    }

    @KafkaListener(topics = "cashBackFile", groupId = "group_cashBackFile")
    public void cashBackFileGeneration(String msg) throws Exception {
        System.out.println("Start Cash Back File Gen Process");
        //cashBackFileGenConnector.startProcess();
        System.out.println("Complete Cash Back File Gen Process");
    }
}
