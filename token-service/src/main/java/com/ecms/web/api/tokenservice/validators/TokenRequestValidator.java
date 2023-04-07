package com.ecms.web.api.tokenservice.validators;

import com.ecms.web.api.tokenservice.model.bean.TokenRequest;
import com.ecms.web.api.tokenservice.util.MessageVarList;
import com.ecms.web.api.tokenservice.util.ResponseCode;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TokenRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return TokenRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.getErrorCount() == 0) {
            TokenRequest tokenRequest = (TokenRequest) target;
            if (tokenRequest.getUsername() == null || tokenRequest.getUsername().isEmpty()) {
                errors.reject(ResponseCode.RSP_ERROR, MessageVarList.LOGIN_USERNAME_EMPTY);
            } else if (tokenRequest.getPassword() == null || tokenRequest.getPassword().isEmpty()) {
                errors.reject(ResponseCode.RSP_ERROR, MessageVarList.LOGIN_PASSWORD_EMPTY);
            }
        }
    }
}
