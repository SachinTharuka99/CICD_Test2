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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

public abstract class ProcessBuilder {

    public LinkedHashMap details = new LinkedHashMap();
    public LinkedHashMap summery = new LinkedHashMap();
    public ProcessBean processBean = null;

    public List<ErrorCardBean> cardErrorList = Collections.synchronizedList(new ArrayList<ErrorCardBean>());
    public List<ErrorMerchantBean> merchantErrorList = Collections.synchronizedList(new ArrayList<ErrorMerchantBean>());

    public String startHeader = null;
    public String endHeader = null;
    public String failedHeader = null;
    public String completedHeader = null;
    public String processHeader = "Define Process";
    public String StartEodStatus = null;
    public int hasErrorEODandProcess = 0;

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
        try {
            ProcessBean processBean = processBuilderRepo.getProcessDetails(processId);

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
                logManager.logHeader(processHeader, infoLogger);
                logManager.logStartEnd(startHeader, infoLogger);
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
                insertFailedEODCards();
                commonRepo.updateEodProcessSummery(Configurations.ERROR_EOD_ID, statusVarList.SUCCES_STATUS, processId, Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_FAILD_COUNT, eodDashboardProcessProgress(Configurations.PROCESS_SUCCESS_COUNT, Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS));
            } else if (hasErrorEODandProcess == 2 && processBean != null) {
                System.out.println("Skipping this process since Process not under error: " + processBean.getProcessDes());
                commonRepo.updateEODProcessCount(uniqueId);
                return;
            }

        } catch (Exception ex) {
            logManager.logStartEnd(failedHeader, infoLogger);
            logManager.logError(failedHeader, ex, errorLogger);
            if (ex instanceof FailedCardException) {

            }
        } finally {
            addSummaries();
            logManager.logSummery(summery, infoLogger);
            kafkaMessageUpdator.producerWithNoReturn("true", "processStatus");
            kafkaMessageUpdator.producerWithNoReturn(!Configurations.IS_PROCESS_COMPLETELY_FAILED, "eodEngineConsumerStatus");
            System.out.println("Send the process success status");
            logManager.logStartEnd(completedHeader, infoLogger);
            commonRepo.updateEODProcessCount(Configurations.eodUniqueId);
        }
    }

    void insertFailedEODCards() throws Exception {
        int failedCardListSize = cardErrorList.size();
        for (int i = 0; i < cardErrorList.size(); i++) {
            commonRepo.insertErrorEODCard(cardErrorList.get(i));
        }
        if (failedCardListSize > 0) {
            cardErrorList.clear();
            throw new FailedCardException(processHeader);
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
}
