/**
 * Author : yasiru_l
 * Date : 11/18/2022
 * Time : 12:36 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculateMinPaymentBean {
    double MinPayment;
    double FlatAmount;
    double precentage;
    int permenantBlockPeriod;
    String MinOrMax;
}
