/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.*;
import com.epic.cms.service.*;
import com.epic.cms.util.exception.MessageVarList;
import com.epic.cms.util.exception.ResponseCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epic.cms.util.LogManager.*;


@RestController
@RequestMapping("eod-engine/dashboard")
public class EODEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EodIdInfoService eodIdInfoService;

    @Autowired
    EodErrorMerchantListService eodErrorMerchantListService;

    @Autowired
    EodErrorCardListService eodErrorCardListService;

    @Autowired
    EodInvalidTransactionListService eodInvalidTransactionListService;

    @Autowired
    ProcessSummeryService processSummeryService;

    @PostMapping("/starteodid")
    public ResponseBean getNextRunningEodId() {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-Engine Dashboard Get getNextRunningEodId"));
            NextRunningEodBean nextRunningEodId = eodIdInfoService.getNextRunningEodId();

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
            dashboardErrorLogger.error("Failed Next Running EodId ", e);
        }
        return responseBean;
    }

    @PostMapping("/processsummery/{eodid}")
    public ResponseBean getProcessSummeryByEodId(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-Engine Dashboard Get ProcessSummery By EodId :" + eodId));
            List<ProcessSummeryBean> eodProcessSummeryList = processSummeryService.getEodProcessSummeryList(eodId);

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
            dashboardErrorLogger.error("Failed EOD ID ProcessSummery Details", e);
        }
        return responseBean;
    }

    @PostMapping("/eodinfo/{eodid}")
    public ResponseBean getEodInfo(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardErrorLogger.info(processStartEndStyle("EOD-Engine Dashboard EOD info by EODID:" + eodId));
            EodBean eodInfo = eodIdInfoService.getEodInfoList(eodId);

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
            dashboardErrorLogger.error("Failed EOD ID Details Request", e);
        }
        return responseBean;
    }

    @PostMapping("/invalidtransaction/{eodid}")
    public ResponseBean getEodInvalidTransactionList(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-Error Dashboard Get Eod Invalid Transaction List EodId :" + eodId));
            List<Object> invalidTransactionBeanList = eodInvalidTransactionListService.getEodInvalidTransactionList(eodId);

            if (invalidTransactionBeanList.size() > 0) {
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
            dashboardErrorLogger.error("Failed Eod Invalid Transaction List ", e);
        }
        return responseBean;
    }

    @PostMapping("/errormerchant/{eodid}")
    public ResponseBean getEodMerchantList(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-Error Dashboard Get Eod Merchant List EodId :" + eodId));
            List<EodErrorMerchantBean> eodMerchantList = eodErrorMerchantListService.getEodErrorMerchantList(eodId);

            if (eodMerchantList.size() > 0) {
                responseBean.setContent(eodMerchantList);
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
            dashboardErrorLogger.error("Failed Eod Merchant List ", e);
        }
        return responseBean;
    }

    @PostMapping("/errorcard/{eodid}")
    public ResponseBean getEodErrorCardList(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-Error Dashboard Get Eod Error Card List EodId :" + eodId));
            List<EodErrorCardBean> eodErrorCardList = eodErrorCardListService.getEodErrorCardList(eodId);

            if (eodErrorCardList.size() > 0) {
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
            dashboardErrorLogger.error("Failed Eod Error Card List ", e);
        }
        return responseBean;
    }
}
