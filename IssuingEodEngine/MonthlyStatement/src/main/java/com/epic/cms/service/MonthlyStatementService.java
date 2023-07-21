/**
 * Author : yasiru_l
 * Date : 11/14/2022
 * Time : 3:49 PM
 * Project Name : ecms_eod_engine
 */
package com.epic.cms.service;

import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.repository.MonthlyStatementRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;


@Service
public class MonthlyStatementService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    MonthlyStatementRepo monthlyStatementRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void monthlyStatement(String accNo, ArrayList<CardBean> accDetails, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            try {

                List<CardBean> CardBeanList = accDetails;
                StatementBean stBean = new StatementBean();
                stBean = monthlyStatementRepo.CheckBillingCycleChangeRequest(accNo);
                monthlyStatementRepo.UpdateStatementDeatils(CardBeanList, stBean, accNo);

                successCount.add(1);
                //Configurations.PROCESS_SUCCESS_COUNT++;

            } catch (Exception ex) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(accNo), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                //Configurations.PROCESS_FAILD_COUNT++;
                failCount.add(1);
                logError.error("Error Occurs, when running monthly statement process for account " + accNo + " ", ex);
            }
        }
    }
}
