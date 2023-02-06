/**
 * Author :
 * Date : 11/1/2022
 * Time : 6:02 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.Exception;

public class FailedCardException extends Exception{
    public FailedCardException(String message) {
        super(message);
    }

    public FailedCardException(String processHeader, Exception e) {
        super(processHeader+" failed",e); //To change body of generated methods, choose Tools | Templates.
    }
}
