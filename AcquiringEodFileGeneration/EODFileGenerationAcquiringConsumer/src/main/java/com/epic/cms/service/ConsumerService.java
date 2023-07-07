package com.epic.cms.service;


import com.epic.cms.connector.MerchantCustomerStatementConnector;
import com.epic.cms.connector.MerchantLocationStatementConnector;
import com.epic.cms.connector.MerchantPaymentFileConnector;
import com.epic.cms.connector.OutgoingCUPFileConnector;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConsumerService {

    @Autowired
    MerchantLocationStatementConnector merchantLocationStatementConnector;

    @Autowired
    MerchantCustomerStatementConnector merchantCustomerStatementConnector;
    @Autowired
    MerchantPaymentFileConnector merchantPaymentFileConnector;

    @Autowired
    OutgoingCUPFileConnector outgoingCUPFileConnector;

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

    @KafkaListener(topics = "merchantPaymentFileCreation", groupId = "group_merchantPaymentFileCreation")
    public void merchantPaymentFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Merchant Payment File Gen Process");
        merchantPaymentFileConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_PAYMENT_FILE_CREATION, uniqueID);
        System.out.println("Complete Merchant Payment File Gen Process");
    }

    @KafkaListener(topics = "outgoingCupFile", groupId = "group_outgoingCupFile")
    public void outgoingCUPFileGeneration(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Outgoing CUP File Gen Process");
        outgoingCUPFileConnector.startProcess(Configurations.PROCESS_ID_OUTGOING_CUP_FILE_GEN, uniqueID);
        System.out.println("Complete Outgoing CUP File Gen Process");
    }

}
