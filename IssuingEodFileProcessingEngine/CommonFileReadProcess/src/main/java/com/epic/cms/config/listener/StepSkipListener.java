/**
 * Author :
 * Date : 1/31/2023
 * Time : 9:21 AM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.config.listener;

import com.epic.cms.util.LogManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.epic.cms.model.bean.RecInputRowDataBean;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Autowired;

import static com.epic.cms.util.LogManager.*;

public class StepSkipListener implements SkipListener<RecInputRowDataBean, RecInputRowDataBean> {
    @Autowired
    LogManager logManager;

    @Override
    public void onSkipInRead(Throwable throwable) {
        infoLoggerEFPE.info("A failure on read {}", throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(RecInputRowDataBean item, Throwable throwable) {
        try {
            infoLoggerEFPE.info("A failure on write {},{}", throwable.getMessage(), new ObjectMapper().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            errorLoggerEFPE.error(e.getMessage());
        }
    }

    @Override
    public void onSkipInProcess(RecInputRowDataBean item, Throwable throwable) {
        try {
            infoLoggerEFPE.info("Item {} was skipped due to the exception {}", new ObjectMapper().writeValueAsString(item), throwable.getMessage());
        } catch (JsonProcessingException e) {
            errorLoggerEFPE.error(e.getMessage());
        }
    }
}
