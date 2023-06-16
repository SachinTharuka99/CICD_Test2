/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.*;
import com.epic.cms.service.*;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.MessageVarList;
import com.epic.cms.util.ResponseCodes;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("eod-dashboard/eod-engine")
public class EODEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODEngineDashboardService engineDashboardService;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @PostMapping("/starteodid")
    public ResponseBean getNextRunningEodId() {
        try {
            NextRunningEodBean nextRunningEodId = engineDashboardService.getNextRunningEodId();

            if (nextRunningEodId != null) {
                responseBean.setContent(nextRunningEodId);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg(MessageVarList.NO_DATA_FOUND);
            }
        } catch (Exception e) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
            logError.error("Failed Next Running EodId ", e);
        }
        return responseBean;
    }

    @PostMapping("/load-eod-data")
    public void loadDashboardEod() {
        engineDashboardService.getCurrentDashboardEodId();
    }

    @PostMapping("/processsummery/{eodid}")
    public ResponseBean getEodEngineProcessSummery(@PathVariable("eodid") final Long eodId) {
        try {
            List<ProcessSummeryBean> eodProcessSummeryList = engineDashboardService.getEodProcessSummeryList(eodId);

            if (eodProcessSummeryList.size() > 0) {
                responseBean.setContent(eodProcessSummeryList);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg(MessageVarList.NO_DATA_FOUND);
            }
        } catch (Exception e) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
            logError.error("Failed EOD ID ProcessSummery Details", e);
        }
        return responseBean;
    }

    @PostMapping("/eodinfo/{eodid}")
    public ResponseBean getEodInfo(@PathVariable("eodid") final Long eodId) {
        try {
            EodBean eodInfo = engineDashboardService.getEodInfoList(eodId);

            if (eodInfo != null) {
                responseBean.setContent(eodInfo);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg(MessageVarList.NO_DATA_FOUND);
            }

        } catch (Exception e) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
            logError.error("Failed EOD ID Details Request", e);
        }
        return responseBean;
    }

    @PostMapping("/invalidtransaction/{eodid}")
    public ResponseBean getEodInvalidTransactionList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId) {
        try {
            DataTableBean invalidTransactionBeanList = engineDashboardService.getEodInvalidTransactionList(requestBean, eodId);

            if (invalidTransactionBeanList != null) {
                responseBean.setContent(invalidTransactionBeanList);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg(MessageVarList.NO_DATA_FOUND);
            }
        } catch (Exception e) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
            logError.error("Failed Eod Invalid Transaction List ", e);
        }
        return responseBean;
    }

    @PostMapping("/errormerchant/{eodid}")
    public ResponseBean getEodErrorMerchantList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId){
        try {
            DataTableBean eodErrorMerchantList = engineDashboardService.getEodErrorMerchantList(requestBean, eodId);

            if (eodErrorMerchantList != null) {
                responseBean.setContent(eodErrorMerchantList);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg(MessageVarList.NO_DATA_FOUND);
            }
        } catch (Exception e) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
            logError.error("Failed Eod Error Card List ", e);
        }

        return responseBean;
    }

    @PostMapping(value = "/errorcard/{eodid}")
    public ResponseBean getEodErrorCardList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId) {
        try {
            DataTableBean eodErrorCardList = engineDashboardService.getEodErrorCardList(requestBean, eodId);

            if (eodErrorCardList != null) {
                responseBean.setContent(eodErrorCardList);
                responseBean.setResponseCode(ResponseCodes.SUCCESS);
                responseBean.setResponseMsg(MessageVarList.SUCCESS);
            } else {
                responseBean.setResponseCode(ResponseCodes.NO_DATA_FOUND);
                responseBean.setContent(null);
                responseBean.setResponseMsg(MessageVarList.NO_DATA_FOUND);
            }
        } catch (Exception e) {
            responseBean.setResponseCode(ResponseCodes.UNEXPECTED_ERROR);
            responseBean.setContent(null);
            responseBean.setResponseMsg(MessageVarList.NULL_POINTER);
            logError.error("Failed Eod Error Card List ", e);
        }
        return responseBean;
    }
}
