/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:05 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.ProcessSummeryBean;
import com.epic.cms.repository.ProcessSummeryRepo;
import com.epic.cms.service.ProcessSummeryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class ProcessSummeryServiceImpl implements ProcessSummeryService {

    @Autowired
    ProcessSummeryRepo processSummeryRepo;

    @Override
    public List<ProcessSummeryBean> getEodProcessSummeryList(Long eodID) {
        List<ProcessSummeryBean> processSummeryList = new ArrayList<>();
        try {
            processSummeryList = processSummeryRepo.findProcessSummeryListById(eodID);
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod Process Summery List Error", e);
            throw e;
        }
        return processSummeryList;
    }
}
