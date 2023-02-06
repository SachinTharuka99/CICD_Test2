/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 10:48 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlAccountBean {
    private StringBuffer cardNo;
    private String merchantID;
    private String accNo;
    private String glType;
    private String glAmount;
    private double amount;
    private double fuelSurchargeAmount;
    private String crDr;
    private String glDate;
    private String key;
    private int id;
    private String paymentType;
}
