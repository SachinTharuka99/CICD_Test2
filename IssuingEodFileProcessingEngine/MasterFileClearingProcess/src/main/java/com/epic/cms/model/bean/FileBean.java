/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:35 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileBean {
    private String fileName;
    private String fileId;
    private String transactionType;
}
