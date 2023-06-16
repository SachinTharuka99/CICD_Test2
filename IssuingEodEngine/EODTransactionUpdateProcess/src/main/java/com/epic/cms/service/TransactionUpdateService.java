package com.epic.cms.service;

import com.epic.cms.repository.TransactionUpdateRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TransactionUpdateService {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    public LogManager logManager;
    @Autowired
    public StatusVarList status;
    @Autowired
    public TransactionUpdateRepo transactionUpdateRepo;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
                    logError.error("EOD Visa Transaction Update Process process failed ", ex);
                }
                break;
            // update master transactions
            case "MASTER":
                try {
                    txnCounts = transactionUpdateRepo.callStoredProcedureForMasterTxnUpdate();

                    Configurations.MASTER_TXN_UPDATE_COUNT = txnCounts[2];
                    Configurations.FAILED_MASTER_TXN_COUNT = txnCounts[1];
                } catch (Exception ex) {
                    logError.error("EOD Master Transaction Update Process process failed ", ex);
                }
                break;
        }
    }
}
