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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Autowired;

public class StepSkipListener implements SkipListener<RecInputRowDataBean, RecInputRowDataBean> {
    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    @Override
    public void onSkipInRead(Throwable throwable) {
        logInfo.info("A failure on read {}"+ throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(RecInputRowDataBean item, Throwable throwable) {
        try {
            logInfo.info("A failure on write {},{}"+ throwable.getMessage()+ new ObjectMapper().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            logError.error(e.getMessage(), e);
        }
    }

    @Override
    public void onSkipInProcess(RecInputRowDataBean item, Throwable throwable) {
        try {
            logInfo.info("Item {} was skipped due to the exception {}"+ new ObjectMapper().writeValueAsString(item)+ throwable.getMessage());
        } catch (JsonProcessingException e) {
            logError.error(e.getMessage());
        }
    }
}
