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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epic.cms.util.LogManager.*;


@RestController
@RequestMapping("eod-dashboard/eod-engine")
public class EODEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODEngineDashboardService engineDashboardService;

    @Autowired
    LogManager logManager;

    @PostMapping("/starteodid")
    public ResponseBean getNextRunningEodId() {
        try {
            logManager.logHeader("EOD-Engine Dashboard Get getNextRunningEodId", dashboardInfoLogger);
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
            logManager.logError("Failed Next Running EodId ", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/currenteod")
    public ResponseBean getCurrentEodDetails() {
        try {
            logManager.logHeader("EOD-Engine Dashboard Get CurrentEodDetails", dashboardInfoLogger);
            EodBean currentDashboardEodId = engineDashboardService.getCurrentDashboardEodId();

            if (currentDashboardEodId != null) {
                responseBean.setContent(currentDashboardEodId);
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
            logManager.logError("Failed Current Eod Details ", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/processsummery/{eodid}")
    public ResponseBean getProcessSummeryByEodId(@PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-Engine Dashboard Get ProcessSummery By EodId :" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed EOD ID ProcessSummery Details", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/eodinfo/{eodid}")
    public ResponseBean getEodInfo(@PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-Engine Dashboard EOD info by EODID:" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed EOD ID Details Request", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/invalidtransaction/{eodid}")
    public ResponseBean getEodInvalidTransactionList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-Error Dashboard Get Eod Invalid Transaction List EodId :" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed Eod Invalid Transaction List ", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/errormerchant/{eodid}")
    public ResponseBean getEodErrorMerchantList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId){
        try {
            logManager.logHeader("EOD-Error Dashboard Get Eod Merchant List EodId :" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed Eod Error Card List ", e, dashboardErrorLogger);
        }

        return responseBean;
    }

    @PostMapping(value = "/errorcard/{eodid}")
    public ResponseBean getEodErrorCardList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-Error Dashboard Get Eod Error Card List EodId :" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed Eod Error Card List ", e, dashboardErrorLogger);
        }
        return responseBean;
    }
}
