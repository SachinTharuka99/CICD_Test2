package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.DropRequestBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.TxnDropRequestRepo;
import com.epic.cms.service.TxnDropRequestService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class TxnDropRequestConnector extends ProcessBuilder {

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    TxnDropRequestRepo txnDropRequestRepo;
    @Autowired
    StatusVarList statusList;
    @Autowired
    TxnDropRequestService txnDropRequestService;

    @Override
    public void concreteProcess() throws Exception {

        int successTxn = 0;
        int failedCards = 0;
        int failedTxn = 0;
        Set<StringBuffer> failedCardList = new HashSet<>();
        List<DropRequestBean> dropTransactionList = null;

        try {

            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_TRANSACTION_DROP_REQUEST;
            CommonMethods.eodDashboardProgressParametersReset();
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_TRANSACTION_DROP_REQUEST);

            if (processBean != null) {

                /**get transaction validity period*/
                int txnValidityPeriod = txnDropRequestRepo.getTransactionValidityPeriod();

                /**get eligible drop transaction list*/
                dropTransactionList = txnDropRequestRepo.getDropTransactionList(txnValidityPeriod);
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = dropTransactionList.size();

                if (dropTransactionList.size() > 0) {
                    for (DropRequestBean bean : dropTransactionList) {
                        txnDropRequestService.processTxnDropRequest(bean, processBean);
                    }
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }

                successTxn = Configurations.SuccessCount_TxnDropRequest;
                failedTxn = Configurations.FailedCount_TxnDropRequest;
                failedCards = Configurations.FailedCards_TxnDropRequest;

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (failedTxn + successTxn);
                Configurations.PROCESS_SUCCESS_COUNT = successTxn;
                Configurations.PROCESS_FAILD_COUNT = failedTxn;

                summery.put("Started Date ", Configurations.EOD_DATE.toString());
                summery.put("No of Drop Requests ", Configurations.PROCESS_SUCCESS_COUNT);
                summery.put("No of Fail Requests ", Configurations.PROCESS_FAILD_COUNT);
                summery.put("No of Fail Cards ", Configurations.FailedCards_TxnDropRequest);

                infoLogger.info(logManager.processSummeryStyles(summery));
            }
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            try {
                errorLogger.error("Transaction Drop Request process failed", e);
                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }
            } catch (Exception e2) {
                errorLogger.error("Exception in Transaction Drop Request", e2);
            }
        } finally {
            try {
                /** PADSS Change -
                 variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                if (dropTransactionList != null && dropTransactionList.size() != 0) {
                    for (DropRequestBean bean : dropTransactionList) {
                        CommonMethods.clearStringBuffer(bean.getCardNumber());
                    }
                    dropTransactionList = null;
                }
                failedCardList = null;

            } catch (Exception e3) {
                errorLogger.error("Exception in Transaction Drop Request", e3);
            }
        }
    }
}
