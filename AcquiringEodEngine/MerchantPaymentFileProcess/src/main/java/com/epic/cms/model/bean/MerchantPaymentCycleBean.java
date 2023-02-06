/**
 * Author : sharuka_j
 * Date : 2/2/2023
 * Time : 9:40 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantPaymentCycleBean {
    private String paymentCycleCode;
    private String paymentOption;
    private String paymentDate;
    private String description;
    private String holidayAction;
    private String status;
    private String merchantId;
    private String merchantCustomer;
    private String merchantStatus;
    private String merchantCustomerStatus;
    private int payOption;
    private int payDate;
    private String payMode;
    private String accountNo;
    private String accountName;
    private String bankCode;
    private String branchCode;
    private String currencyCode;
}
