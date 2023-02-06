/**
 * Author : lahiru_p
 * Date : 1/30/2023
 * Time : 10:11 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MerchantLocationBean {
    private String merchantId;
    private String riskProfile;
    private String feeProfile;
    private String comisionProfile;
    private String statementmaintenanceStatus;
    private String merchantCusNo;
    private String merchantCusName;
    private String postalCode;
    private String merchantType;
    private String merchantEmail;
    private String merchantCurrency;
    private String bankName;
    private String paymentMode;
    private String merchantDes;
    private String merchantStatCycleCode;
    private String address1;
    private String address2;
    private String address3;
    private String status;
    private Date nextBillingDate;
    private Date billingDate;
    private Date lastBillingDate;
    private int startEodId;
    private int endEodId;
    private double netPaymentAmount;
    private String netCrDr;
    private double paymentAmount;
    private String paymentCrDr;
    private double commissionAmount;
    private String commissionCrDr;
    private double feeAmount;
    private double openingBalance;
    private double closingBalance;
    private String feeCrDr;
    private String statementID;
    private boolean isFirstStatement;
    private String accNumber;
    private int txnCount;
    private String calMethod;
    private String merchantCustomerNo;
}
