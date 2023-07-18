/**
 * Author :
 * Date : 7/12/2023
 * Time : 1:42 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.util.exception;

import com.epic.cms.util.*;
import com.epic.cms.util.EODEngineStartFailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @ExceptionHandler(InvalidEodId.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody
    Map<String, Object> handleInvalidEodId(final InvalidEodId exception,
                                           final HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);//to be changed
        logError.error(exception.getMessage());

        return response;
    }

    @ExceptionHandler(UploadedFileNotCompleted.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody
    Map<String, Object> handleFileProcessingNotCompleted(final UploadedFileNotCompleted exception,
                                                         final HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);//to be changed
        logError.error(exception.getMessage());

        return response;
    }

    @ExceptionHandler(EODEngineStartFailException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Map<String, Object> handleEodEngineStartFailException(final EODEngineStartFailException exception,
                                                          final HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);//to be changed
        logManager.logDashboardInfo(exception.getMessage();
        logError.error(exception.getMessage());

        return response;
    }

    @ExceptionHandler(FileProcessingNotCompletedException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Map<String, Object> handleFileProcessingNotCompletedException(final Exception exception,
                                                                  final HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);//to be changed
        logError.error(exception.getMessage());

        return response;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Map<String, Object> handleException(final Exception exception,
                                        final HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);//to be changed
        logError.error(exception.getMessage());

        return response;
    }

}
