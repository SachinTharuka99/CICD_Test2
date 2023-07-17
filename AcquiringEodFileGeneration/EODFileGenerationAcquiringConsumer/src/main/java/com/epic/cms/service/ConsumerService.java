package com.epic.cms.service;


import com.epic.cms.connector.MerchantCustomerStatementGenerationConnector;
import com.epic.cms.connector.MerchantGLSummaryFileConnector;
import com.epic.cms.connector.MerchantLocationStatementGenerationConnector;
import com.epic.cms.connector.OutgoingIPMFileGenConnector;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConsumerService {

    @Autowired
    MerchantGLSummaryFileConnector merchantGLSummaryFileConnector;

    @Autowired
    MerchantLocationStatementGenerationConnector merchantLocationStatementConnector;

    @Autowired
    MerchantCustomerStatementGenerationConnector merchantCustomerStatementConnector;

    @Autowired
    OutgoingIPMFileGenConnector outgoingIPMFileGenConnector;

    @KafkaListener(topics = "merchantGLSummaryFile", groupId = "group_merchantGLSummaryFile")
    public void merchantGLSummaryConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Merchant GLSummary File Process");
        merchantGLSummaryFileConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION, uniqueID);
        System.out.println("Complete Merchant GLSummary File Process");
    }

    @KafkaListener(topics = "merchantStatementGeneration", groupId = "group_merchantStatementGeneration")
    public void merchantLocationFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Merchant Location File Gen Process");
        merchantLocationStatementConnector.startProcess(Configurations.PROCESS_MERCHANT_STATEMENT_FILE_CREATION, uniqueID);
        System.out.println("Complete Merchant Location File Gen Process");
    }

    @KafkaListener(topics = "merchantCustomerStatementGeneration", groupId = "group_merchantCustomerStatementGeneration")
    public void merchantCustomerFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Merchant Customer File Gen Process");
        merchantCustomerStatementConnector.startProcess(Configurations.PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION, uniqueID);
        System.out.println("Complete Merchant Customer File Gen Process");
    }

    @KafkaListener(topics = "outgoingIPMFile", groupId = "group_outgoingIPMFile")
    public void outgoingIPMFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start outgoing IPM File Gen Process");
        outgoingIPMFileGenConnector.startProcess(Configurations.PROCESS_ID_OUTGOING_IPM_FILE_GEN, uniqueID);
        System.out.println("Complete outgoing IPM File Gen Process");
    }

}
