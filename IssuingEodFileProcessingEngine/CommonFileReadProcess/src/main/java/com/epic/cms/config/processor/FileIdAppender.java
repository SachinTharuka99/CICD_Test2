/**
 * Author :
 * Date : 2/1/2023
 * Time : 4:43 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.config.processor;

import com.epic.cms.model.bean.RecInputRowDataBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

public class FileIdAppender implements ItemProcessor<RecInputRowDataBean, RecInputRowDataBean> {
    @Value("#{jobParameters[fileId]}")
    private String fileId;

    @Override
    public RecInputRowDataBean process(RecInputRowDataBean recInputRowDataBean) {
        if (!recInputRowDataBean.getRecordContent().isEmpty() && recInputRowDataBean.getRecordContent() != null) {
            recInputRowDataBean.setFileId(fileId);
            return recInputRowDataBean;
        }
        return null;
    }
}
