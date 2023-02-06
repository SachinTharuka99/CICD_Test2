/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 3:45 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EodOuputFileBean {

    private String createdTime;
    private String fileType;
    private int eodId;
    private int noOfRecords;
    private String fileName;
    private String subFolder;
}
