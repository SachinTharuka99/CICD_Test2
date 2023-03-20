/**
 * Author : lahiru_p
 * Date : 2/22/2023
 * Time : 5:15 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.exception;

import javax.naming.AuthenticationException;

public class JwtTokenMissingException  extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public JwtTokenMissingException(String msg) {
        super(msg);
    }
}
