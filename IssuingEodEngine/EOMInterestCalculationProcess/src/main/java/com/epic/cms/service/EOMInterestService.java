package com.epic.cms.service;

import com.epic.cms.model.bean.CardBillingInfoBean;
import com.epic.cms.model.bean.EomCardBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EOMInterestRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class EOMInterestService {

    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    EOMInterestRepo eomInterestRepo;

    @Async("taskExecutor2")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void EOMInterestCalculation(ProcessBean processBean, EomCardBean eomCardBean) {

        if (!Configurations.isInterrupted) {
            int flag = 0;
            int failedAccounts = 0;
            LinkedHashMap cardDetails = new LinkedHashMap();
            String cardStatus;
            ArrayList<Date> lastBillingDates;
            CardBillingInfoBean lastBillingDatesAndEodId;
            ArrayList<Double> logDetails;
            try {
                String maskedCardNumber = CommonMethods.cardNumberMask(eomCardBean.getCardNo());
                try {
                    cardStatus = eomInterestRepo.CheckForCardIncrementStatus(eomCardBean.getCardNo());

                    if (!cardStatus.equalsIgnoreCase(status.getCARD_CLOSED_STATUS())) {
                        flag = eomInterestRepo.clearEomInterest(eomCardBean.getCardNo());
                        lastBillingDates = eomInterestRepo.getLastTwoBillingDatesOnAccount(eomCardBean.getAccNo());
                        lastBillingDatesAndEodId = eomInterestRepo.getLastTwoBillingDatesAndEodIdOnAccount(eomCardBean.getAccNo());

                        cardDetails.put("Account Number", eomCardBean.getAccNo());
                        cardDetails.put("Main Card Number", maskedCardNumber);
                        cardDetails.put("Interest Rate", eomCardBean.getInterestRate());

                        if (lastBillingDates.size() == 2) {/**This card is older than two months. Normal process*/
                            logDetails = eomInterestRepo.getEOMInterest(eomCardBean, lastBillingDatesAndEodId, lastBillingDates.size());

                            cardDetails.put("Account Status", "Old Account");
                            cardDetails.put("Last Bill Closing Balance", logDetails.get(0));
                            cardDetails.put("Calculated Interest", logDetails.get(1));

                        } else if (lastBillingDates.size() == 1) {/**This card is only 1 month old.Consider the payments of 1st month*/
                            logDetails = eomInterestRepo.getEOMInterest(eomCardBean, lastBillingDatesAndEodId, lastBillingDates.size());

                            cardDetails.put("Account Status", "One month old Account");
                            cardDetails.put("Last Bill Closing Balance", logDetails.get(0));
                            cardDetails.put("Calculated Interest", logDetails.get(1));

                        } else {/**This is a new card. No need of calculate Interest.Initial interest is 0.0*/
                            flag = eomInterestRepo.insertIntoEomInterest(eomCardBean.getCardNo(), eomCardBean.getAccNo(), 0.0, 0.0, Configurations.EOD_ID, status.getEOD_PENDING_STATUS());
                            eomInterestRepo.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, eomCardBean.getCardNo(), Configurations.TXN_TYPE_INTEREST_INCOME, 0.0, Configurations.DEBIT, null);

                            cardDetails.put("Account Status", "New Account");
                            cardDetails.put("Last Bill Closing Balance", "0.00");
                            cardDetails.put("Calculated Interest", "0.00");
                        }
                        if (flag > 1) {
                            cardDetails.put("Temp table Clearing Status", "Passed");
                        }
                    }

                    cardDetails.put("Process Status", "Passed");
                    Configurations.PROCESS_SUCCESS_COUNT++;

                } catch (Exception e) {
                    eomInterestRepo.clearTempTxnDetails(eomCardBean.getAccNo());
                    cardDetails.put("Process Status", "Failed");
                    Configurations.PROCESS_FAILD_COUNT++;
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(eomCardBean.getAccNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                    errorLogger.error("EOM interest calculation Process failed for account number " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                }

                infoLogger.info(logManager.processDetailsStyles(cardDetails));

            } catch (Exception e) {
                errorLogger.error("EOM interest calculation Process failed ", e);
                failedAccounts++;
            }
            Configurations.PROCESS_FAILD_COUNT += failedAccounts;
        }
    }
}
