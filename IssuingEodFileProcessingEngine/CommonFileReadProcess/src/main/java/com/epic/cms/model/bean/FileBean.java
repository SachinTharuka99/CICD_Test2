/**
 * Author :
 * Date : 1/31/2023
 * Time : 9:21 AM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileBean {
    private String fileId;
    private String fileName;
    private String fileStatus;
    private String filePath;
    private String noOfLines;
}
