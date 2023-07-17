package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.AcqTxnUpdateRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.AcqTxnUpdateService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AcqTxnUpdateConnector extends ProcessBuilder {
    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("taskExecutor2")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    AcqTxnUpdateService acqTxnUpdateService;

    @Autowired
    AcqTxnUpdateRepo acqTxnUpdateRepo;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    public List<ErrorMerchantBean> merchantErrorList = new ArrayList<ErrorMerchantBean>();
    public int configProcess = Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS;
    public String processHeader = "ACQUIRING_TXN_UPDATE_PROCESS";
    HashMap<Integer, ArrayList<EodTransactionBean>> txnMap;
    ArrayList<EodTransactionBean> txnList;
    public List<ErrorCardBean> cardErrorList = new ArrayList<ErrorCardBean>();
    int issFailedTxn = 0;
    int acqFailedMerchants = 0;
    int onusTxnCount = 0;
    int totalTxnCount = 0;
    String forexPercentage = "0";
    String fuelSurchargeRate = "0";
    List<String> fuelMccList;
    HashMap<String, String> visaTxnFields;
    @Override
    public void concreteProcess() throws Exception {
        ProcessBean processBean = null;

        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS);
            if (processBean != null) {
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS;
                CommonMethods.eodDashboardProgressParametersReset();

                //Get all settle txn based on REQUESTFROM,EODSTATUS,STATUS,RESPONSECODE
                txnMap = commonRepo.getAllSettledTxnFromTxn();

                forexPercentage = acqTxnUpdateRepo.getForexPercentage();
                fuelSurchargeRate = acqTxnUpdateRepo.getFuelSurchargeRatePercentage();
                fuelMccList = acqTxnUpdateRepo.getFuelMccList();

                visaTxnFields = acqTxnUpdateRepo.getFinancialStatus();

                //Insert all settle txn to EODMERCHANTTXN Table
                for (Map.Entry<Integer, ArrayList<EodTransactionBean>> entrySet : txnMap.entrySet()) {
                    Integer key = entrySet.getKey();
                    txnList = entrySet.getValue();
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += txnList.size();


                    if (txnList != null && txnList.size() > 0) {
                        for (EodTransactionBean eodTransactionBean : txnList) {
                            acqTxnUpdateService.processAcqTxnUpdate(key, eodTransactionBean, visaTxnFields, fuelMccList);
                        }
                    }
                    //wait till all the threads are completed
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                }

                totalTxnCount = Configurations.totalTxnCount_AcqTxnUpdateProcess;
                issFailedTxn = Configurations.failedTxnCount_AcqTxnUpdateProcess;
                acqFailedMerchants = Configurations.acqFailedMerchantCount_AcqTxnUpdateProcess;
                onusTxnCount = Configurations.onusTxnCount_AcqTxnUpdateProcess;

                //***********************************************IMPORTANT ***********************************************
                //Set card product for sync txn in eodmerchanttxn table
                this.setCardProduct();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = totalTxnCount;
                Configurations.PROCESS_SUCCESS_COUNT = (totalTxnCount - (issFailedTxn + acqFailedMerchants));
                Configurations.PROCESS_FAILD_COUNT = (issFailedTxn + acqFailedMerchants);

                if (issFailedTxn > 0) {
                    //CommonMethods.insertFailedEODCards(cardErrorList, conComitTrue, processHeader);
//                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                    //logInfo.info(logManager.logStartEnd(processHeader + "  completed with errors in card level"));
                }
                if (acqFailedMerchants > 0) {
                    //CommonMethods.insertFailedEODMerchants(merchantErrorList, conComitTrue, processHeader);
//                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getERROR_STATUS(), Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                    //logInfo.info(logManager.logStartEnd(processHeader + "  completed with errors in merchant level"));
                }
                if (issFailedTxn == 0 && acqFailedMerchants == 0) {
//                    commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, status.getSUCCES_STATUS(), Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, CommonMethods.eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
                    //logInfo.info(logManager.logStartEnd(processHeader + "completed without errors"));
                }
            }
        }catch (Exception e){
            try {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                logError.error(processHeader + " failed", e);
                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }
            } catch (Exception e2) {
                logError.error("Failed Acq Txn Update Process ",e2);
            }
        } finally {
            //logInfo.info(logManager.logSummery(summery));
            try {
                /* PADSS Change -
                variables handling card data should be nullified
                by replacing the value of variable with zero and call NULL function */
                if (txnList != null && txnList.size() != 0) {
                    for (EodTransactionBean eodTransactionBean : txnList) {
                        CommonMethods.clearStringBuffer(eodTransactionBean.getCardNo());
                    }
                    txnList = null;
                }
            } catch (Exception e3) {
                logError.error("Failed Acq Txn Update Process ",e3);
            }
        }

    }

    @Override
    public void addSummaries() {
        summery.put("Started Date", Configurations.EOD_DATE.toString());
        summery.put("No of OnUsTxn effected", Integer.toString(onusTxnCount));
        summery.put("No of Success OnUsTxn ", Integer.toString(onusTxnCount - issFailedTxn));
        summery.put("No of fail OnUsTxn ", Integer.toString(issFailedTxn));
        summery.put("No of Total Txn", Integer.toString(totalTxnCount));
        summery.put("No of Total failed Txn ", Integer.toString(issFailedTxn + acqFailedMerchants));

    }


    private void setCardProduct() {
        try {
            //acqBackendDbConn.setCardProductToEodMerTxn();
            //con.commit();
        } catch (Exception e) {
            try {
                //con.rollback();
            } catch (Exception ex) {
                /*try {
                    //WebComHandler.showOnWeb(CommonMethods.eodDashboardProcessInfoStyle(ex.getMessage()));
                    errorLogger.error(String.valueOf(ex));
                } catch (IOException ex1) {
                    Logger.getLogger(OnusAcqTxnUpdateProcess.class.getName()).log(Level.SEVERE, null, ex1);
                }*/
            }

        }
    }
}
