/**
 * Author : rasintha_j
 * Date : 2/15/2023
 * Time : 3:43 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EodErrorMerchantBean {
    private Long eodId;
    private String merchantId;
    private String errorProcessId;
    private String errorReason;
}
