/**
 * Author : rasintha_j
 * Date : 7/11/2023
 * Time : 11:14 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EODInputFileDetailBean {
    private String fileId;
    private String fileName;
    private int noOfRecords;
    private int noOfTransactions;
    private String fileType;
    private String checkSum;
}
