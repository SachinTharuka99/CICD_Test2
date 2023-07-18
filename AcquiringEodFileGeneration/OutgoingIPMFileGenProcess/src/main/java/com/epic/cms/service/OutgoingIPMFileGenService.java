package com.epic.cms.service;

import com.epic.cms.Exception.RejectException;
import com.epic.cms.model.bean.IRDCriteriaBean;
import com.epic.cms.model.bean.TransactionDataBean;
import com.epic.cms.repository.OutgoingIPMFileGenRepo;
import com.epic.cms.util.Configurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@Service
public class OutgoingIPMFileGenService {

    @Autowired
    MasterFieldValidator masterFieldValidator;

    @Autowired
    OutgoingIPMFileGenRepo outgoingIPMFileGenRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void processOutgoingTransactionData(TransactionDataBean bean) {
        try {
            masterFieldValidator.addTxnToMasterOutgoingFieldIdentityTable(bean);
            outgoingIPMFileGenRepo.updateEodMerchantTransactionFileStatus(bean.getTxnId()); // set file status to 1 in EODMERCHANTTRANSACTION Table

            //commit the transaction one by one
        } catch (InvocationTargetException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof RejectException) { //if exception occured for reject from validation
                RejectException rejEx = (RejectException) ex.getCause();
                //add reject transaction to OUTGOINGREJECT Table
                outgoingIPMFileGenRepo.insertRejectMasterOutgoingTransaction(Integer.toString(Configurations.EOD_ID), bean.getTxnId(), rejEx.getMessage());

            } else { //not a reject exception
                logError.error("Exception occured while processing the transaction: " + bean.getTxnId() ,ex);
            }
            //failedTxnCount++;

        } catch (Exception ex) {
            logError.error("Exception occured while processing the transaction: " + bean.getTxnId(), ex);
            //failedTxnCount++;
        }
    }

    /**
     * Recalculate IRD VALUE (PDS 0158 Subfield 04) since it need to decide based on lots of criteria. this will override default value
     * get IRD criteria from Database
     *
     * @return
     */
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public int recalculateIRDValue() {
        int irdNotDecidedTxnCount = 0;
        ArrayList<IRDCriteriaBean> irdCriteriaList = outgoingIPMFileGenRepo.getIRDCriteriaList("SLINTRACOUNTRY");
        for (IRDCriteriaBean irdCriteriaRule : irdCriteriaList) {
            ArrayList<String> matchingTxnListForIRDCriteria = outgoingIPMFileGenRepo.getMatchingTxnsForIRDCriteria(irdCriteriaRule.getCriterias(), irdCriteriaRule.getID());
            for (String matchingTxnId : matchingTxnListForIRDCriteria) {
                outgoingIPMFileGenRepo.updateIRDValue(irdCriteriaRule.getIRD(), matchingTxnId);
            }
        }

        irdCriteriaList = outgoingIPMFileGenRepo.getIRDCriteriaList("ASIAREGION");
        for (IRDCriteriaBean irdCriteriaRule : irdCriteriaList) {
            ArrayList<String> matchingTxnListForIRDCriteria = outgoingIPMFileGenRepo.getMatchingTxnsForIRDCriteria(irdCriteriaRule.getCriterias(), irdCriteriaRule.getID());
            for (String matchingTxnId : matchingTxnListForIRDCriteria) {
                outgoingIPMFileGenRepo.updateIRDValue(irdCriteriaRule.getIRD(), matchingTxnId);
            }
        }

        irdCriteriaList = outgoingIPMFileGenRepo.getIRDCriteriaList("INTERREGION");
        for (IRDCriteriaBean irdCriteriaRule : irdCriteriaList) {
            ArrayList<String> matchingTxnListForIRDCriteria = outgoingIPMFileGenRepo.getMatchingTxnsForIRDCriteria(irdCriteriaRule.getCriterias(), irdCriteriaRule.getID());
            for (String matchingTxnId : matchingTxnListForIRDCriteria) {
                outgoingIPMFileGenRepo.updateIRDValue(irdCriteriaRule.getIRD(), matchingTxnId);
            }
        }
        //set file status = 2 for undecided ird value txns. rule might not defined. prevent include them in ipm file. when correct the rule it will automatically pick to next day file
        irdNotDecidedTxnCount = outgoingIPMFileGenRepo.updateUndecidedIRDFileStatus();
        //con.commit();
        return irdNotDecidedTxnCount;
    }
}
