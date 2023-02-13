package com.ecms.web.api.tokenservice.exception;

import com.ecms.web.api.tokenservice.model.bean.ResponseBean;
import com.ecms.web.api.tokenservice.util.MessageVarList;
import com.ecms.web.api.tokenservice.util.ResponseCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseBean> handleNoHandlerFoundException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.COMMON_NOT_FOUND);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseBean> handleAccessDeniedException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.COMMON_ACCESS_DENIED);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBean> handleHttpMessageNotReadableException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.COMMON_INVALID_REQUEST_BODY);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseBean> handleIllegalArgumentException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(e.getMessage());
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ResponseBean> handleSignatureException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.JWT_TOKEN_SIGNATURE_EXCEPTION);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ResponseBean> handleMalformedJwtException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.JWT_TOKEN_MALFORMED_EXCEPTION);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResponseBean> handleExpiredJwtException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.JWT_TOKEN_EXPIRED_EXCEPTION);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ResponseBean> handleUnsupportedJwtException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.JWT_TOKEN_UNSUPPORTED_EXCEPTION);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(NullPointerException.class) // exception handled
    public ResponseEntity<ResponseBean> handleNullPointerExceptions(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.COMMON_INVALID_DATA);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(CannotCreateTransactionException.class) // exception handled
    public ResponseEntity<ResponseBean> handleCannotCreateTransactionException(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.COMMON_INTERNAL_SERVER_ERROR);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBean> handleExceptions(Exception e) {
        e.printStackTrace();

        ResponseBean responseBean = new ResponseBean();
        responseBean.setResponseCode(ResponseCode.RSP_ERROR);
        responseBean.setResponseMsg(MessageVarList.COMMON_INVALID_REQUEST);
        responseBean.setContent(null);

        return new ResponseEntity<>(responseBean, HttpStatus.OK);
    }

}
