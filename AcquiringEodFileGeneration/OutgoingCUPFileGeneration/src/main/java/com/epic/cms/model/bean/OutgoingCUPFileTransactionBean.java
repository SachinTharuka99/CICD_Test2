/**
 * Author : yasiru_l
 * Date : 6/30/2023
 * Time : 10:38 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class OutgoingCUPFileTransactionBean {

    private String transactionId;
    private String transactionType;
    private String transactionCode;
    private String blockBitmap;
    private StringBuffer PAN;
    private String amount;
    private String currencyCode;
    private String transmissionDate;
    private String systemTraceAuditNumber;
    private String authorizationIdentificationResponse;
    private String authorizationDate;
    private String retrievalRefNumber;
    private String acquiringInstitutionIdentificationNumber;
    private String forwardingInstitutionIdentificationCode;
    private String merchantType;
    private String cardAcceptorTerminalIdentification;
    private String cardAcceptorIdentificationCode;
    private String cardAcceptorNameOrLocation;
    private String merchantName;
    private String merchantCity;
    private String merchantCounty;
    private String originalTransactionInformation;
    private String issuingInstitutionIdentificationCode;
    private String transactionInitiatingChannel;

    private String otherInformation;
    private String installmentPaymentTerms;
    private String standInAuthorizationIdentifier;
    private String posConditionCode;
    private String merchantCountryCode;
    private String transactionInitiationMethod;
    private String authorizationType;
    private String posEntryMode;

    private String currencyCodeSettlement;
    private String conversionRateSettlement;
    private String amountCardholderBilling;
    private String currencyCodeCardholderBilling;
    private String conversionRateCardholderBilling;
    private String netFeeAmount;
    private String IRFBillingCurrency;
    private String exchangeRate;

    private String QRCVoucherNumber;

    private String pointOfServiceEntryMode;
    private String terminalCapabilities;
    private String terminalVerificationResults;
    private String unpredictableNumber;
    private String serialNumberInterfaceDevice;
    private String issuingBankApplicationData;
    private String applicationTransactionCounter;
    private String applicationAlternationCharacteristic;
    private String transactionDate;
    private String terminalCountryCode;
    private String scriptResultCardIssuer;
    private String transactionResponseCode;
    private String transactionCategory;
    private String authorizedAmount;
    private String currencyCodeBlock2;
    private String cipherTextInformationData;
    private String otherAmount;
    private String authenticationMethodAndResultOfTheCardholder;
    private String terminalCategory;
    private String dedicatedDocumentName;
    private String applicationVersionNo;
    private String transactionSerialCounter;
    private String F60_DATA;
    private String appliedCryptogram;

    private String originalTxnId; // for reversal transaction
    private String UMPS_TID;
    private String UMPS_MID;
    private String originalTxnInitChannel;
}
