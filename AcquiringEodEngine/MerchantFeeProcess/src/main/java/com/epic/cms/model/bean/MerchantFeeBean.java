/**
 * Author : rasintha_j
 * Date : 1/24/2023
 * Time : 2:02 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantFeeBean {
    private String MID;
    private String feeCode;
    private int feeCount;
    private int currCode;
    private String crORdr;
    private double flatFee;
    private double minAmount;
    private double maxAmount;
    private double percentageAmount;
    private String combination;
    private double cashAmount;
    private String custAccountNo;
    private String merchantAccountNo;
    private String merchantCustomerNo;
}
