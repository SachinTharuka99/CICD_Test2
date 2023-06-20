package com.epic.cms.service;


import com.epic.cms.connector.*;
import com.epic.cms.util.Configurations;
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



    @Autowired
    CustomerStatementConnector customerStatementConnector;

    @KafkaListener(topics = "GlSummeryFile", groupId = "group_GlSummeryFile")
    public void GLSummeryFileConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start GL Summery File Gen Process");
        glSummaryFileConnector.startProcess(Configurations.PROCESS_ID_GL_FILE_CREATION, uniqueID);
        System.out.println("Complete GL Summery File Gen Process");
    }

    @KafkaListener(topics = "OutgoingIPMFileGen", groupId = "group_OutgoingIPMFileGen")
    public void OutgoingIPMFileGenConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Outgoing IPM File Gen Process");
        outgoingIPMFileGenConnector.startProcess(Configurations.PROCESS_ID_OUTGOING_IPM_FILE_GEN, uniqueID);
        System.out.println("Complete Outgoing IPM File Gen Process");
    }

    @KafkaListener(topics = "rb36File", groupId = "group_rb36File")
    public void rb36FileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start RB36 File Gen Process");
        rb36FileGenerationConnector.startProcess(Configurations.PROCESS_RB36_FILE_CREATION, uniqueID);
        System.out.println("Complete RB36 File Gen Process");
    }

    @KafkaListener(topics = "exposureFile", groupId = "group_exposureFile")
    public void exposureFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Exposure File Gen Process");
        exposureFileGenerationConnector.startProcess(Configurations.PROCESS_EXPOSURE_FILE, uniqueID);
        System.out.println("Complete Exposure File Gen Process");
    }

    @KafkaListener(topics = "cardRenewLetter", groupId = "group_cardRenewLetter")
    public void cardRenewLetterGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Renew Letter Gen Process");
        cardRenewLetterConnector.startProcess(Configurations.PROCESS_ID_CARDRENEW_LETTER, uniqueID);
        System.out.println("Complete Card Renew Letter Gen Process");
    }

    @KafkaListener(topics = "collectionAndRecoveryLetter", groupId = "group_collectionAndRecoveryLetter")
    public void collectionAndRecoveryLetterGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start collection And Recovery Letter Gen Process");
        collectionAndRecoveryLetterConnector.startProcess(Configurations.PROCESS_ID_INITIAL_PROCESS, uniqueID);
        System.out.println("Complete collection And Recovery Letter Gen Process");
    }
    @KafkaListener(topics = "cardApplicationConfirmLetter", groupId = "group_cardApplicationConfirmLetter")
    public void cardApplicationConfirmLetter(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Application Confirmation Letter Process");
        cardApplicationConfirmationLetterConnector.startProcess(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE, uniqueID);
        System.out.println("Complete Card Application Confirmation Letter Process");
    }

    @KafkaListener(topics = "cardReplaceLetter", groupId = "group_cardReplaceLetter")
    public void cardReplaceLetter(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Replace Letter Process");
        cardReplaceLetterConnector.startProcess(Configurations.PROCESS_ID_CARDREPLACE_LETTER, uniqueID);
        System.out.println("Complete Card Replace Letter Process");
    }

    @KafkaListener(topics = "cardApplicationRejectLetter", groupId = "group_cardApplicationRejectLetter")
    public void cardApplicationRejectLetterConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Card Application Reject Letter Process");
        cardApplicationRejectLetterConnector.startProcess(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT, uniqueID);
        System.out.println("Complete Card Application Reject Letter Process");
    }

    @KafkaListener(topics = "autoSettlement", groupId = "group_autoSettlement")
    public void autoSettlementFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Auto Settlement File Gen Process");
        autoSettlementConnector.startProcess(Configurations.AUTO_SETTLEMENT_PROCESS, uniqueID);
        System.out.println("Complete Auto Settlement File Gen Process");
    }

    @KafkaListener(topics = "cashBackFile", groupId = "group_cashBackFile")
    public void cashBackFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Cash Back File Gen Process");
        cashBackFileGenConnector.startProcess(Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION, uniqueID);
        System.out.println("Complete Cash Back File Gen Process");
    }

    @KafkaListener(topics = "monthlyStatementFile", groupId = "group_monthlyStatementFile")
    public void statementFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Customer Statement Gen Process");
        customerStatementConnector.startProcess(Configurations.PROCESS_MONTHLY_STATEMENT_FILE_CREATION, uniqueID);
        System.out.println("Complete Customer Statement Gen Process");
    }
}
