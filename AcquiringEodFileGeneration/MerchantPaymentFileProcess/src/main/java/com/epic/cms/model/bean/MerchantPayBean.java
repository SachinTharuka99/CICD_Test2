/**
 * Author : yasiru_l
 * Date : 6/30/2023
 * Time : 9:15 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantPayBean {
    private int eodPayId;
    private String merchantId;
    private String merchantCusId;
    private String merchantAccNo;
    private String merchantCusAccNo;
    private int txncount;
    private double drTxnAmount;
    private double crTxnAmount;
    private double commAmount;
    private double feeAmount;
    private double paymentAmount;
    private String netPayAmount;
    private String crDrnetPayment;
    private int currencyType;
    private String accountNo;
    private String paymentCrDr;
    private int adjustType;
    private String crDrCommision;
}

