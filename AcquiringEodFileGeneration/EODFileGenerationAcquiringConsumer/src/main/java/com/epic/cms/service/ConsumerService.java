package com.epic.cms.service;


import com.epic.cms.connector.MerchantCustomerStatementConnector;
import com.epic.cms.connector.MerchantLocationStatementConnector;
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

}
