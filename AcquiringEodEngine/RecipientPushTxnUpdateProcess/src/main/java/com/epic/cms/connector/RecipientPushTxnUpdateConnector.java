package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import com.epic.cms.dao.RecipientPushTxnUpdateDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.epic.cms.service.RecipientPushTxnUpdateService;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class RecipientPushTxnUpdateConnector extends ProcessBuilder {

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    CommonRepo commonRepo;



    ArrayList<EodTransactionBean> txnList;

    @Autowired
    RecipientPushTxnUpdateDao recipientPushTxnUpdateDao;

    @Autowired
    RecipientPushTxnUpdateService recipientPushTxnUpdateService;

    @Override
    public void concreteProcess() throws Exception {

        ProcessBean processBean = null;

        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_RECIPIENT_PUSH_TXN_UPDATE);

            if (processBean != null) {

                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_RECIPIENT_PUSH_TXN_UPDATE;
                CommonMethods.eodDashboardProgressParametersReset();

                //Get all Recipient push txn based on REQUESTFROM,EODSTATUS,STATUS,RESPONSECODE
                txnList = recipientPushTxnUpdateDao.getAllRecipientPushTxn();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = txnList.size();


                for (EodTransactionBean eodTransactionBean : txnList) {
                    recipientPushTxnUpdateService.recipientPushTxnUpdate(eodTransactionBean);
                }

                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
            }


        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            throw e;
        }
    }

    @Override
    public void addSummaries() {

        Configurations.PROCESS_SUCCESS_COUNT = (Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS - Configurations.PROCESS_FAILD_COUNT);

        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("No of Total Txn", Integer.toString( Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
        summery.put("No of Total failed Txn ", Integer.toString( Configurations.PROCESS_FAILD_COUNT));
        summery.put("No of Total success Txn ", Integer.toString( Configurations.PROCESS_SUCCESS_COUNT));
    }
}
