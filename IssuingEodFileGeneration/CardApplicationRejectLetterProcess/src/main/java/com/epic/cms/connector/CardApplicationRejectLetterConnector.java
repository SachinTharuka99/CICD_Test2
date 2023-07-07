/**
 * Author : yasiru_l
 * Date : 12/5/2022
 * Time : 11:37 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.repository.CardApplicationRejectLetterRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.LetterRepo;
import com.epic.cms.service.CardApplicationRejectLetterService;
import com.epic.cms.service.FileGenerationService;
import com.epic.cms.service.LetterService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CardApplicationRejectLetterConnector extends FileGenProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    CardApplicationRejectLetterRepo cardApplicationRejectLetterRepo;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    LetterRepo letterRepo;
    @Autowired
    LetterService letterService;
    @Autowired
    FileGenerationService fileGenerationService;
    @Autowired
    CardApplicationRejectLetterService cardApplicationRejectLetterService;
    String[] fileNameAndPath = null;

    @Override
    public void concreteProcess() throws Exception {
        try {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT;
            CommonMethods.eodDashboardProgressParametersReset();
            ArrayList<String> applicationIdList;
            String StartEodStatus = Configurations.STARTING_EOD_STATUS;

            boolean isErrorProcess = commonRepo.isErrorProcess(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            boolean isProcessCompletlyFail = commonRepo.isProcessCompletlyFail(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            applicationIdList = cardApplicationRejectLetterRepo.getRejectApplictionIDsToGenerateLetters(StartEodStatus, isErrorProcess, isProcessCompletlyFail);
            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = applicationIdList.size();

            int sequenceNo = 0;
            for (int i = 0; i < applicationIdList.size(); i++) {
                fileNameAndPath = cardApplicationRejectLetterService.processCardApplicationReject(applicationIdList.get(i), sequenceNo);
                sequenceNo++;
            }

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;

            ErrorCardBean errorBean = new ErrorCardBean();
            errorBean.setCardNo(new StringBuffer());
            errorBean.setAccountNo("");
            errorBean.setCustomerID("");
            errorBean.setEodID(Configurations.EOD_ID);
            errorBean.setProcessId(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            errorBean.setIsProcessFails(1);

            logError.error("Card Application Rejected Letter Process Failed", e);

            if (fileNameAndPath != null) {
                fileGenerationService.deleteExistFile(fileNameAndPath[0]);
            }
        } finally {
            logInfo.info(logManager.logSummery(summery));
        }
    }

    @Override
    public void addSummaries() {
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("Total No of Effected Files ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("Process Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("Process Failed Count ", Configurations.PROCESS_FAILD_COUNT);
    }
}
