package com.ecms.web.api.tokenservice.service;

import com.ecms.web.api.tokenservice.model.bean.RequestBean;
import com.ecms.web.api.tokenservice.model.bean.ResponseBean;

public interface TokenService {
    ResponseBean generateJWT(RequestBean requestBean, ResponseBean responseBean, String requestID) throws Exception;

    ResponseBean validateJWT(RequestBean requestBean, ResponseBean responseBean) throws Exception;
}
