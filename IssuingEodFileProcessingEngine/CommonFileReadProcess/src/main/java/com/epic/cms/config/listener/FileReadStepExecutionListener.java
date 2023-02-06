/**
 * Author :
 * Date : 2/2/2023
 * Time : 1:43 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.config.listener;

import com.epic.cms.repository.CommonFileReadRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

public class FileReadStepExecutionListener implements StepExecutionListener {
    @Value("#{jobParameters[fileId]}")
    private String fileId;
    @Value("#{jobParameters[fileName]}")
    private String fileName;
    @Value("#{jobParameters[tableName]}")
    private String tableName;

    @Autowired
    private CommonFileReadRepo commonFileReadRepo;
    @Autowired
    private LogManager logManager;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            infoLogger.info(logManager.ProcessStartEndStyle("ATM File Reading Started"));
            commonFileReadRepo.updateFileReadStartTime(tableName, fileId);
        } catch (Exception ex) {
            errorLogger.error("", ex);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        LinkedHashMap details = new LinkedHashMap();
        if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            try {
                //update file reading end time and record count
                commonFileReadRepo.updateFileReadSummery(tableName, stepExecution.getWriteCount(), fileId);

                infoLogger.info(logManager.ProcessStartEndStyle("ATM File Reading Completed"));

                details.put("Step Name ", stepExecution.getStepName());
                details.put("File ID ", fileId);
                details.put("File Name ", fileName);
                details.put("Read Count ", stepExecution.getReadCount());
                details.put("Filter Count ", stepExecution.getFilterCount());
                details.put("Write Count ", stepExecution.getWriteCount());
                details.put("Read Skip Count ", stepExecution.getReadSkipCount());
                details.put("Process Skip Count ", stepExecution.getProcessSkipCount());
                details.put("Write Skip Count ", stepExecution.getWriteSkipCount());
                details.put("Commit Count ", stepExecution.getCommitCount());
                details.put("Rollback Count ", stepExecution.getRollbackCount());

                infoLogger.info(logManager.processDetailsStyles(details));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return stepExecution.getExitStatus();
    }
}
