/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.*;
import com.epic.cms.model.entity.EODERRORCARDS;
import com.epic.cms.service.EODEngineDashboardService;
import com.epic.cms.util.MessageVarList;
import com.epic.cms.util.ResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("eod-dashboard/eod-engine")
public class EODEngineDashboardController {

    private static final Logger logError = LoggerFactory.getLogger("logError");
    ResponseBean responseBean = new ResponseBean();
    @Autowired
    EODEngineDashboardService engineDashboardService;

    @Operation(summary = "Get Next Running EOD Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Next Running EOD Id",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = NextRunningEodBean.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
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

    @Operation(summary = "Load Current Dashboard EOD Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Set the Current Dashboard EOD Id",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping("/load-eod-data")
    public void loadDashboardEod() {
        engineDashboardService.getCurrentDashboardEodId();
    }

    @Operation(summary = "Get EOD Process Summary Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Eod Engine Process Summery",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProcessSummeryBean.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
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

    @Operation(summary = "Get EOD Info Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the EOD Info Details",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EodBean.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
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

    @Operation(summary = "Get EOD Invalid Transactions List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the EOD Invalid Transactions",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
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

    @Operation(summary = "Get Eod Error Merchant List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the EOD Error Merchant List",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping("/errormerchant/{eodid}")
    public ResponseBean getEodErrorMerchantList(@RequestBody RequestBean requestBean, @PathVariable("eodid") final Long eodId) {
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

    @Operation(summary = "Get EOD Error Card List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the EOD Error Card List",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EODERRORCARDS.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
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
