/**
 * Author : lahiru_p
 * Date : 2/21/2023
 * Time : 4:26 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseBean {
    String responseCode;
    String responseMsg;
    Object content;
}
