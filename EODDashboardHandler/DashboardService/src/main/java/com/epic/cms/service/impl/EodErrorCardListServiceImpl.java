/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 11:26 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.EodErrorCardBean;
import com.epic.cms.model.entity.EODERRORCARDS;
import com.epic.cms.repository.EodErrorCardListRepo;
import com.epic.cms.service.EodErrorCardListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.epic.cms.util.CommonMethods;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class EodErrorCardListServiceImpl implements EodErrorCardListService {

    @Autowired
    EodErrorCardListRepo eodErrorCardListRepo;

    @Override
    public List<EodErrorCardBean> getEodErrorCardList(Long eodId) {
        List<EodErrorCardBean> errorCardBeanList = new ArrayList<>();
        try {
            List<EODERRORCARDS> eodErrorCardsList = eodErrorCardListRepo.findEodErrorCardByEodId(eodId);

            eodErrorCardsList.forEach(eod -> {
                EodErrorCardBean errorCardBean = new EodErrorCardBean();
                errorCardBean.setEodId(eod.getEODID());
                //errorCardBean.setCardNumber(CommonMethods.cardNumberMask(eod.getCARDNO()));
                errorCardBean.setCardNumber(eod.getCARDNO());
                errorCardBean.setErrorProcess(eod.getERRORPROCESSID());
                errorCardBean.setErrorReason(eod.getERRORREMARK());

                errorCardBeanList.add(errorCardBean);
            });
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod Error Card List Error", e);
            throw e;
        }
        return errorCardBeanList;
    }
}
