package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.EomCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EOMInterestRepo;
import com.epic.cms.service.EOMInterestService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class EOMInterestConnector extends ProcessBuilder {

    @Autowired
    StatusVarList statusList;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    LogManager logManager;

    @Autowired
    EOMInterestRepo eomInterestRepo;

    @Autowired
    EOMInterestService eomInterestService;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Override
    public void concreteProcess() throws Exception {

        int noOfAccounts = 0;
        int failedAccounts = 0;
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_EOM_INTEREST_CALCULATION;
            ArrayList<EomCardBean> accountList;
            DateFormat dateFormatforRenew = new SimpleDateFormat("dd");
            String curDateforRenew = dateFormatforRenew.format(Configurations.EOD_DATE);
            int day = Integer.parseInt(curDateforRenew);
            accountList = eomInterestRepo.getEomCardList(day);
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_EOM_INTEREST_CALCULATION);

            noOfAccounts = accountList.size();
            for (int i = 0; i < accountList.size(); i++) {
                eomInterestService.EOMInterestCalculation(processBean, accountList.get(i));
            }

            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = noOfAccounts;
            Configurations.PROCESS_SUCCESS_COUNT = (noOfAccounts - failedAccounts);
            Configurations.PROCESS_FAILD_COUNT = failedAccounts;

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            logManager.logError("EOM Interest Calculation Process failed", e, errorLogger);
        } finally {
            logManager.logSummery(summery, infoLogger);
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Process Name ", processBean.getProcessDes());
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("No of Accounts effected ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("No of Success Accounts ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("No of fail Accounts ", Configurations.PROCESS_FAILD_COUNT);
    }
}
