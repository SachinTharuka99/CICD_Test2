/**
 * Author : lahiru_p
 * Date : 2/1/2023
 * Time : 9:38 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.connector.*;
import com.epic.cms.util.Configurations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProcessThreadService {
    @Async("ThreadPool_100")
    public void startProcessByProcessId(int processId) throws Exception {

        if (processId == Configurations.AUTO_SETTLEMENT_PROCESS) {
            AutoSettlementConnector autoSettlement = new AutoSettlementConnector();
            autoSettlement.startProcess();
        } else if (processId == Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE) {
            CardApplicationConfirmationLetterConnector cardApplicationConfirmationLetterConnector = new CardApplicationConfirmationLetterConnector();
            cardApplicationConfirmationLetterConnector.startProcess();
        } else if (processId == Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT) {
            CardApplicationRejectLetterConnector cardApplicationRejectLetterConnector = new CardApplicationRejectLetterConnector();
            cardApplicationRejectLetterConnector.startProcess();
        } else if (processId == Configurations.PROCESS_ID_CARDRENEW_LETTER) {
            CardRenewLetterConnector cardRenewLetterConnector = new CardRenewLetterConnector();
            cardRenewLetterConnector.startProcess();
        } else if (processId == Configurations.PROCESS_ID_CARD_REPLACE) {
            CardReplaceLetterConnector cardReplaceLetterConnector = new CardReplaceLetterConnector();
            cardReplaceLetterConnector.startProcess();
        } else if (processId == Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION) {
            CashBackFileGenConnector cashBackFileGenConnector = new CashBackFileGenConnector();
            cashBackFileGenConnector.startProcess();
        } else if (processId == Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS) {
            CollectionAndRecoveryLetterConnector collectionAndRecoveryLetterConnector = new CollectionAndRecoveryLetterConnector();
            collectionAndRecoveryLetterConnector.startProcess();
        } else if (processId == Configurations.PROCESS_EXPOSURE_FILE) {
            ExposureFileConnector exposureFileConnector = new ExposureFileConnector();
            exposureFileConnector.startProcess();
        } else if (processId == Configurations.PROCESS_ID_GL_FILE_CREATION) {
            GLSummaryFileConnector glSummaryFileConnector = new GLSummaryFileConnector();
            glSummaryFileConnector.startProcess();
        } else if (processId == Configurations.PROCESS_RB36_FILE_CREATION) {
            RB36FileGenerationConnector rb36FileGenerationConnector = new RB36FileGenerationConnector();
            rb36FileGenerationConnector.startProcess();
        }
    }
}
