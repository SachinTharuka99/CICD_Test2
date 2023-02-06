/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:48 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.TransactionPostDao;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.service.TransactionPostService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class TransactionPostConnector extends ProcessBuilder {
    @Autowired
    StatusVarList statusList;

    @Autowired
    TransactionPostDao transactionPostDao;

    @Autowired
    TransactionPostService transactionPostService;

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    LinkedHashMap summery = new LinkedHashMap();
    private ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();
    private ArrayList<OtbBean> txnList;
    private int failedCount = 0;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_TXN_POST;
            CommonMethods.eodDashboardProgressParametersReset();
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                custAccList = transactionPostDao.getInitEodTxnPostCustAcc();
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                custAccList = transactionPostDao.getErrorEodTxnPostCustAcc();
            }

            for (OtbBean bean : custAccList) {
                transactionPostService.transactionList(bean);
            }
            //wait till all the threads are completed
            while (!(taskExecutor.getActiveCount() == 0)) {
                Thread.sleep(1000);
            }
            if (custAccList != null) {
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = (custAccList.size());
                Configurations.PROCESS_SUCCESS_COUNT = (custAccList.size() - Configurations.PROCESS_FAILD_COUNT);
            }
            summery.put("Number of accounts to fee post ", custAccList.size());
            summery.put("Number of success fee post ", custAccList.size() - Configurations.PROCESS_FAILD_COUNT);
            summery.put("Number of failure fee post ", Configurations.PROCESS_FAILD_COUNT);
            infoLogger.info(logManager.processSummeryStyles(summery));
        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
            errorLogger.error("Failed Transaction Post Process Completely ", e);
        } finally {
            try {
               /* PADSS Change -
            variables handling card data should be nullified by replacing the value of variable with zero and call NULL function */
                if (custAccList != null && custAccList.size() != 0) {
                    for (OtbBean bean : custAccList) {
                        CommonMethods.clearStringBuffer(bean.getCardnumber());
                        CommonMethods.clearStringBuffer(bean.getMaincardno());
                    }
                    custAccList = null;
                }

                if (txnList != null && txnList.size() != 0) {
                    for (OtbBean card : txnList) {
                        CommonMethods.clearStringBuffer(card.getCardnumber());
                        CommonMethods.clearStringBuffer(card.getMaincardno());
                    }
                    txnList = null;
                }
            } catch (Exception e) {
                errorLogger.error("Transaction Post Process Clear StringBuffer Fail" + e);
            }
        }
    }

}