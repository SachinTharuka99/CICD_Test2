/**
 * Author : rasintha_j
 * Date : 2/13/2023
 * Time : 7:45 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EodBean {
    private Long eodId;
    private Date startTime;
    private Date endTime;
    private String status;
    private String subEodStatus;
    private Integer noOfSuccessProcess;
    private Integer noOfErrorProcess;
}
