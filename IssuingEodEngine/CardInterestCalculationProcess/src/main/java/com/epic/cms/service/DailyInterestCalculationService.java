/**
 * Created By Lahiru Sandaruwan
 * Date : 10/24/2022
 * Time : 8:04 PM
 * Project Name : ecms_eod_engine
 * Topic : dailyInterestCalculation
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.DailyInterestBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.InterestDetailBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.repository.DailyInterestCalculationRepo;
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

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class DailyInterestCalculationService {

    @Autowired
    DailyInterestCalculationRepo interestCalculationRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startDailyInterestCalculation(StatementBean stmtBean) {
        if (!Configurations.isInterrupted) {
            try {
                InterestDetailBean interestDetailBean = interestCalculationRepo.getIntProf(stmtBean.getAccountNo()); /**get interest profile details  for given acc no*/

                /** get transaction, fee, payment list with calculated date diff from
                 *  settlement date to current eod date
                 */
                ArrayList<DailyInterestBean> transactionPaymentList = interestCalculationRepo.getTxnOrPaymentDetailByAccount(stmtBean.getAccountNo(), stmtBean.getStartEodID(), Configurations.EOD_ID, Configurations.EOD_DATE, stmtBean.getStartingBalance(), stmtBean.getStatementStartDate(), stmtBean.getStatementEndDate(), stmtBean.getEndEodID());
                int listSize = 0;
                double accumulateAmount = 0.0;
                double accumulateInterest = 0.0;

                if (transactionPaymentList != null && transactionPaymentList.size() != 0) {
                    listSize = transactionPaymentList.size();
                    int datediff;
                    for (int i = 0; i < listSize - 1; i++) {
                        DailyInterestBean bean = transactionPaymentList.get(i);
                        datediff = bean.getNoOfDays() - transactionPaymentList.get(i + 1).getNoOfDays();
                        accumulateAmount += bean.getAmount();
                        if (accumulateAmount > 0) {
                            accumulateInterest += calculateInterest(accumulateAmount, datediff, interestDetailBean);
                        }
                    }
                    /**calculate interest for last element*/
                    DailyInterestBean lastBean = transactionPaymentList.get(listSize - 1);
                    accumulateAmount += lastBean.getAmount();
                    if (accumulateAmount > 0) {
                        accumulateInterest += calculateInterest(accumulateAmount, lastBean.getNoOfDays(), interestDetailBean);
                    }
                }
                stmtBean.setClosingBalance(stmtBean.getClosingBalance() + accumulateAmount); /**set new outstanding balance*/
                interestCalculationRepo.updateEodInterest(stmtBean, accumulateInterest, interestDetailBean.getInterest()); /**insert or update a record to EODINTEREST table*/

                Configurations.PROCESS_SUCCESS_COUNT++;
                //infoLogger.info("Interest calculated for card number " + CommonMethods.cardNumberMask(stmtBean.getCardNo()));
                LogManager.logInfo("Interest calculated for card number " + CommonMethods.cardNumberMask(stmtBean.getCardNo()), infoLogger);
            } catch (Exception ex) {
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(stmtBean.getCardNo()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.PROCESS_FAILD_COUNT++;
                //errorLogger.error("Interest calculation process failed for card number " + CommonMethods.cardNumberMask(stmtBean.getCardNo()), ex);
                LogManager.logError("Interest calculation process failed for card number " + CommonMethods.cardNumberMask(stmtBean.getCardNo()), ex, errorLogger);
            }
        }
    }

    public synchronized double calculateInterest(Double txnAmount, int dateDiff, InterestDetailBean interestDetailBean) throws Exception {
        double txnInterest = 0.0;
        double a = 0.0;
        double b = 0.0;
        try {
            a = (txnAmount * interestDetailBean.getInterest() * dateDiff);
            b = (100 * interestDetailBean.getInterestperiod());
            if (a != 0.0 & b != 0.0) {
                txnInterest = (txnAmount * interestDetailBean.getInterest() * dateDiff) / (100 * interestDetailBean.getInterestperiod());
            }
        } catch (Exception e) {
            throw e;
        }
        return txnInterest;
    }
}
