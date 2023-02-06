/**
 * Author : lahiru_p
 * Date : 1/30/2023
 * Time : 11:16 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommissionProfileBean {
    private String crdr;
    private String cardAssociation;
    private String binType;
    private String segment;
    private String profCode;
    private String cardProduct;
    private String combination;
    private double flatValue;
    private double percentage;
    private String volumeId;
}
