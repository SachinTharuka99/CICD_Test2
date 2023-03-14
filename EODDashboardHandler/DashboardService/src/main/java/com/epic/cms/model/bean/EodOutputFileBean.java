/**
 * Author : rasintha_j
 * Date : 2/14/2023
 * Time : 11:17 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EodOutputFileBean {
    private Date createdTime;
    private String fileType;
    private Long eodId;
    private int noOfRecords;
    private String fileName;
    private String subFolder;
}
