/**
 * Author : yasiru_l
 * Date : 4/21/2023
 * Time : 8:22 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.Configurations.EOD_STATEMENT_GEN_BASE_URL;

@Service
public class MerchantCustomerStatementGenerationConnector extends FileGenProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    final RestTemplate restTemplate;
    @Autowired
    LogManager logManager;

    public MerchantCustomerStatementGenerationConnector(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public void concreteProcess() throws Exception {
        try {

            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION);

            if (processBean != null) {

                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION;
                CommonMethods.eodDashboardProgressParametersReset();

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("eodDate", Configurations.EOD_DATE);

                JsonNode response = restTemplate.postForObject(EOD_STATEMENT_GEN_BASE_URL + "/merchantCustomer?eodDate={eodDate}", requestBody, JsonNode.class, Map.of("eodDate", Configurations.EOD_DATE));

                // Access the properties of the JsonNode
                JsonNode successFile = response.get("successno");
                JsonNode errorFile = response.get("errorno");

                int successCount = Integer.parseInt(successFile.toString());
                int errorCount = Integer.parseInt(errorFile.toString());
                int totalCount = successCount + errorCount;

                Configurations.PROCESS_SUCCESS_COUNT = successCount;
                Configurations.PROCESS_FAILD_COUNT = errorCount;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = totalCount;
            }

        } catch (Exception e) {
            logError.error("Exception in Merchant Customer Statement Gen Process", e);
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Selected File Count in Merchant Customer Statement ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("No of Success Merchant Customer Statement ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("No of Failed Merchant Customer Statement ", Configurations.PROCESS_FAILD_COUNT);
        summery.put("Process Status for Merchant Customer Statement", "Passed");
    }
}
