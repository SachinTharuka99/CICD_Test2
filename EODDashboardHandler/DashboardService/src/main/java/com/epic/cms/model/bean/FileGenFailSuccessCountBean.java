/**
 * Author : rasintha_j
 * Date : 7/18/2023
 * Time : 8:20 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileGenFailSuccessCountBean {
    private Integer fileGenNoOfSuccessProcess;
    private Integer fileGenNoOfErrorProcess;
}
