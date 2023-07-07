/**
 * Author : rasintha_j
 * Date : 2/13/2023
 * Time : 3:02 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseBean {
    String responseCode;
    String responseMsg;
    Object content;
}
