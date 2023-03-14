/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:27 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.repository.StatementGenSummeryListRepo;
import com.epic.cms.service.StatementGenSummeryListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class StatementGenSummeryListServiceImpl implements StatementGenSummeryListService {

    @Autowired
    StatementGenSummeryListRepo genSummeryListRepo;

    @Override
    public List<StatementGenSummeryBean> getStatementGenSummeryList(Long eodId) {
        List<StatementGenSummeryBean> stmtGenSummeryList = new ArrayList<>();

        try {
            stmtGenSummeryList = genSummeryListRepo.findStmtGenSummeryListByEodId(eodId);
        } catch (Exception e) {
            dashboardErrorLogger.error("Get StatementGen Summery List Error", e);
            throw e;
        }
        return stmtGenSummeryList;
    }
}
