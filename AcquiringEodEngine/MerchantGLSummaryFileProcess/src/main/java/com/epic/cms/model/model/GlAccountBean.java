/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 6:26 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.model;

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
