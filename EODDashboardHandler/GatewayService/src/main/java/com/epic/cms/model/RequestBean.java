/**
 * Author : lahiru_p
 * Date : 2/21/2023
 * Time : 4:34 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBean {
    private String client_ip;
    private String token;
    private String userrole;
    private String username;
    private Object requestBody;

}
