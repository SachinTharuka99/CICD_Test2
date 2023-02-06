/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 2:13 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.model.bean;

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
