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
import com.epic.cms.service.EODFileGenerationEngineDashboardService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.MessageVarList;
import com.epic.cms.util.ResponseCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epic.cms.util.LogManager.*;

@RestController
@RequestMapping("eod-dashboard/file-generation")
public class EODFileGenerationEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EODFileGenerationEngineDashboardService engineDashboardService;

    @Autowired
    LogManager logManager;

    @PostMapping("/outputfile/{eodid}")
    public ResponseBean getEodOutputFIleList(@PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-File-Generation Dashboard Get Eod Output FIleList EodId :" + eodId, dashboardInfoLogger);
            List<EodOutputFileBean> eodOutputFIleList = engineDashboardService.getEodOutputFIleList(eodId);

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
            logManager.logError("Failed Eod Output FIleList ", e, dashboardErrorLogger);
        }
        return responseBean;
    }

    @PostMapping("/stmtgensummery/{eodid}")
    public ResponseBean getStatementGenSummeryList(@PathVariable("eodid") final Long eodId) {
        try {
            logManager.logHeader("EOD-File-Generation Dashboard Get Statement GenSummery List EodId :" + eodId, dashboardInfoLogger);
            List<StatementGenSummeryBean> genSummeryBeanList = engineDashboardService.getStatementGenSummeryList(eodId);

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
            logManager.logError("Failed Statement GenSummery List ", e, dashboardErrorLogger);
        }
        return responseBean;
    }
}
