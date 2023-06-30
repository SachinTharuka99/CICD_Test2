/**
 * Author : rasintha_j
 * Date : 6/20/2023
 * Time : 10:36 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.connnector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.OriginatorPushTxnUpdateDao;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.epic.cms.service.OriginatorPushTxnUpdateService;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class OriginatorPushTxnUpdateConnector extends ProcessBuilder {
    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    OriginatorPushTxnUpdateDao originatorPushTxnUpdateDao;
    @Autowired
    OriginatorPushTxnUpdateService originatorPushTxnUpdateService;
    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    ArrayList<EodTransactionBean> txnMap;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public void concreteProcess() throws Exception {
        Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_ORIGINATOR_PUSH_TXN_UPDATE;
        CommonMethods.eodDashboardProgressParametersReset();
        processBean = new ProcessBean();
        processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_ORIGINATOR_PUSH_TXN_UPDATE);

        if(processBean != null) {
            try {
                txnMap = originatorPushTxnUpdateDao.getAllOriginatorPushTxn();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = txnMap.size();

                if (txnMap != null && !txnMap.isEmpty()) {
                    txnMap.forEach(eodTransactionBean -> {
                        try {
                            originatorPushTxnUpdateService.originatorPushTxnUpdate(eodTransactionBean);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }

                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = Configurations.totalTxnCount;
                    Configurations.PROCESS_SUCCESS_COUNT = Configurations.totalTxnCount - (Configurations.onusTxnCount + Configurations.acqFailedMerchants);
                    Configurations.PROCESS_FAILD_COUNT = Configurations.issFailedTxn + Configurations.acqFailedMerchants;
                }
            } catch (Exception ex) {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                throw ex;
            } finally {
                logInfo.info(logManager.logSummery(summery));
            }
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("No of originator txn effected", Integer.toString(Configurations.onusTxnCount));
        summery.put("No of Success originator txn ", Integer.toString(Configurations.onusTxnCount - Configurations.issFailedTxn));
        summery.put("No of fail originator txn ", Integer.toString(Configurations.issFailedTxn));
        summery.put("No of Total Txn", Integer.toString(Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
        summery.put("No of Total failed Txn ", Integer.toString(Configurations.issFailedTxn + Configurations.acqFailedMerchants));
    }
}
