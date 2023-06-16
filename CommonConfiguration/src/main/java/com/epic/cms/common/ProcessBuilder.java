package com.epic.cms.common;

import com.epic.cms.Exception.FailedCardException;
import com.epic.cms.dao.ProcessBuilderDao;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.KafkaMessageUpdator;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;


public abstract class ProcessBuilder {

    public LinkedHashMap details = new LinkedHashMap();
    public LinkedHashMap summery = new LinkedHashMap();
    public ProcessBean processBean = null;

    //public List<ErrorCardBean> cardErrorList = Collections.synchronizedList(new ArrayList<ErrorCardBean>());
    //public List<ErrorMerchantBean> merchantErrorList = Collections.synchronizedList(new ArrayList<ErrorMerchantBean>());

    public String startHeader = null;
    public String endHeader = null;
    public String failedHeader = null;
    public String completedHeader = null;
    public String processHeader = "Define Process";
    public String StartEodStatus = null;
    public int hasErrorEODandProcess = 0;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    ProcessBuilderDao processBuilderRepo;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    LogManager logManager;


    public void startProcess(int processId, String uniqueId) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            ProcessBean processBean = processBuilderRepo.getProcessDetails(processId);
            Configurations.EOD_ID = processBuilderRepo.getRuninngEODId(statusVarList.getINPROGRESS_STATUS(), statusVarList.getERROR_INPR_STATUS());
            Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
            System.out.println("EOD ID :" + Configurations.ERROR_EOD_ID);
            Configurations.EOD_DATE = getDateFromEODID(Configurations.EOD_ID);
            Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);
            StartEodStatus = Configurations.STARTING_EOD_STATUS;
            boolean isErrorProcess = processBuilderRepo.isErrorProcess(processId);
            this.processHeader = processBean.getProcessDes();
            setupProcessDescriptions();

            if (StartEodStatus.equals("EROR")) {
                //This is running on a failed EOD process
                if (processId != Configurations.PROCESS_ID_SNAPSHOT) {
                    if (isErrorProcess) {
                        //Current process threw errors at previous EOD
                        hasErrorEODandProcess = 1;
                    } else {
                        //Current process did not throw errors. So ignore this process.
                        hasErrorEODandProcess = 2;
                    }
                } else {
                    hasErrorEODandProcess = 0;
                }
            } else {
                //This is a normal process in INIT EOD state
                hasErrorEODandProcess = 0;
            }

            if (hasErrorEODandProcess == 1 && processBean != null || hasErrorEODandProcess == 0 && processBean != null) {
                logInfo.info(logManager.logHeader(processHeader));
                logInfo.info(logManager.logStartEnd(startHeader));
                commonRepo.insertToEodProcessSumery(processId);
                /**
                 * Abstract method call.
                 */
                concreteProcess();

                if (Configurations.IS_PROCESS_COMPLETELY_FAILED) {
                    throw new FailedCardException(processHeader);
                }
                /**
                 * Add any failed cards at EOD
                 */
                insertFailedEODCards(processId);
                //commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getSUCCES_STATUS(), processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
            } else if (hasErrorEODandProcess == 2 && processBean != null) {
                System.out.println("Skipping this process since Process not under error: " + processBean.getProcessDes());
                commonRepo.updateEODProcessCount(uniqueId);
                return;
            }

        } catch (FailedCardException ex) {
            System.out.println(" --------------------- Failed card exception 1------------------");
            logInfo.info(logManager.logStartEnd(failedHeader));
            logInfo.error(failedHeader);
            commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, "EROR", processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
        } catch (Exception ex) {
            System.out.println(" --------------------- Failed card exception 2------------------");
            logInfo.info(logManager.logStartEnd(failedHeader));
            logInfo.error(failedHeader);
            //add updateeodprocesssummary
//            if (ex instanceof FailedCardException) {
//                commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getERROR_STATUS(), processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
//            }
        } finally {
            addSummaries();
            logInfo.info(logManager.logSummery(summery));
            kafkaMessageUpdator.producerWithNoReturn("true", "processStatus");
            kafkaMessageUpdator.producerWithNoReturn(!Configurations.IS_PROCESS_COMPLETELY_FAILED, "eodEngineConsumerStatus");
            System.out.println("Send the process success status");
            logInfo.info(logManager.logStartEnd(completedHeader));
            commonRepo.updateEODProcessCount(Configurations.eodUniqueId);
        }
    }

    void insertFailedEODCards(int processId) throws Exception {
        int failedCardListSize = Configurations.errorCardList.size();
        if (failedCardListSize == 0) {
            commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.getSUCCES_STATUS(), processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
        } else {
            for (int i = 0; i < failedCardListSize; i++) {
                commonRepo.insertErrorEODCard(Configurations.errorCardList.get(i));
            }
            if (failedCardListSize > 0) {
                Configurations.errorCardList.clear();
                throw new FailedCardException(processHeader);
            }
        }
    }

    private void setupProcessDescriptions() {
        this.startHeader = processHeader + " started";
        this.endHeader = processHeader + " finished";
        this.failedHeader = processHeader + " failed";
        this.completedHeader = processHeader + " completed";
    }

    public static String eodDashboardProcessProgress(int successCount, int totalCount) {
        String progressStr = "";
        int progress = 0;
        if (successCount != 0 && totalCount != 0) {
            progress = (successCount * 100 / totalCount);
            progressStr = String.valueOf(progress) + "%";
            return progressStr;
        } else if (successCount == 0 && totalCount != 0) {
            return "0%";
        } else {
            return "100%";
        }
    }

    public abstract void concreteProcess() throws Exception;

    public abstract void addSummaries();

    public Date getDateFromEODID(int eodId) {
        Date parsedDate = null;
        String streodID = "";
        try {
            if (eodId > 10000000) {
                streodID = eodId + "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                String eodIDsubs = streodID.substring(0, streodID.length() - 2);
                parsedDate = sdf.parse(eodIDsubs);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            return parsedDate;
        }
    }
}
