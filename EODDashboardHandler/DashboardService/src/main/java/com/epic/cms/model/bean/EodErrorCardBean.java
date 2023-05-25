/**
 * Author : rasintha_j
 * Date : 2/14/2023
 * Time : 12:18 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.sql.In;

@Getter
@Setter
public class EodErrorCardBean {
    private Long eodId;
    private String cardNumber;
    private Integer errorProcess;
    private String errorReason;
}
