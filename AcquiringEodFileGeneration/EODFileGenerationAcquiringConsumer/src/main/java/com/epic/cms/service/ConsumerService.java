package com.epic.cms.service;


import com.epic.cms.connector.MerchantCustomerStatementGenerationConnector;
import com.epic.cms.connector.MerchantGLSummaryFileConnector;
import com.epic.cms.connector.MerchantLocationStatementGenerationConnector;
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

    @KafkaListener(topics = "merchantGLSummaryFile", groupId = "group_merchantGLSummaryFile")
    public void merchantGLSummaryConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Merchant GLSummary File Process");
        merchantGLSummaryFileConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION, uniqueID);
        System.out.println("Complete Merchant GLSummary File Process");
    }

}
