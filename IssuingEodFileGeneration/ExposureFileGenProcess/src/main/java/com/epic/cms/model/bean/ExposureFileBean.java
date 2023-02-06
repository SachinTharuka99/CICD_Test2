/**
 * Author : lahiru_p
 * Date : 11/17/2022
 * Time : 10:51 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExposureFileBean {
    private String product;
    private String externalRef;
    private String capitalOutstanding;
    private String facilityType;
    private String branch;
    private String currency;
    private String status;
    private String maturityDate;
    private String creditLimit;
}
