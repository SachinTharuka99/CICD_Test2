package com.epic.cms.service;

import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CashBackAlertRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CashBackAlertService {

    @Autowired
    AlertService alert;

    @Autowired
    CashBackAlertRepo cashBackAlertRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processCashBackAlertService(String accountNumber, ArrayList<CashBackAlertBean> cashBackList, ProcessBean processBean) {

        if (!Configurations.isInterrupted) {
            LinkedHashMap adjustDetails = new LinkedHashMap();
            try {
                for (CashBackAlertBean cashBackBean : cashBackList) {
                    try {
                        adjustDetails.put("Account Number", cashBackBean.getAccNo());
                        adjustDetails.put("Card Number", CommonMethods.cardNumberMask(cashBackBean.getMainCardNo()));
                        adjustDetails.put("Cash Back Amount", cashBackBean.getCashBackAmount());
                        adjustDetails.put("Cash Back SMS Template Code", Configurations.CASH_BACK_SMS_CODE);

                        boolean isCBNull = cashBackBean.isCBNull();
                        if (!isCBNull) {
                            if (cashBackBean.getCashBackAmount() > 0) {
                                alert.alertGenerationCashBack(Configurations.CASH_BACK_SMS_CODE, cashBackBean);
                            }
                            if (cashBackBean.isMinPayAvl()) {
                                alert.alertGenerationCashBack(Configurations.STMT_WITH_CASH_BACK_SMS_CODE, cashBackBean);
                            }
                            cashBackAlertRepo.updateCashBackAlertGenStatus(cashBackBean.getReqId());

                        } else {
                            if (cashBackBean.isMinPayAvl()) {
                                alert.alertGenerationCashBack(Configurations.STMT_WITHOUT_CASH_BACK_SMS_CODE, cashBackBean);
                            }
                        }

                        cashBackAlertRepo.updateBillingStatementAlertGenStatus(cashBackBean.getStatementId());
                        adjustDetails.put("Cash Back Alert Process Status", "Passed");
                        Configurations.successCardNoCount_CashBackAlert++;
                        Configurations.PROCESS_SUCCESS_COUNT++;
                    } catch (Exception ex) {
                        Configurations.failedCardNoCount_CashBackAlert++;
                        Configurations.PROCESS_FAILD_COUNT++;
                        adjustDetails.put("Cash Back Alert Process Status", "Failed");
                        Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(cashBackBean.getMainCardNo()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));

                    }
                    //infoLogger.info(logManager.processDetailsStyles(adjustDetails));
                }
            } catch (Exception e) {
                //errorLogger.error("Error Occurs, when running Cash Back alert process for Acc No " + accountNumber + " ", e);
                LogManager.processErrorLog("Error Occurs, when running Cash Back alert process for Acc No " + accountNumber, e, errorLogger);
            } finally {
                LogManager.processDetailsLog(adjustDetails, infoLogger);
            }
        }
    }
}
