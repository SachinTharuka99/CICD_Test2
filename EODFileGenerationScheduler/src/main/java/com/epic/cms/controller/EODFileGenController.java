/**
 * Author : lahiru_p
 * Date : 4/11/2023
 * Time : 9:03 AM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.controller;

import com.epic.cms.service.FileGenMainService;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.epic.cms.util.LogManager.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/eod-file-generation")
public class EODFileGenController {

    @Autowired
    LogManager logManager;

    @Autowired
    FileGenMainService fileGenMainService;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @GetMapping("/generateAll/{eodid}")
    public Map<String, Object> generateAllFiles(@PathVariable("eodid") final String eodId) throws Exception{
        Map<String, Object> response = new HashMap<>();
        try {
            int categoryId = 10;
            Configurations.IssuingOrAcquiring =1;
            int issuingOrAcquiring = Configurations.IssuingOrAcquiring;
            fileGenMainService.startEODFileGenEngine(categoryId,issuingOrAcquiring, Integer.parseInt(eodId));
            response.put(Util.STATUS_VALUE, Util.STATUS_SUCCESS);
        }catch (Exception e){
            logError.error("Error in file generation", e);
            response.put(Util.STATUS_VALUE, Util.STATUS_FAILED);
        }
        return response;
    }
}
