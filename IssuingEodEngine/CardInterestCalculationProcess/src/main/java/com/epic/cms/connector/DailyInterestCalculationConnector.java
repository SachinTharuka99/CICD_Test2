/**
 * Created By Lahiru Sandaruwan
 * Date : 10/24/2022
 * Time : 8:01 PM
 * Project Name : ecms_eod_engine
 * Topic : dailyInterestCalculation
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.DailyInterestCalculationRepo;
import com.epic.cms.service.DailyInterestCalculationService;
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
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class DailyInterestCalculationConnector extends ProcessBuilder {

    private static final Logger logError = LoggerFactory.getLogger("logError");
    public AtomicInteger faileCardCount = new AtomicInteger(0);
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    StatusVarList statusList;
    @Autowired
    DailyInterestCalculationRepo interestCalculationRepo;
    @Autowired
    DailyInterestCalculationService interestCalculationService;
    @Autowired
    LogManager logManager;

    @Override
    public void concreteProcess() throws Exception {
        ArrayList<StatementBean> accountList = new ArrayList<>();

        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_INTEREST_CALCULATION);
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_INTEREST_CALCULATION;
            CommonMethods.eodDashboardProgressParametersReset();

            if (processBean != null) {

                /** get account list (accounts that have main card not in 'CACL' and first statement generated) */
                accountList = interestCalculationRepo.getLatestStatementAccountList();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = accountList.size();

                if (accountList.size() > 0) {
                    accountList.forEach(statementBean -> {
                        interestCalculationService.startDailyInterestCalculation(statementBean, faileCardCount);
                    });
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            logError.error("Interest calculation process failed", e);
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;

        } finally {
            if (accountList != null && accountList.size() != 0) {
                for (StatementBean accBean : accountList) {
                    CommonMethods.clearStringBuffer(accBean.getCardNo());
                    CommonMethods.clearStringBuffer(accBean.getMainCardNo());
                }
                accountList = null;
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("No of Card effected ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("No of Success Card ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - faileCardCount.get());
        summery.put("No of fail Card ", faileCardCount.get());

    }
}
