/**
 * Author : yasiru_l
 * Date : 12/5/2022
 * Time : 11:37 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CardApplicationRejectLetterConnector extends ProcessBuilder {

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

    @Override
    public void concreteProcess() throws Exception {

        String[] fileNameAndPath = null;
        try {

            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT;
            CommonMethods.eodDashboardProgressParametersReset();
            ArrayList<String> applicationIdList;
            String StartEodStatus = Configurations.STARTING_EOD_STATUS;

            boolean isErrorProcess = commonRepo.isErrorProcess(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            boolean isProcessCompletlyFail = commonRepo.isProcessCompletlyFail(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            applicationIdList = cardApplicationRejectLetterRepo.getRejectApplictionIDsToGenerateLetters(StartEodStatus, isErrorProcess, isProcessCompletlyFail);

            int sequenceNo = 0;
            for (int i = 0; i < applicationIdList.size(); i++) {
                fileNameAndPath = cardApplicationRejectLetterService.processCardApplicationReject(applicationIdList.get(i),sequenceNo);
                sequenceNo++;
            }

            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = applicationIdList.size();

            summery.put("Started Date ", Configurations.EOD_DATE.toString());
            summery.put("Total No of Effected Files ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
            summery.put("Process Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
            summery.put("Process Failed Count ", Configurations.PROCESS_FAILD_COUNT);
            summery.put("File Name and Path ", fileNameAndPath);

            infoLogger.info(logManager.processSummeryStyles(summery));

        } catch (Exception e) {
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;

            ErrorCardBean errorBean = new ErrorCardBean();
            errorBean.setCardNo(new StringBuffer());
            errorBean.setAccountNo("");
            errorBean.setCustomerID("");
            errorBean.setEodID(Configurations.EOD_ID);
            errorBean.setProcessId(Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT);
            errorBean.setIsProcessFails(1);

            errorLogger.error("Card Application Rejected Letter Process Failed", e);

            if(fileNameAndPath!= null){
                fileGenerationService.deleteExistFile(fileNameAndPath[0]);
            }
        }
    }
}
