/**
 * Author : rasintha_j
 * Date : 2/13/2023
 * Time : 3:14 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProcessSummeryBean {
    private int processId;
    private String description;
    private String status;
    private String processProgress;
}
