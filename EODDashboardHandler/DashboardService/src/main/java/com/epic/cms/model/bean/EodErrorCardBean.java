/**
 * Author : rasintha_j
 * Date : 2/14/2023
 * Time : 12:18 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EodErrorCardBean {
    private Long eodId;
    private String cardNumber;
    private String errorProcess;
    private String errorReason;
}
