/**
 * Author :
 * Date : 2/1/2023
 * Time : 4:39 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.config.mapper;

import com.epic.cms.model.bean.RecInputRowDataBean;
import org.springframework.batch.item.file.LineMapper;

public class FileRowMapper implements LineMapper<RecInputRowDataBean> {
    @Override
    public RecInputRowDataBean mapLine(String recordContent, int lineNumber) throws Exception {
        RecInputRowDataBean bean = new RecInputRowDataBean();
        bean.setLineNumber(lineNumber);
        bean.setRecordContent(recordContent);
        return bean;
    }
}
