/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;


import com.epic.cms.model.bean.EodOutputFileBean;
import com.epic.cms.model.bean.ResponseBean;
import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.service.EODEngineDashboardService;
import com.epic.cms.service.EODFileGenerationEngineDashboardService;
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
@RequestMapping("eod-dashboard/file-generation")
public class EODFileGenerationEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODFileGenerationEngineDashboardService engineDashboardService;

    @Autowired
    EODEngineDashboardService eodEngineDashboardService;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


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
