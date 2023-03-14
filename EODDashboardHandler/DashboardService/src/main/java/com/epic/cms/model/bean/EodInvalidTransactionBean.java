/**
 * Author : rasintha_j
 * Date : 2/14/2023
 * Time : 11:56 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EodInvalidTransactionBean {
    private Long eodId;
    private String fileId;
    private int lineNumber;
    private String errorRemark;
    private String fileType;
}
