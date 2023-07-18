/**
 * Author : rasintha_j
 * Date : 7/11/2023
 * Time : 11:45 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IP0075T1Bean {
    private String key;
    private String effectiveTimeStamp;
    private String activeInactiveCode;
    private String tableID;
    private String MCC; //card Acceptor Business Code
    private String CAB; //card Acceptor Business Program
    private String CABProgramLifecycleIndicator;
    private String CABType;
    private String CABLifeCycleIndicator; // Issuer Card Program Identifier Priority Code
}
