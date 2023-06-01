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
import com.epic.cms.service.EODFileProcessingEngineDashboardService;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epic.cms.util.LogManager.*;

@RestController
@RequestMapping("eod-dashboard/file-processing")
public class EODFileProcessingEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODFileProcessingEngineDashboardService processingEngineDashboardService;

    @Autowired
    EODEngineDashboardService eodEngineDashboardService;

    @Autowired
    LogManager logManager;


    @PostMapping("/inputfile/{eodid}")
    public ResponseBean getEodInputFIleList(@PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-File-Processing Dashboard Get Eod Input FIleList EodId :" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed Eod Input FIleList ", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/processing/{eodid}")
    public ResponseBean getProcessingSummeryList(@PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-File-Processing Dashboard Get Processing SummeryList EodId :" + eodId, dashboardInfoLogger);
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
            logManager.logError("Failed Eod Input FIleList ", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/fileUpload/{fileId}/{processId}")
    public void inputFileUploadListener(@PathVariable("fileId") final String fileId, @PathVariable("processId") final int processId) {
        try {
            processingEngineDashboardService.sendInputFileUploadListener(fileId,processId);

        } catch (Exception e) {
            logManager.logError("Failed Input" + fileId + "File Upload Listener ", e, dashboardErrorLogger);
        }
    }
}
