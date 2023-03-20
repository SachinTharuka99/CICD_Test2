/**
 * Author : lahiru_p
 * Date : 3/1/2023
 * Time : 8:57 AM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("eod-dashboard")
public class TestController {

    @GetMapping("/test/{eodid}")
    public String testService(@PathVariable("eodid") final String eodId) throws Exception{

        return "Succcesfully";
    }
}
