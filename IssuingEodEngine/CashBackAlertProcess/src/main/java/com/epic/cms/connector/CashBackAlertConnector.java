package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.repository.CashBackAlertRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.CashBackAlertService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;
@Service
public class CashBackAlertConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CashBackAlertRepo cashBackAlertRepo;

    @Autowired
    CashBackAlertService cashBackAlertService;

    @Autowired
    CommonRepo commonRepo;

    @Override
    public void concreteProcess() throws Exception {

        HashMap<String, ArrayList<CashBackAlertBean>> confirmAccountlist = null;
        try {

            Configurations.RUNNING_PROCESS_ID=Configurations.PROCESS_ID_CASH_BACK_ALERT_PROCESS;
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_CASH_BACK_ALERT_PROCESS);
            CommonMethods.eodDashboardProgressParametersReset();

            confirmAccountlist = cashBackAlertRepo.getConfirmedAccountToAlert();

            if(confirmAccountlist !=null && confirmAccountlist.size()>0){
                for (Map.Entry<String, ArrayList<CashBackAlertBean>> entry : confirmAccountlist.entrySet()) {
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS +=entry.getValue().size();

                    cashBackAlertService.processCashBackAlertService(entry.getKey(),entry.getValue(),processBean);
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS=(Configurations.successCardNoCount_CashBackAlert + Configurations.failedCardNoCount_CashBackAlert);
                Configurations.PROCESS_SUCCESS_COUNT=(Configurations.successCardNoCount_CashBackAlert);
                Configurations.PROCESS_FAILD_COUNT=(Configurations.failedCardNoCount_CashBackAlert);

                summery.put("Process Name", processBean.getProcessDes());
                summery.put("Started Date", Configurations.EOD_DATE.toString());
                summery.put("No of Account effected",  Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
                summery.put("No of Success Account ", Configurations.PROCESS_SUCCESS_COUNT);
                summery.put("No of fail Account ", Configurations.PROCESS_FAILD_COUNT);
            }
        }catch (Exception e){
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Exception occurred CashBackAlert ",e);
        } finally {
            infoLogger.info(logManager.processSummeryStyles(summery));
        }
    }
}
