/**
 * Author :
 * Date : 1/31/2023
 * Time : 9:22 AM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecInputRowDataBean {
    private String fileId;
    private int lineNumber;
    private String recordContent;
}
