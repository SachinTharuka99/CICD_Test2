/**
 * Author : rasintha_j
 * Date : 2/22/2023
 * Time : 1:09 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.EodBean;
import com.epic.cms.model.bean.NextRunningEodBean;
import com.epic.cms.model.entity.EOD;
import com.epic.cms.repository.EodIdInfoRepo;
import com.epic.cms.service.EodIdInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class EodIdInfoServiceImpl implements EodIdInfoService {

    @Autowired
    EodIdInfoRepo eodIdInfoRepo;

    @Override
    public EodBean getEodInfoList(Long eodId) {

        EodBean eodBean = new EodBean();

        try {
            Optional<EOD> eodInfo = eodIdInfoRepo.findById(eodId);

            eodInfo.ifPresent(eod -> {
                eodBean.setEodId(eod.getEODID());
                eodBean.setStartTime(eod.getSTARTTIME());
                eodBean.setEndTime(eod.getENDTIME());
                eodBean.setStatus(eod.getSTATUS().getSTATUSCODE());
                eodBean.setSubEodStatus(eod.getSUBEODSTATUS());
                eodBean.setNoOfSuccessProcess(eod.getNOOFSUCCESSPROCESS());
                eodBean.setNoOfErrorProcess(eod.getNOOFERRORPAROCESS());
            });
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod Info List Error", e);
            throw e;
        }
        return eodBean;
    }

    @Override
    public NextRunningEodBean getNextRunningEodId() throws Exception {
        NextRunningEodBean nextRunningEodBean = new NextRunningEodBean();
        try {
            nextRunningEodBean.setEodId(eodIdInfoRepo.findByNextRunnindEodId());

        } catch (Exception e) {
            //dashboardErrorLogger.error("Get Next Running EodId Error", e);
            throw e;
        }
        return nextRunningEodBean;
    }
}
