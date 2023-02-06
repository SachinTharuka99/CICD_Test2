package com.epic.cms.Exception;

public class FailedCardNumConvertException extends Exception{
    public FailedCardNumConvertException(String message) {
        super(message);
    }

    public FailedCardNumConvertException(String processHeader, Exception e) {
        super(processHeader+" failed",e);
    }
}
