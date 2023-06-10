package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.CashBackBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CashBackRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CashBackService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CashBackConnector extends ProcessBuilder {

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CashBackRepo cashBackRepo;

    @Autowired
    CashBackService cashBackService;

    @Autowired
    CommonRepo commonRepo;

    List<CashBackBean> beanList = null;
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_CASHBACK;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = new ProcessBean();
            processBean = cashBackRepo.getProcessDetails(Configurations.PROCESS_CASHBACK);

            if (processBean != null) {
                //commonRepo.insertToEodProcessSumery(Configurations.PROCESS_CASHBACK, processBean.getEodmodule());

                //load initial configurations for cashback
                cashBackRepo.loadInitialConfigurationsForCashback();

                //get account list eligible for cashback
                beanList = cashBackRepo.getEligibleAccountsForCashback();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = beanList.size();
                if (beanList != null && beanList.size() > 0) {
                    //create thread pool..
                    for (CashBackBean bean : beanList) {
                        cashBackService.cashBack(bean);
                    }

                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }

                    failedCount = Configurations.PROCESS_FAILD_COUNT;
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = beanList.size();
                    Configurations.PROCESS_SUCCESS_COUNT = (beanList.size() - failedCount);
                    Configurations.PROCESS_FAILD_COUNT = failedCount;
                } else {
                    summery.put("Accounts eligible for Cash Back process ", 0 + "");
                }
            }

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logManager.logError("Exception in cashback process", e, errorLogger);
        } finally {
            logManager.logSummery(summery, infoLogger);
            try {
                if (beanList != null && beanList.size() != 0) {
                    //nullify beanList
                    for (CashBackBean bean : beanList) {
                        CommonMethods.clearStringBuffer(bean.getMainCardNumber());
                    }
                    beanList = null;
                }
            } catch (Exception e3) {
                logManager.logError("Exception in cashback process", e3, errorLogger);
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("Number of total Cards Count", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Number of success Cards Count", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - failedCount);
        summery.put("Number of failure Cards Count", failedCount);
    }
}
