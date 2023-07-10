/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;


import com.epic.cms.model.bean.EodOutputFileBean;
import com.epic.cms.model.bean.NextRunningEodBean;
import com.epic.cms.model.bean.ResponseBean;
import com.epic.cms.model.bean.StatementGenSummeryBean;
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
@RequestMapping("eod-dashboard/file-generation")
public class EODFileGenerationEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODEngineDashboardService eodEngineDashboardService;

    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Operation(summary = "Get Eod Output FIle List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the Eod Output FIle List",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EodOutputFileBean.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping("/outputfile/{eodid}")
    public ResponseBean getEodOutputFIleList(@PathVariable("eodid") final Long eodId) {
        try {
            List<EodOutputFileBean> eodOutputFIleList = eodEngineDashboardService.getEodOutputFIleList(eodId);

            if (eodOutputFIleList.size() > 0) {
                responseBean.setContent(eodOutputFIleList);
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
            logError.error("Failed Eod Output FIleList ", e);
        }
        return responseBean;
    }
    @Operation(summary = "Get EOD File Generation Summery List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the File Generation Summery",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = StatementGenSummeryBean.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content)})
    @PostMapping("/stmtgensummery/{eodid}")
    public ResponseBean getStatementGenSummeryList(@PathVariable("eodid") final Long eodId) {
        try {
            List<StatementGenSummeryBean> genSummeryBeanList = eodEngineDashboardService.getStatementGenSummeryList(eodId);

            if (genSummeryBeanList.size() > 0) {
                responseBean.setContent(genSummeryBeanList);
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
            logError.error("Failed Statement GenSummery List ", e);
        }
        return responseBean;
    }
}
