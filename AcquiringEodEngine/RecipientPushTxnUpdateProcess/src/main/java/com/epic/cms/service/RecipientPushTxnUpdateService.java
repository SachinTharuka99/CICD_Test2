package com.epic.cms.service;

import com.epic.cms.dao.RecipientPushTxnUpdateDao;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.RecipientPushTxnUpdateRepo;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.Configurations.merchantErrorList;

@Service
public class RecipientPushTxnUpdateService {

    HashMap<String, String> financialStatusList;
    int acqFailedMerchants = 0;
    int totalTxnCount = 0;
    String backendTxnType;
    String destinationAmount = "0";
    public List<ErrorMerchantBean> merchantErrorList = new ArrayList<ErrorMerchantBean>();
    LinkedHashMap details = new LinkedHashMap();
    public int configProcess = Configurations.PROCESS_ID_RECIPIENT_PUSH_TXN_UPDATE;
    public String processHeader = "RECIPIENT_PUSH_TXN_UPDATE_PROCESS";

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList status;

    @Autowired
    RecipientPushTxnUpdateRepo recipientPushTxnUpdateRepo;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    RecipientPushTxnUpdateDao recipientPushTxnUpdateDao;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void recipientPushTxnUpdate(EodTransactionBean eodTransactionBean) throws Exception {
        financialStatusList = recipientPushTxnUpdateDao.getFinancialStatus();
        //Check for financial status yes
        if (isFinancialStatusYes(eodTransactionBean.getTxnType())) {
            totalTxnCount++;
            destinationAmount = "0";
            try {
                String maskedCardNumber = CommonMethods.cardNumberMask(eodTransactionBean.getCardNo());
                details.put("Cardnumber", maskedCardNumber);
                //Mapping with tc code
                backendTxnType = eodTransactionBean.getTxnType();
                details.put("Online Txn Type", eodTransactionBean.getTxnType());
                details.put("EOD Txn Type", backendTxnType);
                destinationAmount = eodTransactionBean.getTxnAmount();
                eodTransactionBean.setCrDr(Configurations.DEBIT);
                details.put("CRDR Type", eodTransactionBean.getCrDr());

                //decide card association based on channel type(5 visa, 3 master)
                if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_VISA) {
                    eodTransactionBean.setCardAssociation(Configurations.VISA_ASSOCIATION);
                } else if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_MASTER) {
                    eodTransactionBean.setCardAssociation(Configurations.MASTER_ASSOCIATION);
                }

                //define cardproduct as QR all products for commision calculation
                String cardProduct = status.getPRODUCT_CODE_QR_ALL();
                try {
                    // for mvisa transactions with visa card bin card product will be VAL
                    cardProduct = recipientPushTxnUpdateRepo.getCardProduct(eodTransactionBean.getCardNo().substring(0, 6));
                    if (cardProduct == null) { // lanka QR transactions, (bin will be not available as it contain account number instead of card number)
                        cardProduct = status.getPRODUCT_CODE_QR_ALL();
                    }
                } catch (Exception ex) {
                    logError.error("error occured when card product decide for recipient transaction" + eodTransactionBean.getTxnId(), ex);
                    cardProduct = status.getPRODUCT_CODE_QR_ALL();
                }
                eodTransactionBean.setCardProduct(cardProduct);

                //Txn amount without forex amount
                eodTransactionBean.setTxnAmount(destinationAmount);
                eodTransactionBean.setBin(eodTransactionBean.getCardNo().substring(0, 6));
                int count = 0;
                count = recipientPushTxnUpdateRepo.insertIntoEodMerchantTransaction(eodTransactionBean, status.getEOD_PENDING_STATUS());//insert querry
                if (count == 1) {
                    details.put("Sync to Merchant Txn table", "Passed");
                    count = 0;
                    //Update txn table to EDON
                    count = recipientPushTxnUpdateRepo.updateTransactionToEDON(eodTransactionBean.getTxnId(), eodTransactionBean.getCardNo());
                }
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                details.put("Sync to Merchant Txn table", "Failed");
                acqFailedMerchants++;
                Configurations.PROCESS_FAILD_COUNT++;

                Configurations.merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, eodTransactionBean.getMid(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, MerchantCustomer.MERCHANTLOCATION));
                details.put("Process Status", "Failed");

                logError.error(processHeader + " failed for mid " + eodTransactionBean.getMid(), e);
                logInfo.info(processHeader + " failed for mid " + eodTransactionBean.getMid());

            }
           logInfo.info(logManager.logDetails(details));
            details.clear();
        }

    }

    private boolean isFinancialStatusYes(String txnType) throws Exception {
        boolean status = false;
        String financialStatus = "NO";
        try {
            financialStatus = financialStatusList.get(txnType);
            if (financialStatus.equalsIgnoreCase(Configurations.YES_STATUS)) {
                status = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return status;
    }




}
