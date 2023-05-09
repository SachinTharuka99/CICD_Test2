package com.epic.cms.service;

import com.epic.cms.dao.AdjustmentDao;
import com.epic.cms.model.bean.AdjustmentBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class AdjustmentService {

    @Autowired
    public LogManager logManager;

    @Autowired
    public CommonRepo commonRepo;

    @Autowired
    public AdjustmentDao adjustmentDao;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void proceedAdjustment(AdjustmentBean adjustmentBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            PaymentBean pb = new PaymentBean();
            String seqNo;
            String maskedCardNumber = null;

            try {
                maskedCardNumber = CommonMethods.cardNumberMask(adjustmentBean.getCardNumber());
                details.put("Card Number", maskedCardNumber);
                details.put("Account Number", adjustmentBean.getAccNo());
                details.put("Transaction Type", adjustmentBean.getTxnType());
                details.put("Adjustment Amount", adjustmentBean.getAdjustAmount());
                details.put("CRDR Type", adjustmentBean.getCrDr());
                details.put("Adjustment ID", adjustmentBean.getId());

                seqNo = CommonMethods.validate(Integer.toString(Configurations.ADJUSTMENT_SEQUENCE_NO), 8, '0');

                //If a CR it is considered as a credit payment
                if (adjustmentBean.getCrDr().equalsIgnoreCase("CR")) {
                    adjustmentBean.setSequenceNo(seqNo);
                    adjustmentBean.setAdjustTxnType(Configurations.TXN_TYPE_ADJUSTMENT_CREDIT);
                    adjustmentBean.setTxnType(Configurations.TXN_TYPE_PAYMENT);
                    adjustmentBean.setAdjustType(Integer.toString(Configurations.PAYMENT_ADJUSTMENT_TYPE));
                    adjustmentBean.setPaymentType("CASH");

                    pb.setEodid(Configurations.EOD_ID);
                    pb.setTraceid(seqNo);
                    pb.setSequencenumber(seqNo);
                    pb.setCardnumber(adjustmentBean.getCardNumber());
                    pb.setMaincardno(commonRepo.getMainCardNumber(adjustmentBean.getCardNumber()));
                    if (pb.getCardnumber().equals(pb.getMaincardno())) {
                        pb.setIsprimary("YES");
                    } else {
                        pb.setIsprimary("NO");
                    }
                    pb.setAmount(adjustmentBean.getAdjustAmount());
                    pb.setPaymenttype(adjustmentBean.getPaymentType());
                    //insert to eod payment
                    adjustmentDao.insertToEODPayments(pb);
                }

                //All other DB insted fee & Cash advances act as sale
                if (Integer.parseInt(adjustmentBean.getAdjustType()) == Configurations.FEE_ADJUSTMENT_TYPE || Integer.parseInt(adjustmentBean.getAdjustType()) == Configurations.INTEREST_ADJUSTMENT_TYPE || Integer.parseInt(adjustmentBean.getAdjustType()) == Configurations.INSTALLMENT_ADJUSTMENT_TYPE || Integer.parseInt(adjustmentBean.getAdjustType()) == Configurations.TRANSACTION_ADJUSTMENT_TYPE || (Integer.parseInt(adjustmentBean.getAdjustType()) == Configurations.PAYMENT_ADJUSTMENT_TYPE && adjustmentBean.getCrDr().equalsIgnoreCase("DR"))) {
                    adjustmentBean.setTxnType(Configurations.TXN_TYPE_SALE);
                    adjustmentBean.setAdjustTxnType(Configurations.TXN_TYPE_ADJUSTMENT_DEBIT);
                }
                //Cash advances must consider as cash advance debit
                if (Integer.parseInt(adjustmentBean.getAdjustType()) == Configurations.CASH_ADVANCE_ADJUSTMENT_TYPE) {
                    adjustmentBean.setAdjustTxnType(Configurations.TXN_TYPE_ADJUSTMENT_DEBIT);
                }
                details.put("Adjustment Type", adjustmentBean.getAdjustTxnType());
                String cardAssociation = adjustmentDao.getCardAssociationFromCardBin(adjustmentBean.getCardNumber().substring(0, 6));
                if (cardAssociation == null) {
                    cardAssociation = adjustmentDao.getCardAssociationFromCardBin(adjustmentBean.getCardNumber().substring(0, 8)); //check 8 digit bin available
                }
                adjustmentDao.insertInToEODTransaction(adjustmentBean, cardAssociation);
                adjustmentDao.updateAdjustmentStatus(adjustmentBean.getId());
                adjustmentDao.updateTransactionToEDON(adjustmentBean.getTxnId());

                details.put("Adjustment Sync Status", "Passed");
                Configurations.ADJUSTMENT_SEQUENCE_NO++;
                Configurations.PROCESS_SUCCESS_COUNT++;

            } catch (Exception ex) {
                Configurations.PROCESS_FAILD_COUNT++;
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(adjustmentBean.getCardNumber()), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                details.put("Adjustment Sync Status", "Failed");
                logManager.logInfo("ADJUSTMENT_PROCESS failed for card number " + maskedCardNumber, infoLogger);
                logManager.logError("ADJUSTMENT_PROCESS failed for card number " + maskedCardNumber, ex, errorLogger);
            } finally {
                logManager.logDetails(details, infoLogger);
                /** PADSS Change -variables handling card data should be nullified
                 by replacing the value of variable with zero and call NULL function */
                CommonMethods.clearStringBuffer(pb.getCardnumber());
                CommonMethods.clearStringBuffer(pb.getMaincardno());
            }
        }
    }
}
