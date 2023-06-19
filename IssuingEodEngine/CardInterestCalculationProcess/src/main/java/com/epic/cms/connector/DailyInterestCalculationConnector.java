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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
public class DailyInterestCalculationConnector extends ProcessBuilder {

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

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public void concreteProcess() throws Exception {
        ArrayList<StatementBean> accountList = new ArrayList<>();
        int noOfCards = 0;
        int failedCards = 0;

        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_INTEREST_CALCULATION);
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_INTEREST_CALCULATION;
            CommonMethods.eodDashboardProgressParametersReset();

            if (processBean != null) {

                /** get account list (accounts that have main card not in 'CACL' and first statement generated) */
                accountList = interestCalculationRepo.getLatestStatementAccountList();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = accountList.size();
                noOfCards = Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS;

                if (accountList.size() > 0) {
                    for (StatementBean statementBean : accountList) {
                        interestCalculationService.startDailyInterestCalculation(statementBean);
                    }
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                failedCards = Configurations.PROCESS_FAILD_COUNT;
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = noOfCards;
                Configurations.PROCESS_SUCCESS_COUNT = (noOfCards - failedCards);
                Configurations.PROCESS_FAILD_COUNT = failedCards;
            }
        } catch (Exception e) {
            logError.error("Interest calculation process failed", e);
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;

        } finally {
            logInfo.info(logManager.logSummery(summery));
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
        summery.put("No of Success Card ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("No of fail Card ", Configurations.PROCESS_FAILD_COUNT);

    }
}
