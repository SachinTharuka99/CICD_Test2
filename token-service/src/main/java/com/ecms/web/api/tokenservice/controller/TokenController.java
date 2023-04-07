package com.ecms.web.api.tokenservice.controller;

import com.ecms.web.api.tokenservice.model.bean.RequestBean;
import com.ecms.web.api.tokenservice.model.bean.ResponseBean;
import com.ecms.web.api.tokenservice.service.TokenService;
import com.ecms.web.api.tokenservice.util.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
public class TokenController {

    Logger logger = LoggerFactory.getLogger(TokenController.class);
    @Autowired
    private ResponseBean responseBean;
    @Autowired
    private TokenService tokenService;

    @PostMapping(value = "/obtain")
    public ResponseBean getJWT(@RequestBody RequestBean requestBean, @RequestHeader String requestID) throws Exception {
        logger.info(requestID + " - " + "TokenController.getJWT");
        logger.debug(requestID + " - " + "TokenController.getJWT - " + "clientip    - " + requestBean.getClient_ip());

        logger.info(requestID + " - " + "TokenController.getJWT - " + "initializing token generation process");
        responseBean = tokenService.generateJWT(requestBean, responseBean, requestID);

        logger.info(requestID + " - " + "TokenController.getJWT - " + "token generation completed");

        return responseBean;
    }

    @PostMapping(value = "/validate")
    public ResponseBean validateJWT(@RequestBody RequestBean requestBean, @RequestHeader String requestID) throws Exception {
        logger.info(requestID + " - " + "TokenController.validateJWT");
        logger.debug(requestID + " - " + "TokenController.validateJWT - " + "clientip    - " + requestBean.getClient_ip());

        logger.info(requestID + " - " + "TokenController.validateJWT - " + "initializing token validation process");
        responseBean = tokenService.validateJWT(requestBean, responseBean);

        if (responseBean.getResponseCode().equals(ResponseCode.RSP_SUCCESS)) {
            logger.info(requestID + " - " + "TokenController.validateJWT - " + "token is valid");
        } else {
            logger.info(requestID + " - " + "TokenController.validateJWT - " + "token is invalid");
        }

        logger.info(requestID + " - " + "TokenController.validateJWT - " + "token validation completed");

        return responseBean;
    }

}
