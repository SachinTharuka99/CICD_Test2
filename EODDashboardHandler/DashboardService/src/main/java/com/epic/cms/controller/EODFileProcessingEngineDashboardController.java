/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:42 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.ResponseBean;
import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.service.EODEngineDashboardService;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("eod-dashboard/file-processing")
public class EODFileProcessingEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODEngineDashboardService eodEngineDashboardService;

    private static final Logger logError = LoggerFactory.getLogger("logError");

    @PostMapping("/inputfile/{eodid}")
    public ResponseBean getEodInputFIleList(@PathVariable("eodid") final Long eodId) {
        try {
            List<Object> eodInputFIleList = eodEngineDashboardService.getEodInputFIleList(eodId);

            if (eodInputFIleList.size() > 0) {
                responseBean.setContent(eodInputFIleList);
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
            logError.error("Failed Eod Input FIleList ", e);
        }
        return responseBean;
    }

    @PostMapping("/processing/{eodid}")
    public ResponseBean getProcessingSummeryList(@PathVariable("eodid") final Long eodId) {
        try {
            List<StatementGenSummeryBean> processingSummeryList = eodEngineDashboardService.getProcessingSummeryList(eodId);

            if (processingSummeryList.size() > 0) {
                responseBean.setContent(processingSummeryList);
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
            logError.error("Failed Eod Input FIleList ", e);
        }
        return responseBean;
    }

    @PostMapping("/fileUpload/{fileId}/{processId}")
    public void inputFileUploadListener(@PathVariable("fileId") final String fileId, @PathVariable("processId") final int processId) {
        try {
            eodEngineDashboardService.sendInputFileUploadListener(fileId,processId);

        } catch (Exception e) {
            logError.error("Failed Input" + fileId + "File Upload Listener ", e);
        }
    }
}
