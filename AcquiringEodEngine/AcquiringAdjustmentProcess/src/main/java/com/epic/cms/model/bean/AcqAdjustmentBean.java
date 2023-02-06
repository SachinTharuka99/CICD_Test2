/**
 * Author : sharuka_j
 * Date : 1/25/2023
 * Time : 7:21 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
@Getter
@Setter
public class AcqAdjustmentBean {
    private String adjustAmount;
    private Date adjustDate;
    private String adjustDes;
    private String txnType;
    private String crDr;
    private String merchantId;
    private String cardOrMerchant;
    private String adjustType;
    private String curruncyType;
    private String id;
    private String txnId;
    private StringBuffer cardNumber;
    private String description;
    private String mcc;

    private String cardAssociation;
}
