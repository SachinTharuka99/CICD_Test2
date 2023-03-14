/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 9:42 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.controller;

import com.epic.cms.model.bean.ResponseBean;
import com.epic.cms.service.EodInputFileListService;
import com.epic.cms.util.exception.MessageVarList;
import com.epic.cms.util.exception.ResponseCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epic.cms.util.LogManager.*;

@RestController
@RequestMapping("eod-file-processing/dashboard")
public class EODFileProcessingEngineDashboardController {

    ResponseBean responseBean = new ResponseBean();

    @Autowired
    EodInputFileListService inputFileListService;

    @PostMapping("/inputfile/{eodid}")
    public ResponseBean getEodInputFIleList(@PathVariable("eodid") final Long eodId) {
        try {
            dashboardInfoLogger.info(processStartEndStyle("EOD-File-Processing Dashboard Get Eod Input FIleList EodId :" + eodId));
            List<Object> eodInputFIleList = inputFileListService.getEodInputFIleList(eodId);

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
            dashboardErrorLogger.error("Failed Eod Input FIleList ", e);
        }
        return responseBean;
    }
}
