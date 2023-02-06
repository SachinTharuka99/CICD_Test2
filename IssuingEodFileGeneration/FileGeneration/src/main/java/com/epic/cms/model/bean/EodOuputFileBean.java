/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 11:22 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EodOuputFileBean {
    private String createdTime;
    private String fileType;
    private int eodId;
    private int noOfRecords;
    private String fileName;
    private String subFolder;
}
