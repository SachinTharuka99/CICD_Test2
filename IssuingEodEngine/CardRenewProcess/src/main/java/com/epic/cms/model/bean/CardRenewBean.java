/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:21 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardRenewBean {
    private StringBuffer CardNumber;
    private String EarlyRenew;
    private String Expirydate;
    private  String CardStatus;
    private String isProductChange;
}
