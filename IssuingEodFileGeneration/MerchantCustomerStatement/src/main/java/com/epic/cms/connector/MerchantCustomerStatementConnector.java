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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.LogManager.errorLoggerEFGE;
import static com.epic.cms.util.LogManager.infoLoggerEFGE;

@Service
public class MerchantCustomerStatementConnector extends FileGenProcessBuilder {

    final RestTemplate restTemplate;

    public MerchantCustomerStatementConnector(RestTemplateBuilder restTemplateBuilder){
        this.restTemplate = restTemplateBuilder.build();
    }

    @Autowired
    LogManager logManager;

    @Override
    public void concreteProcess() throws Exception {
        try{

            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION);

            if (processBean != null) {

                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION;
                CommonMethods.eodDashboardProgressParametersReset();

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("eodDate", Configurations.EOD_DATE);

                JsonNode response = restTemplate.postForObject("http://127.0.0.1:5000/eod-engine/merchantCustomer?eodDate={eodDate}", requestBody, JsonNode.class, Map.of("eodDate",Configurations.EOD_DATE));

                // Access the properties of the JsonNode
                JsonNode successFile = response.get("successno");
                JsonNode errorFile= response.get("errorno");

                int successCount = Integer.parseInt(successFile.toString());
                int errorCount = Integer.parseInt(errorFile.toString());
                int totalCount = successCount + errorCount;

                Configurations.PROCESS_SUCCESS_COUNT = successCount;
                Configurations.PROCESS_FAILD_COUNT = errorCount;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = totalCount;
            }

        } catch (Exception e) {
            logManager.logError("Exception in Merchant Customer Statement Gen Process", e, errorLoggerEFGE);
        } finally {
            logManager.logSummery(summery, infoLoggerEFGE);
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
