/**
 * Author : rasintha_j
 * Date : 2/14/2023
 * Time : 10:22 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EodInputFileBean {
    private Date uploadTime;
    private String fileType;
    private String fileId;
    private String fileName;
    private String status;
}
