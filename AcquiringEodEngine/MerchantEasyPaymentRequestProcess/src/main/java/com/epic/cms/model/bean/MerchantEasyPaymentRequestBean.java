/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 1:52 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class MerchantEasyPaymentRequestBean {
    private StringBuffer cardNumber;
    private BigDecimal backendTxnAmount;
    private BigDecimal onlineTxnAmount;
    private String planCode;
    private String txnId;
    private String rrn;
    private String currencyNumCode;
    private int duration;
    private double interestRateOrFee;
    private double minimumAmount;
    private double maximumAmount;
    private String firstMonthInclude;
    private String feeApplyInFirstMonth;
    private String processingFeeType;
    private String mid;
    private String tid;

    private BigDecimal nextInstallmentAmount;
    private Date nextTxnDate;
    private int runningStatus;
    private int remainingCount;
    private String acceleratedStatus;
    private Date effectiveDate;
    private int currInstallment;
    private BigDecimal firstInstallmentAmount;
    private BigDecimal InterestOrFeeAmount;
    private BigDecimal InterestOrFeeTotalAmount;
}
