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
import com.epic.cms.service.EodOutputFIleListService;
import com.epic.cms.service.StatementGenSummeryListService;
import com.epic.cms.util.exception.MessageVarList;
import com.epic.cms.util.exception.ResponseCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epic.cms.util.LogManager.*;

@RestController
@RequestMapping("eod-file-generation/dashboard")
public class EODFileGenerationEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EodOutputFIleListService outputFIleListService;

    @Autowired
    StatementGenSummeryListService genSummeryListService;

    @PostMapping("/outputfile/{eodid}")
    public ResponseBean getEodOutputFIleList(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-File-Generation Dashboard Get Eod Output FIleList EodId :" + eodId));
            List<EodOutputFileBean> eodOutputFIleList = outputFIleListService.getEodOutputFIleList(eodId);

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
            dashboardErrorLogger.error("Failed Eod Output FIleList ", e);
        }
        return responseBean;
    }

    @PostMapping("/stmtgensummery/{eodid}")
    public ResponseBean getStatementGenSummeryList(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-File-Generation Dashboard Get Statement GenSummery List EodId :" + eodId));
            List<StatementGenSummeryBean> genSummeryBeanList = genSummeryListService.getStatementGenSummeryList(eodId);

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
            dashboardErrorLogger.error("Failed Statement GenSummery List ", e);
        }
        return responseBean;
    }
}
