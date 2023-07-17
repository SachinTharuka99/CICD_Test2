/**
 * Author : lahiru_p
 * Date : 7/11/2023
 * Time : 3:03 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MPGSAdditionalDataBean {
    //6220 - transaction detail record 1
    private String ECI; //field 22
    private String POSCardHolderPresenceIndicator; // field 31
    private String cardHolderActivatedTerminalLevelInd; //field 33
    private String pointOfServiceDataCode; //field 38

    //6221 - transaction detail record 2
    private String walletProgramData; //field 13
    private String directoryServerTxnId; //field 14
    private String programProtocol; //field 15
    private String transactionTypeIndicator; //field 22

    //6222 - Master specific data
    private String banknetNetworkCode; // field 02
    private String banknetReferenceNumber; //field 03
    private String banknetDate; // field 04
    private String electronicAcceptanceIndicator; //field 05
    private String promotionCode; //field 08

    //6223
    private String visa3DSecureIndicator; //field 25

    //6225 - emv data
    String panSequenceNumber;

    private String visaTransactionIdentification; //field 08
}
