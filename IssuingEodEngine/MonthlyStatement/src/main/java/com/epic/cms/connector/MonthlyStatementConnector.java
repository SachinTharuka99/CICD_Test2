/**
 * Author : yasiru_d
 * Date : 11/14/2022
 * Time : 1:17 PM
 * Project Name : ecms_eod_engine
 * <p>
 * ********************************************************************************
 * *  In this process it will insert|update BillingStatement and BillingLastStatementSummery tables
 * *  with new data(due date,nextbilling date,otb cash,otbcredit.....)
 * *
 * *  first select account numbers along with card numbers and its current details
 * *  (nextBillind date = eod date)
 * *
 * *  And select All transations from eod transation table between two eod ids
 * * (StatementStartdate eodid and StatementEnddate eodID)
 * *
 * * Then looping AccountList
 * *      1) Check the billing Cycle change requests and if yes,
 * *              then calculate new nextbilling date,
 * *      2) then loop cardList
 * *              i) calculate all data for supplimentary card(set those in supStmtBean) and insert those
 * *                 data into BillingStatement table and set those data to,
 * *                 mainStmtBean
 * *      3) Once done with all supplimentary cards,Calculate data with main Card and set to mainStmtBean
 * *         ( and at this point mainStmtBean also have all suplimentary cards details)
 * *
 * *      4) insert mainStmtBean data into Billing Statement tble and BillingLastStatmentBean
 * *
 * *----NOTE----
 * * In BillingStatement table there are statement details for all card Types
 * * But in BillingLastStatementSummery table have main cards statement data only
 * <p>
 * ********************************************************************************
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MonthlyStatementRepo;
import com.epic.cms.service.MonthlyStatementService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MonthlyStatementConnector extends ProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    public AtomicInteger faileCardCount = new AtomicInteger(0);
    @Autowired
    StatusVarList statusList;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    LogManager logManager;
    @Autowired
    MonthlyStatementRepo monthlyStatementRepo;
    @Autowired
    MonthlyStatementService monthlyStatementService;
    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void concreteProcess() throws Exception {

        String accNo = "";
        HashMap<String, ArrayList<CardBean>> cardAccountMap = new HashMap<>();

        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_MONTHLY_STATEMENT);
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_MONTHLY_STATEMENT;
            CommonMethods.eodDashboardProgressParametersReset();

            if (processBean != null) {
                LinkedHashMap summery = new LinkedHashMap();
                cardAccountMap = monthlyStatementRepo.getCardAccountListForBilling();

                for (Map.Entry<String, ArrayList<CardBean>> entry : cardAccountMap.entrySet()) {
                    accNo = entry.getKey();
                    ArrayList<CardBean> CardBeanList = entry.getValue();
                    monthlyStatementService.monthlyStatement(accNo, CardBeanList,faileCardCount);
                }

//                cardAccountMap.forEach((entryKey, entryValue) -> {
//                     accNo.set(entryKey);
//                    ArrayList<CardBean> cardBeanList = entryValue;
//                    monthlyStatementService.monthlyStatement(accNo.get(), cardBeanList,faileCardCount);
//                });

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = cardAccountMap.size();
            }

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logError.error("Error!!! Monthly Statement Process Complete with Errors ", e);
            throw e;
        } finally {
            //logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Total No of Effected Cards ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Total Success Cards ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - faileCardCount.get());
        summery.put("Total Fail Cards ", faileCardCount.get());
    }
}
