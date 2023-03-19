package com.epic.cms.service;

import com.epic.cms.repository.TransactionUpdateRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class TransactionUpdateService {
    @Autowired
    LogManager logManager;

    @Autowired
    public StatusVarList status;

    @Autowired
    public TransactionUpdateRepo transactionUpdateRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void transactionUpdate(String cardAssociation) {
        int[] txnCounts = new int[3];
        switch (cardAssociation) {
            // update visa transactions
            case "VISA":
                try {
                    txnCounts = transactionUpdateRepo.callStoredProcedureForVisaTxnUpdate();

                    Configurations.VISA_TXN_UPDATE_COUNT = txnCounts[2];
                    Configurations.FAILED_VISA_TXN_COUNT = txnCounts[1];
                } catch (Exception ex) {
                    logManager.logError("EOD Visa Transaction Update Process process failed ", ex, errorLogger);
                }
                break;
            // update master transactions
            case "MASTER":
                try {
                    txnCounts = transactionUpdateRepo.callStoredProcedureForMasterTxnUpdate();

                    Configurations.MASTER_TXN_UPDATE_COUNT = txnCounts[2];
                    Configurations.FAILED_MASTER_TXN_COUNT = txnCounts[1];
                } catch (Exception ex) {
                    logManager.logError("EOD Master Transaction Update Process process failed ", ex, errorLogger);
                }
                break;
        }
    }
}
