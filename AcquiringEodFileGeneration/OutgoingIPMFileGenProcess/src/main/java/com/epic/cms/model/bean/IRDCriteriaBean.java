/**
 * Author : lahiru_p
 * Date : 7/12/2023
 * Time : 11:24 AM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IRDCriteriaBean {
    private String ID;
    private String IRD;
    private String criterias;
    private String rateCharities;
    private String rateFuel;
    private String rateEmergingMarket;
    private String rateAllOther;
    private String IRDCategory;
}
