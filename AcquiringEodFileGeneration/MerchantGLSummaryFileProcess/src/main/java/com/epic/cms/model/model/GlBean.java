/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 11:07 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlBean {
    private String groupNo;
    private String currencyCode;
    private String glCode;
    private String clientNo;
    private String seqNo;
    private String crDr;
    private String amount;
    private String profitCenter;
    private String narration;
    private String refference;
    private StringBuffer cardNo;
    private String branch;
    private String prodCategory;
}
