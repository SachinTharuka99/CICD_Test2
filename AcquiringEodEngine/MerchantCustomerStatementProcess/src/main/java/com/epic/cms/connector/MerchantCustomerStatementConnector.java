/**
 * Author : lahiru_p
 * Date : 6/26/2023
 * Time : 3:38 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantCustomerStatementRepo;
import com.epic.cms.service.MerchantCustomerStatementService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
public class MerchantCustomerStatementConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    CommonRepo commonRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    MerchantCustomerStatementRepo merchantCustomerStatementRepo;

    @Autowired
    MerchantCustomerStatementService merchantCustomerStatementService;

    @Override
    public void concreteProcess() throws Exception {
        HashMap<String, MerchantCustomerBean> merchantCusList = new HashMap<String, MerchantCustomerBean>();
        processBean = new ProcessBean();
        processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_MERCHANT_CUSTOMER_STATEMENT);
        try {
            if (processBean != null) {
                LinkedHashMap details = new LinkedHashMap();
                LinkedHashMap summery = new LinkedHashMap();
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MERCHANT_CUSTOMER_STATEMENT;
                CommonMethods.eodDashboardProgressParametersReset();

                merchantCusList = merchantCustomerStatementRepo.getMerchantCustomersToBill();
                logInfo.info("  " + merchantCusList.size() + " Merchant Customers Selected for Billing Statement Process");

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = merchantCusList.size();

                merchantCusList.entrySet().forEach(entry -> {

                    String MerchantCusNo = entry.getKey();

                    MerchantCustomerBean merchantBean = entry.getValue();
                    try {
                        merchantCustomerStatementService.insertMerchantCustomerStatement(merchantBean, MerchantCusNo);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                if (merchantCusList.size() > 0) {
                    try {
                        merchantCustomerStatementRepo.insertMerchantEodStatus("MC", "Y");
                        merchantCustomerStatementRepo.insertAuditMerchantEodStatus("MC", "Y");
                        merchantCustomerStatementRepo.callMerchantCustomerStatementProcedure();
                        merchantCustomerStatementRepo.callAuditMerchantCustomerStatementProcedure();
                        logInfo.info("Merchant Customer E-Statement AP procedures Finished");
                    } catch (Exception ex) {
                        logError.error("Error Occurs, when running merchant customer E-Statement AP procedures. ", ex);
                    }
                }
                /*Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS=(successMerchantCusCount+failedMerchantCusCount);
                Configurations.PROCESS_SUCCESS_COUNT=successMerchantCusCount;
                Configurations.PROCESS_FAILD_COUNT=failedMerchantCusCount;
                summery.put("Total No of Effected Merchant Customers", (Integer) merchantCusList.size());
                summery.put("Total Success Merchant Customers", (Integer) successMerchantCusCount);
                summery.put("Total Fail Merchant Customers", (Integer) failedMerchantCusCount);
*/
                logInfo.info(logManager.logSummery(summery));
            }

        } catch (Exception e) {

        }
    }

    @Override
    public void addSummaries() {

    }
}
