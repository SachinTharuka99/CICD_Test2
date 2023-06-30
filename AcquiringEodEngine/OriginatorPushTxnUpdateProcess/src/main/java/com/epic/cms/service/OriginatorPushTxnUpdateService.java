/**
 * Author : rasintha_j
 * Date : 6/20/2023
 * Time : 10:36 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.dao.OriginatorPushTxnUpdateDao;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.util.CardAccount;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
public class OriginatorPushTxnUpdateService {

    @Autowired
    OriginatorPushTxnUpdateDao originatorPushTxnUpdateDao;
    @Autowired
    LogManager logManager;
    BigDecimal destinationAmount = new BigDecimal(BigInteger.ZERO);
    String backendTxnType;
    String forexPercentage = "0";
    HashMap<String, String> visaTxnFields;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void originatorPushTxnUpdate(EodTransactionBean eodTransactionBean) throws Exception {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();

            forexPercentage = originatorPushTxnUpdateDao.getForexPercentage();

            visaTxnFields = originatorPushTxnUpdateDao.getFinancialStatus();

            //Check for financial status yes
            if (isFinancialStatusYes(eodTransactionBean.getTxnType())) {
                Configurations.totalTxnCount++;
                destinationAmount = new BigDecimal(BigInteger.ZERO);
                String maskedCardNumber = CommonMethods.cardNumberMask(eodTransactionBean.getCardNo());
                try {

                    details.put("Cardnumber", maskedCardNumber);
                    //Mapping with tc code
                    backendTxnType = eodTransactionBean.getTxnType();
                    details.put("Online Txn Type", eodTransactionBean.getTxnType());
                    details.put("EOD Txn Type", backendTxnType);
                    destinationAmount = new BigDecimal(eodTransactionBean.getTxnAmount());

                    eodTransactionBean.setCrDr(Configurations.DEBIT);

                    details.put("Currency Type", eodTransactionBean.getCurrencyType());
                    Configurations.onusTxnCount++;
                    //Calculate Forex Amount
                    if (!eodTransactionBean.getCurrencyType().equalsIgnoreCase(Configurations.BASE_CURRENCY)) {
                        BigDecimal forexMarkupAmount = destinationAmount.multiply(new BigDecimal(forexPercentage)).divide(BigDecimal.valueOf(100), MathContext.DECIMAL32).setScale(2, RoundingMode.DOWN);
                        String destinationAmountWithForex = destinationAmount.add(forexMarkupAmount).setScale(2, RoundingMode.DOWN).toString();
                        eodTransactionBean.setForexMarkupAmount(forexMarkupAmount.toString());
                        eodTransactionBean.setTxnAmount(destinationAmountWithForex);
                        details.put("Forex amount", eodTransactionBean.getForexMarkupAmount());
                    }

                    //decide card association based on channel type(5 visa, 3 master)
                    if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_VISA) {
                        eodTransactionBean.setCardAssociation(Configurations.VISA_ASSOCIATION);
                    } else if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_MASTER) {
                        eodTransactionBean.setCardAssociation(Configurations.MASTER_ASSOCIATION);
                    }
                    int count = originatorPushTxnUpdateDao.insertToEODTransaction(eodTransactionBean.getCardNo(), eodTransactionBean.getAccountNo(),
                            eodTransactionBean.getMid(), eodTransactionBean.getTid(), eodTransactionBean.getBillingAmount(), Integer.parseInt(eodTransactionBean.getCurrencyType()),
                            eodTransactionBean.getCrDr(), eodTransactionBean.getSettlementDate(), eodTransactionBean.getTxnDate(), backendTxnType,
                            eodTransactionBean.getBatchNo(), eodTransactionBean.getTxnId(), eodTransactionBean.getToAccNo(), 0.0, eodTransactionBean.getTxnDescription(),
                            eodTransactionBean.getCountryNumCode(), eodTransactionBean.getOnOffStatus(), eodTransactionBean.getPosEntryMode(), eodTransactionBean.getTraceId(),
                            eodTransactionBean.getAuthCode(), 5, eodTransactionBean.getRequestFrom(), eodTransactionBean.getSecondPartyPan(), eodTransactionBean.getFuelSurchargeAmount(), eodTransactionBean.getMcc(), eodTransactionBean.getCardAssociation());
                    if (count == 1) {
                        count = 0;
                        //Update txn table to EDON
                        count = originatorPushTxnUpdateDao.updateTransactionToEDON(eodTransactionBean.getTxnId(), eodTransactionBean.getCardNo());
                    }
                    details.put("Total txn amount", eodTransactionBean.getTxnAmount());
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } catch (Exception e) {
                    details.put("Insert to EOD Txn table", "Failed");
                    Configurations.issFailedTxn++;
                    Configurations.PROCESS_FAILD_COUNT++;
                    Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(eodTransactionBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                    details.put("Process Status", "Failed");
                    logInfo.info("failed for cardnumber " + eodTransactionBean.getCardNo());
                }
                logInfo.info(logManager.logDetails(details));
                details.clear();
            }
        }
    }

    private boolean isFinancialStatusYes(String txnType) throws Exception {
        boolean status = false;
        String financialStatus = "NO";
        try {
            financialStatus = visaTxnFields.get(txnType);
            if (financialStatus.equalsIgnoreCase(Configurations.YES_STATUS)) {
                status = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return status;
    }
}
