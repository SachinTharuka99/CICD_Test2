/**
 * Author : rasintha_j
 * Date : 7/11/2023
 * Time : 11:36 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IP0040T1Bean {
    private String key;
    private String effectiveTimeStamp;
    private String activeInactiveCode;
    private String tableID;
    private String lowAccountRange;
    private String GCMSProductID;
    private String highAccountRange;
    private String cardProgramIdentifier;
    private String ICPIPriorityCode; // Issuer Card Program Identifier Priority Code
    private String memberId;
    private String productTypeId;
    private String endpoint;
    private String countryCodeAlpha;
    private String countryCodeNumeric;
    private String region;
    private String productClass;
    private String txnRoutingIndicator;
    private String licensedProductId;
    private String mappingServiceIndicator;
    private String billingCurrencyDefault;
    private String billingExponentDefault;
    private String billingPrimaryCurrency;
    private String contaclessEnableInd;
    private String currencyIndicator;
}
