/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:36 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterRejectBean {
    private String sessionId = null;
    private String fileId = null;
    private String lineNumber = null;
    private String fieldId = null;
    private String validationId = null;
    private String lineContent = null;
    private String fieldContent = null;
}
