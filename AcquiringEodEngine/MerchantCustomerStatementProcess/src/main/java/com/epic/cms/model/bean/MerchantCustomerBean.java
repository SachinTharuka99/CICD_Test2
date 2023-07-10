/**
 * Author : lahiru_p
 * Date : 6/26/2023
 * Time : 3:52 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MerchantCustomerBean {
    private String merchantCusNo;
    private String merchantCusDes;
    private String legalName;
    private String address1;
    private String address2;
    private String address3;
    private String statementCycleCode;
    private String status;
    private String comisionProfile;
    private String feeProfile;
    private String riskProfile;
    private String paymentmaintananceStatus;
    private String statementMaintananceStatus;
    private String accountNo;
    private String merchantAccNo;
    private String accountName;
    private String accountType;
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
    private String feeCrDr;
    private String statementID;
    private String currencyCode;
    private boolean isFirstStatement;
    private int txnCount;
    private String bankCode;
    private String branchCode;
}
