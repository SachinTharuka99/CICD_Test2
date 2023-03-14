/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 9:31 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.EodErrorMerchantBean;
import com.epic.cms.model.entity.EODERRORMERCHANT;
import com.epic.cms.repository.EodErrorMerchantListRepo;
import com.epic.cms.service.EodErrorMerchantListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class EodErrorMerchantListServiceImpl implements EodErrorMerchantListService {

    @Autowired
    EodErrorMerchantListRepo eodErrorMerchantListRepo;

    @Override
    public List<EodErrorMerchantBean> getEodErrorMerchantList(Long eodId) {
        List<EodErrorMerchantBean> eodErrorMerchantBeansList = new ArrayList<>();

        try {
            List<EODERRORMERCHANT> eodErrorMerchantList = eodErrorMerchantListRepo.findEodErrorMerchantByEodId(eodId);

            eodErrorMerchantList.forEach(eod -> {
                EodErrorMerchantBean errorMerchantBean = new EodErrorMerchantBean();
                errorMerchantBean.setEodId(eod.getEODID());
                errorMerchantBean.setMerchantId(eod.getMID());
                errorMerchantBean.setErrorProcessId(eod.getERRORPROCESSID());
                errorMerchantBean.setErrorReason(eod.getERRORREMARK());

                eodErrorMerchantBeansList.add(errorMerchantBean);
            });
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod Error Merchant List Error", e);
            throw e;
        }
        return eodErrorMerchantBeansList;
    }
}
