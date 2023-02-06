/**
 * Author : sharuka_j
 * Date : 1/27/2023
 * Time : 7:03 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Setter
@Getter
public class MerchantBeanForFee {
    String merchantId;
    String feeProfile;
    Date nextAnniversaryDate; // for merchant annual fee
    List<TerminalBeanForFee> terminalList;
    List eligibleFeeCodeList;
    String merchantCustomerNo;
    Date nextBiMonthlyDate; // for merchant bi monthly fee
    Date nextQuarterlyDate; // for merchant quarterly fee
    Date nextHalfYearlyDate; //for merchant half yearly fee
}
