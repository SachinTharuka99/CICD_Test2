/**
 * Author : rasintha_j
 * Date : 2/27/2023
 * Time : 8:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatementGenSummeryBean {
    private Integer processId;
    private String description;
    private String status;
    private String processProgress;
    private Integer successCount;
    private Integer failCount;
}
