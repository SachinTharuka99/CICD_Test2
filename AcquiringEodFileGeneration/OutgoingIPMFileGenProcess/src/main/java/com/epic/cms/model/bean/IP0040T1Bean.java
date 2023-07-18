/**
 * Author : lahiru_p
 * Date : 7/11/2023
 * Time : 4:12 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Ruwan Chanaka, this bean is used to transfer data of IP00040T1 table
 * data(Issuer Account Range) in master card T67 update file
 */

@Getter
@Setter
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

    @Override
    public String toString() {
        return "IP0040T1Bean{" + "key=" + key + ", effectiveTimeStamp=" + effectiveTimeStamp + ", activeInactiveCode=" + activeInactiveCode + ", tableID=" + tableID + ", lowAccountRange=" + lowAccountRange + ", GCMSProductID=" + GCMSProductID + ", highAccountRange=" + highAccountRange + ", cardProgramIdentifier=" + cardProgramIdentifier + ", ICPIPriorityCode=" + ICPIPriorityCode + ", memberId=" + memberId + ", productTypeId=" + productTypeId + ", endpoint=" + endpoint + ", countryCodeAlpha=" + countryCodeAlpha + ", countryCodeNumeric=" + countryCodeNumeric + ", region=" + region + ", productClass=" + productClass + ", txnRoutingIndicator=" + txnRoutingIndicator + ", licensedProductId=" + licensedProductId + ", mappingServiceIndicator=" + mappingServiceIndicator + ", billingCurrencyDefault=" + billingCurrencyDefault + ", billingExponentDefault=" + billingExponentDefault + ", billingPrimaryCurrency=" + billingPrimaryCurrency + ", contaclessEnableInd=" + contaclessEnableInd + ", currencyIndicator=" + currencyIndicator + '}';
    }
}
